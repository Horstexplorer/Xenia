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

package de.netbeacon.xenia.tools.cooldown;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Takes care of providing a simple way to deny processing for some time
 */
public class CooldownManager {

    private final ConcurrentHashMap<Long, Long> list = new ConcurrentHashMap<>();

    /**
     * Check weather the id still has an active cool down or not
     *
     * @param id of the user
     * @return true if the cool down is still running
     */
    public boolean isActive(long id){
        if(list.containsKey(id)){
            return list.get(id) > System.currentTimeMillis();
        }
        return false;
    }

    /**
     * Activates a cool down for a user with the given duration
     *
     * @param id of the user
     * @param milliseconds of the duration
     */
    public void activateFor(long id, long milliseconds){
        list.put(id, System.currentTimeMillis()+milliseconds);
    }
}
