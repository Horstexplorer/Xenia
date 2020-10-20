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
import de.netbeacon.xenia.backend.client.objects.external.misc.Tag;
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;

import java.util.List;

public class HYBRIDTag extends HybridCommand {

    public HYBRIDTag(CommandGroup parent) {
        super(parent,"tag", "Create, manage and use tags for this guild", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, List.of("tag_name"));
        addChildCommand(new CMDCreate());
        addChildCommand(new CMDModify());
        addChildCommand(new CMDDelete());
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        TagCache tagCache = commandEvent.backendDataPack().getbGuild().getMiscCaches().getTagCache();
        try{
            Tag tag = tagCache.get(args.get(0));
            commandEvent.getEvent().getChannel().sendMessage(onSuccess(tag.getTagContent()+"\n\n"+"*Tag Created By "+tag.getUserId()+"*")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Tag "+args.get(0)+" Not Found")).queue();
        }
    }
}
