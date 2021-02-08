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

import de.netbeacon.d43z.one.algo.LiamusJaccard;
import de.netbeacon.d43z.one.objects.bp.*;
import de.netbeacon.d43z.one.objects.settings.StaticSettings;
import de.netbeacon.utils.json.serial.IJSONSerializable;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import org.json.JSONObject;

import java.util.UUID;

public class Content implements IIdentifiable, IContentprovider, ISimilarity, ILJEvaluable, IWeightable, IJSONSerializable {

    private UUID uuid;
    private String content;
    private LiamusJaccard.BitArray64 contentHash;
    private float weight = 1.0F;

    public Content(){}

    public Content(String content){
        this.uuid = UUID.randomUUID();
        this.content = content;
        this.contentHash = LiamusJaccard.hashString(content, StaticSettings.EVAL_LIAMUS_JACCARD_NGRAM);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public LiamusJaccard.BitArray64 getContentHash() {
        return contentHash;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(float f) {
        this.weight = Math.max(f, 0);
    }

    @Override
    public JSONObject asJSON() throws JSONSerializationException {
        return new JSONObject()
                .put("contentId", uuid.toString())
                .put("contentWeight", weight)
                .put("content", content);
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
        this.uuid = UUID.fromString(jsonObject.getString("contentId"));
        this.weight = jsonObject.getFloat("contentWeight");
        this.content = jsonObject.getString("content");
        this.contentHash = LiamusJaccard.hashString(content, StaticSettings.EVAL_LIAMUS_JACCARD_NGRAM);
    }
}
