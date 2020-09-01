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

package de.netbeacon.xenia.commands.global.help;

import de.netbeacon.xenia.commands.objects.Command;
import de.netbeacon.xenia.commands.objects.CommandEvent;
import de.netbeacon.xenia.commands.objects.CommandGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Global help command
 */
public class CMDHelp extends Command {

    private CommandGroup parent;
    private HashMap<String, Command> commandMap;

    public CMDHelp(CommandGroup parent){
        super("help", "Displays a list of commands", new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)),new HashSet<>(Arrays.asList()), Arrays.asList());
        this.parent = parent;
    }

    /**
     * Creates a new instance of this class
     *
     * @param commandMap containing all commands
     */
    public CMDHelp(HashMap<String, Command> commandMap){
        super("help", "Displays a list of commands", new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)),new HashSet<>(Arrays.asList()), Arrays.asList());
        this.commandMap = commandMap;
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
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
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Help"+((parent != null)?(" <"+parent.getAlias()+">"):""))
                .setColor(Color.CYAN);
        StringBuilder help = new StringBuilder();
        for(Map.Entry<String, Command> entry : (parent != null)?parent.getChildCommands().entrySet():commandMap.entrySet()){
            Command c = entry.getValue();
            help.append(commandPath).append(" ").append(c.getAlias()).append(" ");
            if(c.isCommandHandler()){
                help.append("#");
            }else{
                for(String s : c.getRequiredArgs()){
                    help.append("<").append(s).append(">").append(" ");
                }
            }
            help.append("-").append(" ").append(c.getDescription());
            help.append("\n");
        }
        embedBuilder.addField("Commands", help.toString(), false);
        // send result
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
