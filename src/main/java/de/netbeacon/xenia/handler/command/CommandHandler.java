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

package de.netbeacon.xenia.handler.command;

import de.netbeacon.xenia.commands.objects.Command;
import de.netbeacon.xenia.commands.objects.CommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.List;

import static de.netbeacon.xenia.tools.pattern.StaticPattern.MultiWhiteSpacePattern;

/**
 * Acts as the first layer of command handling
 */
public class CommandHandler{

    private final String prefix;
    private final HashMap<String, Command> commandMap;

    /**
     * Creates a new instance of this class
     * @param prefix of the command
     * @param commandMap containing all commands
     */
    public CommandHandler(String prefix, HashMap<String, Command> commandMap){
        this.prefix = prefix;
        this.commandMap = commandMap;
    }

    /**
     * Processes the given event
     * @param event GuildMessageReceivedEvent
     */
    public void process(GuildMessageReceivedEvent event){
        // get the message
        String msg = event.getMessage().getContentRaw();
        if(!msg.startsWith(prefix)){
            return;
        }
        // split to list
        List<String> args = new ArrayList<>(Arrays.asList(MultiWhiteSpacePattern.split(msg.replace(prefix, ""))));
        if(args.size() <= 0){
            return;
        }
        // get the command
        Command command = commandMap.get(args.get(0));
        if(command != null){
            args.remove(0);
            // start the madness
            command.execute(args, new CommandEvent(event));
        }
    }

}
