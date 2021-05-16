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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.d43z.one.objects.base.ContentContext;
import de.netbeacon.d43z.one.objects.base.ContentShard;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.d43z1imp.D43Z1Imp;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.paginator.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.netbeacon.d43z.one.settings.StaticSettings.*;

public class CMDD43Z1Stats extends AdminCommand{

	public CMDD43Z1Stats(){
		super("d43z1_stats", new CommandCooldown(CommandCooldown.Type.User, 2000),
			null,
			null,
			null,
			null
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		var eval = D43Z1Imp.getInstance().getEval();
		var master = D43Z1Imp.getInstance().getContextPoolMaster();

		var contexts = master.getContentContexts();
		var shards = contexts.stream().map(ContentContext::getContentShards).flatMap(List::stream).collect(Collectors.toList());
		var contents = shards.stream().map(ContentShard::getOrderedContent).flatMap(List::stream).collect(Collectors.toList());
		long queueLength = eval.getQueueLength();
		long getAVGQueueDuration = eval.getQueueTimeAVGMs();
		float getAVGEvalTime = eval.getEvalTimeAVGNs() / (float) 1000000;

		ArrayList<Page> pages = new ArrayList<>();
		pages.add(new Page(EmbedBuilderFactory.getDefaultEmbed("D43Z1 Stats")
			.addField("ContextPoolMaster", master.getUUID().toString(), false)
			.addField("Contexts in master", String.valueOf(contexts.size()), true)
			.addField("Shards in master", String.valueOf(shards.size()), true)
			.addField("Contents in master", String.valueOf(contents.size()), true)
			.addField("Expected shard size", String.valueOf(CONTENT_SHARD_SIZE.get()), true)
			.addField("Actual shard size avg", String.valueOf(contents.size() / shards.size()), true)
			.addField("Current Queue Size", String.valueOf(queueLength), true)
			.addField("AVG Queue Duration", getAVGQueueDuration + "ms", true)
			.addField("AVG Eval Duration", getAVGEvalTime + "ms", true)
			.build()));
		pages.add(new Page(EmbedBuilderFactory.getDefaultEmbed("D43Z1 Stats")
			.addField("CONTENT_SHARD_SIZE", String.valueOf(CONTENT_SHARD_SIZE.get()), true)
			.addField("BUFFER_MAX_SIZE", String.valueOf(BUFFER_MAX_SIZE.get()), true)
			.addField("EVAL_ENABLE_BUFFER_BONUS_POLICY", String.valueOf(EVAL_ENABLE_BUFFER_BONUS_POLICY.get()), true)
			.addField("BUFFER_BONUS", String.valueOf(BUFFER_BONUS.get()), true)
			.addField("BUFFER_BONUS_SUBTRACTION", String.valueOf(BUFFER_BONUS_SUBTRACTION.get()), true)
			.addField("EVAL_ENABLE_TAG_POLICY", String.valueOf(EVAL_ENABLE_TAG_POLICY.get()), true)
			.addField("EVAL_TAG_BONUS_PER_MATCH", String.valueOf(EVAL_TAG_BONUS_PER_MATCH.get()), true)
			.addField("EVAL_TAG_POLICY_OVERRIDE_THRESHOLD", String.valueOf(EVAL_TAG_POLICY_OVERRIDE_THRESHOLD.get()), true)
			.addField("EVAL_LIAMUS_JACCARD_NGRAM", String.valueOf(EVAL_LIAMUS_JACCARD_NGRAM.get()), true)
			.addField("EVAL_LIAMUS_JACCARD_LOWERCASE_MATCH", String.valueOf(EVAL_LIAMUS_JACCARD_LOWERCASE_MATCH.get()), true)
			.addField("EVAL_RANDOM_DIF", String.valueOf(EVAL_RANDOM_DIF.get()), true)
			.addField("EVAL_MAX_PROCESSING_THREADS", String.valueOf(EVAL_MAX_PROCESSING_THREADS.get()), true)
			.addField("EVAL_MAX_THREADS_PER_REQUEST", String.valueOf(EVAL_MAX_THREADS_PER_REQUEST.get()), true)
			.addField("EVAL_MAX_CONCURRENT_TASKS", String.valueOf(EVAL_MAX_CONCURRENT_TASKS.get()), true)
			.addField("EVAL_ALGORITHM", String.valueOf(EVAL_ALGORITHM.get()), true)
			.addField("EVAL_AVG_BASE", String.valueOf(EVAL_AVG_BASE.get()), true)
			.addField("EVAL_MAX_PROCESSING_TIME", String.valueOf(EVAL_MAX_PROCESSING_TIME.get()), true)
			.addField("EVAL_MIN_PROCESSING_TIME", String.valueOf(EVAL_MIN_PROCESSING_TIME.get()), true)
			.build()));
		commandEvent.getPaginatorManager().createPaginator(commandEvent.getEvent().getChannel(), commandEvent.getEvent().getAuthor(), pages);
	}

}
