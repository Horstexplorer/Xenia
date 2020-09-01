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

package de.netbeacon.xenia.commands.structure.setup;

import de.netbeacon.xenia.commands.objects.CommandGroup;

/**
 * Contains all commands used to setup the bot properly
 */
public class GROUPSetup extends CommandGroup {

    public GROUPSetup(CommandGroup parent) {
        super(parent, "setup", "Contains commands used to customize the experience using me");
    }

}
