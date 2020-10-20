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
import de.netbeacon.xenia.bot.commands.objects.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.CommandCooldown;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CMDEdited extends Command {

    public CMDEdited() {
        super("edited", "Restores the message which has been edited last in this channel", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null);
    }

    @Override
    public void onExecution(List<String> args, CommandEvent commandEvent) {
        Channel bChannel = commandEvent.backendDataPack().getbChannel();
        MessageCache messageCache = bChannel.getMessageCache();
        Message bMessage = bChannel.getMessageCache().getLast("edited");
        if(bMessage == null){
            commandEvent.getEvent().getChannel().sendMessage(EmbedBuilderFactory.getDefaultEmbed("Error Restoring Edited Message", commandEvent.getEvent().getJDA().getSelfUser())
                    .setColor(Color.red)
                    .addField("Error", "No message found to restore", true)
                    .build()
            ).queue(s->{s.delete().queueAfter(5, TimeUnit.SECONDS);}, e->{});
        }else{
            commandEvent.getEvent().getChannel().sendMessage(EmbedBuilderFactory.getDefaultEmbed("Edited Message Restored:", commandEvent.getEvent().getJDA().getSelfUser())
                    .addField("MessageID", String.valueOf(bMessage.getId()), true)
                    .addField("AuthorID", String.valueOf(bMessage.getUserId()), true)
                    .addField("Old Message", bMessage.getOldMessageContent(messageCache.getBackendProcessor().getBackendClient().getBackendSettings().getMessageCryptKey()), false)
                    .build()
            ).queue(s->{}, e->{});
        }
    }
}