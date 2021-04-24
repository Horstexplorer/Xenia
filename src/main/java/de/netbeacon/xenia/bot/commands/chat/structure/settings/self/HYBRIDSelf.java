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

package de.netbeacon.xenia.bot.commands.chat.structure.settings.self;

import de.netbeacon.xenia.backend.client.objects.external.User;
import de.netbeacon.xenia.bot.commands.chat.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.chat.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;


public class HYBRIDSelf extends HybridCommand {

    public HYBRIDSelf(CommandGroup parent) {
        super(parent, "self", false, new CommandCooldown(CommandCooldown.Type.Guild, 2000),
                null,
                null,
                null,
                null
        );
        addChildCommand(new CMDLanguage());
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        User user = commandEvent.getBackendDataPack().getbUser();
        commandEvent.getEvent().getChannel().sendMessage(
                EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getAuthor())
                        .addField(translationPackage.getTranslation(getClass(), "response.field.1.title"), user.getMetaUsername(),false)
                        .addField(translationPackage.getTranslation(getClass(), "response.field.2.title"), String.valueOf(user.getId()),false)
                        .addField(translationPackage.getTranslation(getClass(), "response.field.3.title"), user.getInternalRole(),false)
                        .addField(translationPackage.getTranslation(getClass(), "response.field.4.title"), user.getPreferredLanguage(),false)
                        .addField(translationPackage.getTranslation(getClass(), "response.field.5.title"), translationPackage.getTranslationWithPlaceholders(getClass(), "response.field.5.content", user.getMetaIconUrl()), false)
                        .build()
        ).queue();
    }
}
