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

import de.netbeacon.xenia.backend.client.objects.cache.misc.TagCache;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CMDDelete extends Command {

    public CMDDelete() {
        super("delete", "Deletes an existing tag", new CommandCooldown(CommandCooldown.Type.User
                , 5000), null, null, List.of("tag_name"));
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        TagCache tagCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTagCache();
        try{
            tagCache.delete(args.get(0), commandEvent.getEvent().getAuthor().getIdLong());
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Tag "+args.get(0)+" Deleted")).queue();;
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Delete Tag "+args.get(0)+" Not Found / Not Owner")).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
        }
    }
}
