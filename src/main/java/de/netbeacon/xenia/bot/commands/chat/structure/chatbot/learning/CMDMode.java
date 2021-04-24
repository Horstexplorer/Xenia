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

package de.netbeacon.xenia.bot.commands.chat.structure.chatbot.learning;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.CB_CHATBOT_MODE;

public class CMDMode extends Command{

	public CMDMode(){
		super("mode", false, new CommandCooldown(CommandCooldown.Type.Guild, 2500),
			null,
			new HashSet<>(List.of(Permission.MANAGE_SERVER)),
			new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
			List.of(CB_CHATBOT_MODE)
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		try{
			CmdArg<String> modeStringArg = args.getByIndex(0);
			Guild.D43Z1Mode.Modes modeSelect = Guild.D43Z1Mode.Modes.valueOf(modeStringArg.getValue().toUpperCase());
			Guild g = commandEvent.getBackendDataPack().getbGuild();
			Guild.D43Z1Mode mode = new Guild.D43Z1Mode(0);
			mode.set(modeSelect);
			g.setD43Z1Mode(mode);
			commandEvent.getPoolManager().getPoolFor(commandEvent.getBackendDataPack().getbGuild(), true);
			commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "success.msg"))).queue();
		}
		catch(IllegalArgumentException e){
			commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "error.invalid.arg.msg", Arrays.toString(Guild.D43Z1Mode.Modes.values())))).queue();
		}
	}

}
