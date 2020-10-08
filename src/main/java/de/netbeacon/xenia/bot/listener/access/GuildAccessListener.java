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

package de.netbeacon.xenia.bot.listener.access;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildAccessListener extends ListenerAdapter {

    private final XeniaBackendClient backendClient;
    private final Logger logger = LoggerFactory.getLogger(GuildAccessListener.class);

    public GuildAccessListener(XeniaBackendClient backendClient){
        this.backendClient = backendClient;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        logger.warn("Joined A New Guild: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
        backendClient.getGuildCache().get(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        logger.warn("Guild Has Been Left: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
        backendClient.getGuildCache().delete(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {
        logger.warn("Joined A New Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
        backendClient.getGuildCache().get(event.getGuildIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        logger.warn("Left A Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
        backendClient.getGuildCache().delete(event.getGuildIdLong());
    }

    @Override
    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
        logger.warn("Guild "+event.getGuild().getName()+"("+event.getGuild().getId()+") Is Available");
    }

    @Override
    public void onGuildUnavailable(@NotNull GuildUnavailableEvent event) {
        logger.warn("Guild "+event.getGuild().getName()+"("+event.getGuild().getId()+") Is Unavailable");
    }
}
