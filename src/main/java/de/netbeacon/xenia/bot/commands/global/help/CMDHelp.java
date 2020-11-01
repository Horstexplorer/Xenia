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

package de.netbeacon.xenia.bot.commands.global.help;

import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Global help command
 */
public class CMDHelp extends Command {

    private CommandGroup parent;
    private HashMap<String, Command> commandMap;

    public CMDHelp(CommandGroup parent){
        super("help", "Displays a list of commands", null,null, null, null);
        this.parent = parent;
    }

    /**
     * Creates a new instance of this class
     *
     * @param commandMap containing all commands
     */
    public CMDHelp(HashMap<String, Command> commandMap){
        super("help", "Displays a list of commands", null, null, null, null);
        this.commandMap = commandMap;
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent) {
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        // build command path
        StringBuilder commandPathBuilder = new StringBuilder();
        CommandGroup current = parent;
        while(current != null){
            commandPathBuilder.insert(0, current.getAlias() + " ");
            current = current.getParent();
        }
        String commandPath = commandPathBuilder.toString().trim();
        // build help page
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Help"+((parent != null)?(" <"+parent.getAlias()+">"):""), commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor());
        StringBuilder help = new StringBuilder().append("```");
        for(Map.Entry<String, Command> entry : (parent != null)?parent.getChildCommands().entrySet():commandMap.entrySet()){
            Command c = entry.getValue();
            help.append(commandPath).append(" ").append(c.getAlias()).append(" ");
            if(c.isCommandHandler()){
                help.append("#").append(" ");
            }else{
                for(CmdArgDef s : c.getCommandArgs()){
                    help.append("<").append(s.getName()).append(">").append(" ");
                }
            }
            help.append("-").append(" ").append(c.getDescription()).append("\n");
        }
        help.append("```");
        embedBuilder.addField("Commands", help.toString(), false);
        // send result
        event.getChannel().sendMessage("**Help / Commands**\n"+help.toString()).queue();
    }
}
