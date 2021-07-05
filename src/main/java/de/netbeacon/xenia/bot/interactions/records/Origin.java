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

package de.netbeacon.xenia.bot.interactions.records;

import net.dv8tion.jda.api.entities.Message;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Origin(Long... origin){

	public static Origin ANY = new Origin((Long) null);

	public static Origin CUSTOM(Long... origin){
		return new Origin(origin);
	}

	public Set<Long> asSet(){
		return new HashSet<>(List.of(origin));
	}

	public boolean isAllowedOrigin(long messageId){
		return origin[0] == messageId;
	}

	public boolean isAllowedOrigin(Message message){
		if(this.equals(ANY)){
			return true;
		}
		return isAllowedOrigin(message.getIdLong());
	}

}
