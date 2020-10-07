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

package de.netbeacon.xenia.bot.handler.command;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.tools.pattern.StaticPattern.ArgPattern;

/**
 * Acts as the first layer of command handling
 */
public class CommandHandler{

    private final String prefix;
    private final HashMap<String, Command> commandMap;
    private final CommandCooldown commandCooldown = new CommandCooldown(CommandCooldown.Type.User, 1000);

    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

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
    public void process(GuildMessageReceivedEvent event, XeniaBackendClient backendClient){
        // get the message
        String msg = event.getMessage().getContentRaw();
        if(!msg.startsWith(prefix)){
            return;
        }
        // cd
        if(!commandCooldown.allow(event.getGuild().getIdLong(), event.getAuthor().getIdLong())){
            return;
        }
        commandCooldown.deny(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
        // split to list
        List<String> args = new ArrayList<>();
        Matcher matcher = ArgPattern.matcher(msg.substring(prefix.length()));
        while(matcher.find()){
            args.add((matcher.group(2) != null)?matcher.group(2):matcher.group());
        }
        if(args.size() <= 0){
            return;
        }
        // get the command
        Command command = commandMap.get(args.get(0));
        if(command != null){
            args.remove(0);
            // get data from backend
            long a = System.currentTimeMillis();
            Guild bGuild = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            long b = System.currentTimeMillis();
            License bLicense = backendClient.getLicenseCache().get(event.getGuild().getIdLong());
            long c = System.currentTimeMillis();
            User bUser = backendClient.getUserCache().get(event.getAuthor().getIdLong());
            long d = System.currentTimeMillis();
            Member bMember = bGuild.getMemberCache().get(event.getAuthor().getIdLong());
            long e = System.currentTimeMillis();
            Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
            long f = System.currentTimeMillis();
            logger.debug("Retrieved Data (GLUMC) in "+(b-a)+" "+(c-b)+" "+(d-c)+" "+(e-d)+" "+(f-d)+" :"+(f-a)+"ms");
            // start the madness
            command.execute(args, new CommandEvent(event, bGuild, bLicense, bUser, bMember, bChannel));
        }
    }

}
