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

package de.netbeacon.xenia.bot.commands.structure.settings.guild.info;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CMDGuildInfo extends Command {

    public CMDGuildInfo() {
        super("list", "Display information about yourself / another member", new CommandCooldown(CommandCooldown.Type.User, 2500),null, null, null);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        Guild guild = commandEvent.backendDataPack().getbGuild();
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Guild Info: "+event.getGuild().getName(), event.getJDA().getSelfUser(), event.getAuthor())
                .setThumbnail(event.getGuild().getIconUrl())
                .addField("ID", event.getGuild().getId(), true)
                .addField("Name", event.getGuild().getName(), true)
                .addField("Owner", event.getGuild().getOwnerId(), true)
                .addField("Preferred Language", guild.getPreferredLanguage(), true)
                .addField("Roles", "-", false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
