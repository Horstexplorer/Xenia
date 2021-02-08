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
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class D43Z1Imp {

    private final IContextPool contextPoolMaster;
    private final List<IContextPool> contextPools = new LinkedList<>();

    private final ConcurrentHashMap<Long, ContentMatchBuffer> contentMatchBuffers = new ConcurrentHashMap<>();

    private final Eval eval;

    public D43Z1Imp(InputStream index) throws IOException {
        JSONObject indexJSON = new JSONObject(IOUtils.toString(index, StandardCharsets.UTF_8));
        JSONArray base = indexJSON.getJSONArray("base");
        for(int i = 0; i < base.length(); i++){
            InputStream fileInput = getClass().getClassLoader().getResourceAsStream("d43z1/"+base.getString(i)+".cp.json");
            if(fileInput == null){
                throw new RuntimeException("Invalid Entry In D43Z1 Base Index");
            }
            JSONObject contextPoolJSON = new JSONObject(IOUtils.toString(fileInput, StandardCharsets.UTF_8));
            ContextPool contextPool = new ContextPool();
            contextPool.fromJSON(contextPoolJSON);
            contextPools.add(contextPool);
        }
        JSONArray extended = indexJSON.getJSONArray("extended");
        for(int i = 0; i < extended.length(); i++){
            InputStream fileInput = getClass().getClassLoader().getResourceAsStream("d43z1/"+extended.getString(i)+".ccp.json");
            if(fileInput == null){
                throw new RuntimeException("Invalid Entry In D43Z1 Extended Index");
            }
            JSONObject contextPoolJSON = new JSONObject(IOUtils.toString(fileInput, StandardCharsets.UTF_8));
            CombinedContextPool combinedContextPool = new CombinedContextPool(contextPoolJSON, contextPools);
            contextPools.add(combinedContextPool);
        }
        this.contextPoolMaster = contextPools.stream().filter(contextPool -> contextPool.getUUID().toString().equals(indexJSON.getString("master"))).findFirst().orElseThrow();
        this.eval = new Eval();
    }

    public Eval getEval() {
        return eval;
    }

    public IContextPool getContextPoolMaster() {
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
            return contentMatchBuffers.get(userId);
        }finally {
            lock.unlock();
        }
    }
}
