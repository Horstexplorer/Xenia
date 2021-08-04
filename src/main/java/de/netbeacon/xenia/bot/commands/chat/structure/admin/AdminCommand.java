/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public abstract class AdminCommand extends Command{

	public AdminCommand(String alias, CommandCooldown commandCooldown, HashSet<Permission> botPermissions, HashSet<Permission> memberPrimaryPermissions, HashSet<Role.Permissions.Bit> memberSecondaryPermission, List<CmdArgDef> commandArgs){
		super(alias, false, commandCooldown, botPermissions, memberPrimaryPermissions, memberSecondaryPermission, commandArgs);
	}

	@Override
	public MessageEmbed onMissingMemberPerms(CommandEvent commandEvent, TranslationPackage translationPackage, boolean v){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingMemberPerms.title"))
			.setColor(Color.RED)
			.appendDescription("You are not allowed to do this")
			.build();
	}

	@Override
	public void execute(List<String> args, CommandEvent commandEvent){
		var selfMember = commandEvent.getEvent().getGuild().getSelfMember();
		var textChannel = commandEvent.getEvent().getChannel();

		TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(commandEvent.getBackendDataPack().guild(), commandEvent.getBackendDataPack().member());
		if(translationPackage == null){
			textChannel.sendMessage("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue();
			return;
		}
		// check required args
		CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
		if(!cmdArgs.verify()){
			// missing args
			textChannel.sendMessageEmbeds(onMissingArgs(translationPackage)).queue();
			return;
		}
		if(commandEvent.getEvent().getAuthor().getIdLong() != XeniaCore.getInstance().getConfig().getLong("ownerID")){
			// invalid permission
			textChannel.sendMessageEmbeds(onMissingMemberPerms(commandEvent, translationPackage, false)).queue();
			return;
		}
		if(!selfMember.hasPermission(getBotPermissions())){
			// bot does not have the required permissions
			textChannel.sendMessageEmbeds(onMissingBotPerms(commandEvent, translationPackage)).queue();
			return;
		}
		// everything alright
		try{
			onExecution(cmdArgs, commandEvent, translationPackage);
		}
		catch(Exception e){
			textChannel.sendMessageEmbeds(onUnhandledException(translationPackage, e)).queue();
		}
	}

}
