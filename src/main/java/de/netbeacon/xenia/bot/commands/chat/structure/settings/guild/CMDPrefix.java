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

package de.netbeacon.xenia.bot.commands.chat.structure.settings.guild;

import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.GUILD_PREFIX_DEF;

public class CMDPrefix extends Command{

	public CMDPrefix(){
		super("prefix", false, new CommandCooldown(CommandCooldown.Type.Guild, 2000),
			null,
			new HashSet<>(List.of(Permission.MANAGE_SERVER)),
			new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
			List.of(GUILD_PREFIX_DEF)
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		CmdArg<String> newPrefix = args.getByIndex(0);
		String prefix = (newPrefix.getValue() != null) ? newPrefix.getValue() : "~";
		Guild guild = commandEvent.getBackendDataPack().guild();
		try{
			guild.setPrefix(prefix);
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", guild.getPrefix()))).queue();
		}
		catch(DataException e){
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
		}
	}

}
