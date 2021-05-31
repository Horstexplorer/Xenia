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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes;

import net.dv8tion.jda.api.entities.Message;

import java.util.regex.Matcher;

public class Mention{

	private final long id;
	private final Message.MentionType mentionType;

	private Mention(long id, Message.MentionType mentionType){
		this.id = id;
		this.mentionType = mentionType;
	}

	public static Mention parse(String string){
		Matcher m = Message.MentionType.CHANNEL.getPattern().matcher(string);
		if(m.matches()){
			return new Mention(Long.parseLong(m.group(1)), Message.MentionType.CHANNEL);
		}
		m = Message.MentionType.ROLE.getPattern().matcher(string);
		if(m.matches()){
			return new Mention(Long.parseLong(m.group(1)), Message.MentionType.ROLE);
		}
		m = Message.MentionType.USER.getPattern().matcher(string);
		if(m.matches()){
			return new Mention(Long.parseLong(m.group(1)), Message.MentionType.USER);
		}
		m = Message.MentionType.EMOTE.getPattern().matcher(string);
		if(m.matches()){
			return new Mention(Long.parseLong(m.group(2)), Message.MentionType.EMOTE);
		}
		return null;
	}

	public long getId(){
		return id;
	}

	public Message.MentionType getMentionType(){
		return mentionType;
	}

}
