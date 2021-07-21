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

import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.backend.BackendQuickAction;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.ArgPattern;

public class MessageHandler{

	private final HashMap<String, Command> commandMap;
	private final CommandCooldown commandCooldown = new CommandCooldown(CommandCooldown.Type.User, 1000);
	private final ToolBundle toolBundle;
	private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

	public MessageHandler(HashMap<String, Command> commandMap, ToolBundle toolBundle){
		this.commandMap = commandMap;
		this.toolBundle = toolBundle;
	}

	public void processNew(GuildMessageReceivedEvent event){ // ! note ! events from the bot itself get passed through
		// get backend data (move this back before the stm block when traffic is too high; this will speed up preloading data)
		Guild bGuild = toolBundle.backendClient().getGuildCache().get(event.getGuild().getIdLong());
		User bUser = toolBundle.backendClient().getUserCache().get(event.getAuthor().getIdLong());
		Member bMember = bGuild.getMemberCache().get(event.getAuthor().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
		License bLicense = toolBundle.backendClient().getLicenseCache().get(event.getGuild().getIdLong());
		// try to update
		try{
			BackendQuickAction.Update.execute(bUser, event.getAuthor(), true, false);
			BackendQuickAction.Update.execute(bMember, event.getMember(), true, false);
		}
		catch(Exception ignore){
		}
		// wrap in single object
		CommandEvent.BackendDataPack backendDataPack = new CommandEvent.BackendDataPack(bGuild, bUser, bMember, bChannel, bLicense);
		// check if xenia has been disabled in which case we dont do anything
		if(bChannel.getAccessMode().has(Channel.AccessMode.Mode.DISABLED)){
			return;
		}
		// feed for leveling
		toolBundle.levelPointManager().feed(bMember);
		// get the message & check prefix
		String msg = event.getMessage().getContentRaw();
		if(!msg.startsWith(bGuild.getPrefix())){
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
			if(bGuild.getSettings().has(Guild.GuildSettings.Settings.COMMAND_AUTO_CORRECT_MESSAGE)){
				event.getChannel().sendMessageEmbeds(estimatedCommands.get(0).onError(translationPackage, translationPackage.getTranslationWithPlaceholders("default.estimatedCommand.msg", args.get(0), estimatedCommands.get(0).getAlias()))).queue();
				return;
			}
			if(!bGuild.getSettings().has(Guild.GuildSettings.Settings.COMMAND_AUTO_CORRECT)){
				return;
			}
			command = estimatedCommands.get(0);
		}
		args.remove(0);
		// start the madness
		command.execute(args, new CommandEvent(event, backendDataPack, toolBundle));
	}

}
