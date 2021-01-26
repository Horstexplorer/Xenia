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
import de.netbeacon.xenia.bot.event.listener.access.GuildAccessListener;
import de.netbeacon.xenia.bot.event.listener.message.GuildMessageListener;
import de.netbeacon.xenia.bot.event.listener.message.GuildReactionListener;
import de.netbeacon.xenia.bot.event.listener.status.StatusListener;
import de.netbeacon.xenia.bot.event.manager.EventManagerProvider;
import de.netbeacon.xenia.bot.event.manager.MultiThreadedEventManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.misc.listener.NotificationListener;
import de.netbeacon.xenia.bot.utils.misc.listener.NotificationListenerInserter;
import de.netbeacon.xenia.bot.utils.misc.task.TaskManager;
import de.netbeacon.xenia.bot.utils.shared.executor.SharedExecutor;
import de.netbeacon.xenia.bot.utils.shared.okhttpclient.SharedOkHttpClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ApplicationInfo;
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
    private final long ownerId;
    
    private final Logger logger = LoggerFactory.getLogger(XeniaCore.class);

    private XeniaCore() throws LoginException, IOException {
        // shutdown hook
        ShutdownHook shutdownHook = new ShutdownHook();
        // load config
        logger.info("Loading Config...");
        config = new Config(new File("./xenia/config/sys.config"));

        // load backend
        logger.info("Loading Backend...");
        BackendSettings backendSettings = new BackendSettings(config.getString("backendScheme"), config.getString("backendHost"), config.getInt("backendPort"), config.getLong("backendClientId"), config.getString("backendPassword"), config.getString("messageCryptKey"));
        xeniaBackendClient = new XeniaBackendClient(backendSettings, this::getShardManager);
        shutdownHook.addShutdownAble(xeniaBackendClient);

        // setup data
        logger.info("Retrieving Setup Data...");
        SetupData setupData = xeniaBackendClient.getSetupData();
        logger.info("Retrieved Setup Data:"+"\n"+
                    "ClientName: "+setupData.getClientName()+"\n"+
                    "ClientDescription: "+setupData.getClientDescription()+"\n"+
                    "Total Shards: "+setupData.getTotalShards()+"\n"+
                    "Shards To Use: "+Arrays.toString(setupData.getShards())+"\n"
                );

        // setup other things
        logger.info("Preparing Other Things...");
        eventWaiter = new EventWaiter(); // Event Waiter
        shutdownHook.addShutdownAble(SharedExecutor.getInstance(true)); // Shared executor
        shutdownHook.addShutdownAble(TaskManager.getInstance(true)); // Task manager
        SharedOkHttpClient.getInstance(true);
        xeniaBackendClient.getGuildCache().addEventListeners(new NotificationListenerInserter(new NotificationListener(TaskManager.getInstance()))); // insert notification listener on its own

        // set up event manager
        logger.info("Preparing Event Manager (Provider)...");
        EventManagerProvider eventManagerProvider = new EventManagerProvider()
                .setFactory(obj -> new MultiThreadedEventManager());
        shutdownHook.addShutdownAble(eventManagerProvider);

        // setup shard builder
        logger.info("Setting Up Shard Builder...");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
                .createLight(setupData.getDiscordToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setEventManagerProvider(eventManagerProvider::provideOrCreate)
                .setActivity(Activity.playing(config.getString("activity")))
                .addEventListeners(
                        new StatusListener(),
                        new GuildAccessListener(xeniaBackendClient),
                        new GuildMessageListener(config, xeniaBackendClient, eventWaiter),
                        new GuildReactionListener(eventWaiter)
                );
        if(setupData.getTotalShards() != 0 && setupData.getShards().length != 0){
            builder
                    .setShardsTotal(setupData.getTotalShards())
                    .setShards(setupData.getShards());
        }
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
        logger.info("Building Shards...");
        shardManager = builder.build();
        shutdownHook.addShutdownAble(new SMH(shardManager));
        // application info
        logger.info("Getting Application Info...");
        ApplicationInfo applicationInfo = shardManager.retrieveApplicationInfo().complete();
        ownerId = applicationInfo.getOwner().getIdLong();
        // watchdog
        logger.info("Starting Watchdog...");
        XeniaWatchdog xeniaWatchdog = new XeniaWatchdog();
        shutdownHook.addShutdownAble(xeniaWatchdog);
        // add wd tasks here

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

    public JDA getShardByGuildId(long guildId){
        for(JDA jda : shardManager.getShards()){
            if(jda.getGuildCache().getElementById(guildId) != null){
                return jda;
            }
        }
        return null;
    }

    public long getOwnerID(){
        return ownerId;
    }
}
