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

package de.netbeacon.xenia.bot.utils.level;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LevelPointManager{

	private static final int POINTS_PER_MESSAGE = 1;
	private static final int DELAY = 30000;
	private static final Function<Long, Long> CALCULATE_LEVEL = ep -> (long)(1/(float)(8) * Math.sqrt(ep));
	private static final Function<Long, Long> CALCULATE_EP = level -> (long) 64 * (long) Math.pow(level, 2);

	private final ConcurrentHashMap<Guild, ConcurrentHashMap<Member, Long>> accessMap = new ConcurrentHashMap<>();

	public void trackGuild(Guild guild){
		accessMap.put(guild, new ConcurrentHashMap<>());
	}

	public void unTrackGuild(Guild guild){
		accessMap.remove(guild);
	}

	public void trackMember(Member member){
		try{
			if(!accessMap.containsKey(member.getGuild())){
				return;
			}
			accessMap.get(member.getGuild()).put(member, 0L);
		}
		catch(Exception ignore){

		}
	}

	public void unTrackMember(Member member){
		try{
			if(!accessMap.containsKey(member.getGuild())){
				return;
			}
			accessMap.get(member.getGuild()).remove(member);
		}
		catch(Exception ignore){

		}
	}

	public synchronized void feed(Member member){
		try{
			Guild g = member.getGuild();
			if(!accessMap.containsKey(g)){
				return;
			}
			var gM = accessMap.get(g);
			if(!gM.containsKey(member)){
				trackMember(member);
			}
			long last = gM.get(member);
			if(System.currentTimeMillis() < last + DELAY){
				return;
			}
			gM.put(member, System.currentTimeMillis());
			member.lSetLevelPoints(member.getLevelPoints() + POINTS_PER_MESSAGE);
			member.updateAsync();
		}
		catch(Exception ignore){

		}
	}

	public static long calculateLevel(Member member){
		return calculateLevel(member.getLevelPoints());
	}

	public static long calculateLevel(long levelPoints){
		return CALCULATE_LEVEL.apply(levelPoints);
	}

	public static long calculateLevelMin(long level){
		return CALCULATE_EP.apply(level-1);
	}

	public static long calculateLevelMax(long level){
		return CALCULATE_EP.apply(level);
	}

	public static LevelPointCard getLevelPointCard(Member member){
		return new LevelPointCard(member);
	}

}
