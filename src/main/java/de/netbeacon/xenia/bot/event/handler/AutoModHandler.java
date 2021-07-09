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

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.User;
import de.netbeacon.xenia.bot.event.manager.ExpectedInterruptException;
import de.netbeacon.xenia.bot.utils.automod.filter.AFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.SpamFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.URLFilter;
import de.netbeacon.xenia.bot.utils.automod.filter.imp.WordFilter;
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
		// get data from backend
		Guild bGuild = toolBundle.backendClient().getGuildCache().get(event.getGuild().getIdLong());
		User bUser = toolBundle.backendClient().getUserCache().get(event.getAuthor().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());
		// apply filters; if an ExpectedInterruptException has been thrown the filter applied
		for(AFilter filter : filters){
			filter.filter(event.getMessage(), bGuild, bChannel, bUser);
		}
	}
}
