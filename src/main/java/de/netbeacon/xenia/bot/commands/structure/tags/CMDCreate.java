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
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Creates a new tag with a given tag name and content", new CommandCooldown(CommandCooldown.Type.Guild, 10000), null, null, List.of("tag_name", "\"content\""));
    }

    private final static Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]*$");

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        TagCache tagCache = commandEvent.backendDataPack().getbGuild().getMiscCaches().getTagCache();
        try{
            if(!KEY_PATTERN.matcher(args.get(0)).matches() || args.get(0).length() > 32){
                commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Create Tag. Tag Name Can Only Be 32 Chars Long And Support Chars, Numbers And Underscores")).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
                return;
            }
            if(args.get(1).length() > 1500){
                commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Create Tag. Content Cant Be Longer Than 1500 Chars")).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
                return;
            }
            tagCache.createNew(args.get(0), commandEvent.getEvent().getAuthor().getIdLong(), args.get(1));
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Tag Created")).queue();;
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Failed To Create Tag "+args.get(0))).queue();
        }
    }
}
