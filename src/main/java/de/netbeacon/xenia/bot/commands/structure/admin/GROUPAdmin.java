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

package de.netbeacon.xenia.bot.commands.structure.admin;

import de.netbeacon.xenia.bot.commands.objects.CommandGroup;

/**
 * Contains all commands providing some admin features to the developer
 */
public class GROUPAdmin extends CommandGroup {

    public GROUPAdmin() {
        super(null, "admin");
        addChildCommand(new CMDEval());
        addChildCommand(new CMDInfo());
        addChildCommand(new CMDPing());
        addChildCommand(new CMDClientIdentify());
        addChildCommand(new CMDGlobalInfo());
        addChildCommand(new CMDChatlog());
        addChildCommand(new CMDD43Z1());
        addChildCommand(new CMDD43Z1Stats());
    }

}
