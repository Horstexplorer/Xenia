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

package de.netbeacon.xenia.bot.event.handler;

import de.netbeacon.d43z.one.eval.io.EvalRequest;
import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.anime.AnimeTask;
import de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.eval.DefaultEvalTask;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;
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
import java.util.stream.Collectors;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.ArgPattern;

public class MessageHandler{

	private final HashMap<String, Command> commandMap;
	private final CommandCooldown commandCooldown = new CommandCooldown(CommandCooldown.Type.User, 1000);
	private final EventWaiter eventWaiter;
	private final XeniaBackendClient backendClient;
	private final PaginatorManager paginatorManager;
	private final LevelPointManager levelPointManager;
	private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
	private final D43Z1ContextPoolManager contextPoolManager;

	public MessageHandler(HashMap<String, Command> commandMap, EventWaiter eventWaiter, PaginatorManager paginatorManager, XeniaBackendClient backendClient, D43Z1ContextPoolManager contextPoolManager, LevelPointManager levelPointManager){
		this.commandMap = commandMap;
		this.eventWaiter = eventWaiter;
		this.paginatorManager = paginatorManager;
		this.backendClient = backendClient;
		this.contextPoolManager = contextPoolManager;
		this.levelPointManager = levelPointManager;
	}

	public void processNew(GuildMessageReceivedEvent event){ // ! note ! events from the bot itself get passed through
		// get backend data (move this back before the stm block when traffic is too high; this will speed up preloading data)
		Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
		User bUser = backendClient.getUserCache().get(event.getAuthor().getIdLong());
		Member bMember = bGuild.getMemberCache().get(event.getAuthor().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
		License bLicense = backendClient.getLicenseCache().get(event.getGuild().getIdLong());
		// wrap in single object
		CommandEvent.BackendDataPack backendDataPack = new CommandEvent.BackendDataPack(bGuild, bUser, bMember, bChannel, bLicense);
		// check if xenia has been disabled in which case we dont do anything
		if(bChannel.getAccessMode().has(Channel.AccessMode.Mode.DISABLED)){
			return;
		}
		// feed for leveling
		levelPointManager.feed(bMember);
		// get the message & check prefix
		String msg = event.getMessage().getContentRaw();
		if(!msg.startsWith(bGuild.getPrefix())){
			if(bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE) && event.getChannel().isNSFW() && !event.getAuthor().isBot()){ // bot messages should be logged in some cases but we do not want to process em
				try{
					D43Z1_EXECUTE(event, backendDataPack);
				}
				catch(Exception e){
					logger.warn("An exception occurred while handing message over to D43Z1 ", e);
				}
			}
			else if(bChannel.tmpLoggingIsActive()){ // check if the message should be logged
				// bot messages should be logged in some cases
				if(event.getAuthor().isBot() && !(bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE) && bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING))){
					// will return when it is from the bot but either the chatbot or selflearning is not active
					return;
				}
				// log the message
				var message = event.getMessage();
				bChannel.getMessageCache()
					.create(
						message.getIdLong(),
						message.getTimeCreated().toInstant().toEpochMilli(),
						message.getAuthor().getIdLong(),
						message.getContentRaw(),
						message.getAttachments().stream().map(net.dv8tion.jda.api.entities.Message.Attachment::getUrl).collect(Collectors.toList()),
						true
					);
			}
			return;
		}
		// ! note ! events from the bot itself get passed through | we filter em out here again
		if(event.getAuthor().isBot()){
			return;
		}
		// check if xenia is not active or inactive in which case we dont do anything
		if(!bChannel.getAccessMode().has(Channel.AccessMode.Mode.ACTIVE) || bChannel.getAccessMode().has(Channel.AccessMode.Mode.INACTIVE)){
			return;
		}
		// check cooldown
		if(!commandCooldown.allow(event.getGuild().getIdLong(), event.getAuthor().getIdLong())){
			return;
		}
		commandCooldown.deny(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
		// split to list
		List<String> args = new ArrayList<>();
		Matcher matcher = ArgPattern.matcher(msg.substring(bGuild.getPrefix().length()));
		while(matcher.find()){
			args.add((matcher.group(2) != null) ? matcher.group(2) : matcher.group());
		}
		if(args.isEmpty()){
			return;
		}
		// get the command
		Command command = commandMap.get(args.get(0));
		if(command == null){
			if(bGuild.getSettings().has(Guild.GuildSettings.Settings.DISABLE_COMMAND_AUTO_CORRECT_MESSAGE)){
				return;
			}
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
			if(!bGuild.getSettings().has(Guild.GuildSettings.Settings.COMMAND_AUTO_CORRECT)){
				event.getChannel().sendMessage(estimatedCommands.get(0).onError(translationPackage, translationPackage.getTranslationWithPlaceholders("default.estimatedCommand.msg", args.get(0), estimatedCommands.get(0).getAlias()))).queue();
				return;
			}
			command = estimatedCommands.get(0);
		}
		args.remove(0);
		// start the madness
		command.execute(args, new CommandEvent(event, backendDataPack, backendClient, eventWaiter, paginatorManager, contextPoolManager));
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
		message.lSetMessageContent(event.getMessage().getContentRaw(), messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey());
		message.updateAsync(true);
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
		channel.sendMessage(EmbedBuilderFactory.getDefaultEmbed("Message Edited!")
			.addField("MessageID", event.getMessageId(), true)
			.addField("Author", event.getAuthor().getAsTag(), true)
			.addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
			.build()
		).queue(s -> {}, e -> {});
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
		channel.sendMessage(EmbedBuilderFactory.getDefaultEmbed("Message Deleted!")
			.addField("MessageID", event.getMessageId(), true)
			.addField("AuthorID", String.valueOf(message.getUserId()), true)
			.addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
			.build()
		).queue(s -> {}, e -> {});
	}

	public void D43Z1_EXECUTE(GuildMessageReceivedEvent event, CommandEvent.BackendDataPack backendDataPack) throws Exception{
		var bGuild = backendDataPack.getbGuild();
		var d43Z1Imp = D43Z1Imp.getInstance();

		var taskMaster = d43Z1Imp.getTaskMaster();
		var content = new Content(event.getMessage().getContentRaw());
		var taskResult = taskMaster.getTaskOrDefault(content, 0.7F, d43Z1Imp.getDefaultEvalTask());

		if(taskResult.getValue1() instanceof DefaultEvalTask){
			// send thinking...
			event.getMessage().reply("thinking ...").mentionRepliedUser(false).queue( thinking -> {
				// process
				ContentMatchBuffer contextMatchBuffer = d43Z1Imp.getContentMatchBufferFor(event.getAuthor().getIdLong());
				IContextPool contextPool = contextPoolManager.getPoolFor(bGuild);
				EvalRequest evalRequest = new EvalRequest(contextPool, contextMatchBuffer, new Content(event.getMessage().getContentRaw()),
					evalResult -> {
						if(evalResult.ok()){
							thinking.editMessage(evalResult.getContentMatch().getEstimatedOutput().getContent()).mentionRepliedUser(false).queue();
						}
					}, SharedExecutor.getInstance().getScheduledExecutor());
				((DefaultEvalTask) taskResult.getValue1()).execute(content, new Pair<>(d43Z1Imp.getEval(), evalRequest));
			});
		}
		else if(taskResult.getValue1() instanceof AnimeTask){
			((AnimeTask) taskResult.getValue1()).execute(content, new Pair<>(event.getMember(), event.getChannel()));
		}
		else{
			// ??? -> Fallback for when bad things happen
			ContentMatchBuffer contextMatchBuffer = d43Z1Imp.getContentMatchBufferFor(event.getAuthor().getIdLong());
			IContextPool contextPool = contextPoolManager.getPoolFor(bGuild);
			EvalRequest evalRequest = new EvalRequest(contextPool, contextMatchBuffer, new Content(event.getMessage().getContentRaw()),
				evalResult -> {
					if(evalResult.ok()){
						event.getMessage().reply(evalResult.getContentMatch().getEstimatedOutput().getContent()).mentionRepliedUser(false).queue();
					}
				}, SharedExecutor.getInstance().getScheduledExecutor());
			d43Z1Imp.getEval().enqueue(evalRequest);
		}
	}

}
