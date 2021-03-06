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

package de.netbeacon.xenia.bot.event.manager;

import de.netbeacon.utils.executor.ScalingExecutor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreadedEventManager implements IExtendedEventManager{

	private final ScalingExecutor primaryScalingExecutor;
	private final ScalingExecutor secondaryScalingExecutor;
	private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	private final AtomicBoolean halt = new AtomicBoolean(false);
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private long lastEvent;

	public MultiThreadedEventManager(){
		this.primaryScalingExecutor = new ScalingExecutor(2, 30, -1, 10, TimeUnit.SECONDS);
		this.secondaryScalingExecutor = new ScalingExecutor(2, 16, -1, 10, TimeUnit.SECONDS);
	}

	@Override
	public void register(@NotNull Object listener){
		if(!(listener instanceof EventListener)){
			throw new IllegalArgumentException("Listener must implement EventListener");
		}
		listeners.add((EventListener) listener);
	}

	@Override
	public void unregister(@NotNull Object listener){
		if(!(listener instanceof EventListener)){
			throw new IllegalArgumentException("Listener must implement EventListener");
		}
		listeners.remove(listener);
	}

	public void halt(boolean value){
		halt.set(value);
	}

	@Override
	public void handle(@NotNull GenericEvent event){
		try{
			lastEvent = System.currentTimeMillis();
			if(halt.get()){
				return;
			}
			if(event instanceof GenericGuildMessageEvent){
				primaryScalingExecutor.execute(() -> eventConsumer(event));
			}
			else{
				secondaryScalingExecutor.execute(() -> eventConsumer(event));
			}
		}
		catch(Exception e){
			logger.error("Something rly bad happened while handling an event", e);
		}
	}

	private void eventConsumer(GenericEvent event){
		for(EventListener listener : listeners){
			try{
				listener.onEvent(event);
			}
			catch(Exception e){
				JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", e);
			}catch(ExpectedInterruptException e){
				break;
			}
		}
	}

	@NotNull
	@Override
	public List<Object> getRegisteredListeners(){
		return List.copyOf(listeners);
	}

	@Override
	public void onShutdown() throws Exception{
		halt(true);
		primaryScalingExecutor.shutdown();
		secondaryScalingExecutor.shutdown();
	}

	@Override
	public long getLastEventTimestamp(){
		return lastEvent;
	}

	@Override
	public long getLastEventTimeDif(){
		return System.currentTimeMillis() - lastEvent;
	}

}
