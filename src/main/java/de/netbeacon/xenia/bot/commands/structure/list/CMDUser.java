/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.commands.structure.list;

import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CMDUser extends Command {

    public CMDUser() {
        super("user", "Show information about yourself", new CommandCooldown(CommandCooldown.Type.User, 3000), null, null, null);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        CommandEvent.BackendDataPack backendDataPack = commandEvent.backendDataPack();
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("User Info: "+event.getAuthor().getName(), event.getJDA().getSelfUser(), event.getAuthor())
                .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
                .addField("ID", event.getAuthor().getId(), true)
                .addField("Name", event.getAuthor().getName(), true)
                .addField("Avatar Url", "[Link]("+event.getAuthor().getEffectiveAvatarUrl()+")", true)
                .addField("Preferred Language", backendDataPack.getbUser().getPreferredLanguage(), true)
                .addField("Internal Role", backendDataPack.getbUser().getInternalRole(), true);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
