/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.backend.client.objects.internal;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.client.objects.internal.io.BackendRequest;
import de.netbeacon.xenia.backend.client.objects.internal.io.BackendResult;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BackendProcessor implements IShutdown {

    private final OkHttpClient okHttpClient;
    private final BackendSettings backendSettings;
    private final Logger logger = LoggerFactory.getLogger(BackendProcessor.class);
    private final ExecutorService executorService = Executors.newScheduledThreadPool(4);

    public BackendProcessor(OkHttpClient okHttpClient, BackendSettings backendSettings){
        this.okHttpClient = okHttpClient;
        this.backendSettings = backendSettings;
    }

    // auth

    public BackendProcessor activateToken() throws BackendException {
        if(backendSettings.getToken() == null || backendSettings.getToken().isBlank()){
            // send request to backend to get a new token
            BackendRequest backendRequest = new BackendRequest(BackendRequest.Method.GET, BackendRequest.AuthType.Basic, Arrays.asList("auth", "token"), new HashMap<>(), null);
            BackendResult backendResult = process(backendRequest);
            if(backendResult.getStatusCode() == 200){
                backendSettings.setToken(backendResult.getPayloadAsJSON().getString("token"));
                logger.info("Received New Token Successfully");
            }else{
                logger.info("Failed To Receive New Token With Status Code "+backendResult.getStatusCode());
                throw new BackendException(-2, "Requesting Token Failed With Status Code: "+backendResult.getStatusCode());
            }
        }else{
            // renew this token
            BackendRequest backendRequest = new BackendRequest(BackendRequest.Method.GET, BackendRequest.AuthType.Token, Arrays.asList("auth", "token", "renew"), new HashMap<>(), null);
            BackendResult backendResult = process(backendRequest);
            if(backendResult.getStatusCode() == 200){
                logger.info("Renewed Token Successfully");
            }else{
                // renewing failed - remove token and call again to request new one
                logger.info("Failed To Renew Token With Status Code "+backendResult.getStatusCode()+" - Requesting New Token");
                backendSettings.setToken(null);
                return activateToken();
            }
        }
        return this;
    }

    public BackendResult process(BackendRequest backendRequest) throws BackendException {
        try{
            try(Response response = okHttpClient.newCall(buildOkHttpRequest(backendRequest)).execute()){
                // parse response
                int code = response.code();
                if(response.body() != null){
                    return new BackendResult(code, response.body().bytes());
                }else{
                    return new BackendResult(code, null);
                }
            }
        }catch (Exception e){
            logger.error("Failed To Process Request: ", e);
            throw new BackendException(-1, e);
        }
    }

    public void processAsync(BackendRequest backendRequest, Consumer<BackendResult> resultConsumer) throws BackendException {
        try{
            okHttpClient.newCall(buildOkHttpRequest(backendRequest)).enqueue(new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.error("Failed To Process Request Async: ", e);
                    executorService.execute(()->resultConsumer.accept(new BackendResult(-1, null)));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    int code = response.code();
                    if(response.body() != null){
                        byte[] body = response.body().bytes();
                        executorService.execute(()->resultConsumer.accept(new BackendResult(code, body)));
                    }else{
                        executorService.execute(()->resultConsumer.accept(new BackendResult(code, null)));
                    }
                }
            });

        }catch (Exception e){
            logger.error("Failed To Process Request: ", e);
            throw new BackendException(-1, e);
        }
    }

    private Request buildOkHttpRequest(BackendRequest backendRequest){
        try{
            // build url from request
            HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                    .scheme(backendSettings.getScheme())
                    .host(backendSettings.getHost())
                    .port(backendSettings.getPort());
            for(String pathSeg : backendRequest.getPath()){
                urlBuilder.addPathSegment(pathSeg);
            }
            for(Map.Entry<String, String> queryParam : backendRequest.getQueryParams().entrySet()){
                urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
            }
            // build request
            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build());
            switch (backendRequest.getAuthType()){
                case Token:
                    requestBuilder.header("UserID", backendSettings.getClientIdAsString());
                    requestBuilder.header("Token", backendSettings.getToken());
                    break;
                case Basic:
                    requestBuilder.header("Authorization", Credentials.basic(backendSettings.getClientIdAsString(), backendSettings.getPassword()));
                    break;
            }
            switch (backendRequest.getMethod()){
                case GET:
                    requestBuilder.get();
                    break;
                case PUT:
                    if(backendRequest.getPayload().length != 0){
                        requestBuilder.put(RequestBody.create(backendRequest.getPayload(), MediaType.get("application/json")));
                    }else{
                        throw new BackendException(-1, "No Body Specified Which Is Needed For PUT");
                    }
                    break;
                case POST:
                    if(backendRequest.getPayload().length != 0){
                        requestBuilder.post(RequestBody.create(backendRequest.getPayload(), MediaType.get("application/json")));
                    }else{
                        throw new BackendException(-1, "No Body Specified Which Is Needed For POST");
                    }
                case DELETE:
                    if(backendRequest.getPayload().length != 0){
                        requestBuilder.delete(RequestBody.create(backendRequest.getPayload(), MediaType.get("application/json")));
                    }else{
                        requestBuilder.delete();
                    }
                    break;
            }
            return requestBuilder.build();
        }catch (Exception e){
            logger.error("Failed To Process Request: ", e);
            throw new BackendException(-1, e);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void onShutdown() throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        okHttpClient.dispatcher().executorService().shutdown();
    }
}
