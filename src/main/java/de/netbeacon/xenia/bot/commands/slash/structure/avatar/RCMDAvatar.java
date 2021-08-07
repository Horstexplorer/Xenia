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

package de.netbeacon.xenia.bot.commands.slash.structure.avatar;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;

public class RCMDAvatar extends Command{

	public RCMDAvatar(){
		super("avatar", "Get the avatar of someone", false, new CommandCooldown(CommandCooldown.Type.User, 2000),
			null,
			null,
			null,
			List.of(
				new CmdArgDef.Builder<>("user_mention", "user", "user", User.class).setOptional(true).build(),
				new CmdArgDef.Builder<>("user_id", "id of a user", "", Long.class).setOptional(true).build()
			)
		);
	}

	@Override
	public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception{
		CmdArg<User> userCmdArg = cmdArgs.getByName("user_mention");
		CmdArg<Long> userId = cmdArgs.getByName("user_id");
		commandEvent.getEvent().deferReply().queue(
			interactionHook -> {
				EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getUser());
				if(userCmdArg.getValue() != null){
					interactionHook.editOriginalEmbeds(
						embedBuilder
							.setColor(Color.GREEN)
							.setImage(userCmdArg.getValue().getEffectiveAvatarUrl() + "?size=512")
							.setDescription(
								"[256px](" + userCmdArg.getValue().getEffectiveAvatarUrl() + "?size=256) " +
									"[512px](" + userCmdArg.getValue().getEffectiveAvatarUrl() + "?size=512) " +
									"[1024px](" + userCmdArg.getValue().getEffectiveAvatarUrl() + "?size=1024) "
							)
							.build()
					).queue();
				}
				else if(userId.getValue() != null){
					commandEvent.getEvent().getJDA().retrieveUserById(userId.getValue())
						.queue(
							user -> interactionHook.editOriginalEmbeds(
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
							error -> interactionHook.editOriginalEmbeds(
								embedBuilder
									.setColor(Color.RED)
									.setDescription(translationPackage.getTranslation(getClass(), "error.msg"))
									.build()
							).queue()
						);
				}
				else{
					interactionHook.editOriginalEmbeds(
						embedBuilder
							.setColor(Color.GREEN)
							.setImage(commandEvent.getEvent().getUser().getEffectiveAvatarUrl() + "?size=512")
							.setDescription(
								"[256px](" + commandEvent.getEvent().getUser().getEffectiveAvatarUrl() + "?size=256) " +
									"[512px](" + commandEvent.getEvent().getUser().getEffectiveAvatarUrl() + "?size=512) " +
									"[1024px](" + commandEvent.getEvent().getUser().getEffectiveAvatarUrl() + "?size=1024) "
							)
							.build()
					).queue();
				}
			}
		);
	}

}
