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
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.NOTIFICATION_ID_DEF;

public class CMDDelete extends Command {

    public CMDDelete() {
        super("delete", "Delete an existing notification", new CommandCooldown(CommandCooldown.Type.User, 5000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
                List.of(NOTIFICATION_ID_DEF));
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent) {
        CmdArg<Long> longCmdArg = cmdArgs.getByIndex(0);
        try{
            Notification notification = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache().get(longCmdArg.getValue());
            if(notification.getUserId() != commandEvent.getEvent().getAuthor().getIdLong()){
                throw new RuntimeException("User Does Not Own This Notification");
            }
            commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache().delete(notification.getId());
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Notification (ID: "+notification.getId()+") deleted!")).queue(s->{},e->{});
        }catch (Exception ex){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed to delete notification. Perhaps this got created by someone else or does not exist?")).queue(s->{s.delete().queueAfter(5, TimeUnit.SECONDS);}, e->{});;
        }
    }
}
