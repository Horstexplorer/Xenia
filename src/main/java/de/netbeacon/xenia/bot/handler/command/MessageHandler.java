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

package de.netbeacon.xenia.bot.handler.command;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.utils.pattern.StaticPattern.ArgPattern;

public class MessageHandler {

    private final String prefix;
    private final HashMap<String, Command> commandMap;
    private final CommandCooldown commandCooldown = new CommandCooldown(CommandCooldown.Type.User, 1000);
    private final EventWaiter eventWaiter;
    private final XeniaBackendClient backendClient;

    public MessageHandler(String prefix, HashMap<String, Command> commandMap, EventWaiter eventWaiter, XeniaBackendClient backendClient){
        this.prefix = prefix;
        this.commandMap = commandMap;
        this.eventWaiter = eventWaiter;
        this.backendClient = backendClient;
    }

    public void processNew(GuildMessageReceivedEvent event){
        // get backend data (move this back before the stm block when traffic is too high; this will speed up preloading data)
        Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        User bUser = backendClient.getUserCache().get(event.getAuthor().getIdLong());
        Member bMember = bGuild.getMemberCache().get(event.getAuthor().getIdLong());
        Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
        License bLicense = backendClient.getLicenseCache().get(event.getGuild().getIdLong());
        // wrap in single object
        CommandEvent.BackendDataPack backendDataPack = new CommandEvent.BackendDataPack(bGuild, bUser, bMember, bChannel, bLicense);
        // get the message & check prefix
        String msg = event.getMessage().getContentRaw();
        if(!msg.startsWith(prefix)){
            // check if the message should be logged
            if(bChannel.tmpLoggingIsActive()){
                bChannel.getMessageCache().create(event.getMessage().getIdLong(), event.getMessage().getTimeCreated().toInstant().toEpochMilli(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
            }
            return;
        }
        // check cool down
        if(!commandCooldown.allow(event.getGuild().getIdLong(), event.getAuthor().getIdLong())){
            return;
        }
        commandCooldown.deny(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
        // split to list
        List<String> args = new ArrayList<>();
        Matcher matcher = ArgPattern.matcher(msg.substring(prefix.length()));
        while(matcher.find()){
            args.add((matcher.group(2) != null)?matcher.group(2):matcher.group());
        }
        if(args.size() <= 0){
            return;
        }
        // get the command
        Command command = commandMap.get(args.get(0));
        if(command == null){
            return;
        }
        args.remove(0);
        // start the madness
        command.execute(args, new CommandEvent(event, backendDataPack, backendClient, eventWaiter));
    }

    public void processUpdate(GuildMessageUpdateEvent event){
        Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
        MessageCache messageCache = bChannel.getMessageCache();
        Message message = messageCache.get(event.getMessageIdLong());
        if(message == null){
            return;
        }
        // update message content
        message.setMessageContent(event.getMessage().getContentRaw(), messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey());
        // update thingy
        messageCache.setLast("edited", message.getId());
        // check if notification is active
        if(bChannel.getTmpLoggingChannelId() == -1){
            return;
        }
        TextChannel channel = event.getGuild().getTextChannelById(bChannel.getTmpLoggingChannelId());
        if(channel == null){
            bChannel.setTmpLoggingChannelId(-1);
            return;
        }
        channel.sendMessage(EmbedBuilderFactory.getDefaultEmbed("Message Edited!", event.getJDA().getSelfUser())
                .addField("MessageID", event.getMessageId(), true)
                .addField("Author", event.getAuthor().getAsTag(), true)
                .addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
                .build()
        ).queue(s->{}, e->{});
    }

    public void processDelete(GuildMessageDeleteEvent event){
        Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
        MessageCache messageCache = bChannel.getMessageCache();
        Message message = messageCache.get(event.getMessageIdLong());
        if(message == null){
            return;
        }
        // update thingy
        messageCache.setLast("deleted", event.getMessageIdLong());
        // try sending the message there
        if(bChannel.getTmpLoggingChannelId() == -1){
            return;
        }
        TextChannel channel = event.getGuild().getTextChannelById(bChannel.getTmpLoggingChannelId());
        if(channel == null){
            bChannel.setTmpLoggingChannelId(-1);
            return;
        }
        channel.sendMessage(EmbedBuilderFactory.getDefaultEmbed("Message Deleted!", event.getJDA().getSelfUser())
                .addField("MessageID", event.getMessageId(), true)
                .addField("AuthorID", String.valueOf(message.getUserId()), true)
                .addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
                .build()
        ).queue(s->{}, e->{});
    }

}
