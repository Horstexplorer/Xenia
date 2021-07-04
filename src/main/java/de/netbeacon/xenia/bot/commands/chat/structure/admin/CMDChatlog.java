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

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Message;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.hastebin.HastebinUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.ADMIN_CHATLOG_CHANNEL;
import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.ADMIN_CHATLOG_LIMIT;

public class CMDChatlog extends AdminCommand{

	public CMDChatlog(){
		super("chatlog", new CommandCooldown(CommandCooldown.Type.User, 6000), null, null, null, List.of(ADMIN_CHATLOG_CHANNEL, ADMIN_CHATLOG_LIMIT));
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage){
		CmdArg<Mention> channelArg = args.getByIndex(0); // unused for now
		CmdArg<Boolean> limitArg = args.getByIndex(1); // unused for now

		Channel bChannel = commandEvent.getBackendDataPack().channel();

		if(channelArg.getValue() != null && channelArg.getValue().getId() > 0){
			long cId = channelArg.getValue().getId();
			if(commandEvent.getBackendDataPack().guild().getChannelCache().contains(cId)){
				bChannel = commandEvent.getBackendDataPack().guild().getChannelCache().get(cId);
			}
		}

		List<Message> messages = bChannel.getMessageCache().retrieveAllFromBackend(limitArg.getValue() != null && limitArg.getValue(), false);
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject()
			.put("guildId", commandEvent.getEvent().getGuild().getIdLong())
			.put("channelId", bChannel.getChannelId())
			.put("messages", jsonArray);
		messages.forEach(message -> {
			var json = message.asJSON();
			json.remove("channelId");
			json.remove("guildId");
			jsonArray.put(json);
		});
		// upload to hastebin
		String jsonString = jsonObject.toString(3);
		try{
			String url = HastebinUtil.uploadToHastebin(jsonString);
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, "Here is the [chatlog](" + url + ") [" + jsonArray.length() + " messages]")).queue();
		}
		catch(Exception e){
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, "Something went wrong uploading the chat log (sizeOf: " + jsonString.length() + ")")).queue();
		}
	}

}
