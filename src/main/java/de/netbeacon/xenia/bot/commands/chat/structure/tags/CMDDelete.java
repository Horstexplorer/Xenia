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

package de.netbeacon.xenia.bot.commands.chat.structure.tags;

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.backend.client.objects.apidata.misc.Tag;
import de.netbeacon.xenia.backend.client.objects.cache.misc.TagCache;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.TAG_NAME_DEF;

public class CMDDelete extends Command{

	public CMDDelete(){
		super("delete", false, new CommandCooldown(CommandCooldown.Type.User, 5000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.TAG_CREATE)),
			List.of(TAG_NAME_DEF)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		TagCache tagCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getTagCache();
		CmdArg<String> tagA = cmdArgs.getByIndex(0);
		try{
			Tag tag = tagCache.retrieve(tagA.getValue(), true).execute();
			if(tag.getUserId() != commandEvent.getEvent().getAuthor().getIdLong() && !(commandEvent.getBackendDataPack().member().metaIsAdministrator() || commandEvent.getBackendDataPack().member().metaIsOwner())){
				throw new RuntimeException("User Does Not Own This Tag");
			}
			tagCache.delete(tag.getId()).execute();
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", tag.getId()))).queue();
		}
		catch(DataException | CacheException e){
			if(e instanceof DataException && ((DataException) e).getCode() == 404){
				commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
			}
			else{
				throw e;
			}
		}
	}

}
