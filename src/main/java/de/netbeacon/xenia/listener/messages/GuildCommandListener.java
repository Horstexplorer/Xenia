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

package de.netbeacon.xenia.listener.messages;

import de.netbeacon.xenia.commands.objects.Command;
import de.netbeacon.xenia.commands.global.help.CMDHelp;
import de.netbeacon.xenia.commands.structure.CMDInfo;
import de.netbeacon.xenia.commands.structure.admin.GROUPAdmin;
import de.netbeacon.xenia.commands.structure.settings.GROUPSettings;
import de.netbeacon.xenia.commands.structure.setup.GROUPSetup;
import de.netbeacon.xenia.core.XeniaCore;
import de.netbeacon.xenia.handler.command.CommandHandler;
import de.netbeacon.xenia.tools.executor.ScalingExecutor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Listens for messages from guilds to check if they match a known command
 */
public class GuildCommandListener extends ListenerAdapter {

    private final CommandHandler commandHandler;
    private final ScalingExecutor scalingExecutor;

    /**
     * Creates a new instance of this class
     */
    public GuildCommandListener(){
        HashMap<String, Command> commandMap = new HashMap<>();
        Consumer<Command> register = command -> commandMap.put(command.getAlias(), command);

        register.accept(new CMDHelp(commandMap));
        register.accept(new GROUPAdmin(null));
        register.accept(new GROUPSetup(null));
        register.accept(new GROUPSettings(null));
        register.accept(new CMDInfo());

        commandHandler = new CommandHandler("~", commandMap);
        scalingExecutor = new ScalingExecutor(16, 128, 1024*1024, 10, TimeUnit.SECONDS);
    }

    /**
     * Starts processing the incomming events
     *
     * @param event GuildMessageReceivedEvent
     */
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(!event.getAuthor().isBot() && !event.getMessage().isWebhookMessage() && !XeniaCore.getInstance().getEventWaiter().waitingOnThis(event)){
            scalingExecutor.execute(()->commandHandler.process(event));
        }
    }
}
