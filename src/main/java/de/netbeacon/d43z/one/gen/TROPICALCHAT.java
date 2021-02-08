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

package de.netbeacon.d43z.one.gen;

import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.base.ContextPool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TROPICALCHAT {

    public static void main(String...args) throws IOException {
        File file = new File("F:\\D31\\tropicalchattrain.json");
        byte[] bytes = Files.readAllBytes(file.toPath());
        JSONObject jsonObject = new JSONObject(new String(bytes));
        int i = 0;
        List<ContentContext> contentContexts = new LinkedList<>();
        for(String key : jsonObject.keySet()){
            JSONObject set = jsonObject.getJSONObject(key);
            List<Content> contentList = new LinkedList<>();
            JSONArray contentA = set.getJSONArray("content");
            for(int ii = 0; ii < contentA.length(); ii++){
                JSONObject msg = contentA.getJSONObject(ii);
               contentList.add(new Content(msg.getString("message")));
            }
            contentContexts.add(new ContentContext("Tropical-Chat_"+i, new HashSet<>(), contentList));
        }
        ContextPool contextPool = new ContextPool("Tropical-Chat", contentContexts);
        File out = new File("F:\\D31\\OUT\\"+contextPool.getUUID().toString()+".json");
        Files.write(out.toPath(), contextPool.asJSON().toString(1).getBytes());
    }
}
