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
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SlashCommandHandler {

    private final HashMap<String, Command> commandMap;
    private final EventWaiter eventWaiter;
    private final XeniaBackendClient backendClient;
    private final PaginatorManager paginatorManager;
    private final D43Z1ContextPoolManager contextPoolManager;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SlashCommandHandler(HashMap<String, Command> commandMap, EventWaiter eventWaiter, PaginatorManager paginatorManager, XeniaBackendClient backendClient, D43Z1ContextPoolManager contextPoolManager){
        this.commandMap = commandMap;
        this.eventWaiter = eventWaiter;
        this.paginatorManager = paginatorManager;
        this.backendClient = backendClient;
        this.contextPoolManager = contextPoolManager;
    }

    public void updateCommands(ShardManager shardManager){
        shardManager.getShards().forEach(this::updateCommands);
    }

    public void updateCommands(JDA jda){
        CommandUpdateAction commandUpdateAction = jda.updateCommands();
        commandMap.forEach((k, v) -> commandUpdateAction.addCommands(getCommandData(v)));
        commandUpdateAction.queue(s -> logger.debug("Updated Commands"), f -> logger.error("Failed To Update Commands", f));
    }

    private static List<CommandUpdateAction.CommandData> getCommandData(Command cmd){
        if(cmd.isCommandGroup()){
            List<CommandUpdateAction.CommandData> commandData = new ArrayList<>();
            cmd.getChildCommands().forEach(cc -> {
                commandData.addAll(getCommandData(cc));
            });
            return commandData;
        }else{
            return Collections.singletonList(cmd.getCommandData());
        }
    }

    public void handle(SlashCommandEvent event){

    }
}
