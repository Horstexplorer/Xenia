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

package de.netbeacon.xenia.bot.commands.structure.admin;

import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.base.ContentShard;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;

import java.util.List;
import java.util.stream.Collectors;

import static de.netbeacon.d43z.one.objects.settings.StaticSettings.CONTENT_SHARD_SIZE;

public class CMDD43Z1Stats extends AdminCommand {

    public CMDD43Z1Stats() {
        super("d43z1_stats", new CommandCooldown(CommandCooldown.Type.User, 2000),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception {
        var master = D43Z1Imp.getInstance().getContextPoolMaster();
        var contexts = master.getContentContexts();
        var shards = contexts.stream().map(ContentContext::getContentShards).flatMap(List::stream).collect(Collectors.toList());
        var contents = shards.stream().map(ContentShard::getOrderedContent).flatMap(List::stream).collect(Collectors.toList());
        commandEvent.getEvent().getChannel().sendMessage(
                EmbedBuilderFactory.getDefaultEmbed("D43Z1 Response", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                        .addField("ContextPoolMaster", master.getUUID().toString(), false)
                        .addField("Contexts in master", String.valueOf(contexts.size()), true)
                        .addField("Shards in master", String.valueOf(shards.size()), true)
                        .addField("Contents in master", String.valueOf(contents.size()), true)
                        .addField("Expected shard size", String.valueOf(CONTENT_SHARD_SIZE), true)
                        .addField("Actual shard size avg", String.valueOf(contents.size()/shards.size()), true)
                .build()
        ).queue();
    }
}
