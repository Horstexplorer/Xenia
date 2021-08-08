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

package de.netbeacon.xenia.bot.commands.chat.structure.twitch;

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.backend.client.objects.apidata.misc.TwitchNotification;
import de.netbeacon.xenia.bot.commands.chat.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashSet;
import java.util.List;

public class HYBRIDTwitch extends HybridCommand{

	public HYBRIDTwitch(){
		super(null, "twitch", false, new CommandCooldown(CommandCooldown.Type.User, 2000),
			null,
			new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
			new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
			null
		);
		addChildCommand(new CMDCreate());
		addChildCommand(new CMDUpdate());
		addChildCommand(new CMDDelete());
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		// get all stream notifications
		List<TwitchNotification> twitchNotifications = commandEvent.getBackendDataPack().guild().getMiscCaches().getTwitchNotificationCache().getAllAsList();
		StringBuilder stringBuilder = new StringBuilder();
		for(TwitchNotification twitchNotification : twitchNotifications){
			stringBuilder.append(twitchNotification.getId()).append(" ").append(twitchNotification.getTwitchChannelName()).append(" --> ").append(twitchNotification.getChannel().getMetaChannelName()).append("\n");
		}
		// send message
		MessageEmbed result = EmbedBuilderFactory
			.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.success.title"), commandEvent.getEvent().getAuthor())
			.setDescription(stringBuilder)
			.build();
		commandEvent.getEvent().getChannel().sendMessageEmbeds(result).queue();
	}

}
