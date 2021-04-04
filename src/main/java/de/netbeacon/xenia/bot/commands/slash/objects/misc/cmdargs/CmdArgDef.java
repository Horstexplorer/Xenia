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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CmdArgDef<T> {

    private final String name;
    private final String description;
    private final String extendedDescription;
    private final Class<T> tClass;
    private final Predicate<T> predicate;
    private final CommandUpdateAction.OptionData optionData;
    private final Parser<T> parser;

    protected CmdArgDef(String name, String description, String extendedDescription, Class<T> tClass, Predicate<T> predicate, CommandUpdateAction.OptionData optionData, Parser<T> parser){
        this.name = name;
        this.description = description;
        this.extendedDescription = extendedDescription;
        this.tClass = tClass;
        this.predicate = predicate;
        this.optionData = optionData;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

     public String getExtendedDescription(){
        return extendedDescription;
     }

    public Class<T> getTClass() {
        return tClass;
    }

    public Predicate<T> getPredicate() {
        return predicate;
    }

    public CommandUpdateAction.OptionData getOptionData() {
        return optionData;
    }

    public Parser<T> getParser() {
        return parser;
    }

    public static class Builder<T> {

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
            }else{
                this.extendedDescription = extendedDescription;
            }
            this.tClass = tClass;
        }

        public Builder<T> setOptional(boolean value){
            isOptional = value;
            return this;
        }

        public CmdArgDef<T> build(boolean anyMatch){
            // prepare option data
            Command.OptionType optionType = getOptionTypeFor(tClass);
            CommandUpdateAction.OptionData optionData = new CommandUpdateAction.OptionData(optionType, name, description)
                    .setRequired(!isOptional);
            int i = 0;
            choises.forEach(c -> optionData.addChoice(String.valueOf(c),i));
            // build predicate
            Predicate<T> predicate = null;
            for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
                if(predicate == null){ predicate = tPredicate; }
                else{
                    if(anyMatch) { predicate = predicate.or(tPredicate); }
                    else { predicate = predicate.and(tPredicate); }
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

        private Command.OptionType getOptionTypeFor(Class<T> tClass){
            if(tClass.equals(Boolean.class)){
                return Command.OptionType.BOOLEAN;
            }else if(tClass.equals(Short.class) || tClass.equals(Integer.class) || tClass.equals(Long.class)){
                return Command.OptionType.INTEGER;
            }else if(tClass.equals(AbstractChannel.class) || tClass.equals(GuildChannel.class) || tClass.equals(MessageChannel.class) || tClass.equals(PrivateChannel.class)){
                return Command.OptionType.CHANNEL;
            }else if(tClass.equals(Role.class)){
                return Command.OptionType.ROLE;
            }else if(tClass.equals(User.class) || tClass.equals(Member.class)){
                return Command.OptionType.USER;
            }else{
                return Command.OptionType.STRING;
            }
        }

    }

    public static class Parser<T> {

        private final Function<SlashCommandEvent.OptionData, Object> unwrap;
        private final Function<Object, T> parse;

        private Parser(Function<SlashCommandEvent.OptionData, Object> unwrap, Function<Object, T> parse){
            this.unwrap = unwrap;
            this.parse = parse;
        }

        public T parse(SlashCommandEvent.OptionData optionData) throws Exception {
            try{
                return parse.apply(unwrap.apply(optionData));
            }catch (java.lang.Exception e){
                throw new Exception(e);
            }
        }

        public static class Builder<T> {

            private Function<SlashCommandEvent.OptionData, Object> unwrap;
            private Function<Object, T> parse;

            public Parser<T> from(Command.OptionType from, Class<T> to){
                var unwrapped = getUnwrapStrategy(from, to);
                unwrap = unwrapped.getValue2();
                parse = getParseStrategy(unwrapped.getValue1(), to);
                return new Parser<>(unwrap, parse);
            }

            private Pair<Class, Function<SlashCommandEvent.OptionData, Object>> getUnwrapStrategy(Command.OptionType optionType, Class<T> toFav){
                switch (optionType){
                    case BOOLEAN:
                        return new Pair<>(Boolean.class, (o) -> o.getAsBoolean());
                    case INTEGER:
                        return new Pair<>(Long.class, (o) -> o.getAsLong());
                    case USER:
                        if(toFav.equals(Member.class)){
                            return new Pair<>(Member.class, (o) -> o.getAsMember());
                        }else{
                            return new Pair<>(User.class, (o) -> o.getAsUser());
                        }
                    case CHANNEL:
                        if(toFav.equals(GuildChannel.class)){
                            return new Pair<>(GuildChannel.class, (o) -> o.getAsGuildChannel());
                        }else if(toFav.equals(MessageChannel.class)){
                            return new Pair<>(MessageChannel.class, (o) -> o.getAsMessageChannel());
                        }else if(toFav.equals(PrivateChannel.class)){
                            return new Pair<>(PrivateChannel.class, (o) -> o.getAsPrivateChannel());
                        }else{
                            return new Pair<>(AbstractChannel.class, (o) -> o.getAsChannel());
                        }
                    case ROLE:
                        return new Pair<>(Role.class, (o) -> o.getAsRole());
                    case UNKNOWN:
                    case STRING:
                    default:
                        return new Pair<>(String.class, (o) -> o.getAsString());
                }
            }

            private Function<Object, T> getParseStrategy(Class<?> in, Class<T> out){
                if(in.equals(out)){
                    return (o) -> (T) o;
                }else if(Boolean.class.equals(out)){ // shouldnt be needed
                    return (o) -> (T) Boolean.valueOf(String.valueOf(o));
                }else if(Integer.class.equals(out)){
                    return (o) -> (T) (Integer) Integer.parseInt(String.valueOf(o));
                }else if(Double.class.equals(out)){
                    return (o) -> (T) (Double) Double.parseDouble(String.valueOf(o));
                }else if(Float.class.equals(out)){
                    return (o) -> (T) (Float) Float.parseFloat(String.valueOf(o));
                }else if(Long.class.equals(out)){ // shouldnt be needed
                    return (o) -> (T) (Long) Long.parseLong(String.valueOf(o));
                }else if(String.class.equals(out)){ // shouldnt be needed
                    return (o) -> (T) String.valueOf(o);
                }else if(LocalDateTime.class.equals(out)){
                    return (o) -> (T) (LocalDateTime) LocalDateTime.parse(String.valueOf(o), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }else if(LocalDate.class.equals(out)){
                    return (o) -> (T) (LocalDate) LocalDate.parse(String.valueOf(o), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }else if(HumanTime.class.equals(out)){
                    return (o) -> (T) (HumanTime) HumanTime.parse(String.valueOf(o));
                }else{ // might cause issues but we should notice at some point later
                    return (o) -> (T) o;
                }
            }
        }

        public static class Exception extends java.lang.Exception {
            public Exception(java.lang.Exception e){
                super(e);
            }
        }
    }

}
