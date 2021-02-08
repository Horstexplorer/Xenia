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

package de.netbeacon.d43z.one.objects.bp;

import de.netbeacon.d43z.one.algo.LiamusJaccard;
import de.netbeacon.d43z.one.algo.ListBasedSorensenDice;
import de.netbeacon.d43z.one.algo.SetBasedJaccard;
import de.netbeacon.d43z.one.algo.SetBasedSorensenDice;
import de.netbeacon.d43z.one.objects.settings.StaticSettings;

import java.util.Random;

public interface ISimilarity extends IContentprovider, IWeightable{

    Random RANDOM = new Random();

    enum Algorithm{
        LIST_BASED_DICE,
        SET_BASED_DICE,
        SET_BASED_JACCARD,
        LIAMUS_JACCARD,
        AVG
    }

    public default float evalLBDice(ISimilarity iSimilarity){
        return (ListBasedSorensenDice.diceCoefficient(getContent(), iSimilarity.getContent(), 2)*iSimilarity.getWeight())+getRandomDif();
    }

    public default float evalSBDice(ISimilarity iSimilarity){
        return (SetBasedSorensenDice.diceCoefficient(getContent(), iSimilarity.getContent(), 2)*iSimilarity.getWeight())+getRandomDif();
    }

    public default float evalSBJaccard(ISimilarity iSimilarity){
        return (SetBasedJaccard.similarityCoefficient(getContent(), iSimilarity.getContent(), 2)*iSimilarity.getWeight())+getRandomDif();
    }

    public default float evalLMJaccard(ISimilarity iSimilarity){
        if(this instanceof ILJEvaluable && iSimilarity instanceof ILJEvaluable){
            return (LiamusJaccard.similarityCoefficient(((ILJEvaluable) this).getContentHash(), ((ILJEvaluable) iSimilarity).getContentHash()))+getRandomDif();
        }else{
            return (LiamusJaccard.similarityCoefficient(getContent(), iSimilarity.getContent(), 2)*iSimilarity.getWeight()+getRandomDif());
        }
    }

    public default float evalAVG(ISimilarity iSimilarity){
        return (evalLBDice(iSimilarity)+evalSBDice(iSimilarity)+evalSBJaccard(iSimilarity)+evalLMJaccard(iSimilarity))/4;
    }

    public default float eval(Algorithm algorithm, ISimilarity iSimilarity){
        switch (algorithm){
            case LIST_BASED_DICE:
                return evalLBDice(iSimilarity);
            case SET_BASED_DICE:
                return evalSBDice(iSimilarity);
            case SET_BASED_JACCARD:
                return evalSBJaccard(iSimilarity);
            case LIAMUS_JACCARD:
                return evalLMJaccard(iSimilarity);
            case AVG:
            default:
                return evalAVG(iSimilarity);
        }
    }

    public default float getRandomDif(){
        float randomOffset = 0;
        if(StaticSettings.EVAL_RANDOM_DIF > 0){
            randomOffset = (-StaticSettings.EVAL_RANDOM_DIF) + RANDOM.nextFloat()*(2*StaticSettings.EVAL_RANDOM_DIF);
        }
        return randomOffset;
    }

}
