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

package de.netbeacon.xenia.bot.utils;

import de.netbeacon.utils.executor.ScalingExecutor;
import de.netbeacon.utils.shutdownhook.IShutdown;

import java.util.concurrent.TimeUnit;

public class SharedExecutor implements IShutdown {

    private static SharedExecutor instance;
    private ScalingExecutor scalingExecutor;

    private SharedExecutor(){
        scalingExecutor = new ScalingExecutor(2, 24, 24000, 30, TimeUnit.SECONDS);
    }

    public static SharedExecutor getInstance(boolean initIfNeeded){
        if(instance == null && initIfNeeded){
            instance = new SharedExecutor();
        }
        return instance;
    }

    public static SharedExecutor getInstance(){
        return getInstance(false);
    }

    public ScalingExecutor getScalingExecutor(){
        return scalingExecutor;
    }

    @Override
    public void onShutdown() throws Exception {
        scalingExecutor.shutdown();
        scalingExecutor = null;
    }
}
