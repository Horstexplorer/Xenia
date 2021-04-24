/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.utils.misc.task;

import de.netbeacon.utils.shutdownhook.IShutdown;

import java.util.concurrent.*;

public class TaskManager implements IShutdown{

	private static TaskManager instance;
	private final ConcurrentHashMap<Long, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduledExecutorService;

	private TaskManager(){
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	public static synchronized TaskManager getInstance(boolean initializeIfPossible){
		if(instance == null && initializeIfPossible){
			instance = new TaskManager();
		}
		return instance;
	}

	public static TaskManager getInstance(){
		return instance;
	}

	public boolean schedule(long taskId, Runnable runnable, long delay){
		if(scheduledExecutorService.isShutdown() || taskMap.containsKey(taskId)){
			return false;
		}
		ScheduledFuture<?> future = scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		taskMap.put(taskId, future);
		return true;
	}

	public boolean update(long taskId, Runnable runnable, long delay){
		if(scheduledExecutorService.isShutdown() || !taskMap.containsKey(taskId)){
			return false;
		}
		cancel(taskId);
		return schedule(taskId, runnable, delay);
	}

	public boolean cancel(long taskId){
		if(scheduledExecutorService.isShutdown() || !taskMap.containsKey(taskId)){
			return false;
		}
		taskMap.get(taskId).cancel(true);
		taskMap.remove(taskId);
		return true;
	}

	@Override
	public void onShutdown() throws Exception{
		scheduledExecutorService.shutdownNow();
		instance = null;
	}

}
