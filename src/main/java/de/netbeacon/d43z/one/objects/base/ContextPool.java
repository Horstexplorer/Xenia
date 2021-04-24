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
import java.util.UUID;

public class ContextPool implements IContextPool{

	private UUID uuid;
	private String description;
	private List<ContentContext> contentContexts;

	public ContextPool(){}

	public ContextPool(String description, List<ContentContext> contentContexts){
		this.uuid = UUID.randomUUID();
		this.description = description;
		this.contentContexts = contentContexts;
	}

	@Override
	public UUID getUUID(){
		return uuid;
	}

	@Override
	public String getDescription(){
		return description;
	}

	@Override
	public List<ContentContext> getContentContexts(){
		return contentContexts;
	}

	@Override
	public JSONObject asJSON() throws JSONSerializationException{
		JSONArray jsonArray = new JSONArray();
		contentContexts.forEach(contentContext -> jsonArray.put(contentContext.asJSON()));
		return new JSONObject()
			.put("contextPoolId", uuid.toString())
			.put("description", description)
			.put("contexts", jsonArray);
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONSerializationException{
		this.uuid = UUID.fromString(jsonObject.getString("contextPoolId"));
		this.description = jsonObject.getString("description");
		JSONArray jsonArray = jsonObject.getJSONArray("contexts");
		contentContexts = new LinkedList<>();
		for(int i = 0; i < jsonArray.length(); i++){
			ContentContext contentContext = new ContentContext();
			contentContext.fromJSON(jsonArray.getJSONObject(i));
			contentContexts.add(contentContext);
		}
	}

}
