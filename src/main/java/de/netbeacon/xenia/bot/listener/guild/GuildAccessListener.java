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

package de.netbeacon.xenia.bot.listener.guild;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.cache.GuildCache;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildAccessListener extends ListenerAdapter {

    private final XeniaBackendClient backendClient;

    public GuildAccessListener(XeniaBackendClient backendClient){
        this.backendClient = backendClient;
    }

    /**
     * Add a guild to the cache & backend
     * @param event GuildReadyEvent
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        backendClient.getGuildCache().get(event.getGuild().getIdLong());
        backendClient.getLicenseCache().get(event.getGuild().getIdLong());
    }

    /**
     * Add a guild to the cache & backend
     * @param event GuildJoinEvent
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        backendClient.getGuildCache().get(event.getGuild().getIdLong());
        backendClient.getLicenseCache().get(event.getGuild().getIdLong());
    }

    /**
     * Remove a guild from the cache & backend
     * @param event GuildLeaveEvent
     */
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        backendClient.getGuildCache().delete(event.getGuild().getIdLong());
        backendClient.getLicenseCache().remove(event.getGuild().getIdLong());
    }
}
