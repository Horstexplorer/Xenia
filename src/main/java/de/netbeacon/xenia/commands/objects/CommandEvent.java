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

package de.netbeacon.xenia.commands.objects;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Wraps GuildMessageReceivedEvents with aditional data
 */
public class CommandEvent {

    private final GuildMessageReceivedEvent event;

    /**
     * Creates a new instance of this class
     *
     * @param event GuildMessageReceivedEvent
     */
    public CommandEvent(GuildMessageReceivedEvent event){
        this.event = event;
    }

    /**
     * Used to get the GuildMessageReceivedEvent
     *
     * @return GuildMessageReceivedEvent
     */
    public GuildMessageReceivedEvent getEvent() {
        return event;
    }
}
