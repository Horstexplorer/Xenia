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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.interactions.records.*;
import de.netbeacon.xenia.bot.interactions.type.buttons.ButtonRegEntryComponent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;


public class CMDBurger extends AdminCommand{

	public CMDBurger(){
		super("burger", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		var channel = commandEvent.getEvent().getChannel();

		channel.sendMessage("\uD83C\uDF54").queue(message -> {
			ButtonRegEntryComponent burger = new ButtonRegEntryComponent(
				Origin.CUSTOM(message.getIdLong(), channel.getIdLong()),
				Accessor.ANY,
				Activations.CUSTOM(10),
				TimeoutPolicy.NONE,
				ActionHandler.CUSTOM((buttonClickEvent) -> {
					buttonClickEvent.reply("-1 " + "\uD83C\uDF54 taken by " + buttonClickEvent.getUser().getAsTag()).queue();
				}),
				ExceptionHandler.NONE,
				DeactivationMode.ALL
			);
			commandEvent.getToolBundle().componentInteractionRegistry().register(burger);

			ButtonRegEntryComponent salad = new ButtonRegEntryComponent(
				Origin.CUSTOM(message.getIdLong(), channel.getIdLong()),
				Accessor.ANY,
				Activations.ONCE,
				TimeoutPolicy.NONE,
				ActionHandler.CUSTOM((buttonClickEvent) -> {
					buttonClickEvent.reply("The salad wasn't free. No more burgers. " + buttonClickEvent.getUser().getAsTag() + " ruined it.").queue();
				}),
				ExceptionHandler.NONE,
				DeactivationMode.ALL
			);
			commandEvent.getToolBundle().componentInteractionRegistry().register(salad);


			Message messageNew = new MessageBuilder()
				.append("Free burgers!")
				.setActionRows(
					ActionRow.of(
						burger.getButton(ButtonStyle.PRIMARY, " ").withEmoji(Emoji.fromUnicode("\uD83C\uDF54")),
						salad.getButton(ButtonStyle.PRIMARY, " ").withEmoji(Emoji.fromUnicode("\uD83E\uDD57"))
					)
				).build();
			message.editMessage(messageNew).queue();
		});
	}

}
