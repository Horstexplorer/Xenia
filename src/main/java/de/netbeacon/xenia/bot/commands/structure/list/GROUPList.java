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

package de.netbeacon.xenia.bot.commands.structure.list;

import de.netbeacon.xenia.bot.commands.objects.CommandGroup;

public class GROUPList extends CommandGroup {

    public GROUPList(CommandGroup parent) {
        super(parent, "list", "Contains all commands to show information about a user/member/role/guild");
        addChildCommand(new CMDGuild());
        addChildCommand(new CMDRole());
        addChildCommand(new CMDMember());
        addChildCommand(new CMDUser());
    }

}
