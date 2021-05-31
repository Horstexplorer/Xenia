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

package de.netbeacon.xenia.bot.utils.eventwaiter;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Waits for desired events
 */
public class EventWaiter{

	private final LinkedList<Package<?>> eventList = new LinkedList<>();
	private final ExecutorService asyncExecutor;
	private final ScheduledExecutorService asyncDeScheduler;

	public EventWaiter(ExecutorService asyncExecutor, ScheduledExecutorService asyncDeScheduler){
		this.asyncExecutor = asyncExecutor;
		this.asyncDeScheduler = asyncDeScheduler;
	}

	/**
	 * Waits for a specific event to happen, returns the event when detected
	 *
	 * @param eventClassToWait wait for events of this class
	 * @param condition        additional check to differentiate from other events of the same class
	 * @param timeout          after which to cancel the wait
	 * @param <T>              class of the event
	 *
	 * @return event of success, null on timeout
	 *
	 * @throws Exception on exception
	 */
	public <T extends GenericEvent> T waitFor(Class<T> eventClassToWait, Predicate<T> condition, long timeout) throws Exception{
		Package<T> tPackage = new Package<>(eventClassToWait, condition);
		eventList.add(tPackage);
		long l = System.currentTimeMillis();
		synchronized(tPackage){
			tPackage.wait(timeout);
		}
		eventList.remove(tPackage);
		if(tPackage.getEvent() == null){
			throw new TimeoutException("Event wait timed out after " + timeout + " ms");
		}
		return tPackage.getEvent();
	}

	public <T extends GenericEvent> void executeWhen(Class<T> eventClassToWait, Predicate<T> condition, long timeout, Consumer<T> onSuccess){
		executeWhen(eventClassToWait, condition, timeout, onSuccess, null);
	}

	public <T extends GenericEvent> void executeWhen(Class<T> eventClassToWait, Predicate<T> condition, long timeout, Consumer<T> onSuccess, Consumer<Exception> onFailure){
		Package<T> tPackage = new Package<>(eventClassToWait, condition, onSuccess, onFailure);
		eventList.add(tPackage);
		tPackage.setDeSchedulerFuture(
			asyncDeScheduler.schedule(() -> {
				if(tPackage.getFailureConsumer() != null){
					asyncExecutor.execute(() -> tPackage.getFailureConsumer().accept(new TimeoutException("Event wait timed out after " + timeout + " ms")));
				}
			}, timeout, TimeUnit.MILLISECONDS)
		);
	}

	/**
	 * Used to test if something is waiting on this event to happen
	 *
	 * @param event the current event
	 * @param <T>   the event class
	 *
	 * @return true if something has been waiting for this event
	 */
	public <T extends GenericEvent> boolean waitingOnThis(T event){
		boolean wasWaiting = false;
		for(Package<?> p : new LinkedList<>(eventList)){
			if(p.tryFinish(event)){
				if(p.isAsync() && p.getSuccessConsumer() != null){
					eventList.remove(p);
					asyncExecutor.execute(() -> p.getSuccessConsumer().accept(event));
				}
				wasWaiting = true;
			}
		}
		return wasWaiting;
	}

	/**
	 * Used for internal processing
	 *
	 * @param <E>
	 */
	public static class Package<E extends GenericEvent>{

		private final Class<E> classToWait;
		private final Predicate<E> condition;
		private final boolean isAsync;
		private E event;
		private Consumer<E> onSuccess;
		private Consumer<Exception> onFailure;
		private Future<?> cancelFuture = null;

		/**
		 * Creates a new object of this class
		 *
		 * @param classToWait wait for objects of this class
		 * @param condition   to check wether the object is the desired one
		 */
		protected Package(Class<E> classToWait, Predicate<E> condition){
			this.classToWait = classToWait;
			this.condition = condition;
			this.isAsync = false;
		}

		/**
		 * Creates a new object of this class
		 *
		 * @param classToWait wait for objects of this class
		 * @param condition   to check wether the object is the desired one
		 */
		protected Package(Class<E> classToWait, Predicate<E> condition, Consumer<E> onSuccess, Consumer<Exception> onFailure){
			this.classToWait = classToWait;
			this.condition = condition;
			this.isAsync = true;
			this.onSuccess = onSuccess;
			this.onFailure = onFailure;
		}

		/**
		 * Tries to serve the event to this object
		 *
		 * @param event the event
		 *
		 * @return true on success
		 */
		@SuppressWarnings({"unchecked"})
		public synchronized boolean tryFinish(GenericEvent event){
			if(classToWait == event.getClass() && this.event == null){
				if(condition.test((E) event)){
					this.event = (E) event;
					if(this.cancelFuture != null){
						cancelFuture.cancel(true);
					}
					this.notify();
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns the event
		 *
		 * @return event
		 */
		public synchronized E getEvent(){
			return event;
		}

		public void setDeSchedulerFuture(Future<?> future){
			this.cancelFuture = future;
		}

		/**
		 * Whether or not this is wants async execution
		 *
		 * @return boolean
		 */
		public boolean isAsync(){
			return isAsync;
		}

		/**
		 * Returns the success consumer
		 *
		 * @return consumer
		 */
		@SuppressWarnings({"unchecked"})
		public Consumer<GenericEvent> getSuccessConsumer(){
			return (Consumer<GenericEvent>) onSuccess;
		}

		/**
		 * Returns the failure consumer
		 *
		 * @return consumer
		 */
		public Consumer<Exception> getFailureConsumer(){
			return onFailure;
		}

	}

}
