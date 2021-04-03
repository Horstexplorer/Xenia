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

package de.netbeacon.xenia.bot.commands.slash.structure.tag.s;

import de.netbeacon.xenia.backend.client.objects.cache.misc.TagCache;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.Command;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import java.util.HashSet;
import java.util.List;

public class CMDCreate extends Command {

    public CMDCreate() {
        super("create", "Creates a new tag with a given tag name and content", new CommandCooldown(CommandCooldown.Type.User, 10000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.TAG_CREATE)),
                new CommandUpdateAction.OptionData(net.dv8tion.jda.api.entities.Command.OptionType.STRING, "name", "Tag name").setRequired(true),
                new CommandUpdateAction.OptionData(net.dv8tion.jda.api.entities.Command.OptionType.STRING, "content", "Tag content").setRequired(true)
        );
    }

    @Override
    public void onExecution(CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception {
        TagCache tagCache = commandEvent.getBackendDataPack().getbGuild().getMiscCaches().getTagCache();
        String name = commandEvent.getEvent().getOption("name").getAsString();
        String content = commandEvent.getEvent().getOption("content").getAsString();
        try{
            tagCache.createNew(name, commandEvent.getEvent().getUser().getIdLong(), content);
            commandEvent.getEvent().reply(onSuccess(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass(), "response.success.msg", name))).queue();
        }catch (CacheException e){
            if(e.getType().equals(CacheException.Type.ALREADY_EXISTS)){
                commandEvent.getEvent().reply(onError(translationPackage, translationPackage.getTranslation(getClass(), "response.error.msg"))).queue();
            }else{
                throw e;
            }
        }
    }
}
