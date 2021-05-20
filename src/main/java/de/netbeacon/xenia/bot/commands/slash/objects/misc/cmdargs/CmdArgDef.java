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

package de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs;

import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.HumanTime;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CmdArgDef<T>{

	private final String name;
	private final String description;
	private final String extendedDescription;
	private final Class<T> tClass;
	private final Predicate<T> predicate;
	private final OptionData optionData;
	private final Parser<T> parser;

	protected CmdArgDef(String name, String description, String extendedDescription, Class<T> tClass, Predicate<T> predicate, OptionData optionData, Parser<T> parser){
		this.name = name;
		this.description = description;
		this.extendedDescription = extendedDescription;
		this.tClass = tClass;
		this.predicate = predicate;
		this.optionData = optionData;
		this.parser = parser;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public String getExtendedDescription(){
		return extendedDescription;
	}

	public Class<T> getTClass(){
		return tClass;
	}

	public Predicate<T> getPredicate(){
		return predicate;
	}

	public OptionData getOptionData(){
		return optionData;
	}

	public Parser<T> getParser(){
		return parser;
	}

	public static class Builder<T>{

		private final String name;
		private final String description;
		private final List<Predicate<T>> predicates = new ArrayList<>();
		private final List<T> choises = new ArrayList<>();
		private final String extendedDescription;
		private final Class<T> tClass;
		private boolean isOptional;

		public Builder(String name, String description, String extendedDescription, Class<T> tClass){
			this.name = name;
			this.description = description;
			if(extendedDescription == null){
				this.extendedDescription = description;
			}
			else{
				this.extendedDescription = extendedDescription;
			}
			this.tClass = tClass;
		}

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

		public Builder<T> predicateAddMinValue(T object){
			predicates.add(t -> {
				if(object instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) object).compareTo(t) <= 0;
				}
				return true; // its not a number so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		public Builder<T> predicateAddMaxValue(T object){
			predicates.add(t -> {
				if(object instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) object).compareTo(t) >= 0;
				}
				return true; // its not a number so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		public Builder<T> predicateAddValueRange(T min, T max){
			predicates.add(t -> {
				if(min instanceof Comparable && max instanceof Comparable && t instanceof Comparable){
					return ((Comparable<T>) max).compareTo(t) >= 0 && ((Comparable<T>) min).compareTo(t) <= 0;
				}
				return true; // its not a number so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

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
			choises.addAll(Arrays.asList(objects));
			return this;
		}

		public Builder<T> predicateAddStringMinLength(int length){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() >= length;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		public Builder<T> predicateAddStringMaxLength(int length){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() <= length;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

		public Builder<T> predicateAddStringLengthRange(int min, int max){
			predicates.add(t -> {
				if(t instanceof String){
					return ((String) t).length() >= min && ((String) t).length() <= max;
				}
				return true; // its not a string so we dont know - assume true (this is not great but should do for us)
			});
			return this;
		}

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
			choises.addAll(Arrays.asList((T[]) strings));
			return this;
		}

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

		public Builder<T> predicateAddPredicate(Predicate<T> predicate){
			predicates.add(predicate);
			return this;
		}

		public Builder<T> setOptional(boolean value){
			isOptional = value;
			return this;
		}

		public CmdArgDef<T> build(){
			return build(false);
		}

		public CmdArgDef<T> build(boolean anyMatch){
			// prepare option data
			OptionType optionType = getOptionTypeFor(tClass);
			OptionData optionData = new OptionData(optionType, name, description)
				.setRequired(!isOptional);
			int i = 0;
			choises.forEach(c -> optionData.addChoice(String.valueOf(c), i));
			// build predicate
			Predicate<T> predicate = null;
			for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
				if(predicate == null){
					predicate = tPredicate;
				}
				else{
					if(anyMatch){
						predicate = predicate.or(tPredicate);
					}
					else{
						predicate = predicate.and(tPredicate);
					}
				}
			}
			if(predicate == null){
				predicate = t -> true;
			}
			// build parser definition
			Parser<T> parser = new Parser.Builder<T>().from(optionType, tClass);
			// build def
			return new CmdArgDef<>(name, description, extendedDescription, tClass, predicate, optionData, parser);
		}

		private OptionType getOptionTypeFor(Class<T> tClass){
			if(tClass.equals(Boolean.class)){
				return OptionType.BOOLEAN;
			}
			else if(tClass.equals(Short.class) || tClass.equals(Integer.class) || tClass.equals(Long.class)){
				return OptionType.INTEGER;
			}
			else if(tClass.equals(AbstractChannel.class) || tClass.equals(GuildChannel.class) || tClass.equals(MessageChannel.class) || tClass.equals(PrivateChannel.class)){
				return OptionType.CHANNEL;
			}
			else if(tClass.equals(Role.class)){
				return OptionType.ROLE;
			}
			else if(tClass.equals(User.class) || tClass.equals(Member.class)){
				return OptionType.USER;
			}
			else{
				return OptionType.STRING;
			}
		}

	}

	public static class Parser<T>{

		private final Function<OptionMapping, Object> unwrap;
		private final Function<Object, T> parse;

		private Parser(Function<OptionMapping, Object> unwrap, Function<Object, T> parse){
			this.unwrap = unwrap;
			this.parse = parse;
		}

		public T parse(OptionMapping optionData) throws Exception{
			try{
				return parse.apply(unwrap.apply(optionData));
			}
			catch(java.lang.Exception e){
				throw new Exception(e);
			}
		}

		public static class Builder<T>{

			private Function<OptionMapping, Object> unwrap;
			private Function<Object, T> parse;

			public Parser<T> from(OptionType from, Class<T> to){
				var unwrapped = getUnwrapStrategy(from, to);
				unwrap = unwrapped.getValue2();
				parse = getParseStrategy(unwrapped.getValue1(), to);
				return new Parser<>(unwrap, parse);
			}

			private Pair<Class, Function<OptionMapping, Object>> getUnwrapStrategy(OptionType optionType, Class<T> toFav){
				switch(optionType){
					case BOOLEAN:
						return new Pair<>(Boolean.class, OptionMapping::getAsBoolean);
					case INTEGER:
						return new Pair<>(Long.class, OptionMapping::getAsLong);
					case USER:
						if(toFav.equals(Member.class)){
							return new Pair<>(Member.class, OptionMapping::getAsMember);
						}
						else if(toFav.equals(User.class)){
							return new Pair<>(User.class, OptionMapping::getAsUser);
						}
					case CHANNEL:
						if(toFav.equals(GuildChannel.class)){
							return new Pair<>(GuildChannel.class, OptionMapping::getAsGuildChannel);
						}
						else if(toFav.equals(MessageChannel.class)){
							return new Pair<>(MessageChannel.class, OptionMapping::getAsMessageChannel);
						}
					case ROLE:
						return new Pair<>(Role.class, OptionMapping::getAsRole);
					case UNKNOWN:
					case STRING:
					default:
						return new Pair<>(String.class, OptionMapping::getAsString);
				}
			}

			private Function<Object, T> getParseStrategy(Class<?> in, Class<T> out){
				if(in.equals(out)){
					return (o) -> (T) o;
				}
				else if(Boolean.class.equals(out)){ // shouldnt be needed
					return (o) -> (T) (Boolean) Boolean.valueOf(String.valueOf(o));
				}
				else if(Integer.class.equals(out)){
					return (o) -> (T) (Integer) Integer.parseInt(String.valueOf(o));
				}
				else if(Double.class.equals(out)){
					return (o) -> (T) (Double) Double.parseDouble(String.valueOf(o));
				}
				else if(Float.class.equals(out)){
					return (o) -> (T) (Float) Float.parseFloat(String.valueOf(o));
				}
				else if(Long.class.equals(out)){ // shouldnt be needed
					return (o) -> (T) (Long) Long.parseLong(String.valueOf(o));
				}
				else if(String.class.equals(out)){ // shouldnt be needed
					return (o) -> (T) String.valueOf(o);
				}
				else if(LocalDateTime.class.equals(out)){
					return (o) -> (T) (LocalDateTime) LocalDateTime.parse(String.valueOf(o), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				}
				else if(LocalDate.class.equals(out)){
					return (o) -> (T) (LocalDate) LocalDate.parse(String.valueOf(o), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				}
				else if(HumanTime.class.equals(out)){
					return (o) -> (T) (HumanTime) HumanTime.parse(String.valueOf(o));
				}
				else{ // might cause issues but we should notice at some point later
					return (o) -> (T) o;
				}
			}

		}

		public static class Exception extends java.lang.Exception{

			public Exception(java.lang.Exception e){
				super(e);
			}

		}

	}

}
