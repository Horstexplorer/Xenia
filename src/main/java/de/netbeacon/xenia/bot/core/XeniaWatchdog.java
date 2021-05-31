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

package de.netbeacon.xenia.bot.core;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.tuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class XeniaWatchdog implements IShutdown{

	private final Logger logger = LoggerFactory.getLogger(XeniaWatchdog.class);
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	public void scheduleWatchTask(WatchTask<?, ?> watchTask, boolean repeat, long interval){
		Runnable runnable = watchTask::execute;
		if(repeat){
			scheduledExecutorService.scheduleAtFixedRate(runnable, interval, interval, TimeUnit.MILLISECONDS);
		}
		else{
			scheduledExecutorService.schedule(runnable, interval, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void onShutdown() throws Exception{
		scheduledExecutorService.shutdownNow();
	}

	public static class WatchTask<I, O>{

		private final Function<I, O> task;
		private final Consumer<O> onSuccess;
		private final Consumer<Pair<O, Exception>> onFailure;
		private final Logger logger = LoggerFactory.getLogger(WatchTask.class);
		private Predicate<O> predicate;
		private Function<Void, I> inputProvider;
		private O lastOutput;

		public WatchTask(Function<I, O> task, Consumer<O> onSuccess, Consumer<Pair<O, Exception>> onFailure){
			this.task = task;
			this.onSuccess = onSuccess;
			this.onFailure = onFailure;
		}

		public WatchTask<I, O> preloadArgs(Function<Void, I> inputProvider, Predicate<O> predicate){
			this.inputProvider = inputProvider;
			this.predicate = predicate;
			return this;
		}

		public void execute(){
			execute(inputProvider.apply(null), predicate);
		}

		public void execute(I input, Predicate<O> predicate){
			try{
				try{
					O output = task.apply(input);
					lastOutput = output;
					if(predicate != null && !predicate.test(output)){
						if(onFailure != null){
							onFailure.accept(new Pair<>(output, new RuntimeException("Predicate Not Fulfilled")));
						}
						return;
					}
					if(onSuccess != null){
						onSuccess.accept(output);
					}
				}
				catch(Exception e){
					lastOutput = null;
					if(onFailure != null){
						onFailure.accept(new Pair<>(null, e));
					}
				}
			}
			catch(Exception e){
				logger.error("WatchTask Produced An Exception It Could Not Handle ", e);
			}
		}

		public O getLastOutput(){
			return lastOutput;
		}

	}

}
