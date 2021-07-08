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

package de.netbeacon.xenia.bot.event.listener.message;

import de.netbeacon.xenia.bot.commands.chat.global.help.CMDHelp;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.structure.admin.GROUPAdmin;
import de.netbeacon.xenia.bot.commands.chat.structure.anime.GROUPAnime;
import de.netbeacon.xenia.bot.commands.chat.structure.avatar.CMDAvatar;
import de.netbeacon.xenia.bot.commands.chat.structure.chatbot.GROUPChatbot;
import de.netbeacon.xenia.bot.commands.chat.structure.hastebin.CMDHastebin;
import de.netbeacon.xenia.bot.commands.chat.structure.info.CMDInfo;
import de.netbeacon.xenia.bot.commands.chat.structure.last.GROUPLast;
import de.netbeacon.xenia.bot.commands.chat.structure.me.CMDMe;
import de.netbeacon.xenia.bot.commands.chat.structure.notification.GROUPNotification;
import de.netbeacon.xenia.bot.commands.chat.structure.settings.GROUPSettings;
import de.netbeacon.xenia.bot.commands.chat.structure.tags.HYBRIDTag;
import de.netbeacon.xenia.bot.commands.chat.structure.twitch.HYBRIDTwitch;
import de.netbeacon.xenia.bot.event.handler.MessageHandler;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

public class GuildMessageCommandListener extends ListenerAdapter{

	private final MessageHandler commandHandler;

	public GuildMessageCommandListener(ToolBundle toolBundle){
		HashMap<String, Command> commandMap = new HashMap<>();
		Consumer<Command> register = command -> commandMap.put(command.getAlias(), command);

		register.accept(new CMDHelp(commandMap));

		register.accept(new GROUPAdmin());
		register.accept(new GROUPLast());
		register.accept(new GROUPNotification());
		register.accept(new GROUPSettings());
		register.accept(new GROUPChatbot());
		register.accept(new GROUPAnime());

		register.accept(new HYBRIDTwitch());
		register.accept(new HYBRIDTag());

		register.accept(new CMDHastebin());
		register.accept(new CMDInfo());
		register.accept(new CMDMe());
		register.accept(new CMDAvatar());

		commandHandler = new MessageHandler(commandMap, toolBundle);
	}

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event){
		if(event.getAuthor().isSystem() || event.isWebhookMessage() || event.getAuthor().isBot()){
			return;
		}
		commandHandler.processNew(event);
	}

}
