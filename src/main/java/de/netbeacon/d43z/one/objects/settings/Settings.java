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

import org.json.JSONObject;

public class Settings{

	private static JSONObject data;

	private Settings(){}

	static{
		try{
			data = new JSONObject(new String(Settings.class.getClassLoader().getResourceAsStream("d34z.settings").readAllBytes()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static JSONObject get(){
		return new JSONObject(data);
	}

	public static <E> E get(String property){
		property = property.toLowerCase();
		if(data.has(property)){
			return (E) data.get(property);
		}
		return null;
	}

}
