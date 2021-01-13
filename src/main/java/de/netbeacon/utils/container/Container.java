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

package de.netbeacon.utils.container;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Container {

    private ConcurrentHashMap<List<Object>, Object> containedObjcts = new ConcurrentHashMap<>();

    public void insert(Object object, Object...additionalIdentifiers){
        containedObjcts.put(List.of(object.getClass(), additionalIdentifiers), object);
    }

    public <T> T get(Class<?> clazz, Object...additionalIdentifiers){
        return (T) clazz.cast(containedObjcts.get(List.of(clazz, additionalIdentifiers)));
    }

}
