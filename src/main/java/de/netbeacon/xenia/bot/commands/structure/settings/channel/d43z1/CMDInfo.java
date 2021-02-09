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

package de.netbeacon.xenia.bot.commands.structure.settings.channel.d43z1;

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class CMDInfo extends Command {
    public CMDInfo() {
        super("info", new CommandCooldown(CommandCooldown.Type.Guild, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                null
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) {
        try{
            Channel channel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().getAllAsList()
                    .stream().filter(channel1 -> channel1.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE)).findFirst().orElse(null);
            if(channel == null){
                commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.notfound"))).queue();
                return;
            }
            commandEvent.getEvent().getChannel().sendMessage(
                    EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.success.title"), commandEvent.getEvent().getJDA().getSelfUser(), commandEvent.getEvent().getAuthor())
                            .addField(translationPackage.getTranslation(getClass(), "response.success.field.1.title"), Arrays.toString(channel.getD43Z1Settings().getBits().toArray()), false)
                            .addField(translationPackage.getTranslation(getClass(), "response.success.field.2.title"), channel.getD43Z1CustomContextPoolUUID().toString(), false)
                            .addField(translationPackage.getTranslation(getClass(), "response.success.field.3.title"), channel.getMetaChannelName()+"("+channel.getChannelId()+")",false)
                            .build()
            ).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
        }
    }
}
