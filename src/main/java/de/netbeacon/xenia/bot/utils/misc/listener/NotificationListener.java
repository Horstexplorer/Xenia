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

package de.netbeacon.xenia.bot.utils.misc.listener;

import de.netbeacon.xenia.backend.client.objects.external.misc.Notification;
import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataEventListener;
import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataObject;
import de.netbeacon.xenia.backend.client.objects.internal.objects.CacheEventListener;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import de.netbeacon.xenia.bot.utils.misc.task.TaskManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class NotificationListener implements CacheEventListener<Long, Notification>, APIDataEventListener {

    private final TaskManager taskManager;

    public NotificationListener(TaskManager taskManager){
        this.taskManager = taskManager;
    }

    @Override
    public void onInsertion(Long newKey, Notification notification) {
        // new object has been inserted, add event listener
        notification.addEventListener(this);
        // and schedule
        taskManager.schedule(notification.getId(), getRunnable(notification), Math.min(notification.getNotificationTarget()-System.currentTimeMillis(),1));
    }

    @Override
    public void onRemoval(Long oldKey) {
        // cancel scheduling (if it hasnt been executed already)
        taskManager.cancel(oldKey);
    }

    @Override
    public void onUpdate(APIDataObject apiDataObject) {
        Notification notification = ((Notification) apiDataObject);
        // update scheduling
        taskManager.update(notification.getId(), getRunnable(notification), Math.min(notification.getNotificationTarget()-System.currentTimeMillis(),1));
    }

    private Runnable getRunnable(Notification notification) {
        return () -> {
            try{
                TextChannel textChannel = XeniaCore.getInstance().getShardByGuildId(notification.getGuildId()).getTextChannelById(notification.getChannelId());
                if(textChannel == null){
                    return;
                }
                textChannel.sendMessage(getNotificationMessage(notification.getUserId(), notification.getNotificationMessage())).queue(s->{}, f->{});
            }catch (Exception ignore){}
        };
    }

    private MessageEmbed getNotificationMessage(long author, String message){
        User requester = XeniaCore.getInstance().getShardManager().getUserById(author);
        if(requester == null){
            return EmbedBuilderFactory.getDefaultEmbed("Notification", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                    .setDescription(message)
                    .setFooter("Requested By "+requester)
                    .build();
        }else{
            return EmbedBuilderFactory.getDefaultEmbed("Notification", XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser(), requester)
                    .setDescription(message)
                    .build();
        }
    }

    @Override
    public void onDeletion(APIDataObject apiDataObject) {
        apiDataObject.removeEventListener(); // remove listeners from this object if deleted
        // proof check - cancel the task
        onRemoval(((Notification) apiDataObject).getId());
    }
}
