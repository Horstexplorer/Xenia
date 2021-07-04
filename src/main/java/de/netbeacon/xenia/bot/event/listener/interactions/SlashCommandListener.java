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

package de.netbeacon.xenia.bot.event.listener.interactions;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.structure.anime.RCMDGAnime;
import de.netbeacon.xenia.bot.commands.slash.structure.avatar.RCMDAvatar;
import de.netbeacon.xenia.bot.commands.slash.structure.info.RCMDInfo;
import de.netbeacon.xenia.bot.commands.slash.structure.last.RCMDGLast;
import de.netbeacon.xenia.bot.commands.slash.structure.me.RCMDMe;
import de.netbeacon.xenia.bot.commands.slash.structure.notification.RCMDGNotification;
import de.netbeacon.xenia.bot.commands.slash.structure.tag.RCMDGTags;
import de.netbeacon.xenia.bot.commands.slash.structure.tag.RCMDTag;
import de.netbeacon.xenia.bot.commands.slash.structure.twitch.RCMDGTwitch;
import de.netbeacon.xenia.bot.event.handler.interactions.SlashCommandHandler;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonManager;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Consumer;

public class SlashCommandListener extends ListenerAdapter{

	private final EventWaiter eventWaiter;
	private final SlashCommandHandler slashCommandHandler;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public SlashCommandListener(XeniaBackendClient backendClient, EventWaiter eventWaiter, PaginatorManager paginatorManager, ButtonManager buttonManager, D43Z1ContextPoolManager contextPoolManager, LevelPointManager levelPointManager){
		this.eventWaiter = eventWaiter;

		HashMap<String, Command> globalCommandMap = new HashMap<>();
		Consumer<Command> register = command -> globalCommandMap.put(command.getAlias(), command);
		// register up to 100 commands available for all guilds (global pool) here
		register.accept(new RCMDInfo());
		register.accept(new RCMDTag());
		register.accept(new RCMDGTags());
		register.accept(new RCMDGLast());
		register.accept(new RCMDGNotification());
		register.accept(new RCMDGAnime());
		register.accept(new RCMDMe());
		register.accept(new RCMDAvatar());
		register.accept(new RCMDGTwitch());

		// // // // // // // // // //
		HashMap<String, Command> guildCommandMap = new HashMap<>();
		register = command -> guildCommandMap.put(command.getAlias(), command);
		// register up to 100 commands which can be guild specifically toggled

		// // // // // // // // // //
		this.slashCommandHandler = new SlashCommandHandler(globalCommandMap, guildCommandMap, eventWaiter, paginatorManager, buttonManager, backendClient, contextPoolManager, levelPointManager);
	}

	@Override
	public void onReady(@NotNull ReadyEvent event){
		if(event.getJDA().getShardInfo().getShardId() != 0){
			return;
		}
		// update commands global for everything if we are on shard 0
		event.getJDA()
			.updateCommands()
			.addCommands(slashCommandHandler.getGlobalCommandData())
			.queue(s -> {
				logger.info("Updated Global Commands");
			}, f -> {
				logger.warn("Failed To Update Global Commands", f);
			});
	}

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event){
		// update guild specific commands
		try{
			event.getGuild()
				.updateCommands()
				.addCommands(slashCommandHandler.getGuildCommandData(event.getGuild().getIdLong()))
				.queue(s -> {
					logger.debug("Updated Guild Commands For Guild " + event.getGuild().getIdLong());
				}, f -> {
					logger.debug("Failed To Update Commands For Guild " + event.getGuild().getIdLong(), f);
				});
		}
		catch(Exception ignore){
			logger.debug("Failed To Update Commands For Guild " + event.getGuild().getIdLong());
		}
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event){
		if(event.getUser().isBot() || eventWaiter.waitingOnThis(event)){
			return;
		}
		if(event.getGuild() == null){ // listen for events from guilds only
			event.reply("\u274C").setEphemeral(true).queue();
			return;
		}
		slashCommandHandler.handle(event);
	}

}
