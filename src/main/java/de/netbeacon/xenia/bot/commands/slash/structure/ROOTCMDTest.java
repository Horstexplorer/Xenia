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

package de.netbeacon.xenia.bot.commands.slash.structure;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import java.awt.*;

public class ROOTCMDTest extends Command {

    public ROOTCMDTest() {

        super("test", "Used to check if implementation works", new CommandCooldown(CommandCooldown.Type.User, 1000),
                null,
                null,
                null,
                (CommandUpdateAction.OptionData) null
        );
    }

    @Override
    public void onExecution(CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Test")
                .setDescription("This Is A Test")
                .setColor(Color.WHITE)
                .addField("Has Guild", String.valueOf(commandEvent.getEvent().getGuild() != null), true)
                .addField("Has Member", String.valueOf(commandEvent.getEvent().getMember() != null), true)
                .addField("Estimate Processing Time", String.valueOf(commandEvent.getEstimatedProcessingTime()), true);
        commandEvent.getEvent()
                .reply(
                        embedBuilder.build()
                ).queue();
    }
}
