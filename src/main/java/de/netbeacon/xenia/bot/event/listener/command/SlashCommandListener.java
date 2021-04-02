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

package de.netbeacon.xenia.bot.event.listener.command;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.event.handler.SlashCommandHandler;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

public class SlashCommandListener extends ListenerAdapter {

    private final EventWaiter eventWaiter;
    private final SlashCommandHandler slashCommandHandler;

    public SlashCommandListener(XeniaBackendClient backendClient, EventWaiter eventWaiter, PaginatorManager paginatorManager, D43Z1ContextPoolManager contextPoolManager){
        this.eventWaiter = eventWaiter;

        HashMap<String, Command> commandMap = new HashMap<>();
        Consumer<Command> register = command -> commandMap.put(command.getAlias(), command);

        // register commands here

        this.slashCommandHandler = new SlashCommandHandler(commandMap, eventWaiter, paginatorManager, backendClient, contextPoolManager);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        slashCommandHandler.updateCommands(event.getJDA());
    }


    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if(eventWaiter.waitingOnThis(event)){
            return;
        }
        if(event.getGuild() == null){ // listen for events from guilds only
            return;
        }
        slashCommandHandler.handle(event);
    }

}
