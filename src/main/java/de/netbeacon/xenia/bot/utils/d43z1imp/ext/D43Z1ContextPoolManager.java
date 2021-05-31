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

package de.netbeacon.xenia.bot.utils.d43z1imp.ext;

import de.netbeacon.d43z.one.objects.base.CombinedContextPool;
import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.base.ContextPool;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.utils.locks.IdBasedLockHolder;
import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataEventListener;
import de.netbeacon.xenia.backend.client.objects.internal.objects.CacheEventListener;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class D43Z1ContextPoolManager{

	private static final long REFRESH_TIME = 1000 * 60 * 5;
	private final D43Z1Imp d43Z1Imp;
	private final ConcurrentHashMap<Long, Pair<IContextPool, Long>> guildPoolConcurrentHashMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, ChannelContext> channelContextHashMap = new ConcurrentHashMap<>();

	private final IdBasedLockHolder<Long> longIdBasedLockHolder = new IdBasedLockHolder<>();

	private final Listener listener = new Listener(this);


	public D43Z1ContextPoolManager(D43Z1Imp d43Z1Imp){
		this.d43Z1Imp = d43Z1Imp;
	}

	public Listener getListener(){
		return listener;
	}

	public IContextPool getPoolFor(Guild guild){
		return getPoolFor(guild, false);
	}

	public IContextPool getPoolFor(Guild guild, boolean overrideCache){
		try{
			longIdBasedLockHolder.getLock(guild.getId()).lock();
			// fetch from cache
			if(guildPoolConcurrentHashMap.containsKey(guild.getId()) && (guildPoolConcurrentHashMap.get(guild.getId()).getValue2() + REFRESH_TIME) > System.currentTimeMillis() && !overrideCache){
				return guildPoolConcurrentHashMap.get(guild.getId()).getValue1();
			}
			// add to cache / hard refresh
			Guild.D43Z1Mode d43Z1Mode = guild.getD43Z1Mode();
			List<IContextPool> contextPools = new ArrayList<>();
			if(d43Z1Mode.has(Guild.D43Z1Mode.Modes.MASTER_ONLY)){
				contextPools.add(d43Z1Imp.getContextPoolMaster());
			}
			else if(d43Z1Mode.has(Guild.D43Z1Mode.Modes.MIX)){
				contextPools.add(new ContextPool("channel_pool_" + guild.getId(), getContextFor(guild.getChannelCache().getDataMap().values())));
				contextPools.add(d43Z1Imp.getContextPoolMaster());
			}
			else if(d43Z1Mode.has(Guild.D43Z1Mode.Modes.SELF_LEARNING_ONLY)){
				contextPools.add(new ContextPool("channel_pool_" + guild.getId(), getContextFor(guild.getChannelCache().getDataMap().values())));
			}
			else{
				// for when something gone big bad
				contextPools.add(d43Z1Imp.getContextPoolMaster());
			}
			CombinedContextPool combinedContextPool = new CombinedContextPool("guild_pool_" + guild.getId(), contextPools);
			guildPoolConcurrentHashMap.put(guild.getId(), new Pair<>(combinedContextPool, System.currentTimeMillis()));
			return combinedContextPool;
		}
		finally{
			longIdBasedLockHolder.getLock(guild.getId()).unlock();
		}
	}

	private List<ContentContext> getContextFor(Collection<Channel> channels){
		List<ContentContext> channelContexts = new ArrayList<>();
		for(Channel channel : channels){
			if(channel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING)){
				if(!channelContextHashMap.containsKey(channel.getChannelId())){
					ChannelContext channelContext = new ChannelContext(channel.getChannelId(), channel.getBackendProcessor().getBackendClient().getLicenseCache().get(channel.getGuildId()).getPerk_CHANNEL_LOGGING_C());
					channel.getMessageCache().addEventListeners(channelContext.getListener());
					channelContextHashMap.put(channel.getChannelId(), channelContext);
				}
				var channelContext = channelContextHashMap.get(channel.getChannelId());
				channelContext.setMaxSize(channel.getBackendProcessor().getBackendClient().getLicenseCache().get(channel.getGuildId()).getPerk_CHANNEL_LOGGING_C());
				channelContexts.add(channelContext);
			}
			else{
				channelContextHashMap.remove(channel.getChannelId());
			}
		}
		return channelContexts;
	}

	protected void removePoolFor(Guild guild){
		try{
			longIdBasedLockHolder.getLock(guild.getId()).lock();
			guildPoolConcurrentHashMap.remove(guild.getId());
			guild.getChannelCache().getOrderedKeyMap().forEach(channelContextHashMap::remove);
		}
		finally{
			longIdBasedLockHolder.getLock(guild.getId()).unlock();
		}
	}

	protected void removeContextFor(Channel channel){
		channelContextHashMap.remove(channel.getChannelId());
	}

	public static class Listener implements CacheEventListener<Long, Guild>, APIDataEventListener<Guild>{

		private final D43Z1ContextPoolManager d43Z1ContextPoolManager;

		protected Listener(D43Z1ContextPoolManager d43Z1ContextPoolManager){
			this.d43Z1ContextPoolManager = d43Z1ContextPoolManager;
		}

		@Override
		public void onInsertion(Long newKey, Guild newObject){
			// inject this in the guild object
			newObject.addEventListener(this);
			newObject.getChannelCache().addEventListeners(new CacheEventListener<>(){
				@Override
				public void onInsertion(Long newKey, Channel newObject){
					if(!newObject.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING)){
						return;
					}
					d43Z1ContextPoolManager.getPoolFor(newObject.getGuild(), true); // force a reload
				}

				@Override
				public void onRemoval(Long oldKey, Channel oldObject){
					if(!oldObject.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVATE_SELF_LEARNING)){
						return;
					}
					d43Z1ContextPoolManager.removeContextFor(oldObject);
					// update
					d43Z1ContextPoolManager.getPoolFor(oldObject.getGuild(), true); // force a reload
				}
			});
		}

		@Override
		public void onRemoval(Long oldKey, Guild oldObject){
			// welcome
			d43Z1ContextPoolManager.removePoolFor(oldObject);
		}

		@Override
		public void onDeletion(Guild apiDataObject){
			// yes yes fuck off bye
			d43Z1ContextPoolManager.removePoolFor(apiDataObject);
		}

	}

}
