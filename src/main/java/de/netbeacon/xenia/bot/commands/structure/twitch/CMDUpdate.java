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

package de.netbeacon.xenia.bot.commands.structure.twitch;

import de.netbeacon.xenia.backend.client.objects.cache.misc.TwitchNotificationCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.TwitchNotification;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.TWITCH_NOTIFICATION_CUSTOM_MESSAGE;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.TWITCH_NOTIFICATION_ID;

public class CMDUpdate extends Command {

    public CMDUpdate() {
        super("update", "Update stream notifications for a channel", new CommandCooldown(CommandCooldown.Type.User, 5000),
                null,
                new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
                new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
                List.of(TWITCH_NOTIFICATION_ID, TWITCH_NOTIFICATION_CUSTOM_MESSAGE)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        try{
            CmdArg<Long> notificationid = args.getByIndex(0);
            CmdArg<String> customMessage = args.getByIndex(1);
            TwitchNotificationCache notificationCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTwitchNotificationCache();
            TwitchNotification notification = notificationCache.get(notificationid.getValue());
            notification.setNotificationMessage(customMessage.getValue());
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Updated Stream Notification")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Update Stream Notification.")).queue();
        }
    }
}
