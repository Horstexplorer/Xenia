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

import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;

import java.util.HashSet;
import java.util.List;

public class CMDDelete extends Command{

	public CMDDelete(){
		super("delete", "Delete an existing notification", false, new CommandCooldown(CommandCooldown.Type.User, 5000),
			null,
			null,
			new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
			List.of(
				new CmdArgDef.Builder<>("id", "Notification id", "Notification id", Long.class).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		CmdArg<Long> idArg = cmdArgs.getByName("id");
		try{
			Notification notification = commandEvent.getBackendDataPack().guild().getMiscCaches().getNotificationCache().get(idArg.getValue());
			if(notification.getUserId() != commandEvent.getEvent().getUser().getIdLong() && !(commandEvent.getBackendDataPack().member().metaIsAdministrator() || commandEvent.getBackendDataPack().member().metaIsOwner())){
				throw new RuntimeException("User Does Not Own This Notification");
			}
			commandEvent.getBackendDataPack().guild().getMiscCaches().getNotificationCache().delete(notification.getId());
			commandEvent.getEvent().replyEmbeds(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", notification.getId()))).queue();
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
