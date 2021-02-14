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

package de.netbeacon.xenia.bot.commands.structure.notification;

import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.*;

public class CMDModify extends Command {

    public CMDModify() {
        super("update", new CommandCooldown(CommandCooldown.Type.User, 10000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
                List.of(NOTIFICATION_ID_DEF, NOTIFICATION_TARGET_TIME_DEF, NOTIFICATION_MESSAGE_DEF)
        );
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        CmdArg<Long> longCmdArg = cmdArgs.getByIndex(0);
        CmdArg<LocalDateTime> localDateTimeCmdArg = cmdArgs.getByIndex(1);
        CmdArg<String> stringCmdArg = cmdArgs.getByIndex(2);
        try{
            Notification notification = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache().get(longCmdArg.getValue());
            if(notification.getUserId() != commandEvent.getEvent().getAuthor().getIdLong()){
                throw new RuntimeException("User Does Not Own This Notification");
            }
            notification.lSetNotificationTarget(localDateTimeCmdArg.getValue().toInstant(ZoneOffset.UTC).toEpochMilli());
            notification.lSetNotificationMessage(stringCmdArg.getValue());
            notification.update();

            commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.msg"))+" (ID: "+notification.getId()+")").queue();
        }catch (CacheException ex){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
        }
    }
}
