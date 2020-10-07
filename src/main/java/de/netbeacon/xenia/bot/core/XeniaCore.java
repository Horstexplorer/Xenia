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
import de.netbeacon.xenia.backend.client.objects.external.SetupData;
import de.netbeacon.xenia.backend.client.objects.internal.BackendSettings;
import de.netbeacon.xenia.bot.listener.guild.GuildAccessListener;
import de.netbeacon.xenia.bot.listener.messages.GuildCommandListener;
import de.netbeacon.xenia.bot.listener.messages.GuildMessageListener;
import de.netbeacon.xenia.bot.listener.messages.GuildReactionListener;
import de.netbeacon.xenia.bot.listener.utils.StatusListener;
import de.netbeacon.xenia.bot.tools.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.handle.GuildSetupController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class XeniaCore {

    private static XeniaCore instance;

    private final Config config;
    private final XeniaBackendClient backendClient;
    private final EventWaiter eventWaiter;
    private final ShardManager shardManager;

    private final Logger logger = LoggerFactory.getLogger(XeniaCore.class);

    /**
     * Creates a new instance of this class
     *
     * @throws LoginException on auth failure
     * @throws IOException on config errors
     */
    private XeniaCore() throws LoginException, IOException {
        // prepare config
        logger.warn("Preparing Config...");
        config = new Config(new File("./xenia/config/sys.config"));
        // prepare backend
        logger.warn("Prepare Backend...");
        BackendSettings backendSettings = new BackendSettings(config.getString("backendScheme"), config.getString("backendHost"), config.getInt("backendPort"), config.getLong("backendClientId"), config.getString("backendPassword"));
        backendClient = new XeniaBackendClient(backendSettings);
        // get setup data
        logger.warn("Retrieving Setup Data...");
        SetupData setupData = backendClient.getSetupData();
        logger.warn("Retrieved Setup Data:"+"\n"+
                "ClientName: "+setupData.getClientName()+"\n"+
                "ClientDescription: "+setupData.getClientDescription()+"\n"+
                "Total Shards: "+setupData.getTotalShards()+"\n"+
                "Use Shards: "+ Arrays.toString(setupData.getShards())
        );
        logger.warn("Preparing Other Things...");
        eventWaiter = new EventWaiter();
        logger.warn("Preparing Shard Builder...");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
                .createLight(setupData.getDiscordToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing(config.getString("activity")))
                .setShardsTotal(setupData.getTotalShards())
                .setShards(Arrays.stream(setupData.getShards()).mapToInt(Integer::intValue).toArray())
                .addEventListeners(new StatusListener(), new GuildAccessListener(backendClient),new GuildMessageListener(), new GuildReactionListener(), new GuildCommandListener(config, setupData.getShards().length, backendClient));
        logger.warn("Building "+setupData.getShards().length+" Shards...");
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

    /**
     * Returns the backend client
     *
     * @return BackendClient
     */
    public XeniaBackendClient getBackendClient() {
        return backendClient;
    }
}
