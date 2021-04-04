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
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

public class CMDModify extends Command {

    public CMDModify() {
        super("modify", "Update an existing notification", new CommandCooldown(CommandCooldown.Type.User, 10000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
                new CommandUpdateAction.OptionData(net.dv8tion.jda.api.entities.Command.OptionType.INTEGER, "id", "Notification id").setRequired(true),
                new CommandUpdateAction.OptionData(net.dv8tion.jda.api.entities.Command.OptionType.STRING, "duration", "Duration till the notification is due").setRequired(true),
                new CommandUpdateAction.OptionData(net.dv8tion.jda.api.entities.Command.OptionType.STRING, "message", "Notification message").setRequired(true)
        );
    }

    @Override
    public void onExecution(CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception {
        try{
            Notification notification = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache().get(longCmdArg.getValue());
            if(notification.getUserId() != commandEvent.getEvent().getAuthor().getIdLong()){
                throw new RuntimeException("User Does Not Own This Notification");
            }
            notification.lSetNotificationTarget(localDateTimeCmdArg.getValue().toInstant(ZoneOffset.UTC).toEpochMilli());
            notification.lSetNotificationMessage(stringCmdArg.getValue());
            notification.update();

            commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.msg"))+" (ID: "+notification.getId()+")").queue();
        }catch (DataException | CacheException ex){
            if(ex instanceof DataException && ((DataException) ex).getCode() == 404){
                commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
            }else{
                throw ex;
            }
        }
    }
}
