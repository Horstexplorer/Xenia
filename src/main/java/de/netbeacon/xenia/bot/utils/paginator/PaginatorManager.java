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

package de.netbeacon.xenia.bot.utils.paginator;

import de.netbeacon.utils.locks.IdBasedLockHolder;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.tuples.Triplet;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;

public class PaginatorManager implements IShutdown{

	protected static final String PREVIOUS = "\u2B05\uFE0F"; // arrow:left
	protected static final String NEXT = "\u27A1\uFE0F"; // arrow:right
	protected static final String CLOSE = "\u2716\uFE0F"; // heavy:multiplication:x
	private static final long EST_LIFETIME = 1000 * 30;
	private static final long CLEAN_INTERVAL = 1000;
	private static final long WAIT_TIME = 5000;
	private final Listener listener = new Listener(this);
	private final ConcurrentHashMap<Long, Triplet<Paginator, Long, Long>> paginatorConcurrentHashMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Long> userPaginatorConcurrentHashMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Boolean> creationRunning = new ConcurrentHashMap<>();
	private final ScheduledFuture<?> cleaner;
	private final IdBasedLockHolder<Long> idBasedLockHolder = new IdBasedLockHolder<>();

	public PaginatorManager(ScheduledExecutorService scheduledExecutorService){
		cleaner = scheduledExecutorService.scheduleAtFixedRate(() -> {
			try{
				paginatorConcurrentHashMap.values().stream().filter(tri -> tri.getValue3() < System.currentTimeMillis()).forEach(tri -> removePaginator(tri.getValue1().getMessageId()));
			}
			catch(Exception ignore){
			}
		}, CLEAN_INTERVAL, CLEAN_INTERVAL, TimeUnit.MILLISECONDS);
	}

	public void createPaginator(TextChannel textChannel, User user, List<Page> pages){
		try{
			idBasedLockHolder.getLock(user.getIdLong()).lock();
			// check that we aren't waiting for a result for this user already
			if(creationRunning.putIfAbsent(user.getIdLong(), false) != null){
				var waiter = creationRunning.get(user.getIdLong());
				long preWaitTime = System.currentTimeMillis();
				synchronized(waiter){
					waiter.wait(WAIT_TIME);
				}
				if(preWaitTime + WAIT_TIME - 1 <= System.currentTimeMillis()){
					throw new TimeoutException();
				}
			}
			creationRunning.putIfAbsent(user.getIdLong(), false);
			// remove existing
			Paginator old = getPaginatorByUser(user.getIdLong());
			if(old != null){
				paginatorConcurrentHashMap.remove(old.getMessageId());
				userPaginatorConcurrentHashMap.remove(old.getUserId());
			}
			// create new
			Paginator paginator = new Paginator(textChannel.getIdLong(), user.getIdLong(), pages);
			paginator.drawCurrent(textChannel, user, null, (user_, message_) -> {
				paginatorConcurrentHashMap.put(message_.getIdLong(), new Triplet<>(paginator, user_.getIdLong(), System.currentTimeMillis() + EST_LIFETIME));
				userPaginatorConcurrentHashMap.put(user_.getIdLong(), message_.getIdLong());
				var waiter_ = creationRunning.remove(user_.getIdLong());
				synchronized(waiter_){
					waiter_.notify();
				}
			}, (user_, throwable_) -> {
				var waiter_ = creationRunning.remove(user_.getIdLong());
				synchronized(waiter_){
					waiter_.notify();
				}
			});
		}
		catch(InterruptedException | TimeoutException e){
			e.printStackTrace();
		}
		finally{
			idBasedLockHolder.getLock(user.getIdLong()).unlock();
		}
	}

	public void usePaginator(Paginator paginator){
		var paginatorTriplet = paginatorConcurrentHashMap.get(paginator.getMessageId());
		if(paginatorTriplet == null){
			return;
		}
		paginatorConcurrentHashMap.put(paginator.getMessageId(), new Triplet<>(paginatorTriplet.getValue1(), paginatorTriplet.getValue2(), paginatorTriplet.getValue3() + EST_LIFETIME));
	}

	public Paginator getPaginatorByMessage(long messageId){
		var paginatorTriplet = paginatorConcurrentHashMap.get(messageId);
		if(paginatorTriplet == null){
			return null;
		}
		return paginatorTriplet.getValue1();
	}

	public Paginator getPaginatorByUser(long userId){
		var messageId = userPaginatorConcurrentHashMap.get(userId);
		if(messageId == null){
			return null;
		}
		return getPaginatorByMessage(messageId);
	}

	public void removePaginator(long messageId){
		var paginatorTriplet = paginatorConcurrentHashMap.get(messageId);
		if(paginatorTriplet == null){
			return;
		}
		paginatorConcurrentHashMap.remove(messageId);
		userPaginatorConcurrentHashMap.remove(paginatorTriplet.getValue2());
	}

	public Listener getListener(){
		return listener;
	}

	@Override
	public void onShutdown() throws Exception{
		cleaner.cancel(true);
	}

	public static class Listener extends ListenerAdapter{

		private final PaginatorManager paginatorManager;

		protected Listener(PaginatorManager paginatorManager){
			this.paginatorManager = paginatorManager;
		}

		@Override
		public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event){
			paginatorManager.removePaginator(event.getMessageIdLong());
		}

		@Override
		public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event){
			Paginator paginator = paginatorManager.getPaginatorByMessage(event.getMessageIdLong());
			if(paginator == null || paginator.getUserId() != event.getUserIdLong() || !event.getReactionEmote().isEmoji()){
				return;
			}
			paginatorManager.usePaginator(paginator);
			switch(event.getReactionEmote().getEmoji()){
				case NEXT -> {
					paginator.movePosition(Paginator.Move.NEXT);
					paginator.drawCurrent(event.getChannel(), event.getUser(), event.getReactionEmote());
					break;
				}
				case PREVIOUS -> {
					paginator.movePosition(Paginator.Move.PREVIOUS);
					paginator.drawCurrent(event.getChannel(), event.getUser(), event.getReactionEmote());
					break;
				}
				case CLOSE -> {
					paginatorManager.removePaginator(event.getMessageIdLong());
				}
			}
		}

	}

}
