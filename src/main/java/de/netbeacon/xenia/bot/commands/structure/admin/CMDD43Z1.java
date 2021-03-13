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

import de.netbeacon.d43z.one.eval.Eval;
import de.netbeacon.d43z.one.eval.io.EvalRequest;
import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArg;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.shared.executor.SharedExecutor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDefStatics.ADMIN_D43Z1_INPUT;

public class CMDD43Z1 extends AdminCommand {

    private final Logger logger = LoggerFactory.getLogger(CMDD43Z1.class);

    public CMDD43Z1() {
        super("d43z1", new CommandCooldown(CommandCooldown.Type.User, 6000),
                null,
                null,
                null,
                List.of(ADMIN_D43Z1_INPUT)
        );
    }

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) {
        CmdArg<String> inputA = args.getByIndex(0);
        try{
            D43Z1Imp d43Z1Imp = D43Z1Imp.getInstance();
            Eval eval = d43Z1Imp.getEval();
            // get stats
            long queueLength = eval.getQueueLength();
            long getAVGQueueDuration = eval.getQueueTimeAVGMs();
            float getAVGEvalTime = eval.getEvalTimeAVGNs()/(float)1000000;
            commandEvent.getEvent().getChannel().sendMessage(
                    EmbedBuilderFactory.getDefaultEmbed("D43Z1 Stats", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                    .addField("Current Queue Size", String.valueOf(queueLength), true)
                    .addField("AVG Queue Duration", getAVGQueueDuration+"ms", true)
                    .addField("AVG Eval Duration", getAVGEvalTime+"ms", true)
                    .build()
            ).queue();
            EvalRequest evalRequest = new EvalRequest(d43Z1Imp.getContextPoolMaster(), d43Z1Imp.getContentMatchBufferFor(commandEvent.getEvent().getAuthor().getIdLong()), new Content(inputA.getValue()),
                    evalResult -> {
                        if(evalResult.ok()){
                            ContentMatchBuffer.Statistics statistics = d43Z1Imp.getContentMatchBufferFor(commandEvent.getEvent().getAuthor().getIdLong()).getStatistics();
                            commandEvent.getEvent().getChannel().sendMessage(
                                    EmbedBuilderFactory.getDefaultEmbed("D43Z1 Response", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                                            .addField("Input", evalResult.getContentMatch().getInput().getContent(), false)
                                            .addField("Est Input", evalResult.getContentMatch().getEstimatedInput().getContent(), false)
                                            .addField("Est Output", evalResult.getContentMatch().getEstimatedOutput().getContent(), false)
                                            .addField("Raw Coefficient", String.valueOf(evalResult.getContentMatch().getCoefficient()), true)
                                            .addField("Adjusted Coefficient", String.valueOf(evalResult.getContentMatch().getAdjustedCoefficient()), true)
                                            .addField("AVG Coefficient", String.valueOf(statistics.getAvgOutputMatchCoefficient()), true)
                                            .addField("Cache Fill State", statistics.getFillState()+" ("+statistics.getRawFillState()+")", true)
                                            .addField("Match Tendency", statistics.getMatchTendency()+" ("+statistics.getRawMatchTendency()+")", true)
                                            .build()
                            ).queue();
                        }else{
                            commandEvent.getEvent().getChannel().sendMessage(
                                    EmbedBuilderFactory.getDefaultEmbed("D43Z1 Response", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                                            .setDescription(ExceptionUtils.getStackTrace(evalResult.getException()))
                                            .build()
                            ).queue();
                        }
                    }, SharedExecutor.getInstance().getScheduledExecutor());
            eval.enqueue(evalRequest);
        }catch (Exception e){
            commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, "Something Went Wrong. Check Logs")).queue();
            logger.error("Something went wrong processing request ", e);
        }
    }

}
