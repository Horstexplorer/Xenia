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

package de.netbeacon.xenia.bot.commands.objects.misc.event;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Wraps GuildMessageReceivedEvents with aditional data
 */
public class CommandEvent {

    private final GuildMessageReceivedEvent event;
    private final BackendDataPack backendDataPack;
    private final XeniaBackendClient backendClient;
    private final EventWaiter eventWaiter;

    /**
     * Creates a new instance of this class
     *
     * @param event GuildMessageReceivedEvent
     */
    public CommandEvent(GuildMessageReceivedEvent event, BackendDataPack backendDataPack, XeniaBackendClient backendClient, EventWaiter eventWaiter){
        this.event = event;
        this.backendDataPack = backendDataPack;
        this.backendClient = backendClient;
        this.eventWaiter = eventWaiter;
    }

    /**
     * Used to get the GuildMessageReceivedEvent
     *
     * @return GuildMessageReceivedEvent
     */
    public GuildMessageReceivedEvent getEvent() {
        return event;
    }

    /**
     * Returns the bundled data relating to the backend
     *
     * @return backend data
     */
    public BackendDataPack getBackendDataPack(){
        return backendDataPack;
    }

    /**
     * Returns the backend client
     *
     * @return Backend client
     */
    public XeniaBackendClient getBackendClient() {
        return backendClient;
    }

    /**
     * Returns the event waiter
     *
     * @return event waiter
     */
    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public static class BackendDataPack{
        private final Guild bGuild;
        private final User bUser;
        private final Member bMember;
        private final Channel bChannel;
        private final License bLicense;

        /**
         * Creates a new instance of this class
         *
         * @param bGuild
         * @param bUser
         * @param bMember
         * @param bChannel
         * @param bLicense
         */
        public BackendDataPack(Guild bGuild, User bUser, Member bMember, Channel bChannel, License bLicense){
            this.bGuild = bGuild;
            this.bUser = bUser;
            this.bMember = bMember;
            this.bChannel = bChannel;
            this.bLicense = bLicense;
        }

        /**
         * Returns the guild
         *
         * @return guild
         */
        public Guild getbGuild() {
            return bGuild;
        }

        /**
         * Returns the user
         *
         * @return user
         */
        public User getbUser() {
            return bUser;
        }

        /**
         * Returns the member
         *
         * @return member
         */
        public Member getbMember() {
            return bMember;
        }

        /**
         * Returns the channel
         * @return channel
         */
        public Channel getbChannel() {
            return bChannel;
        }

        /**
         * Returns the license
         * @return license
         */
        public License getbLicense() {
            return bLicense;
        }
    }
}
