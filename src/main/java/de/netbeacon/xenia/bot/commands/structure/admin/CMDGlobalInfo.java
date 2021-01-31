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

import de.netbeacon.xenia.backend.client.objects.external.system.Info;
import de.netbeacon.xenia.backend.client.objects.external.system.SetupData;
import de.netbeacon.xenia.backend.client.objects.internal.ws.SecondaryWebsocketListener;
import de.netbeacon.xenia.backend.client.objects.internal.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.client.objects.internal.ws.processor.WSResponse;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class CMDGlobalInfo extends Command {
    public CMDGlobalInfo() {
        super("ginfo", new CommandCooldown(CommandCooldown.Type.User, 6000),null, null, null, null);
    }

    @Override
    public void execute(List<String> args, CommandEvent commandEvent) {
        TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(commandEvent.getBackendDataPack().getbGuild(), commandEvent.getBackendDataPack().getbMember());
        if(translationPackage == null){
            commandEvent.getEvent().getChannel().sendMessage("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue(s->{s.delete().queueAfter(10, TimeUnit.SECONDS);}, e->{});
            return;
        }
        // check required args
        CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
        if(!cmdArgs.verify()){
            // missing args
            commandEvent.getEvent().getChannel().sendMessage(onMissingArgs(translationPackage)).queue();
            return;
        }
        if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(getBotPermissions())){
            // bot does not have the required permissions
            commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms(translationPackage)).queue();
            return;
        }
        if(commandEvent.getEvent().getAuthor().getIdLong() != XeniaCore.getInstance().getConfig().getLong("ownerID")){
            // invalid permission
            commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms(translationPackage,false)).queue();
            return;
        }
        // everything alright
        onExecution(cmdArgs, commandEvent, translationPackage);
    }

    @Override
    public MessageEmbed onMissingMemberPerms(TranslationPackage translationPackage, boolean v){
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingMemberPerms.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription("You are not allowed to do this")
                .build();
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) {
        Message m = commandEvent.getEvent().getChannel().sendMessage(getRequestEmbed()).complete();
        try{
            // get secondary websocket
            SecondaryWebsocketListener secondaryWebsocketListener = commandEvent.getBackendClient().getSecondaryWebsocketListener();
            // create request
            WSRequest wsRequest = new WSRequest.Builder()
                    .action("statistics")
                    .mode(WSRequest.Mode.BROADCAST)
                    .exitOn(WSRequest.ExitOn.TIMEOUT)
                    .build();
            // send request
            List<WSResponse> wsResponses = secondaryWebsocketListener.getWsProcessorCore().process(wsRequest);
            // send result
            Queue<Message> messages = new MessageBuilder()
                    .append(getResultMessage(commandEvent, wsResponses))
                    .buildAll(MessageBuilder.SplitPolicy.NEWLINE);
            m.delete().queue();
            for(Message message : messages){
                commandEvent.getEvent().getChannel().sendMessage(message).queue();
            }
        }catch (Exception e){
            m.editMessage(getResultEmbed()).queue();
        }
    }

    private MessageEmbed getRequestEmbed(){
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("DataRequest", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.ORANGE)
                .addField("Status:", "Running", true)
                .addField("Timeout:", 5000+"ms", true)
                .addField("Action:", "statistics", true);
        return embedBuilder.build();
    }

    private MessageEmbed getResultEmbed(){
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("DataRequest", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .addField("Status:", "Failure", true);
        return embedBuilder.build();
    }

    private String getResultMessage(CommandEvent commandEvent, List<WSResponse> responses){
        // prepare StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        // get own statistics
        SetupData setupData = commandEvent.getBackendClient().getSetupData();
        Info info = commandEvent.getBackendClient().getInfo(Info.Mode.Private);
        Runtime runtime = Runtime.getRuntime();
        stringBuilder.append(getFormattedStats(
                setupData.getClientId(),
                setupData.getClientName(),
                ((runtime.totalMemory()-runtime.freeMemory())/1048576),
                (runtime.totalMemory()/1048576),
                Thread.activeCount(),
                info.getPing(),
                ManagementFactory.getRuntimeMXBean().getUptime()
        ));
        // add from results
        for(WSResponse wsResponse : responses){
            JSONObject jsonObject = wsResponse.getPayload();
            if(jsonObject.getLong("id") == 0){
                // is backend - this will get added later
                stringBuilder.append(getFormattedStats(
                        jsonObject.getLong("id"),
                        jsonObject.getString("name"),
                        jsonObject.getJSONObject("statistics").getJSONObject("memory").getLong("used"),
                        jsonObject.getJSONObject("statistics").getJSONObject("memory").getLong("total"),
                        jsonObject.getJSONObject("statistics").getInt("threads"),
                        0,
                        jsonObject.getJSONObject("statistics").getLong("uptime")
                ));
            }else{
                // is client
                stringBuilder.append(getFormattedStats(
                        jsonObject.getLong("id"),
                        jsonObject.getString("name"),
                        jsonObject.getJSONObject("statistics").getJSONObject("memory").getLong("used"),
                        jsonObject.getJSONObject("statistics").getJSONObject("memory").getLong("total"),
                        jsonObject.getJSONObject("statistics").getInt("threads"),
                        jsonObject.getJSONObject("statistics").getLong("ping"),
                        jsonObject.getJSONObject("statistics").getLong("uptime")
                ));
            }
        }
        // return
        return stringBuilder.toString();
    }

    private StringBuilder getFormattedStats(long clientId, String clientName, long memoryUsed, long memoryTotal, int threads, long ping, long uptime){
        return new StringBuilder()
                .append("```")
                .append(clientId) // id
                .append(" - ")
                .append(clientName) // name
                .append("\n\t").append("Memory:")
                .append("\n\t\t").append(memoryUsed).append("  / ").append(memoryTotal).append(" mb")
                .append("\n\t").append("Threads:")
                .append("\n\t\t").append(threads).append(" threads")
                .append("\n\t").append("Ping:")
                .append("\n\t\t").append(ping).append(" ms")
                .append("\n\t").append("Uptime:")
                .append("\n\t\t").append(String.format("%d days, %d hours, %d min, %d seconds", (int)((uptime / (1000*60*60*24))), (int)((uptime / (1000*60*60)) % 24), (int)((uptime / (1000*60)) % 60),(int)((uptime / (1000)) % 60)))
                .append("```");
    }
}
