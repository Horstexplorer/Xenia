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

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonManager;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SlashCommandHandler{

	private final HashMap<String, Command> globalCommandMap;
	private final ConcurrentHashMap<String, Command> guildCommandMap;
	private final EventWaiter eventWaiter;
	private final XeniaBackendClient backendClient;
	private final PaginatorManager paginatorManager;
	private final ButtonManager buttonManager;
	private final D43Z1ContextPoolManager contextPoolManager;
	private final LevelPointManager levelPointManager;

	public SlashCommandHandler(HashMap<String, Command> globalCommandMap, HashMap<String, Command> guildCommandMap, EventWaiter eventWaiter, PaginatorManager paginatorManager, ButtonManager buttonManager, XeniaBackendClient backendClient, D43Z1ContextPoolManager contextPoolManager, LevelPointManager levelPointManager){
		this.globalCommandMap = globalCommandMap;
		this.guildCommandMap = new ConcurrentHashMap<>(guildCommandMap);
		this.eventWaiter = eventWaiter;
		this.paginatorManager = paginatorManager;
		this.buttonManager = buttonManager;
		this.backendClient = backendClient;
		this.contextPoolManager = contextPoolManager;
		this.levelPointManager = levelPointManager;
	}

	public List<CommandData> getGlobalCommandData(){
		return globalCommandMap.values().stream().map(Command::getCommandData).collect(Collectors.toList());
	}

	public List<CommandData> getGuildCommandData(long guildId){ // currently unused
		return guildCommandMap.values().stream().map(Command::getCommandData).collect(Collectors.toList());
	}

	public void handle(SlashCommandEvent event){
		long start = System.currentTimeMillis();
		// get backend data (move this back before the stm block when traffic is too high; this will speed up preloading data)
		Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
		User bUser = backendClient.getUserCache().get(event.getUser().getIdLong());
		Member bMember = bGuild.getMemberCache().get(event.getUser().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
		License bLicense = backendClient.getLicenseCache().get(event.getGuild().getIdLong());
		// wrap in single object
		CommandEvent.BackendDataPack backendDataPack = new CommandEvent.BackendDataPack(bGuild, bUser, bMember, bChannel, bLicense);
		CommandEvent commandEvent = new CommandEvent(event, backendDataPack, backendClient, eventWaiter, paginatorManager, buttonManager, contextPoolManager);
		// check if xenia is active in this channel
		if(!bChannel.getAccessMode().has(Channel.AccessMode.Mode.ACTIVE)){
			return;
		}
		// feed for leveling
		levelPointManager.feed(bMember);
		// split to list
		ArrayList<String> args = new ArrayList<>();
		args.add(event.getName());
		if(event.getSubcommandGroup() != null){
			args.add(event.getSubcommandGroup());
		}
		if(event.getSubcommandName() != null){
			args.add(event.getSubcommandName());
		}
		if(args.isEmpty()){
			return;
		}
		// update processing time
		commandEvent.addProcessingTime(System.currentTimeMillis() - start);
		// execute command
		Command command = globalCommandMap.get(args.get(0));
		if(command == null){
			command = guildCommandMap.get(args.get(0));
			if(command == null){
				return;
			}
		}
		args.remove(0);
		command.execute(args, commandEvent);
	}

}
