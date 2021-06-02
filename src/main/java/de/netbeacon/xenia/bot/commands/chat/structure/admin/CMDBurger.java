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
import de.netbeacon.xenia.bot.interactions.buttons.ButtonRegEntry;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.ButtonStyle;


public class CMDBurger extends AdminCommand{

	public CMDBurger(){
		super("burger", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		var guild = commandEvent.getEvent().getGuild();
		var channel = commandEvent.getEvent().getChannel();

		channel.sendMessage("\uD83C\uDF54").queue(message -> {
			ButtonRegEntry entry = new ButtonRegEntry(
				ButtonRegEntry.AllowedOrigin.CUSTOM(message.getIdLong(), channel.getIdLong()),
				ButtonRegEntry.AllowedAccessor.ANY,
				ButtonRegEntry.AllowedActivations.LIMIT(3),
				ButtonRegEntry.TimeoutPolicy.NONE,
				ButtonRegEntry.ActionHandler.CUSTOM((buttonClickEvent) -> {
					buttonClickEvent.reply("-1 "+"\uD83C\uDF54").queue();
				}),
				ButtonRegEntry.ExceptionHandler.CUSTOM((exception, event) -> {})
			);
			commandEvent.getButtonManager().register(entry);
			Message messageNew = new MessageBuilder()
				.append("Free burgers!")
				.setActionRows(
					ActionRow.of(
						entry.getButton(ButtonStyle.PRIMARY, " ").withEmoji(Emoji.ofUnicode("\uD83C\uDF54"))
					)
				).build();
			message.editMessage(messageNew).queue();
		});
	}

}
