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

import de.netbeacon.xenia.backend.client.objects.cache.misc.TagCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Tag;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.mentionremover.MentionRemover;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.TAG_NAME_DEF;

public class HYBRIDTag extends HybridCommand{

	public HYBRIDTag(){
		super(null, "tag", false, new CommandCooldown(CommandCooldown.Type.User, 1000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.TAG_USE)),
			List.of(TAG_NAME_DEF)
		);
		addChildCommand(new CMDCreate());
		addChildCommand(new CMDModify());
		addChildCommand(new CMDDelete());
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		TagCache tagCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getTagCache();
		CmdArg<String> tagA = cmdArgs.getByIndex(0);
		try{
			Tag tag = tagCache.get(tagA.getValue());
			commandEvent.getEvent().getChannel().sendMessage(MentionRemover.process(translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", tag.getTagContent(), tag.getMember().getUser().getMetaUsername()))).queue();
		}
		catch(DataException | CacheException e){
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.error.msg", tagA.getValue()))).queue();
		}
	}

}
