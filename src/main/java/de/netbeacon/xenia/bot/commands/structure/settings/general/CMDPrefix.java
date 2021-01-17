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

package de.netbeacon.xenia.bot.commands.structure.settings.general;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.GUILD_PREFIX_DEF;

public class CMDPrefix extends Command {

    public CMDPrefix() {
        super("prefix", "Update the prefix used for commands", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(GUILD_PREFIX_DEF));
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        CmdArg<String> newPrefix = args.getByIndex(0);
        String prefix = (newPrefix.getValue() != null) ? newPrefix.getValue() : "~";
        Guild guild = commandEvent.getBackendDataPack().getbGuild();
        try{
            guild.setPrefix(prefix);
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Prefix Updated To ***"+guild.getPrefix()+"***")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Updating Prefix Failed")).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
        }
    }
}
