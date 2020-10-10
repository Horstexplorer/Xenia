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

package de.netbeacon.xenia.bot.commands.structure.settings.guild;

import de.netbeacon.xenia.bot.commands.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.structure.settings.guild.info.CMDGuildInfo;
import de.netbeacon.xenia.bot.commands.structure.settings.guild.member.GROUPMember;
import de.netbeacon.xenia.bot.commands.structure.settings.guild.settings.CMDGuildSettings;

/**
 * Contains all commands regarding guild settings
 */
public class GROUPGuild extends CommandGroup{

    public GROUPGuild(CommandGroup parent) {
        super(parent, "guild", "Contains commands to change guild specific settings");
        addChildCommand(new GROUPMember(this));
        addChildCommand(new CMDGuildInfo());
        addChildCommand(new CMDGuildSettings());
    }

}
