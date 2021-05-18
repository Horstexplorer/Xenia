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

package de.netbeacon.xenia.bot.commands.slash.structure.me;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;

public class RCMDMe extends Command{

	public RCMDMe(){
		super("me", "Show some stats about yourself", false, new CommandCooldown(CommandCooldown.Type.User, 30000),
			null,
			null,
			null,
			null
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		commandEvent.getEvent().deferReply(false).queue(
			commandHook -> {
				try{
					var baos = LevelPointManager.getLevelPointCard(commandEvent.getBackendDataPack().getbMember()).getByteArrayOutputStream();
					commandHook.editOriginal(baos.toByteArray(), "stats.png").queue();
				}
				catch(Exception e){
					commandHook.editOriginal("\u274C").queue();
				}
			}
		);
	}

}
