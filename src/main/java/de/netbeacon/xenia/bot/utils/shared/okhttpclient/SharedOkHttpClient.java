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

package de.netbeacon.xenia.bot.utils.shared.okhttpclient;

import okhttp3.OkHttpClient;

public class SharedOkHttpClient{

	private static OkHttpClient okHttpClient;

	/**
	 * Returns the instance of this class
	 *
	 * @param initIfNeeded initializes an instance of this class if no other exists
	 *
	 * @return this
	 */
	public static synchronized OkHttpClient getInstance(boolean initIfNeeded){
		if(okHttpClient == null && initIfNeeded){
			okHttpClient = new OkHttpClient.Builder().build();
		}
		return okHttpClient;
	}

	/**
	 * Returns the instance of this class
	 *
	 * @return this
	 */
	public static OkHttpClient getInstance(){
		return getInstance(false);
	}

}
