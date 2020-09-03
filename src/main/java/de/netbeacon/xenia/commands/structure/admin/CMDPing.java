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

package de.netbeacon.xenia.commands.structure.admin;

import de.netbeacon.xenia.commands.objects.Command;
import de.netbeacon.xenia.commands.objects.CommandEvent;
import de.netbeacon.xenia.core.XeniaCore;
import de.netbeacon.xenia.tools.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import java.util.List;

public class CMDPing extends Command {

    public CMDPing() {
        super("ping", "Can be used to check the ping to discord and other linked services", null, null, null);
    }

    @Override
    public void execute(List<String> args, CommandEvent commandEvent) {
        if(getRequiredArgCount() > args.size()){
            // missing args
            commandEvent.getEvent().getChannel().sendMessage(onMissingArgs()).queue(s->{},e->{});
            return;
        }
        if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(getBotPermissions())){
            // bot does not have the required permissions
            commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms()).queue(s->{},e->{});
            return;
        }
        if(commandEvent.getEvent().getAuthor().getIdLong() != XeniaCore.getInstance().getConfig().getLong("ownerID")){
            // invalid permission
            commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms()).queue(s->{},e->{});
            return;
        }
        // everything alright
        onExecution(args, commandEvent);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        double avgGatewayPing = XeniaCore.getInstance().getShardManager().getAverageGatewayPing();
        double gatewayPing = commandEvent.getEvent().getJDA().getGatewayPing();
        double restPing = commandEvent.getEvent().getJDA().getRestPing().complete();

        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Ping", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .addField("AVG Gateway Ping:", avgGatewayPing+"ms", true)
                .addField("Gateway Ping:", gatewayPing+"ms", true)
                .addField("Rest Ping", restPing+"ms", true);

        commandEvent.getEvent().getChannel().sendMessage(embedBuilder.build()).queue(s->{},e->{});
    }
}
