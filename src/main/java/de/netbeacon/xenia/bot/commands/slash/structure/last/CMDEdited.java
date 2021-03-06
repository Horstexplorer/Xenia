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

package de.netbeacon.xenia.bot.commands.slash.structure.last;

import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Message;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;

public class CMDEdited extends Command{

	public CMDEdited(){
		super("edited", "Restores the last message which has been edited in this channel", false, new CommandCooldown(CommandCooldown.Type.User, 1000),
			null,
			new HashSet<>(java.util.List.of(Permission.MESSAGE_MANAGE)),
			new HashSet<>(List.of(Role.Permissions.Bit.MESSAGE_RESTORE_USE)),
			null
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		Channel bChannel = commandEvent.getBackendDataPack().channel();
		MessageCache messageCache = bChannel.getMessageCache();
		Message bMessage = bChannel.getMessageCache().getLast("edited");
		if(bMessage == null){
			commandEvent.getEvent().replyEmbeds(
				onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))
			).queue();
		}
		else{
			StringBuilder attachments = new StringBuilder();
			List<String> attachmentUrls = bMessage.getAttachmentUrls();
			for(int i = 0; i < attachmentUrls.size(); i++){
				attachments.append("[Attachment").append(i != 0 ? "_" + i : "").append("](").append(attachmentUrls.get(i)).append(")").append(" ");
			}
			commandEvent.getEvent().replyEmbeds(EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.success.title"))
				.addField(translationPackage.getTranslation(getClass(), "response.success.field.1.title"), String.valueOf(bMessage.getId()), true)
				.addField(translationPackage.getTranslation(getClass(), "response.success.field.2.title"), bMessage.getMember().getUser().getMetaUsername(), true)
				.addField(translationPackage.getTranslation(getClass(), "response.success.field.3.title"), bMessage.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
				.addField(translationPackage.getTranslation(getClass(), "response.success.field.4.title"), !attachments.isEmpty() ? attachments.toString() : "none", false)
				.build()
			).queue();
		}
	}

}
