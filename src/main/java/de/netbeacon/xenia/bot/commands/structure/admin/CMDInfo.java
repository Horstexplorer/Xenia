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

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.xenia.backend.client.objects.external.Info;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Displays some stats to the user
 */
public class CMDInfo extends Command {

    public CMDInfo(){
        super("info", "Displays some statistics about this bot", new CommandCooldown(CommandCooldown.Type.User, 1000),null, null,null, null);
    }

    @Override
    public void execute(List<String> args, CommandEvent commandEvent) {
        // check required args
        CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
        if(getRequiredArgCount() > args.size() || !cmdArgs.verify()){
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
        ShardManager shardManager = XeniaCore.getInstance().getShardManager();
        Runtime runtime = Runtime.getRuntime();
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        Info bInfo = commandEvent.getBackendClient().getInfo(Info.Mode.Private);
        bInfo.get();
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Info", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .addField("Xenia","Version: "+AppInfo.get("buildVersion")+"_"+ AppInfo.get("buildNumber"), false)
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
        EmbedBuilder embedBuilder2 = EmbedBuilderFactory.getDefaultEmbed("Info", commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                .addField("Xenia-Backend","Version: "+bInfo.getVersion(), false)
                .addField("Request Ping:", bInfo.getPing()+"ms", false)
                .addField("Guilds", String.valueOf(bInfo.getGuildCount()), true)
                .addField("GuildsCOTI", String.valueOf(commandEvent.getBackendClient().getGuildCache().getAllAsList().size()), true)
                .addField("Users", String.valueOf(bInfo.getUserCount()), true)
                .addField("UsersCOTI", String.valueOf(commandEvent.getBackendClient().getUserCache().getAllAsList().size()), true)
                .addField("Members", String.valueOf(bInfo.getMemberCount()), true)
                .addField("Messages", String.valueOf(bInfo.getMessageCount()), true)
                .addField("Channels", bInfo.getChannelCount()+"("+(bInfo.getChannelCount()-bInfo.getForbiddenChannels())+")", true);

        event.getChannel().sendMessage(embedBuilder2.build()).queue();
    }
}
