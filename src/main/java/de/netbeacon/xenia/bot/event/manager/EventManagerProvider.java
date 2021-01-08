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

import de.netbeacon.utils.shutdownhook.IShutdown;
import net.dv8tion.jda.api.hooks.IEventManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EventManagerProvider implements IShutdown {

    private Function<Object, IEventManager> eventManagerFactory = null;
    private final ConcurrentHashMap<Long, IEventManager> eventManagers = new ConcurrentHashMap<>();

    public EventManagerProvider register(long id, IEventManager eventManager){
        eventManagers.put(id, eventManager);
        return this;
    }

    public EventManagerProvider unregister(long id){
        eventManagers.remove(id);
        return this;
    }

    public EventManagerProvider setFactory(Function<Object, IEventManager> factory){
        eventManagerFactory = factory;
        return this;
    }

    public IEventManager provide(long id){
        return eventManagers.get(id);
    }

    public IEventManager provideOrCreate(long id){
        return provideOrCreate(id, id);
    }

    public IEventManager provideOrCreate(long id, Object object){
        IEventManager iEventManager = eventManagers.get(id);
        if(iEventManager != null){
            return iEventManager;
        }
        if(eventManagerFactory == null){
            return null;
        }
        iEventManager = eventManagerFactory.apply(object);
        eventManagers.put(id, iEventManager);
        return iEventManager;
    }

    @Override
    public void onShutdown() throws Exception {
        for(var manager : eventManagers.values()){
            if(manager instanceof IExtendedEventManager){
                ((IExtendedEventManager) manager).onShutdown();
            }
        }
    }
}
