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

package de.netbeacon.xenia.bot.event.handler;

import de.netbeacon.xenia.backend.client.objects.apidata.Channel;
import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.Message;
import de.netbeacon.xenia.backend.client.objects.apidata.User;
import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.bot.utils.backend.BackendQuickAction;
import de.netbeacon.xenia.bot.utils.backend.action.BackendActions;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import java.util.List;
import java.util.stream.Collectors;

public class LoggingHandler{

	private final ToolBundle toolBundle;

	public LoggingHandler(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
	}

	public void processCreate(GuildMessageReceivedEvent event){
		var backendClient = toolBundle.backendClient();
		BackendActions.allOf(List.of(
			backendClient.getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true),
			backendClient.getLicenseCache().retrieve(event.getGuild().getIdLong(), true),
			backendClient.getUserCache().retrieveOrCreate(event.getAuthor().getIdLong(), true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			User bUser = barb.get(User.class);
			BackendQuickAction.Update.execute(bUser, event.getAuthor(), true, false);
			bGuild.getChannelCache().retrieveOrCreate(event.getChannel().getIdLong(), true).queue(bChannel -> {
				if(bChannel.tmpLoggingIsActive()){
					// bot messages should be logged in some cases
					if(event.getAuthor().isBot() && !(bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE) && bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ENABLE_SELF_LEARNING))){
						// will return when it is from the bot but either the chatbot or selflearning is not active
						return;
					}
					// log the message
					var message = event.getMessage();
					bChannel.getMessageCache()
						.create(
							message.getIdLong(),
							message.getTimeCreated().toInstant().toEpochMilli(),
							message.getAuthor().getIdLong(),
							message.getContentRaw(),
							message.getAttachments().stream().map(net.dv8tion.jda.api.entities.Message.Attachment::getUrl).collect(Collectors.toList())
						).queue();
				}
			});
		});
	}

	public void processUpdate(GuildMessageUpdateEvent event){
		toolBundle.backendClient().getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true).queue(bGuild -> {
			bGuild.getChannelCache().retrieveOrCreate(event.getChannel().getIdLong(), true).queue(bChannel -> {
				MessageCache messageCache = bChannel.getMessageCache();
				Message message = messageCache.get_(event.getMessageIdLong());
				if(message == null){
					return;
				}
				// update message content
				message.lSetMessageContent(event.getMessage().getContentRaw(), messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey());
				message.update(true).queue();
				// update thingy
				messageCache.setLast("edited", message.getId());
				// check if notification is active
				if(bChannel.getTmpLoggingChannelId() == -1){
					return;
				}
				TextChannel channel = event.getGuild().getTextChannelById(bChannel.getTmpLoggingChannelId());
				if(channel == null){
					bChannel.setTmpLoggingChannelId(-1);
					return;
				}
				channel.sendMessageEmbeds(EmbedBuilderFactory.getDefaultEmbed("Message Edited!")
					.addField("MessageID", event.getMessageId(), true)
					.addField("Author", event.getAuthor().getAsTag(), true)
					.addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
					.build()
				).queue();
			});
		});
	}

	public void processDelete(GuildMessageDeleteEvent event){
		toolBundle.backendClient().getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true).queue(bGuild -> {
			bGuild.getChannelCache().retrieveOrCreate(event.getChannel().getIdLong(), true).queue(bChannel -> {
				MessageCache messageCache = bChannel.getMessageCache();
				Message message = messageCache.get_(event.getMessageIdLong());
				if(message == null){
					return;
				}
				// update thingy
				messageCache.setLast("deleted", event.getMessageIdLong());
				// try sending the message there
				if(bChannel.getTmpLoggingChannelId() == -1){
					return;
				}
				TextChannel channel = event.getGuild().getTextChannelById(bChannel.getTmpLoggingChannelId());
				if(channel == null){
					bChannel.setTmpLoggingChannelId(-1);
					return;
				}
				channel.sendMessageEmbeds(EmbedBuilderFactory.getDefaultEmbed("Message Deleted!")
					.addField("MessageID", event.getMessageId(), true)
					.addField("AuthorID", String.valueOf(message.getUserId()), true)
					.addField("Old Message", message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
					.build()
				).queue();
			});
		});
	}

}
