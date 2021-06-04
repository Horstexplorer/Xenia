/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.xenia.bot.interactions.buttons;

import de.netbeacon.utils.shutdownhook.IShutdown;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ButtonManager implements IShutdown{

	private final Supplier<ShardManager> shardManagerSupplier;

	private final ConcurrentHashMap<String, ButtonRegEntry> buttonRegistry = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Future<?>> timeoutRegistry = new ConcurrentHashMap<>();

	private final Lock lock = new ReentrantLock();

	private final ScheduledExecutorService timeoutExecutorService = Executors.newScheduledThreadPool(2);


	public ButtonManager(Supplier<ShardManager> shardManagerSupplier){
		this.shardManagerSupplier = shardManagerSupplier;
		this.timeoutExecutorService.scheduleAtFixedRate(() -> {
			try{
				buttonRegistry.forEach((k, v) -> {
					if(!v.allowsActivation() || !v.getTimeoutPolicy().isInTime()){
						unregister(v);
					}
				});
			}catch(Exception ignore){}
		}, 30, 30, TimeUnit.SECONDS);
	}

	public void register(ButtonRegEntry buttonRegEntry){
		try{
			lock.lock();
			buttonRegistry.put(buttonRegEntry.getUuid(), buttonRegEntry);
			ButtonRegEntry.TimeoutPolicy timeoutPolicy = buttonRegEntry.getTimeoutPolicy();
			if(!timeoutPolicy.equals(ButtonRegEntry.TimeoutPolicy.NONE)){
				Future<?> timeoutFuture = timeoutExecutorService.schedule(() -> {
					unregister(buttonRegEntry);
					buttonRegEntry.deactivate(shardManagerSupplier.get());
				}, timeoutPolicy.timeoutInMS(), TimeUnit.MILLISECONDS);
				timeoutRegistry.put(buttonRegEntry.getUuid(), timeoutFuture);
			}
			buttonRegEntry.setButtonManager(this);
		}
		finally{
			lock.unlock();
		}
	}

	public void unregister(ButtonRegEntry buttonRegEntry){
		unregister(buttonRegEntry.getUuid());
	}

	public void unregister(String id){
		buttonRegistry.remove(id);
		Future<?> future = timeoutRegistry.remove(id);
		if(future != null){
			future.cancel(false);
		}
	}

	public void deactivate(ButtonRegEntry buttonRegEntry){
		buttonRegEntry.deactivate(this.shardManagerSupplier.get());
		unregister(buttonRegEntry);
	}

	public ButtonRegEntry get(String uuid){
		return buttonRegistry.get(uuid);
	}

	@Override
	public void onShutdown() throws Exception{
		timeoutExecutorService.shutdown();
	}

}
