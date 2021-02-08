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

import de.netbeacon.d43z.one.eval.Eval;
import de.netbeacon.d43z.one.eval.io.EvalRequest;
import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TESTING {

    public static void main(String...args) throws IOException {
        File file = new File("F:\\D31\\OUT\\tropical-chat\\471abba0-3ed6-4edd-94ef-902f650a1a48.cp.json");
        byte[] bytes = Files.readAllBytes(file.toPath());
        JSONObject jsonObject = new JSONObject(new String(bytes));
        ContextPool contextPool = new ContextPool();
        contextPool.fromJSON(jsonObject);
        ContentMatchBuffer contentMatchBuffer = new ContentMatchBuffer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Eval eval = new Eval();
        System.out.println("Ready");
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("---");
            String input = scanner.nextLine();
            if(input.equals("exit")){
                break;
            }
            EvalRequest evalRequest = new EvalRequest(contextPool, contentMatchBuffer, new Content(input), evalResult -> {
                if(evalResult.ok()){
                    System.out.println("\""+evalResult.getContentMatch().getInput().getContent()+"\" is \""+evalResult.getContentMatch().getEstimatedInput().getContent()+"\" by "+evalResult.getContentMatch().getAdjustedCoefficient()+" ("+evalResult.getContentMatch().getCoefficient()+")");
                    System.out.println(evalResult.getContentMatch().getEstimatedOutput().getContent());
                }
            }, executorService);
            eval.enqueue(evalRequest);
            System.out.println("Q "+eval.getQueueLength()+"x | W "+eval.getQueueTimeAVGMs()+"ms | E "+(eval.getEvalTimeAVGNs()/(float)1000000)+"ms");
        }

    }
}
