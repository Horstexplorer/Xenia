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

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;

import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager {

    private static TranslationManager instance;

    private final ConcurrentHashMap<String, TranslationPackage> translationPackages = new ConcurrentHashMap<>();

    private TranslationManager(){

    }

    public static synchronized TranslationManager getInstance(boolean initIfNeeded){
        if(instance == null && initIfNeeded){
            instance = new TranslationManager();
        }
        return instance;
    }

    public static TranslationManager getInstance(){
        return instance;
    }

    public boolean containsLanguage(String languageId){
        return translationPackages.containsKey(languageId.toLowerCase());
    }

    public TranslationPackage getDefault(){
        return translationPackages.values().stream().filter(TranslationPackage::isDefault).findFirst().orElse(null);
    }

    public TranslationPackage getTranslationPackage(String languageId){
        return translationPackages.get(languageId.toLowerCase());
    }

    public TranslationPackage getTranslationPackage(Guild guild, Member member){
        return null;
    }

}
