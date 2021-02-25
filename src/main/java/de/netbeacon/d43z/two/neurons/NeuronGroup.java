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

import java.util.Arrays;
import java.util.function.UnaryOperator;

public class NeuronGroup {

    private final int inputs;
    private final int outputs;
    private final Neuron[] neurons;

    public NeuronGroup(UnaryOperator<Float> function, int inputs, int outputs, float ... weights){
        this.inputs = inputs;
        this.outputs = outputs;
        if(inputs*outputs != weights.length){
            throw new IllegalArgumentException("Input Weight Size Does Not Match Number Of Weights Stored");
        }
        this.neurons = new Neuron[outputs];
        for(int i = 0; i < neurons.length; i++){
            neurons[i] = new Neuron(function, Arrays.copyOfRange(weights, inputs*i, inputs*(i+1)));
        }
    }

    public int getInputs() {
        return inputs;
    }

    public int getOutputs() {
        return outputs;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    public void activate(float ... inputs){
        for (Neuron neuron : neurons) {
            neuron.activate(inputs);
        }
    }

    public float[] getActivationValue(){
        float[] result = new float[outputs];
        for(int i = 0; i < neurons.length; i++){
            result[i] = neurons[i].getLastActivationValue();
        }
        return result;
    }

    public float[] calculateAndApplyOutputBackProp(float[] outputError){
        if(outputError.length != outputs){
            throw new IllegalArgumentException("Output Error Size Does Not Match Number Of Outputs");
        }
        float[] inputError = new float[inputs];
        for(int i = 0; i < neurons.length; i++){
            var neuronInputError = neurons[i].calculateAndApplyOutputBackProp(outputError[i]);
            for(int ii = 0; ii < inputs; ii++){
                inputError[ii] += neuronInputError[ii];
            }
        }
        return inputError;
    }

}
