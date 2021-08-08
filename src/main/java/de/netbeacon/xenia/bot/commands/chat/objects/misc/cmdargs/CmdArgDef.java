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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CmdArgDef<T>{

	private final String name;
	private final Class<T> aClass;
	private final Predicate<T> predicate;
	private final String predicateAsString;
	private boolean isOptional;

	/**
	 * Creates a new instance of this class
	 *
	 * @param name              name of the argument
	 * @param aClass            of the target type
	 * @param predicate         to find matching values
	 * @param predicateAsString what the requirements are for this arg in a human-readable format
	 */
	private CmdArgDef(String name, Class<T> aClass, Predicate<T> predicate, String predicateAsString){
		this.name = name;
		this.aClass = aClass;
		this.predicate = predicate;
		this.predicateAsString = predicateAsString;
	}

	/**
	 * Returns the class of the target type of the argument
	 *
	 * @return class
	 */
	public Class<T> getAClass(){
		return aClass;
	}

	/**
	 * Returns the name of the argument
	 *
	 * @return name
	 */
	public String getName(){
		return name;
	}

	/**
	 * Returns the predicate
	 *
	 * @return predicate
	 */
	public Predicate<T> getPredicate(){
		return predicate;
	}

	/**
	 * Returns whether this arg value is optional or not
	 *
	 * @return boolean
	 */
	public boolean isOptional(){
		return isOptional;
	}

	/**
	 * Changes whether this arg is optional or not
	 *
	 * @param optional boolean
	 *
	 * @return this
	 */
	public CmdArgDef<T> setOptional(boolean optional){
		this.isOptional = optional;
		return this;
	}

	/**
	 * Used to test the predicate against an object
	 *
	 * @param t object
	 *
	 * @return boolean - true if allowed
	 */
	public boolean test(T t){
		return predicate.test(t);
	}

	/**
	 * Used to retrieve some human-readable information about the string
	 *
	 * @return String
	 */
	public String getPredicateAsString(){
		return predicateAsString;
	}

	public static class Builder<T>{

		private final String name;
		private final Class<T> aClass;
		private final List<Predicate<T>> predicates = new ArrayList<>();
		private String predicateAsString = "No description provided";

		/**
		 * Creates a new instance of this class
		 *
		 * @param name   argument name
		 * @param aClass of the target type
		 */
		public Builder(String name, Class<T> aClass){
			this.name = name;
			this.aClass = aClass;
		}

		/**
		 * Add a predicate to check if the test object matches this object
		 *
		 * @param object object which needs to be matched
		 *
		 * @return this
		 */
		public Builder<T> predicateAddEquals(T object){
			predicates.add(t -> {
				if(object instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) object).compareTo(t) == 0;
				}
				else{
					return object.equals(t);
				}
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test object is larger or equal to this object
		 *
		 * @param object object which needs to be matched
		 *
		 * @return this
		 */
		public Builder<T> predicateAddMinValue(T object){
			predicates.add(t -> {
				if(object instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) object).compareTo(t) <= 0;
				}
				return true; // it's not a number, so we don't know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test object is smaller or equal to this object
		 *
		 * @param object object which needs to be matched
		 *
		 * @return this
		 */
		public Builder<T> predicateAddMaxValue(T object){
			predicates.add(t -> {
				if(object instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) object).compareTo(t) >= 0;
				}
				return true; // its not a number so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test object is in range of those objects
		 *
		 * @param min object which needs to be matched
		 * @param max object which needs to be matched
		 *
		 * @return this
		 */
		public Builder<T> predicateAddValueRange(T min, T max){
			predicates.add(t -> {
				if(min instanceof Comparable && max instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) max).compareTo(t) >= 0 && ((Comparable<T>) min).compareTo(t) <= 0;
				}
				return true; // its not a number so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test object matches any of the given objects
		 *
		 * @param objects where any of em need to match the test one
		 *
		 * @return this
		 */
		public Builder<T> predicateAddEqualsAnyOf(T... objects){
			predicates.add(t -> {
				for(T object : objects){
					if(object instanceof Comparable && t instanceof Comparable){
						if(((Comparable<T>) object).compareTo(t) == 0){
							return true;
						}
					}
					else{
						if(object.equals(t)){
							return true;
						}
					}
				}
				return false;
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test string is longer or equal to the given length
		 *
		 * @param length string length
		 *
		 * @return this
		 */
		public Builder<T> predicateAddStringMinLength(int length){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() >= length;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test string is smaller or equal to the given length
		 *
		 * @param length string length
		 *
		 * @return this
		 */
		public Builder<T> predicateAddStringMaxLength(int length){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() <= length;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test string length is within the given range
		 *
		 * @param min string length
		 * @param max string length
		 *
		 * @return this
		 */
		public Builder<T> predicateAddStringLengthRange(int min, int max){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() >= min && ((String) t).length() <= max;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		/**
		 * Add a predicate to check if the test string matches any of the given strings ignoring case sensitivity
		 *
		 * @param strings where any of em need to match the test one
		 *
		 * @return this
		 */
		public Builder<T> predicateAddStringEqualsAnyOf(String... strings){
			predicates.add(t -> {
				for(String string : strings){
					if(string != null && t instanceof String){
						if(string.equalsIgnoreCase((String) t)){
							return true;
						}
					}
					else{
						return false;
					}
				}
				return false;
			});
			return this;
		}

		/**
		 * Add a custom predicate
		 *
		 * @param gen       function to get Q from T
		 * @param compareTo Q
		 * @param result    int
		 *
		 * @return this
		 */
		public <Q> Builder<T> predicateAddCompare(Function<T, Q> gen, Q compareTo, int result){
			predicates.add(t -> {
				Q res = gen.apply(t);
				if(res instanceof Comparable && compareTo instanceof Comparable){
					return ((Comparable<Q>) compareTo).compareTo(res) == result;
				}
				else{
					return compareTo.equals(res);
				}
			});
			return this;
		}

		/**
		 * Add a custom predicate
		 *
		 * @param predicate to match
		 *
		 * @return this
		 */
		public Builder<T> predicateAddPredicate(Predicate<T> predicate){
			predicates.add(predicate);
			return this;
		}

		public Builder<T> addPredicateDescription(String description){
			this.predicateAsString = description;
			return this;
		}

		/**
		 * Builds the argument definition
		 * <p>
		 * Any of the predicates need to match
		 *
		 * @return argument definition
		 */
		public CmdArgDef<T> buildAnyMatch(){
			Predicate<T> predicate = null;
			for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
				if(predicate == null){
					predicate = tPredicate;
				}
				else{
					predicate = predicate.or(tPredicate);
				}
			}
			if(predicate == null){
				predicate = t -> true;
			}
			return new CmdArgDef<>(name, aClass, predicate, predicateAsString);
		}

		/**
		 * Builds the argument definition
		 * <p>
		 * All of the predicates need to match
		 *
		 * @return argument definition
		 */
		public CmdArgDef<T> buildAllMatch(){
			Predicate<T> predicate = null;
			for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
				if(predicate == null){
					predicate = tPredicate;
				}
				else{
					predicate = predicate.and(tPredicate);
				}
			}
			if(predicate == null){
				predicate = t -> true;
			}
			return new CmdArgDef<>(name, aClass, predicate, predicateAsString);
		}

	}

}
