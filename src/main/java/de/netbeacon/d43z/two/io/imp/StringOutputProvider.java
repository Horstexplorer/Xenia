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

package de.netbeacon.d43z.two.io.imp;

import de.netbeacon.d43z.two.io.bp.IOutputProvider;

public class StringOutputProvider implements IOutputProvider<String>{

	private final Character[] charValues;
	private final float threshold;

	public StringOutputProvider(float threshold){
		this.threshold = threshold;
		this.charValues = StringInputProvider.DEFAULT_CHARS;
	}

	public StringOutputProvider(float threshold, Character[] charValues){
		this.threshold = threshold;
		this.charValues = charValues;
	}

	public float getThreshold(){
		return threshold;
	}

	public Character[] getCharValues(){
		return charValues;
	}

	@Override
	public String getOutput(float[] inputs){
		StringBuilder stringBuilder = new StringBuilder(inputs.length / charValues.length);
		float highestCharValue = 0;
		for(int i = 0; i < inputs.length; i++){
			int charValuePos = i % charValues.length; // position of the char value
			if(charValuePos == 0){
				highestCharValue = 0; // reset
			}
			int stringPos = i / charValues.length; // position on the output
			if(stringPos >= stringBuilder.length()){
				stringBuilder.append('\2');
			}
			if(inputs[i] >= threshold && inputs[i] > highestCharValue){
				highestCharValue = inputs[i];
				if(charValuePos == charValues.length - 3){
					// end of the string
					stringBuilder.deleteCharAt(stringPos);
					break;
				}
				stringBuilder.setCharAt(stringPos, charValues[charValuePos]);
			}
		}
		return stringBuilder.toString();
	}

}
