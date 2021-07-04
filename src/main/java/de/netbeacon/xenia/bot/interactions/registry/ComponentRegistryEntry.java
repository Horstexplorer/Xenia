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

package de.netbeacon.xenia.bot.interactions.registry;

import de.netbeacon.xenia.bot.interactions.records.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class ComponentRegistryEntry{

	private final String id = IDGen.genString(100);

	private final Origin origin;
	private final Accessor accessor;
	private final Activations activations;
	private final TimeoutPolicy timeoutPolicy;
	private final ActionHandler actionHandler;
	private final ExceptionHandler exceptionHandler;
	private final DeactivationMode deactivationMode;
	private final AtomicBoolean deactivated = new AtomicBoolean(false);
	private int remainingActivations;
	private ComponentInteractionRegistry registry;

	public ComponentRegistryEntry(Origin origin, Accessor accessor, Activations activations, TimeoutPolicy timeoutPolicy, ActionHandler actionHandler, ExceptionHandler exceptionHandler, DeactivationMode deactivationMode){
		this.origin = origin;
		this.accessor = accessor;
		this.activations = activations;
		this.remainingActivations = activations.activations();
		this.timeoutPolicy = timeoutPolicy;
		this.actionHandler = actionHandler;
		this.exceptionHandler = exceptionHandler;
		this.deactivationMode = deactivationMode;
	}

	public String getId(){
		return id;
	}


	public Origin getOrigin(){
		return origin;
	}

	public Accessor getAccessor(){
		return accessor;
	}

	public Activations getActivations(){
		return activations;
	}

	public TimeoutPolicy getTimeoutPolicy(){
		return timeoutPolicy;
	}

	public ActionHandler getActionHandler(){
		return actionHandler;
	}

	public ExceptionHandler getExceptionHandler(){
		return exceptionHandler;
	}

	public DeactivationMode getDeactivationHandler(){
		return deactivationMode;
	}

	public ComponentInteractionRegistry getRegistry(){
		return registry;
	}

	public void setRegistry(ComponentInteractionRegistry registry){
		this.registry = registry;
	}

	public int getRemainingActivations(){
		return remainingActivations;
	}

	public synchronized boolean getActivation(){
		if(activations.equals(Activations.UNLIMITED)){
			return true;
		}
		if(remainingActivations > 0){
			remainingActivations--;
			return true;
		}
		return false;
	}

	public synchronized boolean markDeactivated(){
		return deactivated.compareAndSet(false, true);
	}


	public boolean isValid(){
		return !deactivated.get() && timeoutPolicy.isInTime() && (remainingActivations > 0 || activations.equals(Activations.UNLIMITED));
	}

	public static class Exception extends RuntimeException{

		private final Type type;

		public Exception(Type type){
			super(type.name());
			this.type = type;
		}

		public Type getType(){
			return type;
		}

		public enum Type{
			ORIGIN,
			ACCESSOR,
			OUTDATED
		}

	}

}
