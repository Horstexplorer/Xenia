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

package de.netbeacon.xenia.bot.commands.slash.structure.tag.s;

import de.netbeacon.xenia.backend.client.objects.cache.misc.TagCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Tag;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.KEY_PATTERN;

public class CMDDelete extends Command{

	public CMDDelete(){
		super("delete", "Deletes an existing tag", false, new CommandCooldown(CommandCooldown.Type.User, 5000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.TAG_CREATE)),
			List.of(
				new CmdArgDef.Builder<>("name", "Tag name", "Tag name, 3 to 32 chars, only alphanumeric", String.class).predicateAddStringLengthRange(3, 32).predicateAddPredicate(t -> KEY_PATTERN.matcher(t).matches()).predicateAddPredicate(t -> !(t.equalsIgnoreCase("create") || t.equalsIgnoreCase("modify") || t.equalsIgnoreCase("delete"))).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		TagCache tagCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTagCache();
		CmdArg<String> nameArg = cmdArgs.getByName("name");
		try{
			Tag tag = tagCache.get(nameArg.getValue());
			if(tag.getUserId() != commandEvent.getEvent().getUser().getIdLong() && !(commandEvent.getBackendDataPack().getbMember().metaIsAdministrator() || commandEvent.getBackendDataPack().getbMember().metaIsOwner())){
				throw new RuntimeException("User Does Not Own This Tag");
			}
			tagCache.delete(tag.getId());
			commandEvent.getEvent().replyEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", tag.getId()))).queue();
		}
		catch(DataException | CacheException e){
			if(e instanceof DataException && ((DataException) e).getCode() == 404){
				commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
			}
			else{
				throw e;
			}
		}
	}

}
