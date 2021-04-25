/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to make sure a command isn't spammed by the users
 *
 * @author horstexplorer
 */
public class CommandCooldown{

	private final Type type;
	private final long cooldownMs;
	private final ConcurrentHashMap<Long, Long> guildCooldown = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Long>> userCooldown = new ConcurrentHashMap<>();

	public enum Type{
		User,
		Guild;
	}

	/**
	 * Creates a new instance of this class
	 *
	 * @param type       whether this cooldown is guild or user based
	 * @param cooldownMs cooldown in ms
	 */
	public CommandCooldown(Type type, long cooldownMs){
		this.type = type;
		this.cooldownMs = cooldownMs;
	}

	/**
	 * Returns how long the cooldown will run when activated
	 *
	 * @return long
	 */
	public long getCooldownMs(){
		return cooldownMs;
	}

	/**
	 * Returns the type of cooldown
	 *
	 * @return Type
	 */
	public Type getType(){
		return type;
	}

	/**
	 * Activates the cooldown for the id combination
	 *
	 * @param guildID id of the guild
	 * @param userID  id of the user
	 */
	public void deny(long guildID, long userID){
		if(type == Type.User){
			if(!userCooldown.containsKey(guildID)){
				userCooldown.put(guildID, new ConcurrentHashMap<>());
			}
			userCooldown.get(guildID).put(userID, System.currentTimeMillis() + cooldownMs);
		}
		else if(type == Type.Guild){
			guildCooldown.put(guildID, System.currentTimeMillis() + cooldownMs);
		}
	}

	/**
	 * Checks for if the id combination is able to be blocked or not
	 *
	 * @param guildID id of the guild
	 * @param userID  id of the user
	 *
	 * @return true if allowed
	 */
	public boolean allow(long guildID, long userID){
		if(type == Type.User){
			if(userCooldown.containsKey(guildID) && userCooldown.get(guildID).containsKey(userID)){
				return userCooldown.get(guildID).get(userID) <= System.currentTimeMillis();
			}
			return true;
		}
		else if(type == Type.Guild){
			if(guildCooldown.containsKey(guildID)){
				return guildCooldown.get(guildID) <= System.currentTimeMillis();
			}
			return true;
		}
		return false;
	}

}
