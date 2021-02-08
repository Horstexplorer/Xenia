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

package de.netbeacon.d43z.one.objects.base;


import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CombinedContextPool implements IContextPool {

    private final UUID uuid;
    private final String description;
    private final List<IContextPool> iContextPools;

    public CombinedContextPool(String description, List<IContextPool> iContextPools){
        this.uuid = UUID.randomUUID();
        this.description = description;
        this.iContextPools = iContextPools;
    }

    public CombinedContextPool(JSONObject jsonObject, List<IContextPool> iContextPools){
        this.uuid = UUID.fromString(jsonObject.getString("combinedContextPoolId"));
        this.description = jsonObject.getString("description");
        JSONArray jsonArray = jsonObject.getJSONArray("extending");
        this.iContextPools = new LinkedList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            String extending = jsonArray.getString(i);
            Optional<IContextPool> optional = iContextPools.stream().filter(iContextPool -> iContextPool.getUUID().toString().equals(extending)).findFirst();
            if(optional.isEmpty()){
                throw new RuntimeException("List Does Not Contain Correct Context Pools");
            }
            this.iContextPools.add(optional.get());
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<ContentContext> getContentContexts() {
        List<ContentContext> contentContexts = new LinkedList<>();
        iContextPools.forEach(iContextPool -> contentContexts.addAll(iContextPool.getContentContexts()));
        return contentContexts;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public JSONObject asJSON() throws JSONSerializationException {
        JSONArray jsonArray = new JSONArray();
        iContextPools.forEach(iContextPool -> jsonArray.put(iContextPool.getUUID().toString()));
        return new JSONObject()
                .put("combinedContextPoolId", uuid.toString())
                .put("description", description)
                .put("extending", jsonArray);
    }



    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
        throw new JSONSerializationException("Bad Method. Use Correct Constructor Instead");
    }
}
