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

package de.netbeacon.xenia.bot.commands.structure.last;

import de.netbeacon.xenia.backend.client.objects.cache.MessageCache;
import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Message;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.LAST_RESTORE_NUM_DEF;

public class CMDRestore extends Command {

    public CMDRestore() {
        super("restore", "Restore the last n messages", new CommandCooldown(CommandCooldown.Type.Guild, 120000), null, null, List.of(LAST_RESTORE_NUM_DEF));
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        Channel bChannel = commandEvent.getBackendDataPack().getbChannel();
        MessageCache messageCache = bChannel.getMessageCache();
        List<Message> messages = messageCache.getAllAsList();
        int count = messages.size();
        CmdArg<Integer> countT = args.getByIndex(0);
        if(countT != null){
            count = countT.getValue();
        }
        for(Message message : messages){
            if(--count <= 0){
                break;
            }
            String newContent = message.getMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey());
            String oldContent = message.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey());

            EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Edited Message Restored:", commandEvent.getEvent().getJDA().getSelfUser())
                    .addField("MessageID", String.valueOf(message.getId()), true)
                    .addField("AuthorID", String.valueOf(message.getUserId()), true);
            if(newContent.equals(oldContent)){
                embedBuilder.setDescription(newContent).addField("Type", "Message", true);
            }else{
                embedBuilder.setDescription(oldContent).addField("Type", "Modified", true);
            }
            commandEvent.getEvent().getChannel().sendMessage(embedBuilder.build()).queue(s->{}, e->{});
        }
    }
}
