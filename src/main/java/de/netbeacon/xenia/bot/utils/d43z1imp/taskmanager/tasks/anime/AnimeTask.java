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

package de.netbeacon.xenia.bot.utils.d43z1imp.taskmanager.tasks.anime;

import de.netbeacon.d43z.one.objects.base.Task;
import de.netbeacon.d43z.one.objects.base.Trigger;
import de.netbeacon.d43z.one.objects.bp.IContentprovider;
import de.netbeacon.utils.tuples.Pair;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.function.BiFunction;

public abstract class AnimeTask extends Task<IContentprovider, Pair<Member, TextChannel>, Object>{

	public AnimeTask(int pos, String desc, List<Trigger<IContentprovider, ?>> triggers, BiFunction<IContentprovider, Pair<Member, TextChannel>, Object> task){
		super(pos, desc, triggers, task);
	}

}
