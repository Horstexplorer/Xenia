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

package de.netbeacon.xenia.backend.client.objects.external;

import de.netbeacon.utils.json.serial.JSONSerializationException;
import de.netbeacon.xenia.backend.client.objects.internal.BackendProcessor;
import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataObject;
import org.json.JSONObject;

import java.util.List;

public class User extends APIDataObject {

    private final long userId;

    private long creationTimestamp;
    private String internalRole;
    private String preferredLanguage;

    public User(BackendProcessor backendProcessor, long userId) {
        super(backendProcessor, List.of("data", "users", String.valueOf(userId)));
        this.userId = userId;
    }

    public long getId(){
        return userId;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getInternalRole() {
        return internalRole;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setInternalRole(String internalRole){
        this.internalRole = internalRole;
        update();
    }

    public void setPreferredLanguage(String language){
        this.preferredLanguage = language;
        update();
    }

    @Override
    public JSONObject asJSON() throws JSONSerializationException {
        return new JSONObject()
                .put("userId", userId)
                .put("creationTimestamp", creationTimestamp)
                .put("internalRole", internalRole)
                .put("preferredLanguage", preferredLanguage);
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
        if(jsonObject.getLong("userId") != userId){
            throw new JSONSerializationException("Object Do Not Match");
        }
        this.creationTimestamp = jsonObject.getLong("creationTimestamp");
        this.internalRole = jsonObject.getString("internalRole");
        this.preferredLanguage = jsonObject.getString("preferredLanguage");
    }
}
