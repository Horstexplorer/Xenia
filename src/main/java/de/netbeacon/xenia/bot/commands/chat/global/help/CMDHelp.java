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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Global help command
 */
public class CMDHelp extends Command {

    private CommandGroup parent;
    private HashMap<String, Command> commandMap;

    public CMDHelp(CommandGroup parent){
        super("help", null,null, null, null, null);
        this.parent = parent;
    }

    /**
     * Creates a new instance of this class
     *
     * @param commandMap containing all commands
     */
    public CMDHelp(HashMap<String, Command> commandMap){
        super("help", null, null, null,null, null);
        this.commandMap = commandMap;
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) {
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
        var cmdPerPage = 5;
        var subLists = ListUtils.partition(new ArrayList<>(commandEntries), cmdPerPage);
        ArrayList<Page> pages = new ArrayList<>();
        for(var subList : subLists){
            EmbedBuilder embedBuilder = EmbedBuilderFactory
                    .getDefaultEmbed("Help"+((parent != null)?(" <"+parent.getAlias()+">"):""), commandEvent.getEvent().getAuthor());
            for(var cmdEntry : subList){
                Command c = cmdEntry.getValue();
                StringBuilder commandCallBuilder = new StringBuilder()
                        .append(commandPath).append(" ")
                        .append(c.getAlias()).append(" ");
                if(c.isCommandHandler()){
                    commandCallBuilder.append("#");
                }else{
                    for(CmdArgDef s : c.getCommandArgs()){
                        commandCallBuilder.append("<").append(s.getName()).append(">").append(" ");
                    }
                }
                embedBuilder.addField(commandCallBuilder.toString(), c.getDescription(translationPackage), false);
            }
            pages.add(new Page(embedBuilder.build()));
        }
        // send result
        commandEvent.getPaginatorManager().createPaginator(event.getChannel(), event.getAuthor(), pages);
    }
}
