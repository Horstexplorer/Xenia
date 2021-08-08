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

package de.netbeacon.xenia.bot.commands.slash.structure.anime;

import de.netbeacon.purrito.qol.typewrap.ContentType;
import de.netbeacon.purrito.qol.typewrap.ImageType;
import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.purrito.PurrBotAPIWrapper;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.HashSet;
import java.util.List;

public abstract class AnimeImageCommand extends Command{

	private final ImageType imageType;
	private final ContentType contentType;

	public AnimeImageCommand(String alias, String description, boolean optionalUser, boolean isNSFW, ImageType imageType, ContentType contentType){
		super(alias, description, isNSFW, new CommandCooldown(CommandCooldown.Type.User, 2500),
			null,
			null,
			new HashSet<>(List.of(isNSFW ? Role.Permissions.Bit.ANIME_NSFW_USE : Role.Permissions.Bit.ANIME_SFW_USE)),
			(optionalUser) ? List.of(new CmdArgDef.Builder<>("user", "user", "user", User.class).setOptional(true).build()) : null
		);
		this.imageType = imageType;
		this.contentType = contentType;
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		try{
			// ack
			commandEvent.getEvent().deferReply().queue(
				ack -> {
					// pick correct response
					CmdArg<User> cmdArg = cmdArgs.getByName("user");
					String additionalUserTag = null;
					if(cmdArg != null && cmdArg.getValue() != null){
						additionalUserTag = cmdArg.getValue().getAsTag();
					}
					String message = translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg." + (additionalUserTag != null ? 1 : 0), commandEvent.getEvent().getUser().getAsTag(), (additionalUserTag != null ? additionalUserTag : "unknown#unknown"));
					// get image
					getImage(ack, translationPackage, message, 0);
				},
				err -> commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue(s -> {}, e -> {})
			);
		}
		catch(Exception e){
			commandEvent.getEvent().replyEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue(s -> {}, ex -> {});
		}
	}

	private void getImage(InteractionHook ack, TranslationPackage translationPackage, String message, int retries){
		PurrBotAPIWrapper.getInstance().getAnimeImageUrlOf(imageType, contentType).async(
			url -> ack.editOriginalEmbeds(
				EmbedBuilderFactory.getDefaultEmbed(message).setImage(url).build()
			).queue(),
			error -> {
				if(imageType.equals(ImageType.SFW.RANDOM) || imageType.equals(ImageType.NSFW.RANDOM) && retries < 5){
					// retry
					getImage(ack, translationPackage, message, retries + 1);
				}
				else{
					ack.editOriginalEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.img.msg"))).queue(s -> {}, e -> {});
				}
			}
		);
	}

}
