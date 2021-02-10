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

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.CHANNEL_ID_OPTIONAL;

public class CMDSetup extends Command {

    public CMDSetup() {
        super("setup", new CommandCooldown(CommandCooldown.Type.Guild, 2000),
                new HashSet<>(List.of(Permission.MANAGE_CHANNEL)),
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(CHANNEL_ID_OPTIONAL)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) {
        try{
            CmdArg<Mention> channelMention = args.getByIndex(0);
            // check if already a channel has been set up
            Channel existingChatChannel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().getAllAsList().stream()
                    .filter(channel -> channel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE)).findFirst().orElse(null);
            if(channelMention.getValue() == null){
                if(existingChatChannel != null){
                    // deactivate chatbot
                    Channel.D43Z1Settings d43Z1Settings = new Channel.D43Z1Settings(existingChatChannel.getD43Z1Settings().getValue());
                    d43Z1Settings.unset(Channel.D43Z1Settings.Settings.ACTIVE);
                    existingChatChannel.setD43Z1Settings(d43Z1Settings);
                    commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage,  translationPackage.getTranslation(getClass(), "response.success.deactivated"))).queue();
                }else{
                    // create new text channel to be used to chat with the bot
                    commandEvent.getEvent().getGuild()
                            .createTextChannel(translationPackage.getTranslation(getClass(), "chat.default.name"))
                            .setNSFW(true)
                            .queue(textChannel -> {
                                Channel channel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(textChannel.getIdLong());
                                Channel.D43Z1Settings d43Z1Settings = new Channel.D43Z1Settings(0);
                                d43Z1Settings.set(Channel.D43Z1Settings.Settings.ACTIVE);
                                channel.lSetD43Z1Settings(d43Z1Settings);
                                channel.updateAsync(true);
                                commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage,  translationPackage.getTranslation(getClass(), "response.success.msg"))).queue();
                            }, failed -> {
                                commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage,  translationPackage.getTranslation(getClass(), "response.error.create.failed"))).queue();
                            });
                }
            }else{
                if(existingChatChannel != null && existingChatChannel.getChannelId() == channelMention.getValue().getId()){
                    // deactivate chatbot
                    Channel.D43Z1Settings d43Z1Settings = new Channel.D43Z1Settings(existingChatChannel.getD43Z1Settings().getValue());
                    d43Z1Settings.unset(Channel.D43Z1Settings.Settings.ACTIVE);
                    existingChatChannel.setD43Z1Settings(d43Z1Settings);
                    commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "response.success.deactivated"))).queue();
                }else{
                    // activate for the given channel
                    TextChannel textChannel = commandEvent.getEvent().getGuild().getTextChannelById(channelMention.getValue().getId());
                    if(textChannel == null){
                        throw new RuntimeException();
                    }
                    textChannel.getManager().setNSFW(true).queue(success -> {
                        Channel channel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(textChannel.getIdLong());
                        Channel.D43Z1Settings d43Z1Settings = new Channel.D43Z1Settings(0);
                        d43Z1Settings.set(Channel.D43Z1Settings.Settings.ACTIVE);
                        channel.lSetD43Z1Settings(d43Z1Settings);
                        channel.updateAsync();
                        commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage,  translationPackage.getTranslation(getClass(), "response.success.msg"))).queue();
                    }, failed -> {
                        commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage,  translationPackage.getTranslation(getClass(), "response.error.create.failed"))).queue();
                    });
                }
            }
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage,  translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
        }
    }
}