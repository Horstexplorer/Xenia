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

import de.netbeacon.xenia.backend.client.objects.cache.misc.NotificationCache;
import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.NOTIFICATION_MESSAGE_DEF;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.NOTIFICATION_TARGET_TIME_DEF;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Create a new notification", new CommandCooldown(CommandCooldown.Type.User, 10000), null, null, List.of(NOTIFICATION_TARGET_TIME_DEF, NOTIFICATION_MESSAGE_DEF));
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent) {
        CmdArg<LocalDateTime> localDateTimeCmdArg = cmdArgs.getByIndex(0);
        CmdArg<String> stringCmdArg = cmdArgs.getByIndex(1);
        // create a new notification
        NotificationCache notificationCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache();
        try{
            Notification notification = notificationCache.createNew(commandEvent.getEvent().getChannel().getIdLong(), commandEvent.getEvent().getAuthor().getIdLong(), localDateTimeCmdArg.getValue().toInstant(ZoneOffset.UTC).toEpochMilli(), stringCmdArg.getValue());
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Notification (ID: "+notification.getId()+") created by "+commandEvent.getEvent().getAuthor().getAsTag())).queue(s->{},e->{});
        }catch (Exception ex){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed to create notification! Perhaps the cache is full?")).queue(s->{s.delete().queueAfter(5, TimeUnit.SECONDS);}, e->{});
        }
    }
}
