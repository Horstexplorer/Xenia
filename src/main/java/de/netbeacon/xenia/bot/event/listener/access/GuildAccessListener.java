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

package de.netbeacon.xenia.bot.event.listener.access;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.backend.client.objects.external.User;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
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

    // GUILD

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        logger.info("Loading Guild Async "+event.getGuild().getId());
        if(backendClient.getGuildCache().contains(event.getGuild().getIdLong())){
            backendClient.getGuildCache().remove(event.getGuild().getIdLong());
        }
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        g.initAsync();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        logger.info("Joined A New Guild: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        g.lSetMetaData(event.getGuild().getName(), event.getGuild().getIconUrl());
        g.updateAsync();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        logger.info("Guild Has Been Left: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
        backendClient.getGuildCache().delete(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {
        logger.info("Joined A New Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
        backendClient.getGuildCache().get(event.getGuildIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        logger.info("Left A Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
        backendClient.getGuildCache().delete(event.getGuildIdLong());
    }

    @Override
    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
        logger.info("Guild "+event.getGuild().getName()+"("+event.getGuild().getId()+") Is Available");
    }

    @Override
    public void onGuildUnavailable(@NotNull GuildUnavailableEvent event) {
        logger.info("Guild "+event.getGuild().getName()+"("+event.getGuild().getId()+") Is Unavailable");
    }

    // will only work if the guild_member intent is set

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        User u = backendClient.getUserCache().get(event.getUser().getIdLong());
        u.lSetMetaData(event.getUser().getAsTag());
        u.updateAsync();
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Member m = g.getMemberCache().get(event.getMember().getIdLong());
        m.lSetMetaData(event.getMember().getEffectiveName(), event.getMember().hasPermission(Permission.ADMINISTRATOR), event.getMember().isOwner());
        m.updateAsync();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        if(event.getMember() != null){
            g.getMemberCache().delete(event.getMember().getIdLong());
        }
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        User u = backendClient.getUserCache().get(event.getUser().getIdLong());
        Member m = g.getMemberCache().get(event.getUser().getIdLong());
        u.lSetMetaData(event.getUser().getAsTag());
        u.updateAsync();
        m.lSetMetaData(event.getMember().getEffectiveName(), event.getMember().hasPermission(Permission.ADMINISTRATOR), event.getMember().isOwner());
        m.updateAsync();
    }

    // CHANNEL

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

    // GUILD

    @Override
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event) {
        guildMetaUpdate(event.getGuild());
    }

    @Override
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {
        guildMetaUpdate(event.getGuild());
    }

    public void guildMetaUpdate(net.dv8tion.jda.api.entities.Guild guild){
        Guild g = backendClient.getGuildCache().get(guild.getIdLong());
        g.lSetMetaData(guild.getName(), guild.getIconUrl());
        g.updateAsync();
    }
}
