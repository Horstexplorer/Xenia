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
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CMDList extends Command {

    public CMDList() {
        super("list", "View, manage and create notifications", new CommandCooldown(CommandCooldown.Type.User, 2500), null, null, null);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        // list all notifications
        NotificationCache notificationCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("NotificationId - UserId@ChannelId - NotificationTime\n\n");
        for(Notification notification : notificationCache.getAllAsList()){
            stringBuilder.append(notification.getId()).append(" - ").append(notification.getUserId()).append("@").append(notification.getChannelId()).append(" - ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(notification.getNotificationTarget()), ZoneId.of("ECT")))).append("\n");
        }
        MessageEmbed messageEmbed = EmbedBuilderFactory.getDefaultEmbed("Notifications", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .setDescription(stringBuilder.substring(0, 2000)).build();
        commandEvent.getEvent().getChannel().sendMessage(messageEmbed).queue();
    }
}
