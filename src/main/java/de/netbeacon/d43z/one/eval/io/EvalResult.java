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

import de.netbeacon.d43z.one.objects.eval.ContentMatch;

public class EvalResult {

    private ContentMatch contentMatch;
    private Exception exception;

    public EvalResult(ContentMatch contentMatch){
        this.contentMatch = contentMatch;
    }

    public EvalResult(Exception e){
        this.exception = e;
    }

    public ContentMatch getContentMatch() {
        return contentMatch;
    }

    public Exception getException() {
        return exception;
    }

    public boolean ok(){
        return exception == null;
    }
}
