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

package de.netbeacon.xenia.bot.utils.records;

import de.netbeacon.xenia.backend.client.core.XeniaBackendClient;
import de.netbeacon.xenia.bot.interactions.registry.ComponentInteractionRegistry;
import de.netbeacon.xenia.bot.utils.d43z1imp.ext.D43Z1ContextPoolManager;
import de.netbeacon.xenia.bot.utils.eventwaiter.EventWaiter;
import de.netbeacon.xenia.bot.utils.level.LevelPointManager;
import de.netbeacon.xenia.bot.utils.paginator.PaginatorManager;

public record ToolBundle(
	XeniaBackendClient backendClient,
	EventWaiter eventWaiter,
	PaginatorManager paginatorManager,
	D43Z1ContextPoolManager contextPoolManager,
	LevelPointManager levelPointManager,
	ComponentInteractionRegistry componentInteractionRegistry
){}
