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

import de.netbeacon.d43z.one.objects.bp.IIdentifiable;
import de.netbeacon.utils.json.serial.IJSONSerializable;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static de.netbeacon.d43z.one.objects.settings.StaticSettings.CONTENT_SHARD_SIZE;

public class ContentContext implements IIdentifiable, IJSONSerializable{

	private UUID uuid;
	private String description;
	private Set<String> metaTags = new HashSet<>();
	private final List<ContentShard> contentShards = new LinkedList<>();

	public ContentContext(){}

	public ContentContext(String description, Set<String> metaTags, List<Content> contents){
		this.uuid = UUID.randomUUID();
		this.description = description;
		this.metaTags.addAll(metaTags);
		ListUtils.partition(contents, CONTENT_SHARD_SIZE).forEach(contentSub -> contentShards.add(new ContentShard(this, contentSub)));
	}

	@Override
	public UUID getUUID(){
		return uuid;
	}

	public String getDescription(){
		return description;
	}

	public Set<String> getMetaTags(){
		return metaTags;
	}

	public List<ContentShard> getContentShards(){
		return contentShards;
	}

	@Override
	public JSONObject asJSON() throws JSONSerializationException{
		JSONArray orderedContentArray = new JSONArray();
		contentShards.stream().map(ContentShard::getOrderedContent).forEachOrdered(contents -> contents.forEach(content -> orderedContentArray.put(content.asJSON())));
		return new JSONObject()
			.put("contextId", uuid.toString())
			.put("description", description)
			.put("metaTags", metaTags)
			.put("content", orderedContentArray);
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONSerializationException{
		this.uuid = UUID.fromString(jsonObject.getString("contextId"));
		this.description = jsonObject.getString("description");
		JSONArray metaTagsArray = jsonObject.getJSONArray("metaTags");
		metaTags = new HashSet<>();
		for(int i = 0; i < metaTagsArray.length(); i++){
			metaTags.add(metaTagsArray.getString(i).toLowerCase());
		}
		JSONArray orderedContentArray = jsonObject.getJSONArray("content");
		List<Content> contents = new LinkedList<>();
		for(int i = 0; i < orderedContentArray.length(); i++){
			Content content = new Content();
			content.fromJSON(orderedContentArray.getJSONObject(i));
			contents.add(content);
		}
		contentShards.clear();
		ListUtils.partition(contents, CONTENT_SHARD_SIZE).forEach(contentSub -> contentShards.add(new ContentShard(this, contentSub)));
	}

}
