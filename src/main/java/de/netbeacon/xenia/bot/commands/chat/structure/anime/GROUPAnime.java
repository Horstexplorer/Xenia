/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.commands.chat.structure.anime;

import de.netbeacon.xenia.bot.commands.chat.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.chat.structure.anime.nsfw.*;
import de.netbeacon.xenia.bot.commands.chat.structure.anime.sfw.CMDNeko;
import de.netbeacon.xenia.bot.commands.chat.structure.anime.sfw.CMDRandom;
import de.netbeacon.xenia.bot.commands.chat.structure.anime.sfw.*;

public class GROUPAnime extends CommandGroup {
    public GROUPAnime() {
        super(null, "anime", false);
        // SFW
        addChildCommand(new CMDBite());
        addChildCommand(new CMDBlush());
        addChildCommand(new CMDCry());
        addChildCommand(new CMDCuddle());
        addChildCommand(new CMDDance());
        addChildCommand(new CMDEevee());
        addChildCommand(new CMDFeed());
        addChildCommand(new CMDFluff());
        addChildCommand(new CMDHolo());
        addChildCommand(new CMDHug());
        addChildCommand(new CMDKiss());
        addChildCommand(new CMDKitsune());
        addChildCommand(new CMDLick());
        addChildCommand(new CMDNeko());
        addChildCommand(new CMDOokami());
        addChildCommand(new CMDPat());
        addChildCommand(new CMDPoke());
        addChildCommand(new CMDSenko());
        addChildCommand(new CMDSlap());
        addChildCommand(new CMDSmile());
        addChildCommand(new CMDTail());
        addChildCommand(new CMDTickle());
        addChildCommand(new CMDRandom());
        // NSFW
        addChildCommand(new CMDAnal());
        addChildCommand(new CMDBlowjob());
        addChildCommand(new CMDCum());
        addChildCommand(new CMDFuck());
        addChildCommand(new de.netbeacon.xenia.bot.commands.chat.structure.anime.nsfw.CMDNeko());
        addChildCommand(new CMDPussylick());
        addChildCommand(new CMDSolo());
        addChildCommand(new CMDThreesome_FFF());
        addChildCommand(new CMDThreesome_FFM());
        addChildCommand(new CMDThreesome_MMF());
        addChildCommand(new CMDYaoi());
        addChildCommand(new CMDYuri());
        addChildCommand(new de.netbeacon.xenia.bot.commands.chat.structure.anime.nsfw.CMDRandom());
    }
}
