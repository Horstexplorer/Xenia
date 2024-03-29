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

package de.netbeacon.xenia.bot.commands.chat.objects;

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;

public abstract class HybridCommand extends CommandGroup{

	/**
	 * Creates a new instance of this class which acts both as a command and as a command group
	 *
	 * @param parent                    if this group is located inside another group this should be set accordingly, else null
	 * @param alias                     of the command group / command
	 * @param isNSFW                    if the command is a nsfw command
	 * @param commandCooldown           cooldown of the command on execution in command mode
	 * @param botPermissions            required for the user on execution in command mode
	 * @param memberPrimaryPermission   required for the member on execution in command mode
	 * @param memberSecondaryPermission required for the member on execution in command mode
	 * @param commandArgs               for the command on execution in command mode
	 */
	public HybridCommand(CommandGroup parent, String alias, boolean isNSFW, CommandCooldown commandCooldown, HashSet<Permission> botPermissions, HashSet<Permission> memberPrimaryPermission, HashSet<Role.Permissions.Bit> memberSecondaryPermission, List<CmdArgDef> commandArgs){
		super(parent, alias, isNSFW, commandCooldown, botPermissions, memberPrimaryPermission, memberSecondaryPermission, commandArgs);
	}

	/**
	 * Called on execution of the command
	 *
	 * @param args               remaining arguments of the message
	 * @param commandEvent       CommandEvent
	 * @param translationPackage translation package
	 */
	@Override
	public abstract void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception;

}
