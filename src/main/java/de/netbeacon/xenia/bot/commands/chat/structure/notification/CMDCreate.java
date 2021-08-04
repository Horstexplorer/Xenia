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

package de.netbeacon.xenia.bot.commands.chat.structure.notification;

import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.backend.client.objects.apidata.misc.Notification;
import de.netbeacon.xenia.backend.client.objects.cache.misc.NotificationCache;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.HumanTime;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.NOTIFICATION_MESSAGE_DEF;
import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.NOTIFICATION_TARGET_TIME_DEF;

public class CMDCreate extends Command{

	public CMDCreate(){
		super("create", false, new CommandCooldown(CommandCooldown.Type.User, 10000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
			List.of(NOTIFICATION_TARGET_TIME_DEF, NOTIFICATION_MESSAGE_DEF)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		CmdArg<HumanTime> localDateTimeCmdArg = cmdArgs.getByIndex(0);
		CmdArg<String> stringCmdArg = cmdArgs.getByIndex(1);
		// create a new notification
		NotificationCache notificationCache = commandEvent.getBackendDataPack().guild().getMiscCaches().getNotificationCache();
		try{
			Notification notification = notificationCache.create(commandEvent.getEvent().getChannel().getIdLong(), commandEvent.getEvent().getAuthor().getIdLong(), localDateTimeCmdArg.getValue().getFutureTime().toInstant(ZoneOffset.UTC).toEpochMilli(), stringCmdArg.getValue()).execute();
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", notification.getId(), commandEvent.getEvent().getAuthor().getAsTag()))).queue();
		}
		catch(DataException | CacheException ex){
			if(ex instanceof DataException && ((DataException) ex).getCode() == 404){
				commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
			}
			else{
				throw ex;
			}
		}
	}

}
