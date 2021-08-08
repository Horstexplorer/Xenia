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

package de.netbeacon.xenia.bot.interactions.registry;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.bot.interactions.records.DeactivationMode;
import de.netbeacon.xenia.bot.interactions.records.Origin;
import de.netbeacon.xenia.bot.interactions.records.TimeoutPolicy;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ComponentInteractionRegistry implements IShutdown{

	private final Supplier<ShardManager> shardManagerSupplier;
	private final ConcurrentHashMap<String, ComponentRegistryEntry> registry = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Future<?>> registryTimeout = new ConcurrentHashMap<>();
	private final ScheduledExecutorService timeoutExecutorService = Executors.newScheduledThreadPool(2);

	public ComponentInteractionRegistry(Supplier<ShardManager> shardManagerSupplier){
		this.shardManagerSupplier = shardManagerSupplier;
		this.timeoutExecutorService.scheduleAtFixedRate(() -> {
			try{
				registry.forEach((k, v) -> {
					if(!v.isValid()){
						deactivate(v);
						unregister(v);
					}
				});
			}
			catch(Exception ignored){
			}
		}, 30, 30, TimeUnit.SECONDS);
	}

	@Override
	public void onShutdown() throws Exception{
		timeoutExecutorService.shutdown();
	}

	public ComponentRegistryEntry get(String uuid){
		return registry.get(uuid);
	}

	public synchronized void register(ComponentRegistryEntry componentRegistryEntry){
		registry.put(componentRegistryEntry.getId(), componentRegistryEntry);
		TimeoutPolicy top = componentRegistryEntry.getTimeoutPolicy();
		if(!top.equals(TimeoutPolicy.NONE)){
			registryTimeout.put(componentRegistryEntry.getId(),
				timeoutExecutorService.schedule(() -> {
					deactivate(componentRegistryEntry);
					unregister(componentRegistryEntry);
				}, top.timeoutInMS(), TimeUnit.MILLISECONDS)
			);
		}
		componentRegistryEntry.setRegistry(this);
	}

	public void unregister(ComponentRegistryEntry componentRegistryEntry){
		String id = componentRegistryEntry.getId();
		registry.remove(id);
		Future<?> future = registryTimeout.remove(id);
		if(future != null){
			future.cancel(false);
		}
		componentRegistryEntry.setRegistry(null);
	}

	public void deactivate(ComponentRegistryEntry v){
		if(!v.markDeactivated()){
			return;
		}
		DeactivationMode deactivationMode = v.getDeactivationHandler();
		Origin origin = v.getOrigin();
		if(deactivationMode.equals(DeactivationMode.NONE) || origin.origin().length != 2){
			return;
		}
		// only for text channel interactions
		TextChannel textChannel = shardManagerSupplier.get().getTextChannelById(origin.origin()[1]);
		if(textChannel == null){
			return;
		}
		textChannel.retrieveMessageById(origin.origin()[0]).queue(
			message -> {
				MessageBuilder messageBuilder = new MessageBuilder(message);
				List<ActionRow> actionRowList = new ArrayList<>();
				Set<String> toDeactivate = deactivationMode.getIds();
				message.getActionRows().forEach(row -> {
					List<Component> components = new ArrayList<>();
					row.getComponents().forEach(component -> {
						if((deactivationMode.equals(DeactivationMode.SELF) && v.getId().equals(component.getId()))
							|| deactivationMode.equals(DeactivationMode.ALL)
							|| toDeactivate.contains(component.getId())
						){
							// disable this one
							if(component instanceof Button){
								components.add(((Button) component).asDisabled());
							}
							else if(component instanceof SelectionMenu){
								components.add(((SelectionMenu) component).asDisabled());
							}
							// just remove em

							if(!v.getId().equals(component.getId())){ // unregister
								ComponentRegistryEntry componentRegistryEntry = get(component.getId());
								if(componentRegistryEntry != null){
									unregister(componentRegistryEntry);
								}
							}
						}
						else{
							// keep
							components.add(component);
						}
					});
					actionRowList.add(ActionRow.of(components));
				});
				messageBuilder.setActionRows(actionRowList);
				message.editMessage(messageBuilder.build()).queue();
			},
			Throwable::printStackTrace
		);
	}

}
