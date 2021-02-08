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

package de.netbeacon.d43z.one.eval.io;

import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContextPool;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class EvalRequest {

    private final IContextPool contextPool;
    private final ContentMatchBuffer contentMatchBuffer;
    private final Content content;
    private final Consumer<EvalResult> evalResultConsumer;
    private final ExecutorService evalResultConsumerExecutor;
    private final long requestTimestamp;

    public EvalRequest(ContextPool contextPool, ContentMatchBuffer contentMatchBuffer, Content content, Consumer<EvalResult> evalResultConsumer, ExecutorService evalResultConsumerExecutor){
        this.contextPool = contextPool;
        this.contentMatchBuffer = contentMatchBuffer;
        this.content = content;
        this.evalResultConsumer = evalResultConsumer;
        this.evalResultConsumerExecutor = evalResultConsumerExecutor;
        this.requestTimestamp = System.currentTimeMillis();
    }

    public IContextPool getContextPool() {
        return contextPool;
    }

    public ContentMatchBuffer getContentMatchBuffer() {
        return contentMatchBuffer;
    }

    public Content getContent() {
        return content;
    }

    public Consumer<EvalResult> getCallback() {
        return evalResultConsumer;
    }

    public ExecutorService getCallbackExecutor() {
        return evalResultConsumerExecutor;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }
}
