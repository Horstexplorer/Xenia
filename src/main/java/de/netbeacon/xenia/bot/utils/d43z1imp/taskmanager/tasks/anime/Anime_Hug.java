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

import de.netbeacon.d43z.one.algo.LiamusPattern;
import de.netbeacon.d43z.one.objects.imp.trigger.LPTrigger;
import de.netbeacon.purrito.qol.typewrap.ContentType;
import de.netbeacon.purrito.qol.typewrap.ImageType;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.purrito.PurrBotAPIWrapper;

import java.util.List;

public class Anime_Hug extends AnimeTask{

	public Anime_Hug(int pos){
		super(pos, "send when xenia is supposed to hug", List.of(
				new LPTrigger("please_hug", LiamusPattern.compile("\0 please \0 hug \0"), LPTrigger.defaultToBool(), LPTrigger.defaultToFloat()),
				new LPTrigger("give_hug", LiamusPattern.compile("\0 give \0 hug \0"), LPTrigger.defaultToBool(), LPTrigger.defaultToFloat()),
				new LPTrigger("need_hug", LiamusPattern.compile("\0 need \0 hug \0"), LPTrigger.defaultToBool(), LPTrigger.defaultToFloat())
			),
			(content, pair) -> {
				var member = pair.getValue1();
				var textChannel = pair.getValue2();
				PurrBotAPIWrapper.getInstance().getAnimeImageUrlOf(ImageType.SFW.HUG, ContentType.RANDOM)
					.async(url -> textChannel.sendMessageEmbeds(
						EmbedBuilderFactory.getDefaultEmbed("@" + member.getUser().getAsTag()).setImage(url).build()
					).queue(), e -> textChannel.sendMessage(
						"\uD83E\uDD17"
					).queue());
				return null;
			}
		);
	}

}
