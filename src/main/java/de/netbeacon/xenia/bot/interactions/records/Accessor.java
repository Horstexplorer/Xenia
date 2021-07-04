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

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Accessor(Long... accessors){

	public static final Accessor ANY = new Accessor((Long) null);

	public static Accessor ANY_OF(Long... accessors){
		return new Accessor(accessors);
	}

	public Set<Long> asSet(){
		return new HashSet<>(List.of(accessors));
	}

	public boolean isAllowedAccessor(Long accessor){
		if(this.equals(ANY)){
			return true;
		}
		return asSet().contains(accessor);
	}

	public boolean isAllowedAccessor(Long... accessors){
		return Arrays.stream(accessors).anyMatch(this::isAllowedAccessor);
	}

	public boolean isAllowedAccessor(User user){
		return isAllowedAccessor(user.getIdLong());
	}

	public boolean isAllowedAccessor(Member member){
		return isAllowedAccessor(member.getIdLong()) || member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(this::isAllowedAccessor);
	}

}
