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

package de.netbeacon.xenia.bot.commands.chat.structure.settings.self;

import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;

import java.util.Arrays;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.SELF_LANGUAGE_ID_DEF;

public class CMDLanguage extends Command{

	public CMDLanguage(){
		super("language", false, new CommandCooldown(CommandCooldown.Type.User, 2000),
			null,
			null,
			null,
			List.of(SELF_LANGUAGE_ID_DEF)
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		CmdArg<String> langIdA = args.getByIndex(0);
		String languageId = langIdA.getValue();
		TranslationPackage translationPackage1 = TranslationManager.getInstance().getTranslationPackage(languageId);
		try{
			if(translationPackage1 == null){
				throw new IllegalArgumentException();
			}
			commandEvent.getBackendDataPack().user().setPreferredLanguage(translationPackage1.getLanguageId());
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.msg"))).queue();
		}
		catch(IllegalArgumentException e){
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.error.msg", Arrays.toString(TranslationManager.getInstance().getLanguageIds().toArray())))).queue();
		}
	}

}
