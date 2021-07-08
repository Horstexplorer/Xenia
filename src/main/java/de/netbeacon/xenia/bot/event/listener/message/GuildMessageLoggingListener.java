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

package de.netbeacon.xenia.bot.event.listener.message;

import de.netbeacon.xenia.bot.event.handler.LoggingHandler;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMessageLoggingListener extends ListenerAdapter{

	private final ToolBundle toolBundle;
	private final LoggingHandler loggingHandler;

	public GuildMessageLoggingListener(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
		this.loggingHandler = new LoggingHandler(toolBundle);
	}

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event){
		if(event.getAuthor().isSystem() || event.isWebhookMessage() || (event.getAuthor().isBot() && !event.getGuild().getSelfMember().equals(event.getMember()))){
			return;
		}
		toolBundle.eventWaiter().waitingOnThis(event);
		// process new
		loggingHandler.processCreate(event);
	}

	@Override
	public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event){
		if(event.getAuthor().isSystem() || (event.getAuthor().isBot() && !event.getGuild().getSelfMember().equals(event.getMember()))){
			return;
		}
		toolBundle.eventWaiter().waitingOnThis(event);
		// process update
		loggingHandler.processUpdate(event);
	}

	@Override
	public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event){
		toolBundle.eventWaiter().waitingOnThis(event);
		// process delete
		loggingHandler.processDelete(event);
	}

	@Override
	public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event){
		toolBundle.eventWaiter().waitingOnThis(event);
	}

}
