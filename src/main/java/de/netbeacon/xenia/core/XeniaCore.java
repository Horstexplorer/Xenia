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

package de.netbeacon.xenia.core;

import de.netbeacon.xenia.listener.messages.GuildCommandListener;
import de.netbeacon.xenia.listener.messages.GuildMessageListener;
import de.netbeacon.xenia.listener.messages.GuildReactionListener;
import de.netbeacon.xenia.tools.config.Config;
import de.netbeacon.xenia.tools.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class XeniaCore {

    private static XeniaCore instance;

    private final ShardManager shardManager;
    private final Config config;
    private final EventWaiter eventWaiter;
    private final Logger logger = LoggerFactory.getLogger(XeniaCore.class);

    /**
     * Creates a new instance of this class
     *
     * @throws LoginException on auth failure
     * @throws IOException on config errors
     */
    private XeniaCore() throws LoginException, IOException {
        // prepare config
        logger.info("Preparing Config..");
        config = new Config(new File("./xenia/config/sys.config"));
        logger.info("Preparing Other Things...");
        eventWaiter = new EventWaiter();
        logger.info("Preparing Shard Builder...");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
                .createLight(config.getString("loginToken"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing(config.getString("activity")))
                .setShardsTotal(1)
                .setShards(0)
                .addEventListeners(new GuildCommandListener(), new GuildMessageListener(), new GuildReactionListener());
        logger.info("Building Shards...");
        shardManager = builder.build();
    }

    /**
     * Returns an instance of this class
     *
     * @return XeniaCore instance
     */
    public static XeniaCore getInstance(){
        return instance;
    }

    /**
     * Returns an instance of this class, can be used to initialize an instance
     *
     * @param initializeIfPossible tries to create a new instance of this class
     * @return XeniaCore instance
     * @throws LoginException on auth failure
     * @throws IOException on config errors
     */
    public static XeniaCore getInstance(boolean initializeIfPossible) throws LoginException, IOException {
        if(instance == null && initializeIfPossible){
            instance = new XeniaCore();
        }
        return instance;
    }

    /**
     * Returns the shard manager
     *
     * @return ShardManager
     */
    public ShardManager getShardManager(){
        return shardManager;
    }

    /**
     * Returns the config
     *
     * @return Config
     */
    public Config getConfig(){
        return config;
    }

    /**
     * Returns the event waiter
     *
     * @return EventWaiter
     */
    public EventWaiter getEventWaiter(){
        return eventWaiter;
    }
}
