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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CmdArgFactory {

    /**
     * Builds the CmdArgs from a given list of arguments and definitions
     *
     * If argsList > argDefs all additional args will be set as optional strings
     * If argsList < argDefs all missing args will be created as null
     *
     * @param argList list of strings
     * @param argDefs list of definitions
     * @return CmdArgs
     */
    public static CmdArgs getArgs(List<String> argList, List<CmdArgDef> argDefs){
        int i = 0;
        CmdArgs cmdArgs = new CmdArgs();
        for(CmdArgDef def : argDefs){
            if(i >= argList.size()){
                cmdArgs.add(new CmdArg<>(def, null));
            }else{
                cmdArgs.add(new CmdArg<>(def, fromString(argList.get(i), def.getAClass())));
            }
            i++;
        }
        while (i < argList.size()){
            CmdArgDef def = new CmdArgDef.Builder<>("unknown_"+i, String.class).buildAnyMatch().setOptional(true);
            cmdArgs.add(new CmdArg(def, argList.get(i)));
            i++;
        }
        return cmdArgs;
    }

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Tries to convert the string into any of the known types
     *
     * @param string input
     * @param tClass target class
     * @param <T> return this object
     * @return T or null if its an unknown class
     */
    private static <T> T fromString(String string, Class<T> tClass){
        if(Boolean.class.equals(tClass)){
            return (T) (Boolean) Boolean.parseBoolean(string);
        }else if(Integer.class.equals(tClass)){
            return (T) (Integer) Integer.parseInt(string);
        }else if(Double.class.equals(tClass)){
            return (T) (Double) Double.parseDouble(string);
        }else if(Float.class.equals(tClass)){
            return (T) (Float) Float.parseFloat(string);
        }else if(Long.class.equals(tClass)){
            return (T) (Long) Long.parseLong(string);
        }else if(String.class.equals(tClass)){
            return (T) (String) string;
        }else if(LocalDateTime.class.equals(tClass)){
            return (T) (LocalDateTime) LocalDateTime.parse(string, DTF);
        }else if(LocalDate.class.equals(tClass)){
            return (T) (LocalDate) LocalDate.parse(string, DF);
        }else{
            return null;
        }
    }

}
