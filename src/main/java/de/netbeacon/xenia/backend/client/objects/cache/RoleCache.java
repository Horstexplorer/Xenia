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

import de.netbeacon.utils.locks.IdBasedLockHolder;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.internal.BackendProcessor;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.backend.client.objects.internal.io.BackendRequest;
import de.netbeacon.xenia.backend.client.objects.internal.io.BackendResult;
import de.netbeacon.xenia.backend.client.objects.internal.objects.Cache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class RoleCache extends Cache<Long, Role> {

    private final long guildId;
    private final IdBasedLockHolder<Long> idBasedLockHolder = new IdBasedLockHolder<>();
    private final Logger logger = LoggerFactory.getLogger(RoleCache.class);

    public RoleCache(BackendProcessor backendProcessor, long guildId) {
        super(backendProcessor);
        this.guildId = guildId;
    }

    public Role get(long roleId) throws CacheException, DataException {
        try{
            idBasedLockHolder.getLock(roleId).lock();
            Role role = getFromCache(roleId);
            if(role != null){
                return role;
            }
            role = new Role(getBackendProcessor(), guildId, roleId);
            role.get();
            addToCache(roleId, role);
            return role;
        }catch (CacheException | DataException e){
            throw e;
        }catch (Exception e){
            throw new CacheException(CacheException.Type.UNKNOWN, "Failed To Get Role", e);
        }finally {
            idBasedLockHolder.getLock(roleId).unlock();
        }
    }

    public List<Role> retrieveAllFromBackend(boolean cacheInsert) throws CacheException, DataException {
        try{
            if(cacheInsert){
                idBasedLockHolder.getLock().writeLock().lock();
            }
            BackendRequest backendRequest = new BackendRequest(BackendRequest.Method.GET, BackendRequest.AuthType.BEARER, List.of("data", "guilds", String.valueOf(guildId), "roles"),new HashMap<>(), null);
            BackendResult backendResult = getBackendProcessor().process(backendRequest);
            if(backendResult.getStatusCode() != 200){
                logger.warn("Failed To Get All Roles From The Backend");
                return null;
            }
            JSONArray roles = backendResult.getPayloadAsJSON().getJSONArray("roles");
            List<Role> rolesList = new ArrayList<>();
            for(int i = 0; i < roles.length(); i++){
                JSONObject jsonObject = roles.getJSONObject(i);
                Role role = new Role(getBackendProcessor(), guildId, jsonObject.getLong("roleId"));
                role.fromJSON(jsonObject); // manually insert the data as we already received it
                if(cacheInsert){
                    addToCache(role.getId(), role); // this will overwrite already existing ones
                }
                rolesList.add(role);
            }
            return rolesList;
        }catch (CacheException | DataException e){
            throw e;
        }catch (Exception e){
            throw new CacheException(CacheException.Type.UNKNOWN, "Failed To Retrieve All Roles", e);
        }finally {
            if(cacheInsert){
                idBasedLockHolder.getLock().writeLock().unlock();
            }
        }
    }

    private final ReentrantLock creationLock = new ReentrantLock();

    public Role createNew() throws CacheException, DataException {
        try{
            creationLock.lock();
            if(getOrderedKeyMap().size()+1 > getBackendProcessor().getBackendClient().getLicenseCache().get(guildId).getPerk_GUILD_ROLE_C()){
                throw new RuntimeException("Cache Is Full");
            }
            Role role = new Role(getBackendProcessor(), guildId, -1);
            role.create();
            addToCache(role.getId(), role);
            return role;
        }catch (CacheException | DataException e){
            throw e;
        }catch (Exception e){
            throw new CacheException(CacheException.Type.UNKNOWN, "Failed To Create Role", e);
        }finally {
            creationLock.unlock();
        }
    }

    public void remove(long roleId){
        removeFromCache(roleId);
    }

    public void delete(long roleId) throws CacheException, DataException {
        try{
            idBasedLockHolder.getLock(roleId).lock();
            Role role = getFromCache(roleId);
            Objects.requireNonNullElseGet(role, ()->new Role(getBackendProcessor(), guildId, roleId)).delete();
            removeFromCache(roleId);
        }catch (CacheException | DataException e){
            throw e;
        }catch (Exception e){
            throw new CacheException(CacheException.Type.UNKNOWN, "Failed To Delete Role", e);
        }finally {
            idBasedLockHolder.getLock(roleId).unlock();
        }
    }
}
