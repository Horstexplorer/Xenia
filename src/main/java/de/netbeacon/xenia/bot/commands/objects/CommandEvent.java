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
    private final BackendDataPack backendDataPack;

    /**
     * Creates a new instance of this class
     *
     * @param event GuildMessageReceivedEvent
     */
    public CommandEvent(GuildMessageReceivedEvent event, BackendDataPack backendDataPack){
        this.event = event;
        this.backendDataPack = backendDataPack;
    }

    /**
     * Used to get the GuildMessageReceivedEvent
     *
     * @return GuildMessageReceivedEvent
     */
    public GuildMessageReceivedEvent getEvent() {
        return event;
    }

    public BackendDataPack backendDataPack(){
        return backendDataPack;
    }


    public static class BackendDataPack{
        private final Guild bGuild;
        private final User bUser;
        private final Member bMember;
        private final Channel bChannel;
        private final License bLicense;

        public BackendDataPack(Guild bGuild, User bUser, Member bMember, Channel bChannel, License bLicense){
            this.bGuild = bGuild;
            this.bUser = bUser;
            this.bMember = bMember;
            this.bChannel = bChannel;
            this.bLicense = bLicense;
        }

        public Guild getbGuild() {
            return bGuild;
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

        public License getbLicense() {
            return bLicense;
        }
    }
}
