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

package de.netbeacon.xenia.bot.core;

import de.netbeacon.utils.config.Config;
import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class XeniaCore {

    private static XeniaCore instance;

    private final Config config;
    private final XeniaBackendClient xeniaBackendClient;
    private final ShardManager shardManager;
    private final EventWaiter eventWaiter;
    
    private final Logger logger = LoggerFactory.getLogger(XeniaCore.class);

    private XeniaCore() throws LoginException, IOException {

    }

    public static XeniaCore getInstance(){
        return instance;
    }

    public static XeniaCore getInstance(boolean initializeIfPossible) throws LoginException, IOException {
        if(instance == null && initializeIfPossible){
            instance = new XeniaCore();
        }
        return instance;
    }

    public XeniaBackendClient getBackendClient() {
        return xeniaBackendClient;
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public Config getConfig(){
        return config;
    }

    public EventWaiter getEventWaiter(){
        return eventWaiter;
    }
}
