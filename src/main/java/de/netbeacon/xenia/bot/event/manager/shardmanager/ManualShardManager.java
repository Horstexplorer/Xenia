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

package de.netbeacon.xenia.bot.event.manager.shardmanager;

import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.internal.utils.config.sharding.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.Collection;

public class ManualShardManager extends DefaultShardManager{

	public ManualShardManager(@NotNull String token){
		super(token);
	}

	public ManualShardManager(@NotNull String token, @Nullable Collection<Integer> shardIds){
		super(token, shardIds);
	}

	public ManualShardManager(@NotNull String token, @Nullable Collection<Integer> shardIds, @Nullable ShardingConfig shardingConfig, @Nullable EventConfig eventConfig, @Nullable PresenceProviderConfig presenceConfig, @Nullable ThreadingProviderConfig threadingConfig, @Nullable ShardingSessionConfig sessionConfig, @Nullable ShardingMetaConfig metaConfig, @Nullable ChunkingFilter chunkingFilter){
		super(token, shardIds, shardingConfig, eventConfig, presenceConfig, threadingConfig, sessionConfig, metaConfig, chunkingFilter);
	}

	@Override
	public void login() throws LoginException{
		runQueueWorker();
		if (this.shutdownHook != null)
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
	}

}
