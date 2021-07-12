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
import de.netbeacon.xenia.bot.utils.backend.BackendQuickAction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
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

public class GuildAccessListener extends ListenerAdapter{

	private final XeniaBackendClient backendClient;
	private final Logger logger = LoggerFactory.getLogger(GuildAccessListener.class);

	public GuildAccessListener(XeniaBackendClient backendClient){
		this.backendClient = backendClient;
	}

	// SHARD

	@Override
	public void onReady(@NotNull ReadyEvent event){
		logger.info("Finished loading shard " + event.getJDA().getShardInfo().getShardString() + " with " + event.getJDA().getGuildCache().size() + " guilds.");
	}


	// GUILD

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event){
		try{
			if(backendClient.getGuildCache().contains(event.getGuild().getIdLong())){
				logger.debug("Reloading Guild Async " + event.getGuild().getId());
				backendClient.getGuildCache().get(event.getGuild().getIdLong()).clear(false);
				backendClient.getGuildCache().remove(event.getGuild().getIdLong());
			}
			else{
				logger.debug("Loading Guild Async " + event.getGuild().getId());
			}
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			BackendQuickAction.Update.execute(g, event.getGuild(), true, false);
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
						BackendQuickAction.Update.execute(channel, textChannel, true, false);
					}
					catch(CacheException e){
						logger.error("A CacheException occurred during updating the channel data at the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
					}
					catch(DataException e){
						logger.error("A DataException occurred during updating the channel data at the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
					}
					catch(Exception e){
						logger.error("An unknown error occurred during updating the channel data at the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
					}
				}
			};
			g.initAsync(updateChannelInfo);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildReadyEvent of guild " + event.getGuild().getIdLong(), e);
		}
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event){
		try{
			logger.info("Joined A New Guild: " + event.getGuild().getName() + "(" + event.getGuild().getId() + ")");
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			BackendQuickAction.Update.execute(g, event.getGuild(), true, false);
			event.getGuild().getTextChannels().forEach(textChannel -> {
				backendClient.getBackendProcessor().getScalingExecutor().execute(() -> {
					Channel channel = g.getChannelCache().get(textChannel.getIdLong());
					BackendQuickAction.Update.execute(channel, textChannel, true, false);
				});
			});
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildJoinEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildJoinEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildJoinEvent of guild " + event.getGuild().getIdLong(), e);
		}
	}

	@Override
	public void onGuildLeave(@NotNull GuildLeaveEvent event){
		try{
			logger.info("Guild Has Been Left: " + event.getGuild().getName() + "(" + event.getGuild().getId() + ")");
			backendClient.getGuildCache().delete(event.getGuild().getIdLong(), true);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildLeaveEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildLeaveEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildLeaveEvent of guild " + event.getGuild().getIdLong(), e);
		}
	}

	@Override
	public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event){
		try{
			logger.debug("Joined A New Guild Which Is Unavailable ATM: Unknown_Name (" + event.getGuildId() + ")");
			backendClient.getGuildCache().get(event.getGuildIdLong());
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the UnavailableGuildJoinedEvent of guild " + event.getGuildIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the UnavailableGuildJoinedEvent of guild " + event.getGuildIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the UnavailableGuildJoinedEvent of guild " + event.getGuildIdLong(), e);
		}
	}

	@Override
	public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event){
		try{
			logger.debug("Left A Guild Which Is Unavailable ATM: Unknown_Name (" + event.getGuildId() + ")");
			backendClient.getGuildCache().delete(event.getGuildIdLong(), true);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the UnavailableGuildLeaveEvent of guild " + event.getGuildIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the UnavailableGuildLeaveEvent of guild " + event.getGuildIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the UnavailableGuildLeaveEvent of guild " + event.getGuildIdLong(), e);
		}
	}

	@Override
	public void onGuildAvailable(@NotNull GuildAvailableEvent event){
		logger.debug("Guild " + event.getGuild().getName() + "(" + event.getGuild().getId() + ") Is Available");
	}

	@Override
	public void onGuildUnavailable(@NotNull GuildUnavailableEvent event){
		logger.debug("Guild " + event.getGuild().getName() + "(" + event.getGuild().getId() + ") Is Unavailable");
	}

	// MEMBER
	// will only work if the guild_member intent is set

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event){
		try{
			User u = backendClient.getUserCache().get(event.getUser().getIdLong());
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			Member m = g.getMemberCache().get(event.getMember().getIdLong());

			BackendQuickAction.Update.execute(u, event.getUser(), true, true);
			BackendQuickAction.Update.execute(m, event.getMember(), true, true);

		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildMemberJoinEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildMemberJoinEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildMemberJoinEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event){
		try{
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			if(event.getMember() != null){
				g.getMemberCache().delete(event.getMember().getIdLong(), true);
			}
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildMemberRemoveEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildMemberRemoveEvent of guild " + event.getGuild().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildMemberRemoveEvent of guild " + event.getGuild().getIdLong(), e);
		}
	}

	@Override
	public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event){
		try{
			User u = backendClient.getUserCache().get(event.getUser().getIdLong());
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			Member m = g.getMemberCache().get(event.getUser().getIdLong());

			BackendQuickAction.Update.execute(u, event.getUser(), true, true);
			BackendQuickAction.Update.execute(m, event.getMember(), true, true);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the GuildMemberUpdateEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the GuildMemberUpdateEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the GuildMemberUpdateEvent of guild " + event.getGuild().getIdLong() + ", member " + event.getMember().getIdLong(), e);
		}
	}

	// CHANNEL

	@Override
	public void onTextChannelCreate(@NotNull TextChannelCreateEvent event){
		try{
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			Channel c = g.getChannelCache().get(event.getChannel().getIdLong());
			BackendQuickAction.Update.execute(c, event.getChannel(), true, false);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the TextChannelCreateEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the TextChannelCreateEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the TextChannelCreateEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
	}

	@Override
	public void onTextChannelUpdateName(@NotNull TextChannelUpdateNameEvent event){
		channelUpdate(event.getChannel());
	}

	@Override
	public void onTextChannelUpdateTopic(@NotNull TextChannelUpdateTopicEvent event){
		channelUpdate(event.getChannel());
	}

	@Override
	public void onTextChannelUpdateNSFW(@NotNull TextChannelUpdateNSFWEvent event){
		channelUpdate(event.getChannel());
	}

	@Override
	public void onTextChannelUpdateNews(@NotNull TextChannelUpdateNewsEvent event){
		channelUpdate(event.getChannel());
	}

	@Override
	public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event){
		try{
			Guild g = backendClient.getGuildCache().get(event.getGuild().getIdLong());
			g.getChannelCache().delete(event.getChannel().getIdLong(), true);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the TextChannelDeleteEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the TextChannelDeleteEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the TextChannelDeleteEvent of guild " + event.getGuild().getIdLong() + ", channel " + event.getChannel().getIdLong(), e);
		}
	}

	private void channelUpdate(TextChannel textChannel){
		try{
			Guild g = backendClient.getGuildCache().get(textChannel.getGuild().getIdLong());
			Channel c = g.getChannelCache().get(textChannel.getIdLong());

			BackendQuickAction.Update.execute(c, textChannel, true, false);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the TextChannelUpdateEvent of guild " + textChannel.getGuild().getIdLong() + ", channel " + textChannel.getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the TextChannelUpdateEvent of guild " + textChannel.getGuild().getIdLong() + ", channel " + textChannel.getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the TextChannelUpdateEvent of guild " + textChannel.getGuild().getIdLong() + ", channel " + textChannel.getIdLong(), e);
		}
	}

	// GUILD

	@Override
	public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event){
		guildMetaUpdate(event.getGuild());
	}

	@Override
	public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event){
		guildMetaUpdate(event.getGuild());
	}

	private void guildMetaUpdate(net.dv8tion.jda.api.entities.Guild guild){
		try{
			Guild g = backendClient.getGuildCache().get(guild.getIdLong());

			BackendQuickAction.Update.execute(g, guild, true, false);
		}
		catch(CacheException e){
			logger.error("A CacheException occurred during the _meta_GuildUpdateEvent of guild " + guild.getIdLong(), e);
		}
		catch(DataException e){
			logger.error("A DataException occurred during the _meta_GuildUpdateEvent of guild " + guild.getIdLong(), e);
		}
		catch(Exception e){
			logger.error("An unknown error occurred during the _meta_GuildUpdateEvent of guild " + guild.getIdLong(), e);
		}
	}

}
