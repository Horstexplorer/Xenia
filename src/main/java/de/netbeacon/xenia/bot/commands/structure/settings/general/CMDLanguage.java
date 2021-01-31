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

package de.netbeacon.xenia.bot.commands.structure.settings.general;

import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.GUILD_LANGUAGE_ID_DEF;

public class CMDLanguage extends Command {

    public CMDLanguage() {
        super("language", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                new HashSet<>(List.of(Permission.MANAGE_SERVER)),
                new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
                List.of(GUILD_LANGUAGE_ID_DEF)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) {
        CmdArg<String> langIdA = args.getByIndex(0);
        String languageId = langIdA.getValue();
        TranslationPackage translationPackage1 = TranslationManager.getInstance().getTranslationPackage(languageId);
        try{
            if(translationPackage1 == null){
                throw new RuntimeException();
            }
            commandEvent.getBackendDataPack().getbGuild().setPreferredLanguage(translationPackage1.getLanguageId());
            commandEvent.getEvent().getChannel().sendMessage(onSuccess(translationPackage, translationPackage.getTranslation(getClass().getName()+".response.success.msg"))).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslationWithPlaceholders(getClass().getName()+".response.error.msg", Arrays.toString(TranslationManager.getInstance().getLanguageIds().toArray())))).queue(s->s.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
        }
    }
}
