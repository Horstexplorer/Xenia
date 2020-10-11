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

package de.netbeacon.xenia.bot.listener.status;

import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(StatusListener.class);

    @Override
    public void onResume(@NotNull ResumedEvent event) {
        logger.warn("Shard Resumed: "+event.getJDA().getShardInfo().getShardString());
    }

    @Override
    public void onReconnect(@NotNull ReconnectedEvent event) {
        logger.warn("Shard Reconnected: "+event.getJDA().getShardInfo().getShardString());
    }

    @Override
    public void onDisconnect(@NotNull DisconnectEvent event) {
        logger.warn("Shard Disconnected: "+event.getJDA().getShardInfo().getShardString());
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        logger.warn("Shard Shutting Down: "+event.getJDA().getShardInfo().getShardString());
    }

    @Override
    public void onException(@NotNull ExceptionEvent event) {
        logger.warn("Exception On "+event.getJDA().getShardInfo()+": Message: "+event.getCause().getMessage(), event.getCause());
    }
}