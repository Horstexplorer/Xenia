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

package de.netbeacon.xenia.bot.event.handler;

import de.netbeacon.xenia.bot.interactions.buttons.ButtonException;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonManager;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonRegEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ButtonHandler{

	private final ButtonManager buttonManager;

	public ButtonHandler(ButtonManager buttonManager){
		this.buttonManager = buttonManager;
	}

	public void handleClick(ButtonClickEvent buttonClickEvent){

		try{
			String btnid = buttonClickEvent.getButton().getId();

			if(btnid == null){ // shouldnt happen
				return;
			}
			ButtonRegEntry buttonRegEntry = buttonManager.get(btnid);

			if(buttonRegEntry == null){
				return;
			}

			Consumer<ButtonClickEvent> actionConsumer = buttonRegEntry.getActionHandler().getActionConsumer();
			BiConsumer<Exception, ButtonClickEvent> exceptionConsumer = buttonRegEntry.getExceptionHandler().exceptionConsumer();

			long messageId = buttonClickEvent.getMessageIdLong();

			if(!buttonRegEntry.isAllowedOrigin(messageId)){
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.ILLEGAL_ORIGIN), buttonClickEvent);
				return;
			}

			Member member = buttonClickEvent.getMember();
			User user = buttonClickEvent.getUser();

			if(member == null ? !buttonRegEntry.isAllowedAccessor(user) : !buttonRegEntry.isAllowedAccessor(member)){
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.ILLEGAL_ACCESSOR), buttonClickEvent);
				return;
			}

			if(!buttonRegEntry.isInTime() || !buttonRegEntry.allowsActivation()){
				buttonRegEntry.deactivate(buttonClickEvent.getJDA().getShardManager());
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.OUTDATED), buttonClickEvent);
				return;
			}

			if(actionConsumer != null){
				actionConsumer.accept(buttonClickEvent);
			}

		}catch(Exception e){
			// ???
		}
	}

}
