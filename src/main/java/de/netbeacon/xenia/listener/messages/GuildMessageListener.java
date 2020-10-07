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

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for general messages from guilds
 */
public class GuildMessageListener extends ListenerAdapter {

    /**
     * Currently unused
     *
     * @param event GuildMessageReceivedEvent
     */
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

    }

    /**
     * Currently unused
     *
     * @param event GuildMessageUpdateEvent
     */
    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {

    }

    /**
     * Currently unused
     *
     * @param event GuildMessageDeleteEvent
     */
    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {

    }

    /**
     * Currently unused
     *
     * @param event GuildMessageEmbedEvent
     */
    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {

    }
}
