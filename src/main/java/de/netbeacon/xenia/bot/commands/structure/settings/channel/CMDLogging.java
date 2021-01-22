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

package de.netbeacon.xenia.bot.commands.structure.settings.channel;

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.*;

public class CMDLogging extends Command {

    public CMDLogging() {
        super("logging", "Update the logging settings for the specified channel", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(CHANNEL_LOGGING_ENABLE, CHANNEL_ID_OPTIONAL, CHANNEL_ID_TMPLOGGING_OPTIONAL)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        try{
            CmdArg<Boolean> channelLoggingArg = args.getByIndex(0);
            CmdArg<Mention> mentionCmdArg = args.getByIndex(1);
            CmdArg<Mention> mention2CmdArg = args.getByIndex(2);
            Channel channel = (mentionCmdArg.getValue() == null) ? commandEvent.getBackendDataPack().getbChannel() : commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(mentionCmdArg.getValue().getId(), false);
            if(channel == null){
                throw new Exception();
            }
            channel.lSetTmpLoggingActive(channelLoggingArg.getValue());
            if(mention2CmdArg.getValue() != null){
                channel.setTmpLoggingChannelId(mention2CmdArg.getValue().getId());
            }
            channel.update();
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Updated Logging Mode")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Update Logging Mode. \n Available Options: [true, false]")).queue();
        }
    }
}
