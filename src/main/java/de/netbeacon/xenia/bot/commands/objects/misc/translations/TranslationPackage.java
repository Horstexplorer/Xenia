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

package de.netbeacon.xenia.bot.commands.objects.misc.translations;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class TranslationPackage {

    private final String languageId;
    private final String languageName;
    private final String languageDescription;
    private final boolean isDefault;
    private final ConcurrentHashMap<String, String> translations = new ConcurrentHashMap<>();

    public TranslationPackage(JSONObject jsonObject){
        languageId = jsonObject.getString("languageId").toLowerCase();
        languageName = jsonObject.getString("languageName");
        languageDescription = jsonObject.getString("languageDescription");
        isDefault = jsonObject.getBoolean("isDefault");
        JSONObject transl = jsonObject.getJSONObject("translations");
        transl.keySet().forEach(key -> translations.put(key, transl.getString(key)));
    }

    public String getLanguageId() {
        return languageId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageDescription() {
        return languageDescription;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getTranslation(Class<?> clazz, String key){
        return getTranslation(clazz.getName()+"."+key);
    }

    public String getTranslation(String key) {
        String translation = translations.get(key);
        if(translation == null){
            translation = "Missing translation for: "+key;
        }
        return translation;
    }

    public String getTranslationWithPlaceholders(Class<?> clazz, String key, Object...objects) {
        return getTranslationWithPlaceholders(clazz.getName()+"."+key, objects);
    }

    public String getTranslationWithPlaceholders(String key, Object...objects) {
        int i = 0;
        String translation = translations.get(key);
        if(translation == null){
            translation = "Missing translation for: "+key;
            return translation;
        }
        for(Object o : objects){
            String placeholder = "%"+i+"%";
            translation = translation.replace(placeholder, o.toString());
            i++;
        }
        return translation;
    }
}
