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
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class MultiThreadedEventManager implements IExtendedEventManager {

    private long lastEvent;
    private final ScalingExecutor scalingExecutor;
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    public MultiThreadedEventManager(){
        this.scalingExecutor = new ScalingExecutor(2, 25, 2048, 10, TimeUnit.SECONDS);
    }

    @Override
    public void register(@NotNull Object listener) {
        if(!(listener instanceof EventListener)){
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add((EventListener) listener);
    }

    @Override
    public void unregister(@NotNull Object listener) {
        if(!(listener instanceof EventListener)){
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.remove(listener);
    }

    @Override
    public void handle(@NotNull GenericEvent event) {
        lastEvent = System.currentTimeMillis();
        for(EventListener listener : listeners){
            try {
                scalingExecutor.execute(()->{
                    try {
                        listener.onEvent(event);
                    }catch (Throwable t){
                        JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", t);
                        if (t instanceof Error)
                            throw (Error) t;
                    }
                });
            }catch (Throwable t){
                JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", t);
                if (t instanceof Error)
                    throw (Error) t;
            }
        }
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return List.copyOf(listeners);
    }

    @Override
    public void onShutdown() throws Exception {
        scalingExecutor.shutdown();
    }

    @Override
    public long getLastEventTimestamp() {
        return lastEvent;
    }

    @Override
    public long getLastEventTimeDif() {
        return System.currentTimeMillis()-lastEvent;
    }
}
