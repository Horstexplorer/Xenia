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

package de.netbeacon.xenia.bot.utils.embedfactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

public class EmbedBuilderFactory{

	/**
	 * Returns an embed with a given title and the self user as author
	 *
	 * @param title title
	 *
	 * @return embed builder
	 */
	public static EmbedBuilder getDefaultEmbed(String title){
		return new EmbedBuilder()
			.setTitle(title)
			.setColor(Color.CYAN);
	}

	/**
	 * Returns an embed with a given title and the self user as author and a user as requester
	 *
	 * @param title     title
	 * @param requester requester
	 *
	 * @return embed builder
	 */
	public static EmbedBuilder getDefaultEmbed(String title, User requester){
		return new EmbedBuilder()
			.setColor(Color.CYAN)
			.setTitle(title)
			.setFooter("Requested By " + requester.getAsTag(), requester.getAvatarUrl());
	}

}
