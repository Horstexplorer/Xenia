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

package de.netbeacon.d43z.one.objects.settings;

import de.netbeacon.d43z.one.objects.bp.ISimilarity;

public class StaticSettings {

    // CONTENT SETTINGS
    public static final int CONTENT_SHARD_SIZE = 5000;
    public static final int BUFFER_MAX_SIZE = 4;
    public static final float BUFFER_BONUS = 0.01F;
    public static final boolean EVAL_ENABLE_BUFFER_BONUS_POLICY = true;
    public static final boolean EVAL_ENABLE_TAG_POLICY = false;
    public static final float EVAL_TAG_BONUS_PER_MATCH = 0.1F;
    public static final float EVAL_TAG_POLICY_OVERRIDE_THRESHOLD = 0.49F;
    public static final int EVAL_LIAMUS_JACCARD_NGRAM = 2;
    public static final float EVAL_RANDOM_DIF = 0.00001F;
    public static final int EVAL_MAX_PROCESSING_THREADS = Runtime.getRuntime().availableProcessors();
    public static final int EVAL_MAX_THREADS_PER_REQUEST = EVAL_MAX_PROCESSING_THREADS;
    public static final int EVAL_MAX_CONCURRENT_TASKS = EVAL_MAX_PROCESSING_THREADS/EVAL_MAX_THREADS_PER_REQUEST;
    public static final ISimilarity.Algorithm EVAL_ALGORITHM = ISimilarity.Algorithm.LIAMUS_JACCARD;
    public static final int EVAL_MAX_PROCESSING_TIME = 5000;
    public static final int EVAL_MIN_PROCESSING_TIME = 250;

}
