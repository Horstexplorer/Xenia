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

package de.netbeacon.d43z.one.objects.eval;

import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContentShard;
import de.netbeacon.d43z.one.objects.bp.IMatch;

public class ContentMatch implements IMatch {

    public final Content input;
    public final Content estimatedInput;
    public final Content estimatedOutput;
    public final ContentShard origin;
    public final float coefficient;
    public float adjustment;

    public ContentMatch(Content input, Content estimatedInput, Content estimatedOutput, ContentShard origin, float coefficient){
        this.input = input;
        this.estimatedInput = estimatedInput;
        this.estimatedOutput = estimatedOutput;
        this.origin = origin;
        this.coefficient = coefficient;
    }

    @Override
    public Content getInput() {
        return input;
    }

    @Override
    public Content getEstimatedInput() {
        return estimatedInput;
    }

    @Override
    public Content getEstimatedOutput() {
        return estimatedOutput;
    }

    @Override
    public ContentShard getOrigin() {
        return origin;
    }

    @Override
    public float getCoefficient() {
        return coefficient;
    }

    @Override
    public void setCoefficientAdjustment(float value) {
        adjustment = value;
    }

    @Override
    public float getAdjustedCoefficient() {
        return coefficient+adjustment;
    }
}
