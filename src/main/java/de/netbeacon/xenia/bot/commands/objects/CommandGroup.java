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

package de.netbeacon.xenia.bot.commands.objects;

import de.netbeacon.xenia.bot.commands.global.help.CMDHelp;
import java.util.List;

/**
 * Acts as container for multiple commands
 */
public abstract class CommandGroup extends Command{

    private final CommandGroup parent;

    /**
     * Creates a new instance of this class
     *
     * @param parent if this group is located inside of another group this should be set accordingly, else null
     * @param alias of the command group
     * @param description of the command group
     */
    public CommandGroup(CommandGroup parent, String alias, String description){
        super(alias, description);
        this.parent = parent;
        addChildCommand(new CMDHelp(this));
    }

    /**
     * Returns the parent group
     *
     * @return parent group or null
     */
    public CommandGroup getParent(){
        return parent;
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {}
}