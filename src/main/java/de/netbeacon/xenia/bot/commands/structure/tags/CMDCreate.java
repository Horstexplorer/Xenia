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

package de.netbeacon.xenia.bot.commands.structure.tags;

import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Creates a new tag with a given tag name and content", new CommandCooldown(CommandCooldown.Type.Guild, 10000), null, null, List.of("tag_name", "\"content\""));
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        commandEvent.getEvent().getChannel().sendMessage("Create Not Implemented Yet").queue(m->m.delete().queueAfter(2000, TimeUnit.MILLISECONDS));
    }
}
