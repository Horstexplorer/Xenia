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

import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.interactions.registry.ComponentInteractionRegistry;
import de.netbeacon.xenia.bot.interactions.registry.ComponentRegistryEntry;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;

import java.awt.*;

public class ComponentInteractionHandler{

	private final ToolBundle toolBundle;
	private final TranslationManager translationManager = TranslationManager.getInstance();

	public ComponentInteractionHandler(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
	}

	public static MessageEmbed onUnknownId(TranslationPackage translationPackage){
		return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onUnknownId.title"))
			.setColor(Color.RED)
			.setDescription(translationPackage.getTranslation("default.onUnknownId.description"))
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

	public void handle(GenericComponentInteractionCreateEvent genericComponentInteractionCreateEvent){
		// check if we have a fitting interaction component
		ComponentInteractionRegistry componentInteractionRegistry = toolBundle.componentInteractionRegistry();
		ComponentRegistryEntry componentRegistryEntry = componentInteractionRegistry.get(genericComponentInteractionCreateEvent.getComponentId());
		if(componentRegistryEntry == null){
			genericComponentInteractionCreateEvent
				.replyEmbeds(onUnknownId(translationManager.getDefaultTranslationPackage()))
				.setEphemeral(true)
				.queue();
			return;
		}
		// check if the interaction origin matches
		Guild guild = genericComponentInteractionCreateEvent.getGuild();
		Member member = genericComponentInteractionCreateEvent.getMember();
		User user = genericComponentInteractionCreateEvent.getUser();
		Message message = genericComponentInteractionCreateEvent.getMessage();
		if(guild == null || !componentRegistryEntry.getOrigin().isAllowedOrigin(message)){
			genericComponentInteractionCreateEvent
				.replyEmbeds(onIllegalOrigin(translationManager.getDefaultTranslationPackage()))
				.setEphemeral(true)
				.queue();
			if(componentRegistryEntry.getExceptionHandler().exceptionConsumer() != null){
				componentRegistryEntry.getExceptionHandler().exceptionConsumer()
					.accept(new ComponentRegistryEntry.Exception(ComponentRegistryEntry.Exception.Type.ORIGIN), genericComponentInteractionCreateEvent);
			}
			return;
		}
		// is the user allowed to access?
		if(member == null ? !componentRegistryEntry.getAccessor().isAllowedAccessor(user) : !componentRegistryEntry.getAccessor().isAllowedAccessor(member)){
			genericComponentInteractionCreateEvent
				.replyEmbeds(onIllegalAccessor(translationManager.getDefaultTranslationPackage()))
				.setEphemeral(true)
				.queue();
			if(componentRegistryEntry.getExceptionHandler().exceptionConsumer() != null){
				componentRegistryEntry.getExceptionHandler().exceptionConsumer()
					.accept(new ComponentRegistryEntry.Exception(ComponentRegistryEntry.Exception.Type.ACCESSOR), genericComponentInteractionCreateEvent);
			}
			return;
		}
		// is valid to interact
		if(!(componentRegistryEntry.isValid() && componentRegistryEntry.getActivation())){
			genericComponentInteractionCreateEvent
				.replyEmbeds(onIllegalAccessor(translationManager.getDefaultTranslationPackage()))
				.setEphemeral(true)
				.queue();
			if(componentRegistryEntry.getExceptionHandler().exceptionConsumer() != null){
				componentRegistryEntry.getExceptionHandler().exceptionConsumer()
					.accept(new ComponentRegistryEntry.Exception(ComponentRegistryEntry.Exception.Type.OUTDATED), genericComponentInteractionCreateEvent);
			}
			return;
		}
		// do work
		if(componentRegistryEntry.getActionHandler().actionEventConsumer() != null){
			try{
				componentRegistryEntry.getActionHandler().actionEventConsumer().accept(genericComponentInteractionCreateEvent);
			}
			catch(RuntimeException e){
				if(componentRegistryEntry.getExceptionHandler().exceptionConsumer() != null){
					componentRegistryEntry.getExceptionHandler().exceptionConsumer()
						.accept(e, genericComponentInteractionCreateEvent);
				}
			}
		}
		// clean up?
		if(!componentRegistryEntry.isValid()){
			componentInteractionRegistry.deactivate(componentRegistryEntry);
		}
	}

}
