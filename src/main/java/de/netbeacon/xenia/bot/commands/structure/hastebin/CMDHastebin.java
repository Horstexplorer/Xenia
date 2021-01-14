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

package de.netbeacon.xenia.bot.commands.structure.hastebin;

import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.objects.Command;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.utils.hastebin.HastebinUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CMDHastebin extends Command {

    public CMDHastebin() {
        super("hastebin", "Upload the attached file to haste.hypercdn.de", new CommandCooldown(CommandCooldown.Type.User, 5000),
                null,
                null,
                new HashSet<>(List.of(Role.Permissions.Bit.HASTEBIN_UPLOAD_USE)),
                null
        );
    }

    private static final Tika TIKA = new Tika();

    @Override
    public void onExecution(CmdArgs args, CommandEvent commandEvent) {
        List<Message.Attachment> attachments = commandEvent.getEvent().getMessage().getAttachments();
        TextChannel textChannel = commandEvent.getEvent().getChannel();

        if(attachments.isEmpty()){
            textChannel.sendMessage(onError("No file provided")).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }
        StringBuilder stringBuilder = new StringBuilder()
                .append("Here is your haste "+commandEvent.getEvent().getAuthor().getAsMention()+":\n");
        Lock lock = new ReentrantLock();
        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
        attachments.stream()
                .forEach(attachment -> {
                    if(attachment.isImage() || attachment.isVideo()){
                        lock.lock();
                        stringBuilder.append("! ").append(attachment.getFileName()).append("\n");
                        lock.unlock();
                        return;
                    }
                    var future = attachment.retrieveInputStream().thenAccept(inputStream -> {
                        try(inputStream) {
                            // create copy
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            inputStream.transferTo(byteArrayOutputStream);
                            String mediaTypeS;
                            try(var input = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())){
                                var mediaType = TIKA.getDetector().detect(input, new Metadata());
                                mediaTypeS = mediaType.toString();
                            }
                            try(var input = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())){
                                String text = IOUtils.toString(input, StandardCharsets.UTF_8);
                                String url = HastebinUtil.uploadToHastebin(text);
                                lock.lock();
                                stringBuilder.append("+ ").append("[").append(mediaTypeS).append("] ").append("[").append(attachment.getFileName()).append("](").append(url).append(")").append("\n");
                                lock.unlock();
                            }
                        }catch (Exception e){
                            lock.lock();
                            stringBuilder.append("! ").append(attachment.getFileName()).append("\n");
                            lock.unlock();
                        }
                    });
                    completableFutureList.add(future);
                });

        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0])).join();
        textChannel.sendMessage(onSuccess(stringBuilder.toString())).queue();
        // delete original message
        commandEvent.getEvent().getMessage().delete().queue();
    }
}
