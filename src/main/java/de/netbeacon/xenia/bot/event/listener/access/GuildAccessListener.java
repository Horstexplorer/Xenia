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
import de.netbeacon.xenia.backend.client.objects.apidata.Channel;
import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.User;
import de.netbeacon.xenia.backend.client.objects.cache.ChannelCache;
import de.netbeacon.xenia.bot.utils.backend.BackendQuickAction;
import de.netbeacon.xenia.bot.utils.backend.action.BackendActions;
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

import java.util.List;
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
		var guildCache = backendClient.getGuildCache();
		var licenseCache = backendClient.getLicenseCache();
		var guildId = event.getGuild().getIdLong();

		if(guildCache.contains(guildId)){
			logger.debug("Reloading Guild Async " + guildId);
			guildCache.get_(guildId).clear(false);
			guildCache.remove_(guildId);
			licenseCache.remove_(guildId);
		}
		else{
			logger.debug("Loading Guild Async " + guildId);
		}

		BackendActions.allOf(List.of(
			guildCache.retrieveOrCreate(guildId, true),
			licenseCache.retrieve(guildId, true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			BackendQuickAction.Update.execute(bGuild, event.getGuild(), true, true);
			Consumer<Guild> updateChannelInfo = guild -> {
				ChannelCache channelCache = guild.getChannelCache();
				for(Channel channel : channelCache.getAllAsList()){
					TextChannel textChannel = event.getGuild().getTextChannelById(channel.getId());
					if(textChannel == null){ // does no longer exist
						channelCache.delete(channel.getId()).queue();
						continue;
					}
					BackendQuickAction.Update.execute(channel, textChannel, true, false);
				}
			};
			bGuild.initAsync(updateChannelInfo);
		});
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event){
		var guildCache = backendClient.getGuildCache();
		var licenseCache = backendClient.getLicenseCache();
		var guildId = event.getGuild().getIdLong();

		logger.info("Joined A New Guild: " + event.getGuild().getName() + "(" + event.getGuild().getId() + ")");

		BackendActions.allOf(List.of(
			guildCache.retrieveOrCreate(guildId, true),
			licenseCache.retrieve(guildId, true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			BackendQuickAction.Update.execute(bGuild, event.getGuild(), true, true);
			event.getGuild().getTextChannels().forEach(textChannel -> {
				bGuild.getChannelCache().retrieveOrCreate(textChannel.getIdLong(), true)
					.queue(bChannel -> BackendQuickAction.Update.execute(bChannel, textChannel, true, false));
			});
		});
	}

	@Override
	public void onGuildLeave(@NotNull GuildLeaveEvent event){
		logger.info("Guild Has Been Left: " + event.getGuild().getName() + "(" + event.getGuild().getId() + ")");
		backendClient.getGuildCache().delete(event.getGuild().getIdLong()).queue();
	}

	@Override
	public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event){
		logger.debug("Joined A New Guild Which Is Unavailable ATM: Unknown_Name (" + event.getGuildId() + ")");
		backendClient.getGuildCache().retrieveOrCreate(event.getGuildIdLong(), true).queue();
	}

	@Override
	public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event){
		logger.debug("Left A Guild Which Is Unavailable ATM: Unknown_Name (" + event.getGuildId() + ")");
		backendClient.getGuildCache().delete(event.getGuildIdLong()).queue();
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
		var guildCache = backendClient.getGuildCache();
		var licenseCache = backendClient.getLicenseCache();
		var userCache = backendClient.getUserCache();
		var guildId = event.getGuild().getIdLong();
		var userId = event.getUser().getIdLong();

		BackendActions.allOf(List.of(
			guildCache.retrieveOrCreate(guildId, true),
			licenseCache.retrieve(guildId, true),
			userCache.retrieveOrCreate(userId, true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			User bUser = barb.get(User.class);
			BackendQuickAction.Update.execute(bUser, event.getUser(), true, true);
			bGuild.getMemberCache().retrieveOrCreate(userId, true).queue(bMember -> {
				BackendQuickAction.Update.execute(bMember, event.getMember(), true, true);
			});
		});
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event){
		if(event.getMember() == null){
			return;
		}
		backendClient.getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true).queue(bGuild -> {
			bGuild.getMemberCache().delete(event.getMember().getIdLong()).queue();
		});
	}

	@Override
	public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event){
		var guildCache = backendClient.getGuildCache();
		var licenseCache = backendClient.getLicenseCache();
		var userCache = backendClient.getUserCache();
		var guildId = event.getGuild().getIdLong();
		var userId = event.getUser().getIdLong();

		BackendActions.allOf(List.of(
			guildCache.retrieveOrCreate(guildId, true),
			licenseCache.retrieve(guildId, true),
			userCache.retrieveOrCreate(userId, true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			User bUser = barb.get(User.class);
			BackendQuickAction.Update.execute(bUser, event.getUser(), true, true);
			bGuild.getMemberCache().retrieveOrCreate(userId, true).queue(bMember -> {
				BackendQuickAction.Update.execute(bMember, event.getMember(), true, true);
			});
		});
	}

	// CHANNEL

	@Override
	public void onTextChannelCreate(@NotNull TextChannelCreateEvent event){
		var guildCache = backendClient.getGuildCache();
		var licenseCache = backendClient.getLicenseCache();
		var guildId = event.getGuild().getIdLong();
		BackendActions.allOf(List.of(
			guildCache.retrieveOrCreate(guildId, true),
			licenseCache.retrieve(guildId, true)
		)).queue(barb -> {
			Guild bGuild = barb.get(Guild.class);
			bGuild.getChannelCache().retrieveOrCreate(event.getChannel().getIdLong(), true).queue(bChannel -> {
				BackendQuickAction.Update.execute(bChannel, event.getChannel(), true, false);
			});
		});
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
		backendClient.getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true).queue(bGuild -> {
			bGuild.getChannelCache().delete(event.getGuild().getIdLong()).queue();
		});
	}

	private void channelUpdate(TextChannel textChannel){
		backendClient.getGuildCache().retrieveOrCreate(textChannel.getGuild().getIdLong(), true).queue(bGuild -> {
			bGuild.getChannelCache().retrieveOrCreate(textChannel.getIdLong(), true).queue(bChannel -> {
				BackendQuickAction.Update.execute(bChannel, textChannel, true, false);
			});
		});
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
		backendClient.getGuildCache().retrieveOrCreate(guild.getIdLong(), true).queue(bGuild -> {
			BackendQuickAction.Update.execute(bGuild, guild, true, false);
		});
	}

}
