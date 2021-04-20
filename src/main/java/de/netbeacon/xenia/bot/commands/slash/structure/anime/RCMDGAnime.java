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

package de.netbeacon.xenia.bot.commands.slash.structure.anime;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.slash.structure.anime.sfw.*;

public class RCMDGAnime extends Command {
    public RCMDGAnime() {
        super("anime", "Contains various anime themed commands", false,
            new CMDBite(), new CMDBlush(), new CMDCry(), new CMDCuddle(), new CMDDance(), new CMDFeed(), new CMDHug(), new CMDKiss(), new CMDPat(), new CMDPoke(), new CMDSlap(), new CMDSmile(), new CMDTickle()
          //  , new CMDAnal(), new CMDBlowjob(), new CMDCum(), new CMDFuck(), new CMDNeko(), new CMDPussylick(), new CMDSolo(), new CMDYaoi(), new CMDYuri()
        );
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception {}
}
