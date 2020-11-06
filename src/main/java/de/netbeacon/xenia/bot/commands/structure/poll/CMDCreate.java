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

package de.netbeacon.xenia.bot.commands.structure.poll;

import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;

import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.*;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Create a new poll", new CommandCooldown(CommandCooldown.Type.Guild, 10000), null, null, List.of(POLL_DESCRIPTION_DEF, POLL_OPTION_DEF, POLL_OPTION_DEF, POLL_OPTION_OPT_DEF, POLL_OPTION_OPT_DEF));
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {

    }
}