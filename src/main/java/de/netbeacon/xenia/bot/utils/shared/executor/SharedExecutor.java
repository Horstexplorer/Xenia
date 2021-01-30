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

package de.netbeacon.xenia.bot.utils.shared.executor;

import de.netbeacon.utils.executor.ScalingExecutor;
import de.netbeacon.utils.shutdownhook.IShutdown;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SharedExecutor implements IShutdown {

    private static SharedExecutor instance;
    private ScalingExecutor scalingExecutor;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Creates a new instance of this class
     */
    private SharedExecutor(){
        scalingExecutor = new ScalingExecutor(2, 24, 24000, 30, TimeUnit.SECONDS);
        scheduledExecutorService = Executors.newScheduledThreadPool(8);
    }

    /**
     * Returns the instance of this class
     *
     * @param initIfNeeded initializes an instance of this class if no other exists
     * @return this
     */
    public static synchronized SharedExecutor getInstance(boolean initIfNeeded){
        if(instance == null && initIfNeeded){
            instance = new SharedExecutor();
        }
        return instance;
    }

    /**
     * Returns the instance of this class
     * @return this
     */
    public static SharedExecutor getInstance(){
        return getInstance(false);
    }

    /**
     * Returns the scaling executor
     *
     * @return scaling executor
     */
    public ScalingExecutor getScalingExecutor(){
        return scalingExecutor;
    }

    /**
     * Returns the schedules executor
     *
     * @return scheduled executor
     */
    public ScheduledExecutorService getScheduledExecutor() { return scheduledExecutorService; }

    @Override
    public void onShutdown() throws Exception {
        scalingExecutor.shutdown();
        scalingExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        scalingExecutor = null;
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        scheduledExecutorService = null;
    }
}
