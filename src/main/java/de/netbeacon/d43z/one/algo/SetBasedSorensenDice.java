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

/**
 * Set-based Sorensen-Dice Algorithm
 * Ignores multiple occurrences of slices
 * <p>
 * Thanks to @Archduke Liamus#4846
 */
public class SetBasedSorensenDice{

	/**
	 * Calculates multiple coefficients for two strings with given sizes
	 *
	 * @param a          String
	 * @param b          String
	 * @param ngramSizes list of slice sizes
	 *
	 * @return avg of all results
	 */
	public static float multiDiceCoefficient(String a, String b, int... ngramSizes){
		float sum = 0.0f;
		for(int ngramSize : ngramSizes){
			sum += diceCoefficient(a, b, ngramSize);
		}
		return sum / ngramSizes.length;
	}

	/**
	 * Calculates a single coefficient for two strings with a given slice size
	 *
	 * @param a         String
	 * @param b         String
	 * @param sliceSize size of the slice
	 *
	 * @return dice coefficient
	 */
	public static float diceCoefficient(String a, String b, int sliceSize){
		sliceSize = Math.min(sliceSize, 1);
		Set<String> ASlices = sliceString(a, sliceSize);
		Set<String> BSlices = sliceString(b, sliceSize);

		float asize = ASlices.size();
		float bsize = BSlices.size();
		if((asize + bsize) == 0){
			return 0;
		}
		if(asize < bsize){
			ASlices.retainAll(BSlices);
			return (2.0f * ASlices.size()) / (asize + bsize);
		}
		else{
			BSlices.retainAll(ASlices);
			return (2.0f * BSlices.size()) / (asize + bsize);
		}
	}

	/**
	 * Used to slice a string into a given length
	 *
	 * @param a         string
	 * @param sliceSize size of the slice
	 *
	 * @return list of sized slices
	 */
	private static Set<String> sliceString(String a, int sliceSize){
		Set<String> slices = new HashSet<>();
		for(int i = 0; i < a.length() - (sliceSize - 1); i++){
			slices.add(a.substring(i, i + sliceSize));
		}
		return slices;
	}

}

