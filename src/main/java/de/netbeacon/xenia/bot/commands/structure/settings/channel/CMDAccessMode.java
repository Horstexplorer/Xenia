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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.CHANNEL_ACCESS_MODE;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.CHANNEL_ID_OPTIONAL;

public class CMDAccessMode extends Command {

    public CMDAccessMode() {
        super("accessmode", "Update the access mode for the specified channel", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(CHANNEL_ACCESS_MODE, CHANNEL_ID_OPTIONAL)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        try{
            CmdArg<String> channelAccessModeArg = args.getByIndex(0);
            CmdArg<Mention> mentionCmdArg = args.getByIndex(1);
            Channel channel = (mentionCmdArg.getValue() == null) ? commandEvent.getBackendDataPack().getbChannel() : commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(mentionCmdArg.getValue().getId(), false);
            if(channel == null){
                throw new Exception();
            }
            Channel.AccessMode.Mode accessModeMode = Channel.AccessMode.Mode.valueOf(channelAccessModeArg.getValue().toUpperCase());
            Channel.AccessMode accessMode = new Channel.AccessMode(0);
            accessMode.set(accessModeMode);
            channel.setAccessMode(accessMode);
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Updated Access Mode")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Update Access Mode. \n Available Options: "+ Arrays.toString(Channel.AccessMode.Mode.values()))).queue();
        }
    }
}