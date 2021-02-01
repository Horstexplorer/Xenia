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
import de.netbeacon.xenia.backend.client.objects.cache.ChannelCache;
import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.backend.client.objects.external.User;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNewsEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
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

import java.util.function.Consumer;

public class GuildAccessListener extends ListenerAdapter {

    private final XeniaBackendClient backendClient;
    private final Logger logger = LoggerFactory.getLogger(GuildAccessListener.class);

    private static final int MEMBER_PRELOAD_COUNT_THRESHOLD = 10000; // disabled for now
    private static final int MEMBER_LOGGING_COUNT_THRESHOLD = Integer.MAX_VALUE; // disabled for now

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
        g.setMetaData(event.getGuild().getName(), event.getGuild().getIconUrl());
        g.updateAsync();
        Consumer<Guild> updateChannelInfo = guild -> {
            // update all channels
            ChannelCache channelCache = guild.getChannelCache();
            for(Channel channel : channelCache.getAllAsList()){
                TextChannel textChannel = event.getGuild().getTextChannelById(channel.getId());
                if(textChannel == null){ // does no longer exist
                    channelCache.delete(channel.getId());
                    continue;
                }
                // update all names & topics
                channel.lSetMetaData(textChannel.getName(), textChannel.getTopic());
                // update flags
                Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(channel.getChannelFlags().getValue());
                if (textChannel.isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
                if (textChannel.isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
                channel.lSetChannelFlags(channelFlags);
                channel.updateAsync();
            }
        };
        g.initAsync(event.getGuild().getMemberCount() <= MEMBER_PRELOAD_COUNT_THRESHOLD, updateChannelInfo);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        logger.info("Joined A New Guild: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        g.lSetMetaData(event.getGuild().getName(), event.getGuild().getIconUrl());
        g.updateAsync();
        event.getGuild().getTextChannels().forEach(textChannel -> {
            backendClient.getBackendProcessor().getScalingExecutor().execute(()->{
                Channel channel = g.getChannelCache().get(textChannel.getIdLong());
                if(event.getGuild().getMemberCount() >= MEMBER_LOGGING_COUNT_THRESHOLD){ channel.lSetTmpLoggingActive(false); }
                Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(channel.getChannelFlags().getValue());
                if (textChannel.isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
                if (textChannel.isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
                channel.lSetChannelFlags(channelFlags);
                channel.updateAsync();
            });
        });
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
        u.lSetMetaData(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl());
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
        u.lSetMetaData(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl());
        u.updateAsync();
        m.lSetMetaData(event.getMember().getEffectiveName(), event.getMember().hasPermission(Permission.ADMINISTRATOR), event.getMember().isOwner());
        m.updateAsync();
    }

    // CHANNEL

    @Override
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
        c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
        if(event.getGuild().getMemberCount() >= MEMBER_LOGGING_COUNT_THRESHOLD){ c.lSetTmpLoggingActive(false); }
        c.updateAsync();
    }

    @Override
    public void onTextChannelUpdateName(@NotNull TextChannelUpdateNameEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
        c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
        Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
        if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
        if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
        c.lSetChannelFlags(channelFlags);
        c.updateAsync();
    }

    @Override
    public void onTextChannelUpdateTopic(@NotNull TextChannelUpdateTopicEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
        c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
        Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
        if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
        if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
        c.lSetChannelFlags(channelFlags);
        c.updateAsync();
    }

    @Override
    public void onTextChannelUpdateNSFW(@NotNull TextChannelUpdateNSFWEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
        c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
        Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
        if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
        if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
        c.lSetChannelFlags(channelFlags);
        c.updateAsync();
    }

    @Override
    public void onTextChannelUpdateNews(@NotNull TextChannelUpdateNewsEvent event) {
        Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
        Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
        c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
        Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
        if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
        if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
        c.lSetChannelFlags(channelFlags);
        c.updateAsync();
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
