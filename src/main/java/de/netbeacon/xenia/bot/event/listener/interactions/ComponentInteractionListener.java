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

package de.netbeacon.xenia.bot.event.listener.interactions;

import de.netbeacon.xenia.bot.event.handler.interactions.ComponentInteractionHandler;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ComponentInteractionListener extends ListenerAdapter{

	private final ToolBundle toolBundle;
	private final ComponentInteractionHandler componentInteractionHandler;

	public ComponentInteractionListener(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
		this.componentInteractionHandler = new ComponentInteractionHandler(toolBundle);
	}

	@Override
	public void onButtonClick(@NotNull ButtonClickEvent event){
		if(toolBundle.eventWaiter().waitingOnThis(event)){
			return;
		}
		componentInteractionHandler.handle(event);
	}

	@Override
	public void onSelectionMenu(@NotNull SelectionMenuEvent event){
		if(toolBundle.eventWaiter().waitingOnThis(event)){
			return;
		}
		componentInteractionHandler.handle(event);
	}

}
