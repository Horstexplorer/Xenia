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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.xenia.backend.client.objects.apidata.system.SetupData;
import de.netbeacon.xenia.backend.client.objects.internal.ws.SecondaryWebsocketListener;
import de.netbeacon.xenia.backend.client.objects.internal.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.client.objects.internal.ws.processor.WSResponse;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CMDClientIdentify extends AdminCommand{

	public CMDClientIdentify(){
		super("client_identify", new CommandCooldown(CommandCooldown.Type.User, 6000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage){
		Message m = commandEvent.getEvent().getChannel().sendMessageEmbeds(getRequestEmbed()).complete();
		try{
			// get secondary websocket
			SecondaryWebsocketListener secondaryWebsocketListener = commandEvent.getToolBundle().backendClient().getSecondaryWebsocketListener();
			// create request
			WSRequest wsRequest = new WSRequest.Builder()
				.action("identify")
				.mode(WSRequest.Mode.BROADCAST)
				.exitOn(WSRequest.ExitOn.TIMEOUT)
				.build();
			// send request
			List<WSResponse> wsResponses = secondaryWebsocketListener.getWsProcessorCore().process(wsRequest);
			// send result
			m.editMessageEmbeds(getResultEmbed(true, commandEvent.getToolBundle().backendClient().getSetupData(), wsResponses)).queue();
		}
		catch(Exception e){
			m.editMessageEmbeds(getResultEmbed(false, commandEvent.getToolBundle().backendClient().getSetupData(), new ArrayList<>())).queue();
		}
	}

	private MessageEmbed getRequestEmbed(){
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("DataRequest")
			.setColor(Color.ORANGE)
			.addField("Status:", "Running", true)
			.addField("Timeout:", 5000 + "ms", true)
			.addField("Action:", "identify", true);
		return embedBuilder.build();
	}

	private MessageEmbed getResultEmbed(boolean success, SetupData setupData, List<WSResponse> responses){
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("DataRequest");
		if(success){
			embedBuilder
				.addField("Status:", "Success", true)
				.setColor(Color.GREEN);
		}
		else{
			embedBuilder
				.addField("Status:", "Failure", true)
				.setColor(Color.RED);
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(">").append(setupData.getClientId()).append(" - ").append(setupData.getClientName()).append("\n");
		for(WSResponse wsResponse : responses){
			JSONObject jsonObject = wsResponse.getPayload();
			if(jsonObject != null){
				stringBuilder.append(jsonObject.getLong("id")).append(" - ").append(jsonObject.getString("name")).append("\n");
			}
		}
		embedBuilder.addField("Return:", stringBuilder.toString(), false);
		return embedBuilder.build();
	}

}
