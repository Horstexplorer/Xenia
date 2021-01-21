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
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.TWITCH_NOTIFICATION_CHANNEL_URL;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.TWITCH_NOTIFICATION_CUSTOM_MESSAGE_OPTIONAL;
import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.TWITCH_URL_PATTERN;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Request stream notifications for a channel", new CommandCooldown(CommandCooldown.Type.User, 5000),
                null,
                new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
                new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
                List.of(TWITCH_NOTIFICATION_CHANNEL_URL, TWITCH_NOTIFICATION_CUSTOM_MESSAGE_OPTIONAL)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        try{
            CmdArg<String> channelUrl = args.getByIndex(0);
            CmdArg<String> customMessageC = args.getByIndex(1);
            // get name
            Matcher m = TWITCH_URL_PATTERN.matcher(channelUrl.getValue());
            if(!m.matches()){
                throw new RuntimeException("Error Matching URL");
            }
            // get all stream notifications
            TwitchNotificationCache notificationCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTwitchNotificationCache();
            notificationCache.createNew(commandEvent.getEvent().getChannel().getIdLong(), m.group(2), customMessageC.getValue());
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Request Another Stream Notification.")).queue();
        }
    }
}
