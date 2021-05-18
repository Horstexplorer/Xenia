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

package de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CmdArgFactory{

	public static CmdArgs getArgs(Function<String, OptionMapping> optionProvider, List<CmdArgDef> cmdArgDefs) throws Exception{
		List<CmdArg> cmdArgs = new ArrayList<>();
		for(CmdArgDef cmdArgDef : cmdArgDefs){
			var option = optionProvider.apply(cmdArgDef.getName());
			// if not found check if it might be optional
			if(option == null){
				if(cmdArgDef.getOptionData().isRequired()){
					throw new Exception("Required Option " + cmdArgDef.getName() + " is missing");
				}
				// data is optional so null is valid
				cmdArgs.add(new CmdArg(null, cmdArgDef));
				continue;
			}
			// if found we need to parse it
			Object data;
			try{
				data = cmdArgDef.getParser().parse(option);
			}
			catch(CmdArgDef.Parser.Exception e){
				throw new Exception("Option " + cmdArgDef.getName() + " does not seem to be parsable to the right type " + cmdArgDef.getTClass().getSimpleName());
			}
			// check if it meets the requirements
			if(!cmdArgDef.getPredicate().test(data)){
				throw new Exception("Option " + cmdArgDef.getName() + " does not seem to fulfill the required input (" + cmdArgDef.getExtendedDescription() + ")");
			}
			// data is correct and in the right format
			cmdArgs.add(new CmdArg(data, cmdArgDef));
		}
		return new CmdArgs(cmdArgs);
	}

	public static class Exception extends java.lang.Exception{

		public Exception(String message){
			super(message);
		}

	}

}
