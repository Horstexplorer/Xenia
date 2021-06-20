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

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.event.handler.ButtonHandler;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ButtonListener extends ListenerAdapter{

	private final EventWaiter eventWaiter;
	private final ButtonHandler buttonHandler;

	public ButtonListener(XeniaBackendClient backendClient, EventWaiter eventWaiter, ButtonManager buttonManager){
		this.eventWaiter = eventWaiter;
		this.buttonHandler = new ButtonHandler(backendClient, buttonManager);
	}

	@Override
	public void onButtonClick(@NotNull ButtonClickEvent event){
		// button got clicked o.o
		if(eventWaiter.waitingOnThis(event)){
			return;
		}
		buttonHandler.handleClick(event);
	}


}
