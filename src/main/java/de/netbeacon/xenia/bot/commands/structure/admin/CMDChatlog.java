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

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Message;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.hastebin.HastebinUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.ADMIN_CHATLOG_CHANNEL;
import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.ADMIN_CHATLOG_LIMIT;

public class CMDChatlog extends Command {

    public CMDChatlog() {
        super("chatlog", new CommandCooldown(CommandCooldown.Type.User, 6000), null, null, null, List.of(ADMIN_CHATLOG_CHANNEL, ADMIN_CHATLOG_LIMIT));
    }

    @Override
    public void execute(List<String> args, CommandEvent commandEvent) {
        // check required args
        CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
        if(!cmdArgs.verify()){
            // missing args
            commandEvent.getEvent().getChannel().sendMessage(onMissingArgs()).queue(s->{s.delete().queueAfter(10, TimeUnit.SECONDS);}, e->{});
            return;
        }
        if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(getBotPermissions())){
            // bot does not have the required permissions
            commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms()).queue(s->{},e->{});
            return;
        }
        if(commandEvent.getEvent().getAuthor().getIdLong() != XeniaCore.getInstance().getConfig().getLong("ownerID")){
            // invalid permission
            commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms(false)).queue(s->{},e->{});
            return;
        }
        // everything alright
        onExecution(cmdArgs, commandEvent);
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        CmdArg<Mention> channelArg = args.getByIndex(0); // unused for now
        CmdArg<Boolean> limitArg = args.getByIndex(1); // unused for now

        Channel bChannel = commandEvent.getBackendDataPack().getbChannel();

        if(channelArg.getValue() != null && channelArg.getValue().getId() > 0){
            long cId = channelArg.getValue().getId();
            if(commandEvent.getBackendDataPack().getbGuild().getChannelCache().contains(cId)){
                bChannel = commandEvent.getBackendDataPack().getbGuild().getChannelCache().get(cId);
            }
        }

        List<Message> messages = bChannel.getMessageCache().retrieveAllFromBackend(limitArg.getValue() != null && limitArg.getValue(), false);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject()
                .put("guildId", commandEvent.getEvent().getGuild().getIdLong())
                .put("channelId", bChannel.getChannelId())
                .put("messages", jsonArray);
        messages.stream().forEach(message -> {
            var json = message.asJSON();
            json.remove("channelId");
            json.remove("guildId");
            jsonArray.put(json);
        });
        // upload to hastebin
        String jsonString = jsonObject.toString(3);
        try{
            String url = HastebinUtil.uploadToHastebin(jsonString);
            commandEvent.getEvent().getChannel().sendMessage(onSuccess("Here is the [chatlog]("+url+") ["+jsonArray.length()+" messages]")).queue();
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError("Something Went Wrong Uploading The Chat Log (sizeOf: "+jsonString.length()+")")).queue();
        }
    }

    @Override
    public MessageEmbed onMissingMemberPerms(boolean v){
        return EmbedBuilderFactory.getDefaultEmbed("Failed: Not Enough Permissions", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription("You are not allowed to do this !")
                .build();
    }
}
