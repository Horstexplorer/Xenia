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
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.external.misc.Tag;
import de.netbeacon.xenia.bot.commands.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.TAG_NAME_DEF;

public class HYBRIDTag extends HybridCommand {

    public HYBRIDTag() {
        super(null,"tag", new CommandCooldown(CommandCooldown.Type.User, 1000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.TAG_USE)),
                List.of(TAG_NAME_DEF)
        );
        addChildCommand(new CMDCreate());
        addChildCommand(new CMDModify());
        addChildCommand(new CMDDelete());
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) {
        TagCache tagCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTagCache();
        CmdArg<String> tagA = cmdArgs.getByIndex(0);
        try{
            Tag tag = tagCache.get(tagA.getValue());
            commandEvent.getEvent().getChannel().sendMessage(translationPackage.getTranslationWithPlaceholders(getClass().getName()+".response.error.msg", tag.getTagContent(), tag.getId())).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass().getName()+".response.error.msg", tagA.getValue()))).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
        }
    }
}
