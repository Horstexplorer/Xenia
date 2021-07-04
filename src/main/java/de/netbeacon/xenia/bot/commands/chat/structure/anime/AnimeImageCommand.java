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

package de.netbeacon.xenia.bot.commands.chat.structure.anime;


import de.netbeacon.purrito.qol.typewrap.ContentType;
import de.netbeacon.purrito.qol.typewrap.ImageType;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.purrito.PurrBotAPIWrapper;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.ANIME_OPTIONAL_USER;

public abstract class AnimeImageCommand extends Command{

	private final ImageType imageType;
	private final ContentType contentType;

	public AnimeImageCommand(String alias, boolean optionalUser, boolean isNSFW, ImageType imageType, ContentType contentType){
		super(alias, isNSFW, new CommandCooldown(CommandCooldown.Type.User, 2500),
			null,
			null,
			new HashSet<>(List.of(isNSFW ? Role.Permissions.Bit.ANIME_NSFW_USE : Role.Permissions.Bit.ANIME_SFW_USE)),
			(optionalUser) ? List.of(ANIME_OPTIONAL_USER) : null
		);
		this.imageType = imageType;
		this.contentType = contentType;
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		try{
			String additionalUserTag = null;
			if(args.size() > 0){
				CmdArg<Mention> mentionCmdArg = args.getByIndex(0);
				if(mentionCmdArg.getValue() != null){
					long mentionedId = mentionCmdArg.getValue().getId();
					// resolve user with backend
					var uc = commandEvent.getToolBundle().backendClient().getUserCache();
					if(uc.contains(mentionedId)){
						additionalUserTag = uc.get(mentionedId, false).getMetaUsername();
					}
				}
			}
			// get message
			String message = translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg." + (additionalUserTag != null ? 1 : 0), commandEvent.getEvent().getAuthor().getAsTag(), (additionalUserTag != null ? additionalUserTag : "unknown#unknown"));
			// get image
			getImage(commandEvent, message, translationPackage, 0);
		}
		catch(Exception e){
			commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue(s -> {}, ex -> {});
		}
	}

	private void getImage(CommandEvent commandEvent, String message, TranslationPackage translationPackage, int retries){
		PurrBotAPIWrapper.getInstance().getAnimeImageUrlOf(imageType, contentType).async(
			url -> {
				commandEvent.getEvent().getChannel().sendMessage(
					EmbedBuilderFactory.getDefaultEmbed(message).setImage(url).build()
				).queue();
			},
			error -> {
				if(imageType.equals(ImageType.SFW.RANDOM) || imageType.equals(ImageType.NSFW.RANDOM) && retries < 5){
					getImage(commandEvent, message, translationPackage, retries + 1);
				}
				else{
					commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.img.msg"))).queue(s -> {}, e -> {});
				}
			}
		);
	}

}
