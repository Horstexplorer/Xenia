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
import java.util.HashMap;

public class CmdArgs {

    private final HashMap<String, CmdArg> argHashMap = new HashMap<>();
    private final ArrayList<CmdArg> cmdArgs = new ArrayList<>();

    public CmdArgs(){}

    public CmdArgs(CmdArg...args){
        for(CmdArg arg : args){
            if(argHashMap.containsKey(arg.getArgDef().getName())){
                continue;
            }
            argHashMap.put(arg.getArgDef().getName(), arg);
            cmdArgs.add(arg);
        }
    }

    public void add(CmdArg...args){
        for(CmdArg arg : args){
            if(argHashMap.containsKey(arg.getArgDef().getName().toLowerCase())){
                continue;
            }
            argHashMap.put(arg.getArgDef().getName().toLowerCase(), arg);
            cmdArgs.add(arg);
        }
    }

    public void remove(CmdArg...args){
        for(CmdArg arg : args){
            if(!argHashMap.containsKey(arg.getArgDef().getName())){
                continue;
            }
            CmdArg cmdArg = argHashMap.get(arg.getArgDef().getName());
            argHashMap.remove(arg.getArgDef().getName(), arg);
            cmdArgs.remove(cmdArg);
        }
    }

    public <T extends CmdArg> T getByIndex(int index){
        return (T) cmdArgs.get(index);
    }

    public <T extends CmdArg> T getByName(String name){
        return (T) argHashMap.get(name.toLowerCase());
    }

    public boolean verify(){
        for(CmdArg arg : cmdArgs){
            if(!arg.verify()){
                return false;
            }
        }
        return true;
    }

    public int size(){
        return argHashMap.size();
    }
}
