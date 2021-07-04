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

package de.netbeacon.xenia.bot.commands.slash.objects.misc.event;

import de.netbeacon.xenia.backend.client.objects.external.*;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CommandEvent{

	private final SlashCommandEvent event;
	private final BackendDataPack backendDataPack;
	private final ToolBundle toolBundle;
	private float estimatedProcessingTime;

	/**
	 * Creates a new instance of this class
	 *
	 * @param event GuildMessageReceivedEvent
	 */
	public CommandEvent(SlashCommandEvent event, BackendDataPack backendDataPack, ToolBundle toolBundle){
		this.event = event;
		this.backendDataPack = backendDataPack;
		this.toolBundle = toolBundle;
	}

	/**
	 * Used to get the SlashCommandEvent
	 *
	 * @return SlashCommandEvent
	 */
	public SlashCommandEvent getEvent(){
		return event;
	}

	/**
	 * Returns the bundled data relating to the backend
	 *
	 * @return backend data
	 */
	public BackendDataPack getBackendDataPack(){
		return backendDataPack;
	}

	public ToolBundle getToolBundle(){
		return toolBundle;
	}

	/**
	 * Adds processing time to the estimate
	 *
	 * @param processingTime
	 */
	public void addProcessingTime(float processingTime){
		this.estimatedProcessingTime += estimatedProcessingTime;
	}

	/**
	 * Returns the estimated processing time
	 *
	 * @return
	 */
	public float getEstimatedProcessingTime(){
		return estimatedProcessingTime;
	}


	public static record BackendDataPack(Guild guild, User user, Member member, Channel channel, License license){}

}
