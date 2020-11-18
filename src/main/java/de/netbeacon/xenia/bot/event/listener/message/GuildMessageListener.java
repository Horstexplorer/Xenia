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

package de.netbeacon.xenia.bot.event.listener.message;

import de.netbeacon.utils.config.Config;
import de.netbeacon.utils.executor.ScalingExecutor;
import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.commands.global.help.CMDHelp;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.structure.CMDInfo;
import de.netbeacon.xenia.bot.commands.structure.admin.GROUPAdmin;
import de.netbeacon.xenia.bot.commands.structure.last.GROUPLast;
import de.netbeacon.xenia.bot.commands.structure.notification.GROUPNotification;
import de.netbeacon.xenia.bot.commands.structure.tags.HYBRIDTag;
import de.netbeacon.xenia.bot.event.handler.command.MessageHandler;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GuildMessageListener extends ListenerAdapter {

    private final EventWaiter eventWaiter;
    private final MessageHandler commandHandler;
    private final ScalingExecutor scalingExecutor;

    public GuildMessageListener(Config config, XeniaBackendClient backendClient, EventWaiter eventWaiter, int shards){
        this.eventWaiter = eventWaiter;

        HashMap<String, Command> commandMap = new HashMap<>();
        Consumer<Command> register = command -> commandMap.put(command.getAlias(), command);

        register.accept(new CMDHelp(commandMap));

        register.accept(new GROUPAdmin(null));

        register.accept(new GROUPLast(null));
        register.accept(new HYBRIDTag(null));
        register.accept(new GROUPNotification(null));

        register.accept(new CMDInfo());

        commandHandler = new MessageHandler(config.getString("commandPrefix"), commandMap, eventWaiter, backendClient);
        scalingExecutor = new ScalingExecutor(2*shards, 20*shards, 2048*shards, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.isWebhookMessage() || event.getAuthor().isBot()){
            return;
        }
        if(eventWaiter.waitingOnThis(event)){
           return;
        }
        scalingExecutor.execute(()->commandHandler.processNew(event));
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if(event.getAuthor().isBot()){
            return;
        }
        eventWaiter.waitingOnThis(event);
        scalingExecutor.execute(()->commandHandler.processUpdate(event));
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        eventWaiter.waitingOnThis(event);
        scalingExecutor.execute(()->commandHandler.processDelete(event));
    }

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
        eventWaiter.waitingOnThis(event);
    }
}
