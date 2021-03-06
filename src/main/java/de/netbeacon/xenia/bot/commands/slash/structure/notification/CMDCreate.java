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

package de.netbeacon.xenia.bot.commands.slash.structure.notification;

import de.netbeacon.xenia.backend.client.objects.cache.misc.NotificationCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.HumanTime;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

public class CMDCreate extends Command{

	public CMDCreate(){
		super("create", "Create a new notification", false, new CommandCooldown(CommandCooldown.Type.User, 10000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
			List.of(
				new CmdArgDef.Builder<>("duration", "Duration till the notification is due", "\"#h #m #s\" or \"60\" (in minutes) or \"yyyy-MM-dd hh:mm:ss\"", HumanTime.class).build(),
				new CmdArgDef.Builder<>("message", "Notification message", "Notification message", String.class).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		NotificationCache notificationCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getNotificationCache();
		CmdArg<HumanTime> durationArg = cmdArgs.getByName("duration");
		CmdArg<String> messageArg = cmdArgs.getByName("message");
		try{
			Notification notification = notificationCache.create(commandEvent.getEvent().getChannel().getIdLong(), commandEvent.getEvent().getUser().getIdLong(), durationArg.getValue().getFutureTime().toInstant(ZoneOffset.UTC).toEpochMilli(), messageArg.getValue());
			commandEvent.getEvent().replyEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", notification.getId(), commandEvent.getEvent().getUser().getAsTag()))).queue();
		}
		catch(DataException | CacheException ex){
			if(ex instanceof DataException && ((DataException) ex).getCode() == 404){
				commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
			}
			else{
				throw ex;
			}
		}
	}

}
