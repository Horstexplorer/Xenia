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

package de.netbeacon.xenia.bot.commands.chat.structure.chatbot.learning;

import de.netbeacon.xenia.backend.client.objects.apidata.Channel;
import de.netbeacon.xenia.backend.client.objects.apidata.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashSet;
import java.util.List;

import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.CB_CHANNEL_ENABLE;
import static de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgDefStatics.CB_CHANNEL_ID_OPTIONAL;

public class CMDChannelLink extends Command{

	public CMDChannelLink(){
		super("channel_link", false, new CommandCooldown(CommandCooldown.Type.Guild, 10000),
			null,
			new HashSet<>(List.of(Permission.MANAGE_SERVER)),
			new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
			List.of(CB_CHANNEL_ENABLE, CB_CHANNEL_ID_OPTIONAL)
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		try{
			CmdArg<Boolean> modeArg = args.getByIndex(0);
			CmdArg<Mention> channelMentionArg = args.getByIndex(1);

			Channel channel = commandEvent.getBackendDataPack().channel();
			if(channelMentionArg.getValue() != null){
				TextChannel textChannel = commandEvent.getEvent().getGuild().getTextChannelById(channelMentionArg.getValue().getId());
				if(textChannel == null){
					throw new IllegalArgumentException();
				}
				channel = commandEvent.getBackendDataPack().guild().getChannelCache().retrieve(textChannel.getIdLong(), false).execute();
			}
			var v = commandEvent.getBackendDataPack().guild().getChannelCache().getAllAsList().stream().map(Channel::getD43Z1Settings).filter(settings -> settings.has(Channel.D43Z1Settings.Settings.ENABLE_SELF_LEARNING)).count();
			if(modeArg.getValue()
				&& v
				>= channel.getBackendProcessor().getBackendClient().getLicenseCache().retrieve(channel.getGuildId(), true).execute().getPerk_CHANNEL_D43Z1_SELFLEARNING_C()
			){
				throw new IllegalArgumentException();
			}
			Channel.D43Z1Settings newD43Z1ChannelSettings = new Channel.D43Z1Settings(channel.getD43Z1Settings().getValue());
			if(modeArg.getValue()){
				newD43Z1ChannelSettings.set(Channel.D43Z1Settings.Settings.ENABLE_SELF_LEARNING);
			}
			else{
				newD43Z1ChannelSettings.unset(Channel.D43Z1Settings.Settings.ENABLE_SELF_LEARNING);
			}
			channel.setD43Z1Settings(newD43Z1ChannelSettings);
			commandEvent.getToolBundle().contextPoolManager().getPoolFor(commandEvent.getBackendDataPack().guild(), true);
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onSuccess(translationPackage, translationPackage.getTranslation(getClass(), "success.msg"))).queue();
		}
		catch(IllegalArgumentException e){
			commandEvent.getEvent().getChannel().sendMessageEmbeds(onError(translationPackage, translationPackage.getTranslation(getClass(), "error.invalid.arg.msg"))).queue();
		}
	}

}
