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

package de.netbeacon.d43z.one.algo;

import java.util.HashSet;
import java.util.Set;

public class SetBasedJaccard{

	public static float similarityCoefficient(String a, String b, int sliceSize){
		if(a.equals(b)){
			return 1F;
		}
		Set<String> setA = sliceString(a, sliceSize);
		Set<String> setB = sliceString(b, sliceSize);
		Set<String> merge = new HashSet<>();
		merge.addAll(setA);
		merge.addAll(setB);
		int dif = (setA.size() + setB.size()) - merge.size();
		return (dif / (float) merge.size());
	}

	private static Set<String> sliceString(String a, int sliceSize){
		Set<String> slices = new HashSet<>();
		for(int i = 0; i < a.length() - (sliceSize - 1); i++){
			slices.add(a.substring(i, i + sliceSize));
		}
		return slices;
	}

}
