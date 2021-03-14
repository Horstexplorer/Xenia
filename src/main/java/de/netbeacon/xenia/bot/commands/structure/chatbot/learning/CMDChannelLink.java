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

package de.netbeacon.xenia.bot.commands.structure.chatbot.learning;

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.CB_CHANNEL_ENABLE;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.CB_CHANNEL_ID_OPTIONAL;

public class CMDChannelLink extends Command {

    public CMDChannelLink() {
        super("channel_link", new CommandCooldown(CommandCooldown.Type.Guild, 10000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(CB_CHANNEL_ENABLE, CB_CHANNEL_ID_OPTIONAL)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        try{
            CmdArg<Boolean> modeArg = args.getByIndex(0);
            CmdArg<Mention> channelMentionArg = args.getByIndex(1);

            Channel channel = commandEvent.getBackendDataPack().getbChannel();
            if(channelMentionArg.getValue() != null){
                TextChannel textChannel = commandEvent.getEvent().getGuild().getTextChannelById(channelMentionArg.getValue().getId());
                if(textChannel == null){
                    throw new IllegalArgumentException();
                }
                channel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(channel.getChannelId(), false);
            }
            if(modeArg.getValue()
                    && commandEvent.getBackendDataPack().getbGuild().getChannelCache().getAllAsList().stream().filter(channel1 -> channel1.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING)).collect(Collectors.toList()).size()
                    >= channel.getBackendProcessor().getBackendClient().getLicenseCache().get(channel.getGuildId()).getPerk_CHANNEL_D43Z1_SELFLEARNING_C()
            ){
                throw new IllegalArgumentException();
            }
            Channel.D43Z1Settings d43Z1ChannelSettings = channel.getD43Z1Settings();
            Channel.D43Z1Settings newD43Z1ChannelSettings = new Channel.D43Z1Settings(d43Z1ChannelSettings.getValue());
            if(modeArg.getValue()){
                newD43Z1ChannelSettings.set(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING);
            }else{
                newD43Z1ChannelSettings.unset(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING);
            }
            channel.setD43Z1Settings(newD43Z1ChannelSettings);
            commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "success.msg"))).queue();
        }catch (IllegalArgumentException e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslation(getClass(), "error.invalid.arg.msg"))).queue();
        }
    }
}
