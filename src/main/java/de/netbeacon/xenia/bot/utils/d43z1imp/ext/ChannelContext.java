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

package de.netbeacon.xenia.bot.utils.d43z1imp.ext;

import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.base.ContentShard;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import de.netbeacon.xenia.backend.client.objects.apidata.Message;
import de.netbeacon.xenia.backend.client.objects.internal.objects.CacheEventListener;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelContext extends ContentContext{

	private final UUID uuid = UUID.randomUUID();
	private final long channelId;
	private final Set<String> metaTags = new HashSet<>();
	private final List<Content> contents = new LinkedList<>();
	private final ReentrantLock reentrantLock = new ReentrantLock();
	private final Listener listener = new Listener(this);
	private long maxSize;

	public ChannelContext(long channelId, long maxSize){
		//super(); -- we dont need this
		this.channelId = channelId;
		this.maxSize = maxSize;
		metaTags.add(String.valueOf(channelId));
	}

	public void setMaxSize(long maxSize){
		this.maxSize = maxSize;
	}

	protected void insertRaw(String string){
		Content content = new Content(string);
		content.setWeight(1.1F);
		insert(content);
	}

	protected void insert(Content content){
		try{
			reentrantLock.lock();
			contents.add(content);
			while(contents.size() > maxSize && !contents.isEmpty()){
				contents.remove(0);
			}
		}
		finally{
			reentrantLock.unlock();
		}
	}

	public Listener getListener(){
		return listener;
	}

	@Override
	public UUID getUUID(){
		return uuid;
	}

	@Override
	public String getDescription(){
		return uuid + "_" + channelId;
	}

	@Override
	public Set<String> getMetaTags(){
		return metaTags;
	}

	@Override
	public List<ContentShard> getContentShards(){
		return List.of(new ContentShard(this, contents));
	}

	@Override
	public JSONObject asJSON() throws JSONSerializationException{
		return new JSONObject();
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONSerializationException{}

	public static class Listener implements CacheEventListener<Long, Message>{

		private final ChannelContext channelContext;

		protected Listener(ChannelContext channelContext){
			this.channelContext = channelContext;
		}

		@Override
		public void onInsertion(Long newKey, Message newObject){
			channelContext.insertRaw(newObject.getMessageContent(newObject.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()));
		}

	}

}
