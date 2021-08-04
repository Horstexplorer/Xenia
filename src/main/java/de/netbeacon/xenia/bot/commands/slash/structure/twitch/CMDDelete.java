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

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.backend.client.objects.cache.misc.TwitchNotificationCache;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
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

public class CMDDelete extends Command{

	public CMDDelete(){
		super("delete", "Remove stream notifications for a channel", false, new CommandCooldown(CommandCooldown.Type.User, 5000),
			null,
			new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
			new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
			List.of(
				new CmdArgDef.Builder<>("notification_id", "Notification id", "Id of the twitch notification", Long.class).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		try{
			CmdArg<Long> notificationid = cmdArgs.getByName("notification_id");
			TwitchNotificationCache notificationCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getTwitchNotificationCache();
			notificationCache.delete(notificationid.getValue()).execute();
			commandEvent.getEvent().replyEmbeds(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.msg"))).queue();
		}
		catch(DataException | CacheException e){
			if(e instanceof DataException && ((DataException) e).getCode() == 404){
				commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
			}
			else{
				throw e;
			}
		}
	}

}
