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

package de.netbeacon.xenia.bot.utils.d43z1imp;

import de.netbeacon.d43z.one.eval.Eval;
import de.netbeacon.d43z.one.objects.base.CombinedContextPool;
import de.netbeacon.d43z.one.objects.base.ContextPool;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import de.netbeacon.utils.shutdownhook.IShutdown;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class D43Z1Imp implements IShutdown{

	private static D43Z1Imp instance;

	private final IContextPool contextPoolMaster;
	private final List<IContextPool> contextPools = new LinkedList<>();

	private final ConcurrentHashMap<Long, ContentMatchBuffer> contentMatchBuffers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<ContentMatchBuffer, Long> invertedContentMatchBuffers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<ContentMatchBuffer, Long> contentMatchBufferAccessTimestamp = new ConcurrentHashMap<>();

	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private final Eval eval;

	public static D43Z1Imp getInstance() throws IOException{
		return getInstance(false);
	}

	public static D43Z1Imp getInstance(boolean initializeIfNeeded) throws IOException{
		if(instance == null && initializeIfNeeded){
			instance = new D43Z1Imp();
		}
		return instance;
	}

	private D43Z1Imp() throws IOException{
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("d43z1.index.json");
		if(inputStream == null){
			throw new RuntimeException("Missing D43Z1 Index");
		}
		JSONObject indexJSON = new JSONObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
		JSONArray base = indexJSON.getJSONArray("base");
		for(int i = 0; i < base.length(); i++){
			InputStream fileInput = getClass().getClassLoader().getResourceAsStream("d43z1/" + base.getString(i) + ".cp.json");
			if(fileInput == null){
				throw new RuntimeException("Invalid Entry In D43Z1 Base Index " + base.getString(i));
			}
			JSONObject contextPoolJSON = new JSONObject(IOUtils.toString(fileInput, StandardCharsets.UTF_8));
			ContextPool contextPool = new ContextPool();
			contextPool.fromJSON(contextPoolJSON);
			contextPools.add(contextPool);
		}
		JSONArray extended = indexJSON.getJSONArray("extended");
		for(int i = 0; i < extended.length(); i++){
			InputStream fileInput = getClass().getClassLoader().getResourceAsStream("d43z1/" + extended.getString(i) + ".ccp.json");
			if(fileInput == null){
				throw new RuntimeException("Invalid Entry In D43Z1 Extended Index " + extended.getString(i));
			}
			JSONObject contextPoolJSON = new JSONObject(IOUtils.toString(fileInput, StandardCharsets.UTF_8));
			CombinedContextPool combinedContextPool = new CombinedContextPool(contextPoolJSON, contextPools);
			contextPools.add(combinedContextPool);
		}
		this.contextPoolMaster = contextPools.stream().filter(contextPool -> contextPool.getUUID().toString().equals(indexJSON.getString("master"))).findFirst().orElseThrow();
		this.eval = new Eval();
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			for(Map.Entry<ContentMatchBuffer, Long> entry : contentMatchBufferAccessTimestamp.entrySet()){
				if(entry.getValue() + 600000 < System.currentTimeMillis()){
					contentMatchBuffers.remove(invertedContentMatchBuffers.remove(entry.getKey()));
					contentMatchBufferAccessTimestamp.remove(entry.getKey());
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
	}

	public Eval getEval(){
		return eval;
	}

	public IContextPool getContextPoolMaster(){
		return contextPoolMaster;
	}

	public IContextPool getContextPoolByUUID(UUID uuid){
		return contextPools.stream().filter(iContextPool -> iContextPool.getUUID().equals(uuid)).findFirst().orElse(null);
	}

	private final Lock lock = new ReentrantLock();

	public ContentMatchBuffer getContentMatchBufferFor(long userId){
		try{
			lock.lock();
			contentMatchBuffers.putIfAbsent(userId, new ContentMatchBuffer());
			invertedContentMatchBuffers.putIfAbsent(contentMatchBuffers.get(userId), userId);
			contentMatchBufferAccessTimestamp.put(contentMatchBuffers.get(userId), System.currentTimeMillis());
			return contentMatchBuffers.get(userId);
		}
		finally{
			lock.unlock();
		}
	}

	@Override
	public void onShutdown() throws Exception{
		eval.onShutdown();
		scheduledExecutorService.shutdownNow();
	}

}
