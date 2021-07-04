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

package de.netbeacon.xenia.bot.interactions.type.selection;

import de.netbeacon.xenia.bot.interactions.records.*;
import de.netbeacon.xenia.bot.interactions.registry.ComponentRegistryEntry;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class SelectionRegEntryComponent extends ComponentRegistryEntry{

	public SelectionRegEntryComponent(Origin origin, Accessor accessor, Activations activations, TimeoutPolicy timeoutPolicy, ActionHandler actionHandler, ExceptionHandler exceptionHandler, DeactivationMode deactivationMode){
		super(origin, accessor, activations, timeoutPolicy, actionHandler, exceptionHandler, deactivationMode);
	}

	public SelectionMenu getSelectionMenu(SelectOption... options){
		return SelectionMenu.create(getId()).addOptions(options).build();
	}

}
