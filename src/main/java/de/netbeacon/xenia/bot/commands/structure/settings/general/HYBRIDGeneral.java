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

package de.netbeacon.xenia.bot.commands.structure.settings.general;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.License;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class HYBRIDGeneral extends HybridCommand {

    public HYBRIDGeneral(CommandGroup parent) {
        super(parent, "general", "Contains general guild settings", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                null);
        addChildCommand(new CMDLicense());
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        Guild guild = commandEvent.getBackendDataPack().getbGuild();
        License license = commandEvent.getBackendDataPack().getbLicense();
        commandEvent.getEvent().getChannel().sendMessage(
                EmbedBuilderFactory.getDefaultEmbed("General Guild Setting", commandEvent.getEvent().getJDA().getSelfUser(),commandEvent.getEvent().getAuthor())
                        .addField("preferred language", guild.getPreferredLanguage(), true)
                        .addField("use VPerms", String.valueOf(guild.useVPerms()), true)
                        .addBlankField(false)
                        .addField("license name", license.getLicenseName(), false)
                        .addField("valid until", (license.getActivationTimestamp() > -1)? new Date(license.getActivationTimestamp()+ (license.getDurationDays()* 86400000L)).toString() : "-", false)
                        .addField("max VPerm roles", String.valueOf(license.getPerk_GUILD_ROLE_C()), true)
                        .addField("max tags", String.valueOf(license.getPerk_MISC_TAGS_C()), true)
                        .addField("max notification", String.valueOf(license.getPerk_MISC_NOTIFICATIONS_C()), true)
                        .addField("max message logging", String.valueOf(license.getPerk_CHANNEL_LOGGING_C()), true)
                        .build()
        ).queue();
    }
}
