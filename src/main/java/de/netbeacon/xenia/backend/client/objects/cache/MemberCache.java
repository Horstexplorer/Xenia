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

package de.netbeacon.xenia.backend.client.objects.cache;

import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.backend.client.objects.internal.BackendException;
import de.netbeacon.xenia.backend.client.objects.internal.BackendProcessor;

import java.util.Objects;

public class MemberCache extends Cache<Member> {

    private final long guildId;

    public MemberCache(BackendProcessor backendProcessor, long guildId) {
        super(backendProcessor);
        this.guildId = guildId;
    }

    public Member get(long userId){
        Member member = getFromCache(userId);
        if(member != null){
            return member;
        }
        member = new Member(getBackendProcessor(), guildId, userId);
        try{
            member.get();
        }catch (BackendException e){
            if(e.getId() == 404){
                member.create();
            }else{
                throw e;
            }
        }
        addToCache(userId, member);
        return member;
    }

    public void remove(long userId){
        removeFromCache(userId);
    }

    public void delete(long userId){
        Member member = getFromCache(userId);
        Objects.requireNonNullElseGet(member, ()->new Member(getBackendProcessor(), guildId, userId)).delete();
        removeFromCache(userId);
    }
}