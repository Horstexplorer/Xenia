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

package de.netbeacon.xenia.bot.event.handler;

import de.netbeacon.d43z.one.eval.io.EvalRequest;
import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.bp.IContextPool;
import de.netbeacon.d43z.one.objects.eval.ContentMatchBuffer;
import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.anime.AnimeTask;
import de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.eval.DefaultEvalTask;
import de.netbeacon.xenia.bot.utils.records.ToolBundle;
import de.netbeacon.xenia.bot.utils.shared.executor.SharedExecutor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XeniaChatHandler{

	private final ToolBundle toolBundle;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public XeniaChatHandler(ToolBundle toolBundle){
		this.toolBundle = toolBundle;
	}

	public void handle(GuildMessageReceivedEvent event){
		Guild bGuild = toolBundle.backendClient().getGuildCache().get(event.getGuild().getIdLong());
		Channel bChannel = bGuild.getChannelCache().get(event.getChannel().getIdLong());

		String msg = event.getMessage().getContentRaw();
		if(msg.startsWith(bGuild.getPrefix())){
			return;
		}
		if(bChannel.getD43Z1Settings().has(Channel.D43Z1Settings.Settings.ACTIVE) && event.getChannel().isNSFW()){
			try{
				D43Z1_EXECUTE(event, bGuild);
			}
			catch(Exception e){
				logger.warn("An exception occurred while handing message over to D43Z1 ", e);
			}
		}
	}

	public void D43Z1_EXECUTE(GuildMessageReceivedEvent event, Guild guild) throws Exception{
		var d43Z1Imp = D43Z1Imp.getInstance();

		var taskMaster = d43Z1Imp.getTaskMaster();
		var content = new Content(event.getMessage().getContentRaw());
		var taskResult = taskMaster.getTaskOrDefault(content, 0.7F, d43Z1Imp.getDefaultEvalTask());

		if(taskResult.getValue1() instanceof AnimeTask){
			((AnimeTask) taskResult.getValue1()).execute(content, new Pair<>(event.getMember(), event.getChannel()));
		}
		else{
			// ??? -> Fallback for when bad things happen
			ContentMatchBuffer contextMatchBuffer = d43Z1Imp.getContentMatchBufferFor(event.getAuthor().getIdLong());
			IContextPool contextPool = toolBundle.contextPoolManager().getPoolFor(guild);
			EvalRequest evalRequest = new EvalRequest(contextPool, contextMatchBuffer, new Content(event.getMessage().getContentRaw()),
				evalResult -> {
					if(evalResult.ok()){
						event.getMessage().reply(evalResult.getContentMatch().getEstimatedOutput().getContent()).mentionRepliedUser(false).queue();
					}else {
						event.getMessage().reply(":(").mentionRepliedUser(false).queue();
					}
				}, SharedExecutor.getInstance().getScheduledExecutor());
			if(taskResult.getValue1() instanceof DefaultEvalTask){
				((DefaultEvalTask) taskResult.getValue1()).execute(content, new Pair<>(d43Z1Imp.getEval(), evalRequest));
			}else {
				d43Z1Imp.getEval().enqueue(evalRequest);
			}
		}
	}

}
