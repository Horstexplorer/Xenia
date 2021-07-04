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

package de.netbeacon.xenia.bot.commands.slash.structure.twitch;

import de.netbeacon.xenia.backend.client.objects.cache.misc.TwitchNotificationCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.TWITCH_URL_PATTERN;

public class CMDCreate extends Command{

	public CMDCreate(){
		super("create", "Request stream notifications for a channel", false, new CommandCooldown(CommandCooldown.Type.User, 5000),
			null,
			new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
			new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
			List.of(
				new CmdArgDef.Builder<>("channel_url", "Url of the twitch channel", "Url of the twitch channel", String.class).build(),
				new CmdArgDef.Builder<>("custom_message", "Custom notification message", "Custom notification message", String.class).setOptional(true).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		try{
			CmdArg<String> channelUrl = cmdArgs.getByName("channel_url");
			CmdArg<String> customMessageC = cmdArgs.getByName("custom_message");
			// get name
			Matcher m = TWITCH_URL_PATTERN.matcher(channelUrl.getValue());
			if(!m.matches()){
				throw new IllegalArgumentException("Error Matching URL");
			}
			// create new
			TwitchNotificationCache notificationCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getTwitchNotificationCache();
			notificationCache.createNew(commandEvent.getEvent().getChannel().getIdLong(), m.group(2), customMessageC.getValue());
			// send response
			commandEvent.getEvent().replyEmbeds(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.msg"))).queue();
		}
		catch(IllegalArgumentException e){
			commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
		}
	}

}
