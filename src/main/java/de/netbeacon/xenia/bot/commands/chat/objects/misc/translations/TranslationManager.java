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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.translations;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TranslationManager{

	private static final Logger logger = LoggerFactory.getLogger(TranslationManager.class);
	private static TranslationManager instance;
	private final ConcurrentHashMap<String, TranslationPackage> translationPackages = new ConcurrentHashMap<>();

	private TranslationManager() throws IOException{
		JSONArray index = new JSONArray(IOUtils.toString(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("translations.index.json")), StandardCharsets.UTF_8));
		for(int i = 0; i < index.length(); i++){
			JSONObject content = new JSONObject(IOUtils.toString(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("translations/" + index.getString(i) + ".json")), StandardCharsets.UTF_8));
			TranslationPackage translationPackage = new TranslationPackage(content);
			translationPackages.put(translationPackage.getLanguageId(), translationPackage);
		}
		logger.info("Loaded Languages: " + Arrays.toString(translationPackages.values().stream().map(TranslationPackage::getLanguageId).toArray()));
	}

	public static synchronized TranslationManager getInstance(boolean initIfNeeded){
		if(instance == null && initIfNeeded){
			try{
				instance = new TranslationManager();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return instance;
	}

	public static TranslationManager getInstance(){
		return instance;
	}

	public boolean containsLanguage(String languageId){
		return translationPackages.containsKey(languageId.toLowerCase());
	}

	public TranslationPackage getDefaultTranslationPackage(){
		return translationPackages.values().stream().filter(TranslationPackage::isDefault).findFirst().orElse(null);
	}

	public TranslationPackage getTranslationPackage(String languageId){
		return translationPackages.get(languageId.toLowerCase());
	}

	public TranslationPackage getTranslationPackage(Guild guild, Member member){
		if(guild.getSettings().has(Guild.GuildSettings.Settings.ENFORCE_LANGUAGE)){
			return getTranslationPackage(guild.getPreferredLanguage());
		}
		else{
			return getTranslationPackage(member.getUser().getPreferredLanguage());
		}
	}

	public List<String> getLanguageIds(){
		return translationPackages.values().stream().map(TranslationPackage::getLanguageId).collect(Collectors.toList());
	}

}
