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

import de.netbeacon.d43z.two.io.bp.IInputProvider;

import java.util.Arrays;
import java.util.HashSet;

public class StringInputProvider implements IInputProvider<String> {

    private final int maxChars;
    private final Character[] charValues;
    private final HashSet<Character> charValueHashSet;

    public static final Character[] DEFAULT_CHARS = new Character[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '.', '?', '!', ' ',
            '\0', '\1', '\2' // end, undefined
    };

    public static final Character[] EXTENDED_DEFAULT_CHARS = new Character[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '.', '?', '!', ' ',
            '\0', '\1', '\2' // end, empty, undefined
    };

    public StringInputProvider(int maxChars){
        this.maxChars = maxChars;
        this.charValues = DEFAULT_CHARS;
        this.charValueHashSet = new HashSet<>(Arrays.asList(charValues));
    }

    public StringInputProvider(int maxChars, Character[] charValues){
        this.maxChars = maxChars;
        this.charValues = charValues;
        this.charValueHashSet = new HashSet<>(Arrays.asList(charValues));
    }

    public int getMaxChars() {
        return maxChars;
    }

    public Character[] getCharValues() {
        return charValues;
    }

    @Override
    public float[] getInput(String input) {
        if(input.length()+1 > maxChars){
            throw new RuntimeException("Input does not match max length specification");
        }
        // there most likely is a more efficient way to do this; but for now this might be good enough
        float[] result = new float[maxChars*charValues.length];
        var stringCharArray = input.toCharArray();
        for(int i = 0; i < result.length; i++){
            int charValuePos = i % charValues.length; // position of the char value
            int stringPos = i / charValues.length; // position on the input
            if(
                        (stringPos == stringCharArray.length && charValuePos == charValues.length-3) // we reached the end of our string & are on the correct char pos for the end marker
                    ||  (stringPos > stringCharArray.length && charValuePos == charValues.length-2) // this part is not defined by our input & we are on the correct char pos for the empty marker
                    ||  (stringPos < stringCharArray.length && charValues[charValuePos] == stringCharArray[stringPos]) // the char matches the one in our string
                    ||  (stringPos < stringCharArray.length && !charValueHashSet.contains(stringCharArray[stringPos]) && charValuePos == charValues.length-1) // we do not know this char & we are on the position for undefined
            ){
                result[i] = 1;
            }
        }
        return result;
    }

}
