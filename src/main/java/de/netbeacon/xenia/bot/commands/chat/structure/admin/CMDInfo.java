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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.xenia.backend.client.objects.external.system.Info;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.lang.management.ManagementFactory;


/**
 * Displays some stats to the user
 */
public class CMDInfo extends AdminCommand{

	public CMDInfo(){
		super("info", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage){
		ShardManager shardManager = XeniaCore.getInstance().getShardManager();
		Runtime runtime = Runtime.getRuntime();
		GuildMessageReceivedEvent event = commandEvent.getEvent();
		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
		Info bInfo = commandEvent.getToolBundle().backendClient().getInfo(Info.Mode.Private);
		bInfo.get();
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Info", commandEvent.getEvent().getAuthor())
			.addField("Xenia", "Version: " + AppInfo.get("buildVersion") + "_" + AppInfo.get("buildNumber"), false)
			.addField("Gateway Ping:", shardManager.getAverageGatewayPing() + "ms", true)
			.addField("Shards:", String.valueOf(event.getJDA().getShardInfo().getShardTotal()), true)
			.addField("Shard:", String.valueOf(event.getJDA().getShardInfo().getShardId()), true)
			.addField("Instance:", commandEvent.getToolBundle().backendClient().getSetupData().getClientName(), true)
			.addField("Location:", commandEvent.getToolBundle().backendClient().getSetupData().getClientLocation(), true)
			.addField("Uptime:", String.format("%d days, %d hours, %d min, %d seconds",
				(int) ((uptime / (1000 * 60 * 60 * 24))),
				(int) ((uptime / (1000 * 60 * 60)) % 24),
				(int) ((uptime / (1000 * 60)) % 60),
				(int) ((uptime / (1000)) % 60)), true)
			.addField("Threads:", String.valueOf(Thread.activeCount()), true)
			.addField("Memory Usage:", (runtime.totalMemory() - runtime.freeMemory()) / (1048576) + "Mb/" + runtime.totalMemory() / (1048576) + "Mb", true);
		event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
		EmbedBuilder embedBuilder2 = EmbedBuilderFactory.getDefaultEmbed("Info", commandEvent.getEvent().getAuthor())
			.addField("Xenia-Backend", "Version: " + bInfo.getVersion(), false)
			.addField("Request Ping:", bInfo.getPing() + "ms", false)
			.addField("Guilds", String.valueOf(bInfo.getGuildCount()), true)
			.addField("GuildsCOTI", String.valueOf(commandEvent.getToolBundle().backendClient().getGuildCache().getAllAsList().size()), true)
			.addField("Users", String.valueOf(bInfo.getUserCount()), true)
			.addField("UsersCOTI", String.valueOf(commandEvent.getToolBundle().backendClient().getUserCache().getAllAsList().size()), true)
			.addField("Members", String.valueOf(bInfo.getMemberCount()), true)
			.addField("Messages", String.valueOf(bInfo.getMessageCount()), true)
			.addField("Channels", bInfo.getChannelCount() + "(" + (bInfo.getChannelCount() - bInfo.getForbiddenChannels()) + ")", true);
		event.getChannel().sendMessageEmbeds(embedBuilder2.build()).queue();
	}

}
