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

package de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.anime;

import de.netbeacon.purrito.qol.typewrap.ContentType;
import de.netbeacon.purrito.qol.typewrap.ImageType;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.purrito.PurrBotAPIWrapper;

import java.util.List;

public class Anime_Kiss extends AnimeTask{

	public Anime_Kiss(int pos){
		super(pos, "send when xenia is supposed to kiss", List.of(

			),
			(content, pair) -> {
				var member = pair.getValue1();
				var textChannel = pair.getValue2();
				PurrBotAPIWrapper.getInstance().getAnimeImageUrlOf(ImageType.SFW.KISS, ContentType.GIF)
					.async(url -> {
						textChannel.sendMessage(
							EmbedBuilderFactory.getDefaultEmbed("@" + member.getAsMention()).setImage(url).build()
						).queue();
					}, e -> {
						textChannel.sendMessage(
							"\u2764\uFE0F"
						).queue();
					});
				return null;
			}
		);
	}

}
