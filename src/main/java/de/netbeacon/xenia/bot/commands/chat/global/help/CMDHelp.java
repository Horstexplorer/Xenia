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

package de.netbeacon.xenia.bot.commands.chat.global.help;

import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.paginator.Page;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Global help command
 */
public class CMDHelp extends Command{

	private static final int COMMANDS_PER_PAGE = 8;
	private CommandGroup parent;
	private HashMap<String, Command> commandMap;

	public CMDHelp(CommandGroup parent){
		super("help", false, null, new HashSet<>(List.of(Permission.MESSAGE_ADD_REACTION)), null, null, null);
		this.parent = parent;
	}

	/**
	 * Creates a new instance of this class
	 *
	 * @param commandMap containing all commands
	 */
	public CMDHelp(HashMap<String, Command> commandMap){
		super("help", false, null, new HashSet<>(List.of(Permission.MESSAGE_ADD_REACTION)), null, null, null);
		this.commandMap = commandMap;
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage){
		GuildMessageReceivedEvent event = commandEvent.getEvent();
		// build command path
		StringBuilder commandPathBuilder = new StringBuilder();
		CommandGroup current = parent;
		while(current != null){
			commandPathBuilder.insert(0, current.getAlias() + " ");
			current = current.getParent();
		}
		String commandPath = commandPathBuilder.toString().trim();
		// calculate number of pages
		var commandEntries = ((parent != null) ? parent.getChildCommands().entrySet() : commandMap.entrySet());
		var filtered = commandEntries.stream().filter(e -> !e.getValue().isNSFW() || commandEvent.getEvent().getChannel().isNSFW()).sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
		var nsfw_hidden_flag = commandEntries.size() != filtered.size();
		var subLists = ListUtils.partition(new ArrayList<>(filtered), COMMANDS_PER_PAGE);
		ArrayList<Page> pages = new ArrayList<>();
		for(var subList : subLists){
			EmbedBuilder embedBuilder = EmbedBuilderFactory
				.getDefaultEmbed("Help" + ((parent != null) ? (" <" + parent.getAlias() + ">") : ""), commandEvent.getEvent().getAuthor());
			for(var cmdEntry : subList){
				Command c = cmdEntry.getValue();
				StringBuilder commandCallBuilder = new StringBuilder()
					.append(commandPath).append(" ")
					.append(c.getAlias()).append(" ");
				if(c.isCommandHandler()){
					commandCallBuilder.append("#");
				}
				else{
					for(CmdArgDef s : c.getCommandArgs()){
						commandCallBuilder.append("<").append(s.getName()).append(">").append(" ");
					}
				}
				embedBuilder.addField(commandCallBuilder.toString(), c.getDescription(translationPackage), false);
			}
			if(nsfw_hidden_flag){
				embedBuilder.setDescription(translationPackage.getTranslation("default.onMissingNSFW.hidden"));
			}
			pages.add(new Page(embedBuilder.build()));
		}
		// send result
		commandEvent.getToolBundle().paginatorManager().createPaginator(event.getChannel(), event.getAuthor(), pages);
	}

}
