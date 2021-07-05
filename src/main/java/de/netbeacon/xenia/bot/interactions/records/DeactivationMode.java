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

package de.netbeacon.xenia.bot.interactions.records;

import de.netbeacon.xenia.bot.interactions.registry.ComponentRegistryEntry;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record DeactivationMode(ComponentRegistryEntry... registryEntries){

	public static final DeactivationMode NONE = new DeactivationMode();

	public static final DeactivationMode SELF = new DeactivationMode();

	public static final DeactivationMode ALL = new DeactivationMode();

	public static DeactivationMode CUSTOM(ComponentRegistryEntry... registryEntries){
		return new DeactivationMode(registryEntries);
	}

	public Set<String> getIds(){
		return Arrays.stream(registryEntries).map(ComponentRegistryEntry::getId).collect(Collectors.toSet());
	}

}
