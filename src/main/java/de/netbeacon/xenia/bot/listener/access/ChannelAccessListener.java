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
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ChannelAccessListener extends ListenerAdapter {

    private final XeniaBackendClient backendClient;

    public ChannelAccessListener(XeniaBackendClient backendClient){
        this.backendClient = backendClient;
    }

    @Override
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        g.getChannelCache().get(event.getChannel().getIdLong());
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        g.getChannelCache().delete(event.getChannel().getIdLong());
    }
}
