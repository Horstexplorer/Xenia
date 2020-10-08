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

package de.netbeacon.xenia.bot.commands.structure.settings;

import de.netbeacon.xenia.bot.commands.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.structure.settings.guild.GROUPGuild;
import de.netbeacon.xenia.bot.commands.structure.settings.user.GROUPUser;

/**
 * Contains all commands regarding settings
 */
public class GROUPSettings extends CommandGroup{

    public GROUPSettings(CommandGroup parent) {
        super(parent, "settings", "Contains commands to change the settings for users and guilds");
        addChildCommand(new GROUPGuild(this));
        addChildCommand(new GROUPUser(this));
    }

}
