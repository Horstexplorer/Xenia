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
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
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
        try{
            if(backendClient.getGuildCache().contains(event.getGuild().getIdLong())){
                logger.info("Reloading Guild Async "+event.getGuild().getId());
                backendClient.getGuildCache().get(event.getGuild().getIdLong()).clear(false);
                backendClient.getGuildCache().remove(event.getGuild().getIdLong());
            }else{
                logger.info("Loading Guild Async "+event.getGuild().getId());
            }
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            g.setMetaData(event.getGuild().getName(), event.getGuild().getIconUrl());
            g.updateAsync();
            Consumer<Guild> updateChannelInfo = guild -> {
                // update all channels
                ChannelCache channelCache = guild.getChannelCache();
                for(Channel channel : channelCache.getAllAsList()){
                   try{
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
                   }catch (CacheException e){
                       logger.error("A CacheException occurred during updating the channel data at the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
                   }catch (DataException e){
                       logger.error("A DataException occurred during updating the channel data at the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
                   }catch (Exception e){
                       logger.error("An unknown error occurred during updating the channel data at the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
                   }
                }
            };
            g.initAsync(event.getGuild().getMemberCount() <= MEMBER_PRELOAD_COUNT_THRESHOLD, updateChannelInfo);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildReadyEvent of guild "+event.getGuild().getIdLong(), e);
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        try{
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
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildJoinEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildJoinEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildJoinEvent of guild "+event.getGuild().getIdLong(), e);
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        try{
            logger.info("Guild Has Been Left: "+event.getGuild().getName()+"("+event.getGuild().getId()+")");
            backendClient.getGuildCache().delete(event.getGuild().getIdLong(), true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildLeaveEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildLeaveEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildLeaveEvent of guild "+event.getGuild().getIdLong(), e);
        }
    }

    @Override
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {
        try{
            logger.info("Joined A New Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
            backendClient.getGuildCache().get(event.getGuildIdLong());
        }catch (CacheException e){
            logger.error("A CacheException occurred during the UnavailableGuildJoinedEvent of guild "+event.getGuildIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the UnavailableGuildJoinedEvent of guild "+event.getGuildIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the UnavailableGuildJoinedEvent of guild "+event.getGuildIdLong(), e);
        }
    }

    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        try{
            logger.info("Left A Guild Which Is Unavailable ATM: Unknown_Name ("+event.getGuildId()+")");
            backendClient.getGuildCache().delete(event.getGuildIdLong(), true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the UnavailableGuildLeaveEvent of guild "+event.getGuildIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the UnavailableGuildLeaveEvent of guild "+event.getGuildIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the UnavailableGuildLeaveEvent of guild "+event.getGuildIdLong(), e);
        }
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
        try{
            User u = backendClient.getUserCache().get(event.getUser().getIdLong());
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Member m = g.getMemberCache().get(event.getMember().getIdLong());
            u.lSetMetaData(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl());
            m.lSetMetaData(event.getMember().getEffectiveName(), event.getMember().hasPermission(Permission.ADMINISTRATOR), event.getMember().isOwner());
            u.updateAsync(true);
            m.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildMemberJoinEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildMemberJoinEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildMemberJoinEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            if(event.getMember() != null){
                g.getMemberCache().delete(event.getMember().getIdLong(), true);
            }
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildMemberRemoveEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildMemberRemoveEvent of guild "+event.getGuild().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildMemberRemoveEvent of guild "+event.getGuild().getIdLong(), e);
        }
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        try{
            User u = backendClient.getUserCache().get(event.getUser().getIdLong());
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Member m = g.getMemberCache().get(event.getUser().getIdLong());
            u.lSetMetaData(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl());
            m.lSetMetaData(event.getMember().getEffectiveName(), event.getMember().hasPermission(Permission.ADMINISTRATOR), event.getMember().isOwner());
            u.updateAsync(true);
            m.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the GuildMemberUpdateEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the GuildMemberUpdateEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the GuildMemberUpdateEvent of guild "+event.getGuild().getIdLong()+", member "+event.getMember().getIdLong(), e);
        }
    }

    // CHANNEL

    @Override
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
            c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
            if(event.getGuild().getMemberCount() >= MEMBER_LOGGING_COUNT_THRESHOLD){ c.lSetTmpLoggingActive(false); }
            c.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelCreateEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelCreateEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelCreateEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
    }

    @Override
    public void onTextChannelUpdateName(@NotNull TextChannelUpdateNameEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
            c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
            Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
            if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
            if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
            c.lSetChannelFlags(channelFlags);
            c.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelUpdateNameEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelUpdateNameEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelUpdateNameEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
    }

    @Override
    public void onTextChannelUpdateTopic(@NotNull TextChannelUpdateTopicEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
            c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
            Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
            if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
            if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
            c.lSetChannelFlags(channelFlags);
            c.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelUpdateTopicEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelUpdateTopicEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelUpdateTopicEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
    }

    @Override
    public void onTextChannelUpdateNSFW(@NotNull TextChannelUpdateNSFWEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
            c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
            Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
            if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
            if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
            c.lSetChannelFlags(channelFlags);
            c.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelUpdateNSFWEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelUpdateNSFWEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelUpdateNSFWEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
    }

    @Override
    public void onTextChannelUpdateNews(@NotNull TextChannelUpdateNewsEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
            c.lSetMetaData(event.getChannel().getName(), event.getChannel().getTopic());
            Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(c.getChannelFlags().getValue());
            if (event.getChannel().isNews()) { channelFlags.set(Channel.ChannelFlags.Flags.NEWS); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NEWS); }
            if (event.getChannel().isNSFW()) { channelFlags.set(Channel.ChannelFlags.Flags.NSFW); } else { channelFlags.unset(Channel.ChannelFlags.Flags.NSFW); }
            c.lSetChannelFlags(channelFlags);
            c.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelUpdateNewsEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelUpdateNewsEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelUpdateNewsEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        try{
            Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
            g.getChannelCache().delete(event.getChannel().getIdLong(), true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the TextChannelDeleteEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the TextChannelDeleteEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the TextChannelDeleteEvent of guild "+event.getGuild().getIdLong()+", channel "+event.getChannel().getIdLong(), e);
        }
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
        try{
            Guild g = backendClient.getGuildCache().get(guild.getIdLong());
            g.lSetMetaData(guild.getName(), guild.getIconUrl());
            g.updateAsync(true);
        }catch (CacheException e){
            logger.error("A CacheException occurred during the _meta_GuildUpdateEvent of guild "+guild.getIdLong(), e);
        }catch (DataException e){
            logger.error("A DataException occurred during the _meta_GuildUpdateEvent of guild "+guild.getIdLong(), e);
        }catch (Exception e){
            logger.error("An unknown error occurred during the _meta_GuildUpdateEvent of guild "+guild.getIdLong(), e);
        }
    }
}
