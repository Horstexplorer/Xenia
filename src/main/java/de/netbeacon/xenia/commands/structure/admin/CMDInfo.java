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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.tools.statics.Info.VERSION;

/**
 * Displays some stats to the user
 */
public class CMDInfo extends Command {

    public CMDInfo(){
        super("info", "Displays some statistics about this bot", new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)),new HashSet<>(Arrays.asList()), Arrays.asList());
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
        ShardManager shardManager = XeniaCore.getInstance().getShardManager();
        Runtime runtime = Runtime.getRuntime();
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Info")
                .setColor(Color.CYAN)
                .addField("Xenia","Version: "+VERSION, false)
                .addField("Gateway Ping:", shardManager.getAverageGatewayPing()+"ms", true)
                .addField("Shard", String.valueOf(event.getJDA().getShardInfo().getShardId()), true)
                .addField("Shards", String.valueOf(event.getJDA().getShardInfo().getShardTotal()), true)
                .addField("Uptime:", String.format("%d days, %d hours, %d min, %d seconds",
                        (int)((uptime / (1000*60*60*24))),
                        (int)((uptime / (1000*60*60)) % 24),
                        (int)((uptime / (1000*60)) % 60),
                        (int)((uptime / (1000)) % 60)), true)
                .addField("Threads:", String.valueOf(Thread.activeCount()), true)
                .addField("Memory Usage:", (runtime.totalMemory()-runtime.freeMemory())/(1048576)+"Mb/"+runtime.totalMemory()/(1048576)+"Mb", true);

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
