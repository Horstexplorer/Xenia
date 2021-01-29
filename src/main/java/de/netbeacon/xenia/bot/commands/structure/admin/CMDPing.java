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

package de.netbeacon.xenia.bot.commands.structure.admin;

import de.netbeacon.xenia.backend.client.objects.external.system.Info;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CMDPing extends Command {

    public CMDPing() {
        super("ping", "Can be used to check the ping to discord and other linked services", new CommandCooldown(CommandCooldown.Type.User, 1000),null, null, null, null);
    }

    @Override
    public void execute(List<String> args, CommandEvent commandEvent) {
        // check required args
        CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
        if(!cmdArgs.verify()){
            // missing args
            commandEvent.getEvent().getChannel().sendMessage(onMissingArgs()).queue(s->{s.delete().queueAfter(10, TimeUnit.SECONDS);}, e->{});
            return;
        }
        if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(getBotPermissions())){
            // bot does not have the required permissions
            commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms()).queue(s->{},e->{});
            return;
        }
        if(commandEvent.getEvent().getAuthor().getIdLong() != XeniaCore.getInstance().getConfig().getLong("ownerID")){
            // invalid permission
            commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms(false)).queue(s->{},e->{});
            return;
        }
        // everything alright
        onExecution(cmdArgs, commandEvent);
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        double avgGatewayPing = XeniaCore.getInstance().getShardManager().getAverageGatewayPing();
        double gatewayPing = commandEvent.getEvent().getJDA().getGatewayPing();
        double restPing = commandEvent.getEvent().getJDA().getRestPing().complete();
        Info info = new Info(commandEvent.getBackendClient().getBackendProcessor(), Info.Mode.Public);
        info.get();

        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Ping", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .addField("AVG Gateway Ping:", avgGatewayPing+"ms", true)
                .addField("Gateway Ping:", gatewayPing+"ms", true)
                .addField("Rest Ping", restPing+"ms", true)
                .addField("Backend Ping", info.getPing()+"ms", true);

        commandEvent.getEvent().getChannel().sendMessage(embedBuilder.build()).queue(s->{},e->{});
    }

    @Override
    public MessageEmbed onMissingMemberPerms(boolean v){
        return EmbedBuilderFactory.getDefaultEmbed("Failed: Not Enough Permissions", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription("You are not allowed to do this !")
                .build();
    }
}
