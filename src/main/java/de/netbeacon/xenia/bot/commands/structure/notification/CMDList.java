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
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.User;
import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

public class CMDList extends Command {

    public CMDList() {
        super("list", new CommandCooldown(CommandCooldown.Type.User, 2500),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.NOTIFICATION_USE)),
                null
        );
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        // list all notifications
        NotificationCache notificationCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getNotificationCache();
        StringBuilder stringBuilder = new StringBuilder();
        for(Notification notification : notificationCache.getAllAsList()){
            User user = null;
            try{
                user = commandEvent.getBackendClient().getUserCache().get(notification.getUserId());
            }catch (Exception ignore){}
            stringBuilder.append(notification.getId()).append(" - ").append((user != null) ? user.getMetaUsername() : notification.getUserId()).append("@").append(notification.getChannelId()).append(" - ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(Instant.ofEpochMilli(notification.getNotificationTarget()).atZone(ZoneId.systemDefault()).toLocalDateTime())).append("\n");
        }
        MessageEmbed messageEmbed = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .setDescription(stringBuilder.substring(0, Math.min(stringBuilder.toString().length(), 2000))).build();
        commandEvent.getEvent().getChannel().sendMessage(messageEmbed).queue();
    }
}
