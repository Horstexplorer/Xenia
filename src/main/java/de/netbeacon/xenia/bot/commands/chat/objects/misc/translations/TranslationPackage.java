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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationPackage{

	private final String languageId;
	private final String languageName;
	private final String languageDescription;
	private final boolean isDefault;
	private final ConcurrentHashMap<String, String> translations = new ConcurrentHashMap<>();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public TranslationPackage(JSONObject jsonObject){
		languageId = jsonObject.getString("languageId").toLowerCase();
		languageName = jsonObject.getString("languageName");
		languageDescription = jsonObject.getString("languageDescription");
		isDefault = jsonObject.getBoolean("isDefault");
		// build keys
		translations.putAll(toSimpleAccessors(null, jsonObject.getJSONObject("translations")));
	}

	private Map<String, String> toSimpleAccessors(String prefix, Object o){
		Map<String, String> map = new HashMap<>();
		if(o instanceof JSONObject){
			for(String key : ((JSONObject) o).keySet()){
				map.putAll(toSimpleAccessors( prefix == null ? "" : prefix+"."+key, ((JSONObject) o).get(key)));
			}
		}else{
			map.put(prefix, o.toString());
		}
		return map;
	}

	public String getLanguageId(){
		return languageId;
	}

	public String getLanguageName(){
		return languageName;
	}

	public String getLanguageDescription(){
		return languageDescription;
	}

	public boolean isDefault(){
		return isDefault;
	}

	public String getTranslation(Class<?> clazz, String key){
		return getTranslation(clazz.getName() + "." + key);
	}

	public String getTranslation(String key){
		String translation = translations.get(key);
		if(translation == null){
			translation = "Missing translation for: " + key;
		}
		return translation;
	}

	public String getTranslationWithPlaceholders(Class<?> clazz, String key, Object... objects){
		return getTranslationWithPlaceholders(clazz.getName() + "." + key, objects);
	}

	public String getTranslationWithPlaceholders(String key, Object... objects){
		int i = 0;
		String translation = translations.get(key);
		if(translation == null){
			translation = "Missing translation for: " + key;
			return translation;
		}
		for(Object o : objects){
			String placeholder = "%" + i + "%";
			translation = translation.replace(placeholder, o.toString());
			i++;
		}
		return translation;
	}

}
