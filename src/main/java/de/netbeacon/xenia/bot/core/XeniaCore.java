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
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.shutdownhook.ShutdownHook;
import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.external.SetupData;
import de.netbeacon.xenia.backend.client.objects.internal.BackendSettings;
import de.netbeacon.xenia.bot.listener.access.GuildAccessListener;
import de.netbeacon.xenia.bot.listener.message.GuildMessageListener;
import de.netbeacon.xenia.bot.listener.message.GuildReactionListener;
import de.netbeacon.xenia.bot.listener.status.StatusListener;
import de.netbeacon.xenia.bot.utils.SharedExecutor;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class XeniaCore {

    private static XeniaCore instance;

    private final Config config;
    private final XeniaBackendClient xeniaBackendClient;
    private final ShardManager shardManager;
    private final EventWaiter eventWaiter;
    
    private final Logger logger = LoggerFactory.getLogger(XeniaCore.class);

    private XeniaCore() throws LoginException, IOException {
        // shutdown hook
        ShutdownHook shutdownHook = new ShutdownHook();
        // load config
        logger.warn("Loading Config...");
        config = new Config(new File("./xenia/config/sys.config"));

        // load backend
        logger.warn("Loading Backend...");
        BackendSettings backendSettings = new BackendSettings(config.getString("backendScheme"), config.getString("backendHost"), config.getInt("backendPort"), config.getLong("backendClientId"), config.getString("backendPassword"), config.getString("messageCryptKey"));
        xeniaBackendClient = new XeniaBackendClient(backendSettings);
        shutdownHook.addShutdownAble(xeniaBackendClient);

        // setup data
        logger.warn("Retrieving Setup Data...");
        SetupData setupData = xeniaBackendClient.getSetupData();
        logger.warn("Retrieved Setup Data:"+"\n"+
                    "ClientName: "+setupData.getClientName()+"\n"+
                    "ClientDescription: "+setupData.getClientDescription()+"\n"+
                    "Total Shards: "+setupData.getTotalShards()+"\n"+
                    "Shards To Use: "+Arrays.toString(setupData.getShards())+"\n"
                );

        // setup other things
        logger.warn("Preparing Other Things...");
        eventWaiter = new EventWaiter();
        shutdownHook.addShutdownAble(SharedExecutor.getInstance(true));

        // setup shard builder
        logger.warn("Setting Up Shard Builder...");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
                .createLight(setupData.getDiscordToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing(config.getString("activity")))
                .setShardsTotal(setupData.getTotalShards())
                .setShards(setupData.getShards())
                .addEventListeners(
                        new StatusListener(),
                        new GuildAccessListener(xeniaBackendClient),
                        new GuildMessageListener(config, xeniaBackendClient, eventWaiter, setupData.getShards().length),
                        new GuildReactionListener(eventWaiter)
                );
        // prepare helper class
        class SMH implements IShutdown {
            private final ShardManager shardManager;
            SMH(ShardManager shardManager){
                this.shardManager = shardManager;
            }
            @Override
            public void onShutdown() throws Exception {
                shardManager.shutdown();
            }
        }
        // create shards
        logger.warn("Building Shards...");
        shardManager = builder.build();
        shutdownHook.addShutdownAble(new SMH(shardManager));
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
