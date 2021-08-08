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

package de.netbeacon.xenia.bot.commands.chat.structure.avatar;

import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.AVATAR_USER_MENTTION;

public class CMDAvatar extends Command{

	public CMDAvatar(){
		super("avatar", false, new CommandCooldown(CommandCooldown.Type.User, 2000),
			null,
			null,
			null,
			List.of(AVATAR_USER_MENTTION)
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getAuthor());
		CmdArg<Mention> mentionCmdArg = args.getByIndex(0);

		if(mentionCmdArg.getValue() != null){
			commandEvent.getEvent().getJDA().retrieveUserById(mentionCmdArg.getValue().getId())
				.queue(
					user -> commandEvent.getEvent().getChannel().sendMessageEmbeds(
						embedBuilder
							.setColor(Color.GREEN)
							.setImage(user.getEffectiveAvatarUrl() + "?size=512")
							.setDescription(
								"[256px](" + user.getEffectiveAvatarUrl() + "?size=256) " +
									"[512px](" + user.getEffectiveAvatarUrl() + "?size=512) " +
									"[1024px](" + user.getEffectiveAvatarUrl() + "?size=1024) "
							)
							.build()
					).queue(),
					error -> commandEvent.getEvent().getChannel().sendMessageEmbeds(
						embedBuilder
							.setColor(Color.RED)
							.setDescription(translationPackage.getTranslation(getClass(), "error.msg"))
							.build()
					).queue()
				);
		}
		else{
			commandEvent.getEvent().getChannel().sendMessageEmbeds(
				embedBuilder
					.setColor(Color.GREEN)
					.setImage(commandEvent.getEvent().getAuthor().getEffectiveAvatarUrl() + "?size=512")
					.setDescription(
						"[256px](" + commandEvent.getEvent().getAuthor().getEffectiveAvatarUrl() + "?size=256) " +
							"[512px](" + commandEvent.getEvent().getAuthor().getEffectiveAvatarUrl() + "?size=512) " +
							"[1024px](" + commandEvent.getEvent().getAuthor().getEffectiveAvatarUrl() + "?size=1024) "
					)
					.build()
			).queue();
		}
	}

}
