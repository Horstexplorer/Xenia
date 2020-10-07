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

package de.netbeacon.xenia.backend.client.objects.cache;

import de.netbeacon.xenia.backend.client.objects.external.License;
import de.netbeacon.xenia.backend.client.objects.internal.BackendProcessor;

public class LicenseCache extends Cache<License> {

    public LicenseCache(BackendProcessor backendProcessor) {
        super(backendProcessor);
    }

    public License get(long guildId){
        License license = getFromCache(guildId);
        if(license != null){
            return license;
        }
        license = new License(getBackendProcessor(), guildId);
        license.get();
        addToCache(guildId, license);
        return license;
    }

    public void remove(long guildId){
        removeFromCache(guildId);
    }
}