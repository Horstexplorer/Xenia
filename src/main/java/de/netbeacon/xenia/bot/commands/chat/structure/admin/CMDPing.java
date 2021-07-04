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

import de.netbeacon.xenia.backend.client.objects.external.system.Info;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;

public class CMDPing extends AdminCommand{

	public CMDPing(){
		super("ping", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage){
		double avgGatewayPing = XeniaCore.getInstance().getShardManager().getAverageGatewayPing();
		double gatewayPing = commandEvent.getEvent().getJDA().getGatewayPing();
		double restPing = commandEvent.getEvent().getJDA().getRestPing().complete();
		Info info = new Info(commandEvent.getToolBundle().backendClient().getBackendProcessor(), Info.Mode.Public);
		info.get();

		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Ping", commandEvent.getEvent().getAuthor())
			.addField("AVG Gateway Ping:", avgGatewayPing + "ms", true)
			.addField("Gateway Ping:", gatewayPing + "ms", true)
			.addField("Rest Ping", restPing + "ms", true)
			.addField("Backend Ping", info.getPing() + "ms", true);

		commandEvent.getEvent().getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
	}

}
