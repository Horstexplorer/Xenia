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

package de.netbeacon.xenia.bot.commands.objects;

import de.netbeacon.xenia.backend.client.objects.external.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Wraps GuildMessageReceivedEvents with aditional data
 */
public class CommandEvent {

    private final GuildMessageReceivedEvent event;

    private final Guild bGuild;
    private final License bLicense;
    private final User bUser;
    private final Member bMember;
    private final Channel bChannel;

    /**
     * Creates a new instance of this class
     *
     * @param event GuildMessageReceivedEvent
     */
    public CommandEvent(GuildMessageReceivedEvent event, Guild bGuild, License bLicense, User bUser,  Member bMember, Channel bChannel){
        this.event = event;
        this.bGuild = bGuild;
        this.bLicense = bLicense;
        this.bUser = bUser;
        this.bMember = bMember;
        this.bChannel = bChannel;
    }

    /**
     * Used to get the GuildMessageReceivedEvent
     *
     * @return GuildMessageReceivedEvent
     */
    public GuildMessageReceivedEvent getEvent() {
        return event;
    }

    public Guild getbGuild() {
        return bGuild;
    }

    public License getbLicense() {
        return bLicense;
    }

    public User getbUser() {
        return bUser;
    }

    public Member getbMember() {
        return bMember;
    }

    public Channel getbChannel() {
        return bChannel;
    }
}
