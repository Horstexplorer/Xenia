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

package de.netbeacon.xenia.bot.commands.structure;

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.xenia.backend.client.objects.external.Info;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;


/**
 * Basic information about the bot
 */
public class CMDInfo extends Command {

    public CMDInfo() {
        super("info", "Shows some basic information about me", new CommandCooldown(CommandCooldown.Type.User, 1000),null, null,null, null);
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent) {
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Info", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .addField("Xenia:", "Version "+ AppInfo.get("buildVersion")+"_"+ AppInfo.get("buildNumber"), true)
                .addField("Xenia-Backend:","Version "+ XeniaCore.getInstance().getBackendClient().getInfo(Info.Mode.Public).getVersion(), true)
                .addField("Current Shard:", String.valueOf(commandEvent.getEvent().getJDA().getShardInfo().getShardId()) , true)
                .addField("Instance:", commandEvent.getBackendClient().getSetupData().getClientName() , true)
                .addField("Location:", commandEvent.getBackendClient().getSetupData().getClientLocation() , true)
                .addField("Website:", "[Visit Website](https://xenia.netbeacon.de/)", true)
                .addField("Data Processing", "[Visit Website](https://xenia.netbeacon.de/processing)", true)
                .addField("Report Content", "[Visit Website](https://xenia.netbeacon.de/report)", true)
                .addField("Contact", "[Visit Website](https://xenia.netbeacon.de/contact)", true);
        commandEvent.getEvent().getChannel().sendMessage(embedBuilder.build()).queue(s->{},e->{});
    }
}
