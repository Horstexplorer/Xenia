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

package de.netbeacon.xenia.bot.commands.chat.structure.info;

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.xenia.backend.client.objects.external.system.Info;
import de.netbeacon.xenia.bot.commands.chat.objects.Command;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;


/**
 * Basic information about the bot
 */
public class CMDInfo extends Command {

    public CMDInfo() {
        super("info", false, new CommandCooldown(CommandCooldown.Type.User, 1000),null, null,null, null);
    }

    @Override
    public void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getAuthor())
                .addField("Xenia", AppInfo.get("buildVersion")+"_"+ AppInfo.get("buildNumber"), true)
                .addField("Xenia-Backend", XeniaCore.getInstance().getBackendClient().getInfo(Info.Mode.Public).getVersion(), true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.3.title"), String.valueOf(commandEvent.getEvent().getJDA().getShardInfo().getShardId()) , true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.4.title"), commandEvent.getBackendClient().getSetupData().getClientName() , true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.5.title"), commandEvent.getBackendClient().getSetupData().getClientLocation() , true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.6.title"), translationPackage.getTranslationWithPlaceholders(getClass(), "response.field.6.link_text", "https://xenia.netbeacon.de/"), true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.7.title"), translationPackage.getTranslationWithPlaceholders(getClass(), "response.field.7.link_text", "https://xenia.netbeacon.de/processing"), true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.8.title"), translationPackage.getTranslationWithPlaceholders(getClass(), "response.field.8.link_text", "https://xenia.netbeacon.de/report"), true)
                .addField(translationPackage.getTranslation(getClass(), "response.field.9.title"), translationPackage.getTranslationWithPlaceholders(getClass(), "response.field.9.link_text", "https://xenia.netbeacon.de/contact"), true);
        commandEvent.getEvent().getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
