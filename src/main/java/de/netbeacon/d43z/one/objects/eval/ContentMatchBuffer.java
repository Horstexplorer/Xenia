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

package de.netbeacon.d43z.one.objects.eval;

import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.bp.IIdentifiable;

import java.util.*;

import static de.netbeacon.d43z.one.objects.settings.StaticSettings.*;

public class ContentMatchBuffer implements IIdentifiable {

    private final UUID uuid = UUID.randomUUID();
    private final List<ContentMatch> lastMatches = new LinkedList<>();

    public ContentMatchBuffer(){}

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public synchronized void push(ContentMatch contentMatch){
        lastMatches.add(contentMatch);
        if(lastMatches.size() > BUFFER_MAX_SIZE){
            lastMatches.remove(0);
        }
    }

    public synchronized List<ContentMatch> getLastMatches() {
        return lastMatches;
    }

    public synchronized ContentMatch getLastMatch(){
        if(lastMatches.isEmpty()){
            return null;
        }
        return lastMatches.get(lastMatches.size()-1);
    }

    public synchronized Set<String> expectedMetaTags(){
        Set<String> metaTags = new HashSet<>();
        lastMatches.stream().map(lastMatches -> lastMatches.origin.getParent().getMetaTags()).forEachOrdered(metaTags::addAll);
        return metaTags;
    }

    public synchronized Map<ContentContext, Float> getLastMatchContextEval(){
        Map<ContentContext, Float> map = new HashMap<>();
        for(int i = lastMatches.size()-1; i >= 0; i--){
            ContentMatch contentMatch = lastMatches.get(i);
            ContentContext parentContent = contentMatch.getOrigin().getParent();
            if(!map.containsKey(parentContent)){
                map.put(parentContent, getBonusFor(i));
            }else{
                map.put(parentContent, map.get(parentContent)+getBonusFor(i));
            }
        }
        return map;
    }

    private float getBonusFor(int pos){
        return BUFFER_BONUS - BUFFER_BONUS_SUBTRACTION * ((lastMatches.size()-1) - pos);
    }
}
