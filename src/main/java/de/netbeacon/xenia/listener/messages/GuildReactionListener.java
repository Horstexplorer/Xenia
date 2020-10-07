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

import de.netbeacon.xenia.core.XeniaCore;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildReactionListener extends ListenerAdapter {

    /**
     * Used for the event waiter
     *
     * @param event GuildMessageReactionAddEvent
     */
    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        XeniaCore.getInstance().getEventWaiter().waitingOnThis(event);
    }

    /**
     * Used for the event waiter
     *
     * @param event GuildMessageReactionRemoveEvent
     */
    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        XeniaCore.getInstance().getEventWaiter().waitingOnThis(event);
    }

    /**
     * Used for the event waiter
     *
     * @param event GuildMessageReactionRemoveAllEvent
     */
    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        XeniaCore.getInstance().getEventWaiter().waitingOnThis(event);
    }

    /**
     * Used for the event waiter
     *
     * @param event GuildMessageReactionRemoveEmoteEvent
     */
    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        XeniaCore.getInstance().getEventWaiter().waitingOnThis(event);
    }
}
