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

package de.netbeacon.xenia.bot.commands.objects.misc.cmdargs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CmdArgDef<T> {

    private final String name;
    private final Class<T> aClass;
    private boolean isOptional;
    private final Predicate<T> predicate;

    private CmdArgDef(String name, Class<T> aClass, Predicate<T> predicate){
        this.name = name;
        this.aClass = aClass;
        this.predicate = predicate;
    }

    public Class<T> getAClass() {
        return aClass;
    }

    public String getName() {
        return name;
    }

    public Predicate<T> getPredicate() {
        return predicate;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public CmdArgDef<T> setOptional(boolean optional){
        this.isOptional = optional;
        return this;
    }

    public boolean test(T t){
        return predicate.test(t);
    }

    public static class Builder<T> {

        private final String name;
        private final Class<T> aClass;
        private final List<Predicate<T>> predicates = new ArrayList<>();

        public Builder(String name, Class<T> aClass){
            this.name = name;
            this.aClass = aClass;
        }

        public Builder<T> predicateAddEquals(T object){
            predicates.add(t->{
                if(object instanceof Number && object instanceof Comparable && t instanceof Number && t instanceof Comparable){
                    return ((Comparable<T>) object).compareTo(t) == 0;
                }else{
                    return object.equals(t);
                }
            });
            return this;
        }

        public Builder<T> predicateAddMinValue(T object){
            predicates.add(t->{
                if(object instanceof Number && object instanceof Comparable && t instanceof Number && t instanceof Comparable){
                    return ((Comparable<T>) object).compareTo(t) <= 0;
                }
                return true; // its not a number so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddMaxValue(T object){
            predicates.add(t->{
                if(object instanceof Number && object instanceof Comparable && t instanceof Number && t instanceof Comparable){
                    return ((Comparable<T>) object).compareTo(t) >= 0;
                }
                return true; // its not a number so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddValueRange(T min, T max){
            predicates.add(t->{
                if(min instanceof Number && min instanceof Comparable && max instanceof Number && max instanceof Comparable && t instanceof Number && t instanceof Comparable){
                    return ((Comparable<T>) max).compareTo(t) >= 0 && ((Comparable<T>) min).compareTo(t) <= 0;
                }
                return true; // its not a number so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddEqualsAnyOf(T...objects){
            predicates.add(t->{
                for(T object : objects){
                    if(object instanceof Number && object instanceof Comparable && t instanceof Number && t instanceof Comparable){
                        if(((Comparable<T>) object).compareTo(t) == 0){
                            return true;
                        }
                    }else{
                        if(object.equals(t)){
                            return true;
                        }
                    }
                }
                return false;
            });
            return this;
        }

        public Builder<T> predicateAddStringMinLength(int length){
            predicates.add(t->{
                if(t instanceof String){
                    return ((String) t).length() >= length;
                }
                return true; // its not a string so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddStringMaxLength(int length){
            predicates.add(t->{
                if(t instanceof String){
                    return ((String) t).length() <= length;
                }
                return true; // its not a string so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddStringLengthRange(int min, int max){
            predicates.add(t->{
                if(t instanceof String){
                    return ((String) t).length() >= min && ((String) t).length() <= max;
                }
                return true; // its not a string so we dont know - assume true (this is not great but should do for us)
            });
            return this;
        }

        public Builder<T> predicateAddPredicate(Predicate<T> predicate){
            predicates.add(predicate);
            return this;
        }

        public CmdArgDef<T> buildAnyMatch(){
            Predicate<T> predicate = null;
            for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
                if(predicate == null){
                    predicate = tPredicate;
                }else{
                    predicate = predicate.or(tPredicate);
                }
            }
            if(predicate == null){
                predicate = t -> true;
            }
            return new CmdArgDef<T>(name, aClass, predicate);
        }

        public CmdArgDef<T> buildAllMatch(){
            Predicate<T> predicate = null;
            for(Predicate<T> tPredicate : new ArrayList<>(predicates)){
                if(predicate == null){
                    predicate = tPredicate;
                }else{
                    predicate = predicate.and(tPredicate);
                }
            }
            if(predicate == null){
                predicate = t -> true;
            }
            return new CmdArgDef<T>(name, aClass, predicate);
        }
    }
}
