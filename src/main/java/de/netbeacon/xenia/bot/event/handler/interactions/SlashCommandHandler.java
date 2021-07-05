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

package de.netbeacon.xenia.bot.event.handler.interactions;

import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.backend.BackendQuickAction;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
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
	private final ToolBundle toolBundle;

	public SlashCommandHandler(HashMap<String, Command> globalCommandMap, HashMap<String, Command> guildCommandMap, ToolBundle toolBundle){
		this.globalCommandMap = globalCommandMap;
		this.guildCommandMap = new ConcurrentHashMap<>(guildCommandMap);
		this.toolBundle = toolBundle;
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
		Guild bGuild = toolBundle.backendClient().getGuildCache().get(event.getGuild().getIdLong());
		User bUser = toolBundle.backendClient().getUserCache().get(event.getUser().getIdLong());
		Member bMember = bGuild.getMemberCache().get(event.getUser().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
		License bLicense = toolBundle.backendClient().getLicenseCache().get(event.getGuild().getIdLong());
		// try to update
		try{
			BackendQuickAction.Update.execute(bUser, event.getUser(), true, false);
			BackendQuickAction.Update.execute(bMember, event.getMember(), true, false);
		}
		catch(Exception ignore){
		}
		// wrap in single object
		CommandEvent.BackendDataPack backendDataPack = new CommandEvent.BackendDataPack(bGuild, bUser, bMember, bChannel, bLicense);
		CommandEvent commandEvent = new CommandEvent(event, backendDataPack, toolBundle);
		// check if xenia is active in this channel
		if(!bChannel.getAccessMode().has(Channel.AccessMode.Mode.ACTIVE)){
			return;
		}
		// feed for leveling
		toolBundle.levelPointManager().feed(bMember);
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
