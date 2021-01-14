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

package de.netbeacon.xenia.bot.utils.hastebin;

import de.netbeacon.xenia.bot.utils.shared.okhttpclient.SharedOkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class HastebinUtil {

    private static final String DEFAULT_HASTEBIN_URL = "https://haste.hypercdn.de";

    public static String uploadToHastebin(String content) throws Exception {
        return uploadToHastebin(DEFAULT_HASTEBIN_URL, content);
    }

    public static String uploadToHastebin(String host, String content) throws Exception {
        RequestBody requestBody = RequestBody.create(content, MediaType.parse("text/html; charset=utf-8"));
        Request request = new Request.Builder().post(requestBody).url(host+"/documents").build();
        try(Response response = SharedOkHttpClient.getInstance().newCall(request).execute()){
            if(response.code() != 200){
                throw new Exception("Error Executing Request: "+response.code()+" "+request.toString());
            }
            JSONObject jsonObject = new JSONObject(response.body().string());
            return host+"/"+jsonObject.getString("key");
        }
    }
}
