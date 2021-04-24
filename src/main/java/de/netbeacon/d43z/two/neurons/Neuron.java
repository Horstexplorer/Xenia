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

package de.netbeacon.d43z.two.neurons;

import java.util.function.UnaryOperator;

public class Neuron{

	private float[] weights;
	private final UnaryOperator<Float> function;

	private float[] lastInputs;
	private float lastActivationValue;

	public Neuron(UnaryOperator<Float> function, float... weights){
		this.function = function;
		this.weights = weights;
	}

	public void activate(float... inputs){
		if(inputs.length != weights.length){
			throw new IllegalArgumentException("Input Weight Size Does Not Match Number Of Weights Stored");
		}
		float sum = 0;
		for(int i = 0; i < inputs.length; i++){
			sum += inputs[i] * weights[i];
		}
		lastActivationValue = function.apply(sum);
		lastInputs = inputs;
	}

	public float getLastActivationValue(){
		return lastActivationValue;
	}

	public float[] getLastInputs(){
		return lastInputs;
	}

	public float[] getWeights(){
		return weights;
	}

	public float[] calculateAndApplyOutputBackProp(float outputError){
		float[] inputError = new float[weights.length];
		for(int i = 0; i < weights.length; i++){
			float iip = (((lastInputs[i] * weights[i]) / (lastInputs[i] + weights[i]) * lastInputs[i]) / lastActivationValue);
			float wip = (((lastInputs[i] * weights[i]) / (lastInputs[i] + weights[i]) * weights[i]) / lastActivationValue);
			inputError[i] = iip * outputError;
			weights[i] = weights[i] * (1 - wip * outputError);
		}
		return inputError;
	}

}
