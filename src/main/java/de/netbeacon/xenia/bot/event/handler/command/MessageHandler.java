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

package de.netbeacon.xenia.bot.event.handler.command;

import de.netbeacon.d43z.one.eval.io.EvalRequest;
import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.shared.executor.SharedExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.ArgPattern;

public class MessageHandler {

    private final HashMap<String, Command> commandMap;
    private final CommandCooldown commandCooldown = new CommandCooldown(CommandCooldown.Type.User, 1000);
    private final EventWaiter eventWaiter;
    private final XeniaBackendClient backendClient;
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(HashMap<String, Command> commandMap, EventWaiter eventWaiter, XeniaBackendClient backendClient){
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
        // check if xenia has been disabled in which case we dont do anything
        if(bChannel.getAccessMode().has(Channel.AccessMode.Mode.DISABLED)) return;
        // get the message & check prefix
        String msg = event.getMessage().getContentRaw();
        if(!msg.startsWith(bGuild.getPrefix())){
            if(bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE) && event.getChannel().isNSFW()){
                try{
                    D43Z1Imp d43Z1Imp = D43Z1Imp.getInstance();
                    ContentMatchBuffer contextMatchBuffer = d43Z1Imp.getContentMatchBufferFor(event.getAuthor().getIdLong());
                    IContextPool contextPool = d43Z1Imp.getContextPoolByUUID(bChannel.getD43Z1CustomContextPoolUUID());
                    if(contextPool == null){
                        contextPool = d43Z1Imp.getContextPoolMaster();
                    }
                    EvalRequest evalRequest = new EvalRequest(contextPool, contextMatchBuffer, new Content(event.getMessage().getContentRaw()),
                            evalResult -> {
                                if(evalResult.ok()){
                                    event.getChannel().sendMessage(evalResult.getContentMatch().getEstimatedOutput().getContent()).queue();
                                }
                            }, SharedExecutor.getInstance().getScheduledExecutor());
                    d43Z1Imp.getEval().enqueue(evalRequest);
                }catch (Exception e){
                    logger.warn("An exception occurred while handing message over to D43Z1 ", e);
                }
            }else if(bChannel.tmpLoggingIsActive()){ // check if the message should be logged
                bChannel.getMessageCache().create(event.getMessage().getIdLong(), event.getMessage().getTimeCreated().toInstant().toEpochMilli(), event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
            }
            return;
        }
        // check if xenia is not active or inactive in which case we dont do anything
        if(!bChannel.getAccessMode().has(Channel.AccessMode.Mode.ACTIVE) || bChannel.getAccessMode().has(Channel.AccessMode.Mode.INACTIVE)) return;
        // check cooldown
        if(!commandCooldown.allow(event.getGuild().getIdLong(), event.getAuthor().getIdLong())){
            return;
        }
        commandCooldown.deny(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
        // split to list
        List<String> args = new ArrayList<>();
        Matcher matcher = ArgPattern.matcher(msg.substring(bGuild.getPrefix().length()));
        while(matcher.find()){
            args.add((matcher.group(2) != null)?matcher.group(2):matcher.group());
        }
        if(args.isEmpty()){
            return;
        }
        // get the command
        Command command = commandMap.get(args.get(0));
        if(command == null){
            if(!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE)){
                return;
            }
            List<Command> estimatedCommands = Command.getBestMatch(args.get(0), commandMap);
            if(estimatedCommands.isEmpty()){
                return;
            }
            TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(bGuild, bMember);
            if(translationPackage == null){
                event.getChannel().sendMessage("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue();
                return;
            }
            event.getChannel().sendMessage(estimatedCommands.get(0).onError(translationPackage, translationPackage.getTranslationWithPlaceholders("default.estimatedCommand.msg", args.get(0), estimatedCommands.get(0).getAlias()))).queue();
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
