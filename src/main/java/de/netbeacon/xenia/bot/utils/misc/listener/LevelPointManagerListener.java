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

package de.netbeacon.xenia.bot.utils.misc.listener;

import de.netbeacon.xenia.backend.client.objects.apidata.Guild;
import de.netbeacon.xenia.backend.client.objects.apidata.Member;
import de.netbeacon.xenia.backend.client.objects.internal.objects.CacheEventListener;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;

public class LevelPointManagerListener implements CacheEventListener<Long, Guild>{

	private final LevelPointManager levelPointManager;
	private final PointManagerListenerM pointManagerListenerM;

	public LevelPointManagerListener(LevelPointManager levelPointManager){
		this.levelPointManager = levelPointManager;
		this.pointManagerListenerM = new PointManagerListenerM(levelPointManager);
	}

	@Override
	public void onInsertion(Long newKey, Guild newObject){
		this.levelPointManager.trackGuild(newObject);
		newObject.getMemberCache().addEventListeners(pointManagerListenerM);
	}

	@Override
	public void onRemoval(Long oldKey, Guild oldObject){
		this.levelPointManager.unTrackGuild(oldObject);
	}

	public static class PointManagerListenerM implements CacheEventListener<Long, Member>{

		private final LevelPointManager levelPointManager;

		public PointManagerListenerM(LevelPointManager levelPointManager){
			this.levelPointManager = levelPointManager;
		}

		@Override
		public void onInsertion(Long newKey, Member newObject){
			this.levelPointManager.trackMember(newObject);
		}

		@Override
		public void onRemoval(Long oldKey, Member oldObject){
			this.levelPointManager.unTrackMember(oldObject);
		}

	}

}
