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

package de.netbeacon.xenia.bot.event.handler.interactions;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonException;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonManager;
import de.netbeacon.xenia.bot.interactions.buttons.ButtonRegEntry;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ButtonHandler{

	private final XeniaBackendClient backendClient;
	private final ButtonManager buttonManager;
	private final TranslationManager translationManager = TranslationManager.getInstance();

	public ButtonHandler(XeniaBackendClient backendClient, ButtonManager buttonManager){
		this.backendClient = backendClient;
		this.buttonManager = buttonManager;
	}

	public void handleClick(ButtonClickEvent buttonClickEvent){

		try{
			Button button = buttonClickEvent.getButton();
			if(button == null){
				return;
			}
			String btnid = buttonClickEvent.getButton().getId();
			if(btnid == null){
				return;
			}

			Guild guild = buttonClickEvent.getGuild();
			Member member = buttonClickEvent.getMember();
			User user = buttonClickEvent.getUser();
			Message message = buttonClickEvent.getMessage();

			// get translation package from backend later to be used instead of the default one

			ButtonRegEntry buttonRegEntry = buttonManager.get(btnid);

			if(buttonRegEntry == null){
				buttonClickEvent
					.replyEmbeds(onUnknownButtonId(translationManager.getDefaultTranslationPackage()))
					.setEphemeral(true)
					.queue();
				return;
			}

			Consumer<ButtonClickEvent> actionConsumer = buttonRegEntry.getActionHandler().actionConsumer();
			BiConsumer<Exception, ButtonClickEvent> exceptionConsumer = buttonRegEntry.getExceptionHandler().exceptionConsumer();

			if(guild == null || message == null || member == null || !buttonRegEntry.getAllowedOrigin().isAllowedOrigin(message)){
				buttonClickEvent
					.replyEmbeds(onIllegalOrigin(translationManager.getDefaultTranslationPackage()))
					.setEphemeral(true)
					.queue();
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.ILLEGAL_ORIGIN), buttonClickEvent);
				return; // illegal origin, dont ack
			}

			if(!buttonRegEntry.getAllowedAccessor().isAllowedAccessor(member)){
				buttonClickEvent
					.replyEmbeds(onIllegalAccessor(translationManager.getDefaultTranslationPackage()))
					.setEphemeral(true)
					.queue();
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.ILLEGAL_ACCESSOR), buttonClickEvent);
				return;
			}

			if(!buttonRegEntry.getTimeoutPolicy().isInTime() || !buttonRegEntry.allowsActivation()){
				buttonClickEvent
					.replyEmbeds(onOutdatedAccess(translationManager.getDefaultTranslationPackage()))
					.setEphemeral(true)
					.queue();
				buttonManager.deactivate(buttonRegEntry);
				if(exceptionConsumer != null) exceptionConsumer.accept(new ButtonException(ButtonException.Type.OUTDATED), buttonClickEvent);
				return;
			}

			if(actionConsumer != null){
				actionConsumer.accept(buttonClickEvent);
			}

			if(!buttonRegEntry.getTimeoutPolicy().isInTime() || !buttonRegEntry.allowsActivation()){
				buttonManager.deactivate(buttonRegEntry);
			}

		}catch(Exception e){
			// ???
		}
	}

	public static MessageEmbed onUnknownButtonId(TranslationPackage translationPackage){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onUnknownButtonId.title"))
			.setColor(Color.RED)
			.setDescription(translationPackage.getTranslation("default.onUnknownButtonId.description"))
			.build();
	}

	public static MessageEmbed onIllegalOrigin(TranslationPackage translationPackage){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onIllegalOrigin.title"))
			.setColor(Color.RED)
			.setDescription(translationPackage.getTranslation("default.onIllegalOrigin.description"))
			.build();
	}

	public static MessageEmbed onIllegalAccessor(TranslationPackage translationPackage){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onIllegalAccessor.title"))
			.setColor(Color.RED)
			.setDescription(translationPackage.getTranslation("default.onIllegalAccessor.description"))
			.build();
	}

	public static MessageEmbed onOutdatedAccess(TranslationPackage translationPackage){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onOutdatedAccess.title"))
			.setColor(Color.RED)
			.setDescription(translationPackage.getTranslation("default.onOutdatedAccess.description"))
			.build();
	}

}
