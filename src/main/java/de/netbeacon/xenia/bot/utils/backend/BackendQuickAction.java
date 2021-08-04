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

package de.netbeacon.xenia.bot.utils.backend;

import de.netbeacon.xenia.backend.client.objects.apidata.Channel;
import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.Member;
import de.netbeacon.xenia.backend.client.objects.apidata.User;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

public class BackendQuickAction{

	public static class Update{

		public static void execute(User bUser, net.dv8tion.jda.api.entities.User jUser, boolean async, boolean securityOverride){

			bUser.lSetMetaData(jUser.getAsTag(), jUser.getEffectiveAvatarUrl());

			if(async){
				bUser.update(securityOverride).queue();
			}
			else{
				bUser.update(securityOverride).execute();
			}
		}

		public static void execute(Member bMember, net.dv8tion.jda.api.entities.Member jMember, boolean async, boolean securityOverride){

			bMember.lSetMetaData(jMember.getEffectiveName(), jMember.hasPermission(Permission.ADMINISTRATOR), jMember.isOwner());

			if(async){
				bMember.update(securityOverride).queue();
			}
			else{
				bMember.update(securityOverride).execute();
			}
		}

		public static void execute(Guild bGuild, net.dv8tion.jda.api.entities.Guild jGuild, boolean async, boolean securityOverride){

			bGuild.lSetMetaData(jGuild.getName(), jGuild.getIconUrl());

			if(async){
				bGuild.update(securityOverride).queue();
			}
			else{
				bGuild.update(securityOverride).execute();
			}
		}

		public static void execute(Channel bChannel, TextChannel jChannel, boolean async, boolean securityOverride){
			bChannel.lSetMetaData(jChannel.getName(), jChannel.getTopic());

			Channel.ChannelFlags channelFlags = new Channel.ChannelFlags(0);
			if(jChannel.isNews()){
				channelFlags.set(Channel.ChannelFlags.Flags.NEWS);
			}
			if(jChannel.isNSFW()){
				channelFlags.set(Channel.ChannelFlags.Flags.NSFW);
			}
			bChannel.lSetChannelFlags(channelFlags);

			if(async){
				bChannel.update(securityOverride).queue();
			}
			else{
				bChannel.update(securityOverride).execute();
			}
		}

	}

}
