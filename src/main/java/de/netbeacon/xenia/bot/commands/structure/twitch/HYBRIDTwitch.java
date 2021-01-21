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

import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.TwitchNotification;
import de.netbeacon.xenia.bot.commands.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashSet;
import java.util.List;

public class HYBRIDTwitch extends HybridCommand {

    public HYBRIDTwitch() {
        super(null, "twitch", "Create and manage stream notifications", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MESSAGE_MANAGE)),
                new HashSet<>(List.of(Role.Permissions.Bit.TWITCH_NOTIFICATIONS_MANAGE)),
                null
        );
        addChildCommand(new CMDCreate());
        addChildCommand(new CMDUpdate());
        addChildCommand(new CMDDelete());
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        try{
            // get all stream notifications
            List<TwitchNotification> twitchNotifications = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTwitchNotificationCache().getAllAsList();
            StringBuilder stringBuilder = new StringBuilder();
            for(TwitchNotification twitchNotification : twitchNotifications){
                stringBuilder.append(twitchNotification.getId()).append(" ").append(twitchNotification.getTwitchChannelName()).append(" to #").append(twitchNotification.getChannel().getMetaChannelName()).append("\n");
            }
            // send message
            MessageEmbed result = EmbedBuilderFactory
                    .getDefaultEmbed("Stream Notifications", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                    .setDescription(stringBuilder)
                    .build();
            commandEvent.getEvent().getChannel().sendMessage(result).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Get Stream Notifications")).queue();
        }
    }
}
