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

import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CMDMember extends Command {

    public CMDMember() {
        super("member", "Show information about a given member (or yourself)", new CommandCooldown(CommandCooldown.Type.User, 3000), null, null, null);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        GuildMessageReceivedEvent event = commandEvent.getEvent();
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        User user;
        Member bMember;
        if(mentionedUsers.isEmpty()){user = event.getAuthor();}else{user = mentionedUsers.get(0);}
        if(!user.equals(event.getAuthor())){
            try{
                bMember = new Member(commandEvent.backendDataPack().getbGuild().getMemberCache().getBackendProcessor(), event.getGuild().getIdLong(), user.getIdLong());
                bMember.get();
            }catch (Exception e){
                EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Member Info: "+event.getAuthor().getName(), event.getJDA().getSelfUser(), event.getAuthor())
                        .addField("Error", "Member Not Found On Backend", false);
                event.getChannel().sendMessage(embedBuilder.build()).queue(m->m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
        }else{ bMember = commandEvent.backendDataPack().getbMember(); }
        StringBuilder stringBuilder = new StringBuilder();
        for(long l : bMember.getRoleIds()){
            stringBuilder.append(commandEvent.backendDataPack().getbGuild().getRoleCache().get(l).getRoleName()).append(" ");
        }
        String roles = stringBuilder.toString();
        if(roles.isBlank()){roles = "none";}
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Member Info: "+event.getAuthor().getName(), event.getJDA().getSelfUser(), event.getAuthor())
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("ID", user.getId(), true)
                .addField("Name", user.getName(), true)
                .addField("Avatar Url", "[Link]("+user.getEffectiveAvatarUrl()+")", true)
                .addField("Roles",roles, false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
