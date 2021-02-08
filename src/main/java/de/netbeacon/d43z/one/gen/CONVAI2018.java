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

public class CONVAI2018 {

    public static void main(String...args) throws IOException {
        File file = new File("F:\\D31\\summer_wild_evaluation_dialogs.json");
        byte[] bytes = Files.readAllBytes(file.toPath());
        JSONArray jsonArray = new JSONArray(new String(bytes));

        List<ContentContext> a1 = new LinkedList<>();
        List<ContentContext> a2 = new LinkedList<>();
        List<ContentContext> a3 = new LinkedList<>();
        List<ContentContext> a4 = new LinkedList<>();
        List<ContentContext> a5 = new LinkedList<>();
        List<ContentContext> a6 = new LinkedList<>();

        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if(!jsonObject.has("eval_score") || jsonObject.get("eval_score") == JSONObject.NULL){
                continue;
            }
            JSONArray dialog = jsonObject.getJSONArray("dialog");
            List<Content> contents = new LinkedList<>();
            for(int ii = 0; ii < dialog.length(); ii++){
                contents.add(new Content(dialog.getJSONObject(ii).getString("text")));
            }
            ContentContext contentContext = new ContentContext("ConvAI-SummerWildEval2018_"+i, new HashSet<>(), contents);
            if(jsonObject.getInt("eval_score") == 5){
                a6.add(contentContext);
            }else if(jsonObject.getInt("eval_score") == 4){
                a5.add(contentContext);
            }else if(jsonObject.getInt("eval_score") == 3){
                a4.add(contentContext);
            }else if(jsonObject.getInt("eval_score") == 2){
                a3.add(contentContext);
            }else if(jsonObject.getInt("eval_score") == 1){
                a2.add(contentContext);
            }else if(jsonObject.getInt("eval_score") == 0){
                a1.add(contentContext);
            }
        }

        ContextPool c1 = new ContextPool("ConvAI-SummerWildEval2018-R0", a1);
        ContextPool c2 = new ContextPool("ConvAI-SummerWildEval2018-R1", a2);
        ContextPool c3 = new ContextPool("ConvAI-SummerWildEval2018-R2", a3);
        ContextPool c4 = new ContextPool("ConvAI-SummerWildEval2018-R3", a4);
        ContextPool c5 = new ContextPool("ConvAI-SummerWildEval2018-R4", a5);
        ContextPool c6 = new ContextPool("ConvAI-SummerWildEval2018-R5", a6);

        File out1 = new File("F:\\D31\\OUT\\"+c1.getUUID().toString()+".json");
        File out2 = new File("F:\\D31\\OUT\\"+c2.getUUID().toString()+".json");
        File out3 = new File("F:\\D31\\OUT\\"+c3.getUUID().toString()+".json");
        File out4 = new File("F:\\D31\\OUT\\"+c4.getUUID().toString()+".json");
        File out5 = new File("F:\\D31\\OUT\\"+c5.getUUID().toString()+".json");
        File out6 = new File("F:\\D31\\OUT\\"+c6.getUUID().toString()+".json");

        Files.write(out1.toPath(), c1.asJSON().toString(1).getBytes());
        Files.write(out2.toPath(), c2.asJSON().toString(1).getBytes());
        Files.write(out3.toPath(), c3.asJSON().toString(1).getBytes());
        Files.write(out4.toPath(), c4.asJSON().toString(1).getBytes());
        Files.write(out5.toPath(), c5.asJSON().toString(1).getBytes());
        Files.write(out6.toPath(), c6.asJSON().toString(1).getBytes());
    }

}
