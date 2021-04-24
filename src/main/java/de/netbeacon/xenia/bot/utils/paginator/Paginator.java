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

package de.netbeacon.xenia.bot.utils.paginator;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.function.BiConsumer;

public class Paginator {

    private final long channelId;
    private long messageId = -1;
    private final long userId;
    private final List<Page> pages;
    private boolean needsRedraw;
    private int position;

    public Paginator(long channelId, long userId, List<Page> pages){
        this.channelId = channelId;
        this.userId = userId;
        this.pages = pages;
    }

    protected synchronized void link(long messageId){
        this.messageId = messageId;
    }

    public enum Move{

        NEXT(1),
        PREVIOUS(-1);

        private final int i;

        Move(int i){
            this.i = i;
        }

        public int getI() {
            return i;
        }
    }

    public synchronized void movePosition(Move move){
        var tmp = position;
        if(position + move.getI() < 0){
            position = pages.size() - 1;
        }else if(position + move.getI() >= pages.size()){
            position = 0;
        }else {
            position += move.getI();
        }
        if(position != tmp){
            needsRedraw = true;
        }
    }

    public long getUserId() {
        return userId;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public synchronized Page getPage(){
        return pages.get(position);
    }

    public synchronized void drawCurrent(TextChannel textChannel, User user, MessageReaction.ReactionEmote reactionEmote){
        drawCurrent(textChannel, user, reactionEmote, null, null);
    }

    public synchronized void drawCurrent(TextChannel textChannel, User user, MessageReaction.ReactionEmote reactionEmote, BiConsumer<User, Message> then, BiConsumer<User, Throwable> thenNot){
        if(textChannel.getIdLong() != channelId){
            return;
        }
        if(messageId < 0){
            textChannel.sendMessage(getPage().getAsMessageEmbed()).queue(message -> {
                link(message.getIdLong());
                if(pages.size() > 1){
                    message.addReaction(PaginatorManager.PREVIOUS).queue();
                    message.addReaction(PaginatorManager.NEXT).queue();
                    message.addReaction(PaginatorManager.CLOSE).queue();
                }
                if(then != null){
                    then.accept(user, message);
                }
            }, throwable -> {
                if(thenNot != null){
                    thenNot.accept(user, throwable);
                }
            });
        }else if(needsRedraw){
            needsRedraw = false;
            textChannel.editMessageById(messageId, getPage().getAsMessageEmbed()).queue(message -> {
                if(reactionEmote != null && user != null){
                    try{
                        if(reactionEmote.isEmoji()){
                            message.removeReaction(reactionEmote.getEmoji(), user).queue(s -> {}, f -> {});
                        }else if(reactionEmote.isEmote()){
                            message.removeReaction(reactionEmote.getEmote(), user).queue(s -> {}, f -> {});
                        }
                    }catch (Exception ignore){}
                }
                if(then != null){
                    then.accept(user, message);
                }
            }, throwable -> {
                if(thenNot != null){
                    thenNot.accept(user, throwable);
                }
            });
        }
    }

}
