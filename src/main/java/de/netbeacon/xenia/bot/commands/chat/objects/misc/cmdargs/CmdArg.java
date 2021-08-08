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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs;

public class CmdArg<T>{

	private final CmdArgDef<T> argDef;
	private final T value;

	/**
	 * Creates a new instance of this class
	 *
	 * @param def   definition of this argument
	 * @param value of this argument
	 */
	protected CmdArg(CmdArgDef<T> def, T value){
		this.argDef = def;
		this.value = value;
	}

	/**
	 * Checks if the value matches the definition
	 * <p>
	 * Will return true if it either matches or does not exist while being optional
	 *
	 * @return true if it does match
	 */
	public boolean verify(){
		if(value == null){
			return argDef.isOptional();
		}
		else{
			return argDef.test(value);
		}
	}

	/**
	 * Returns the definition of this argument
	 *
	 * @return CmdArgDef
	 */
	public CmdArgDef<?> getArgDef(){
		return argDef;
	}

	/**
	 * Returns the value of the argument
	 *
	 * @return value
	 */
	public T getValue(){
		return value;
	}

}
