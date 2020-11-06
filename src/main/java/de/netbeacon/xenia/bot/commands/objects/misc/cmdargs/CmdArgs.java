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

public class CmdArgs {

    private final ArrayList<CmdArg> cmdArgs = new ArrayList<>();

    /**
     * Creates a new instance of this class
     */
    public CmdArgs(){}

    /**
     * Creates a new instance of this class
     *
     * @param args predefined arguements
     */
    public CmdArgs(CmdArg...args){
        for(CmdArg arg : args){
            cmdArgs.add(arg);
        }
    }

    /**
     * Adds arguments to this container
     *
     * @param args cmd args
     */
    public void add(CmdArg...args){
        for(CmdArg arg : args){
            cmdArgs.add(arg);
        }
    }

    /**
     * Removes arguments from this container
     *
     * @param args cmd args
     */
    public void remove(CmdArg...args){
        for(CmdArg arg : args){
            cmdArgs.remove(arg);
        }
    }

    /**
     * Returns the argument by a given index
     *
     * @param index index
     * @param <T> return type
     * @return CmdArg
     */
    public <T extends CmdArg> T getByIndex(int index){
        return (T) cmdArgs.get(index);
    }

    /**
     * Verifies that all argument definitions within this container are fulfilled.
     * @return true on success
     */
    public boolean verify(){
        for(CmdArg arg : cmdArgs){
            if(!arg.verify()){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of elements within this container
     * @return size
     */
    public int size(){
        return cmdArgs.size();
    }
}
