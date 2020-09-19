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

package de.netbeacon.xenia.tools.discordappender;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.netbeacon.xenia.tools.config.Config;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordWebhookAppender extends AppenderSkeleton {

    private final WebhookClient webhookClient;
    private final Queue<LoggingEvent> eventCache = new LinkedList<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public DiscordWebhookAppender() throws IOException {
        // load config
        Config config = new Config(new File("./xenia/config/sys.config"));
        String webhookURL = config.getString("webhookURL");
        webhookClient = WebhookClient.withUrl(webhookURL);
        scheduledExecutorService.scheduleAtFixedRate(()->{
            if(eventCache.isEmpty()){
                return;
            }
            StringBuilder stringBuilder = new StringBuilder().append("```");
            while(stringBuilder.length() <= 1500){
                if(eventCache.isEmpty()){
                    break;
                }
                LoggingEvent loggingEvent = eventCache.remove();
                stringBuilder.append("[").append(new Date(loggingEvent.timeStamp)).append("][").append(loggingEvent.getLevel()).append("] ")
                        .append("Message: ").append(loggingEvent.getMessage())
                        .append("\tCaused by: ").append(loggingEvent.getLocationInformation().fullInfo)
                        .append("\n");
            }
            stringBuilder.append("Additional errors cached: ").append(eventCache.size()).append("\n").append("```");
            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder()
                    .setUsername("Xenia")
                    .setContent("**Log Report**\n"+stringBuilder.toString());
            webhookClient.send(webhookMessageBuilder.build());
        }, 2, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void append(LoggingEvent event) {
        if(event.getLevel().equals(Level.WARN) || event.getLevel().equals(Level.ERROR) || event.getLevel().equals(Level.FATAL)){
            eventCache.add(event);
        }
    }

    @Override
    public void close() {
        this.closed = true;
        webhookClient.close();
        scheduledExecutorService.shutdownNow();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}