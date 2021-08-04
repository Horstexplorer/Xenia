/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.event.handler;

import de.netbeacon.xenia.backend.client.objects.apidata.Channel;
import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.User;
import de.netbeacon.xenia.bot.event.manager.ExpectedInterruptException;
import de.netbeacon.xenia.bot.utils.automod.filter.AFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.SpamFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.URLFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.WordFilter;
import de.netbeacon.xenia.bot.utils.backend.action.BackendActions;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoModHandler{

	private final ToolBundle toolBundle;
	private final List<AFilter> filters = new ArrayList<>(){{
		add(new WordFilter());
		add(new URLFilter());
		add(new SpamFilter());
	}};

	public AutoModHandler(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
	}

	public void process(GuildMessageReceivedEvent event) throws ExpectedInterruptException {
		var backendClient = toolBundle.backendClient();
		// get data from backend
		var barb = BackendActions.allOf(List.of(
			backendClient.getGuildCache().retrieveOrCreate(event.getGuild().getIdLong(), true),
			backendClient.getLicenseCache().retrieve(event.getGuild().getIdLong(), true),
			backendClient.getUserCache().retrieveOrCreate(event.getAuthor().getIdLong(), true)
		)).execute();
		Guild bGuild = barb.get(Guild.class);
		Channel bChannel = bGuild.getChannelCache().retrieveOrCreate(event.getChannel().getIdLong(), true).execute();
		User bUser = barb.get(User.class);
		// apply filters; if an ExpectedInterruptException has been thrown the filter applied
		for(AFilter filter : filters){
			filter.filter(event.getMessage(), bGuild, bChannel, bUser);
		}
	}
}
