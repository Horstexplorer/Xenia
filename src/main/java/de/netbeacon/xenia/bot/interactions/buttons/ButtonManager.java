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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ButtonManager implements IShutdown{

	private final ConcurrentHashMap<String, ButtonRegEntry> buttonRegistry = new ConcurrentHashMap<>();
	private final Lock lock = new ReentrantLock();
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private final Supplier<ShardManager> shardManagerSupplier;

	public ButtonManager(Supplier<ShardManager> shardManagerSupplier){
		this.shardManagerSupplier = shardManagerSupplier;
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try{
				buttonRegistry.forEach((k, v) -> {
					if(!v.keep()){
						buttonRegistry.remove(k);
						v.deactivate(this.shardManagerSupplier.get());
					}
				});
			}
			catch(Exception ignore){
			}
		}, 1, 1, TimeUnit.MINUTES);
	}

	public void register(ButtonRegEntry buttonRegEntry){
		try{
			lock.lock();
			buttonRegistry.put(buttonRegEntry.getUuid(), buttonRegEntry);
		}
		finally{
			lock.unlock();
		}
	}

	public void deactivate(ButtonRegEntry buttonRegEntry){
		buttonRegEntry.deactivate(this.shardManagerSupplier.get());
		buttonRegistry.remove(buttonRegEntry.getUuid());
	}

	public ButtonRegEntry get(String uuid){
		return buttonRegistry.get(uuid);
	}

	@Override
	public void onShutdown() throws Exception{
		scheduledExecutorService.shutdown();
	}

}
