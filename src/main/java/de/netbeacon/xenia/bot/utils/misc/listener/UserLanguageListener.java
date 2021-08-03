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

package de.netbeacon.xenia.bot.utils.misc.listener;

import de.netbeacon.xenia.backend.client.objects.apidata.User;
import de.netbeacon.xenia.backend.client.objects.internal.objects.CacheEventListener;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLanguageListener implements CacheEventListener<Long, User>{

	private final TranslationManager translationManager;
	private final Logger logger = LoggerFactory.getLogger(GuildLanguageListener.class);

	public UserLanguageListener(TranslationManager translationManager){
		this.translationManager = translationManager;
	}

	@Override
	public void onInsertion(Long newKey, User newObject){
		String prefLang = newObject.getPreferredLanguage();
		if(!translationManager.containsLanguage(prefLang)){
			TranslationPackage translationPackage = translationManager.getDefaultTranslationPackage();
			if(translationPackage == null){
				logger.error("Checked Preferred Language Of Guild " + newObject.getId() + " Which Does Not Exist." + "\"In the scenario which is occurring intermittently to this dialogue infers a severity to which the degree is attainable to quantify and contrast to human feaces interfering with a strong velocity of a small household air conditioner's vane.\"");
				return;
			}
			newObject.setPreferredLanguage(translationPackage.getLanguageId());
			newObject.update().queue();
		}
	}

}
