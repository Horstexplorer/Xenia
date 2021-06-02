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

package de.netbeacon.xenia.bot.interactions.buttons;

import de.netbeacon.xenia.backend.client.objects.external.Message;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.interactions.button.ButtonStyle;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ButtonRegEntry{

	private final String uuid = UUID.randomUUID().toString();
	private final AllowedOrigin allowedOrigin;
	private final AllowedAccessor allowedAccessor;
	private final AllowedActivations allowedActivations;
	private final TimeoutPolicy timeoutPolicy;
	private final long timeoutAt;
	private final ActionHandler actionHandler;
	private final ExceptionHandler exceptionHandler;
	private int remainingActivations;
	private boolean deactivated = false;

	public ButtonRegEntry(AllowedOrigin allowedOrigin, AllowedAccessor allowedAccessor, AllowedActivations allowedActivations, TimeoutPolicy timeoutPolicy, ActionHandler actionHandler, ExceptionHandler exceptionHandler){
		this.allowedOrigin = allowedOrigin;
		this.allowedAccessor = allowedAccessor;
		this.allowedActivations = allowedActivations;
		remainingActivations = allowedActivations.getValue();
		this.timeoutPolicy = timeoutPolicy;
		timeoutAt = System.currentTimeMillis() + timeoutPolicy.ms();
		this.actionHandler = actionHandler;
		this.exceptionHandler = exceptionHandler;
	}

	public String getUuid(){
		return uuid;
	}

	public boolean isAllowedOrigin(long messageId){
		if(allowedOrigin.equals(AllowedOrigin.ANY)){
			return true;
		}
		return allowedOrigin.messageId() == messageId;
	}

	public boolean isAllowedOrigin(Message message){
		return isAllowedOrigin(message.getId());
	}

	public AllowedOrigin getAllowedOrigin(){
		return allowedOrigin;
	}

	public boolean isAllowedAccessor(long value){
		if(allowedAccessor.equals(AllowedAccessor.ANY)){
			return true;
		}
		return allowedAccessor.getValue() == value;
	}

	public boolean isAllowedAccessor(User user){
		return isAllowedAccessor(user.getIdLong());
	}

	public boolean isAllowedAccessor(Member member){
		return isAllowedAccessor(member.getIdLong()) || member.getRoles().stream().anyMatch(role -> allowedAccessor.getValue() == role.getIdLong());
	}

	public AllowedAccessor getAllowedAccessor(){
		return allowedAccessor;
	}

	public boolean isInTime(){
		if(timeoutPolicy.equals(TimeoutPolicy.NONE)){
			return true;
		}
		return System.currentTimeMillis() <= timeoutAt;
	}

	public TimeoutPolicy getTimeoutPolicy(){
		return timeoutPolicy;
	}

	public boolean allowsActivation(){
		synchronized(this){
			if(allowedActivations.equals(AllowedActivations.UNLIMITED)){
				return true;
			}
			if(remainingActivations > 0){
				remainingActivations--;
				return true;
			}
		}
		return false;
	}

	public AllowedActivations getAllowedActivations(){
		return allowedActivations;
	}

	public ActionHandler getActionHandler(){
		return actionHandler;
	}

	public ExceptionHandler getExceptionHandler(){
		return exceptionHandler;
	}

	public Button getButton(ButtonStyle buttonStyle, String label){
		return Button.of(buttonStyle, getUuid(), label);
	}

	public boolean keep(){
		return isInTime() && (allowedActivations.equals(AllowedActivations.UNLIMITED) || remainingActivations > 0);
	}

	public synchronized void deactivate(ShardManager shardManager){
		if(deactivated){
			return;
		}
		deactivated = true;
		TextChannel textChannel = shardManager.getTextChannelById(allowedOrigin.channelId());
		if(textChannel == null) return;
		textChannel.retrieveMessageById(allowedOrigin.messageId()).queue(
			message -> {
				MessageBuilder messageBuilder = new MessageBuilder(message);
				List<ActionRow> newRows = new ArrayList<>();
				message.getActionRows().forEach(row -> {
					List<Button> buttons = new ArrayList<>();
					row.getButtons().forEach(button -> {
						if(getUuid().equals(button.getId())){
							buttons.add(button.asDisabled());
						}else{
							buttons.add(button);
						}
					});
					newRows.add(ActionRow.of(buttons));
				});
				messageBuilder.setActionRows(newRows);
				message.editMessage(messageBuilder.build()).queue();
			}
		);
	}

	public record AllowedOrigin(long messageId, long channelId){

		public static final AllowedOrigin ANY = new AllowedOrigin(0, 0);

		public static AllowedOrigin CUSTOM(long messageId, long channelId){
			return new AllowedOrigin(messageId, channelId);
		}

	}

	public record AllowedAccessor(long value){

		public static final AllowedAccessor ANY = new AllowedAccessor(-1);

		public static AllowedAccessor SPECIFIC(long accessor){
			return new AllowedAccessor(accessor);
		}

		public long getValue(){
			return this.value;
		}

	}

	public record AllowedActivations(int value){

		public static final AllowedActivations ONCE = new AllowedActivations(1);
		public static final AllowedActivations UNLIMITED = new AllowedActivations(-1);

		public static AllowedActivations LIMIT(int count){
			return new AllowedActivations(count);
		}

		public int getValue(){
			return value;
		}

	}

	public record ActionHandler(Consumer<ButtonClickEvent> actionConsumer){

		public static final ActionHandler NONE = new ActionHandler(null);

		public static ActionHandler CUSTOM(Consumer<ButtonClickEvent> actionConsumer){
			return new ActionHandler(actionConsumer);
		}

		public Consumer<ButtonClickEvent> getActionConsumer(){
			return actionConsumer;
		}

	}

	public record ExceptionHandler(BiConsumer<Exception, ButtonClickEvent> exceptionConsumer){

		public static final ExceptionHandler NONE = new ExceptionHandler(null);

		public static ExceptionHandler CUSTOM(BiConsumer<Exception, ButtonClickEvent> exceptionConsumer){
			return new ExceptionHandler(exceptionConsumer);
		}

		public BiConsumer<Exception, ButtonClickEvent> getExceptionConsumer(){
			return exceptionConsumer;
		}

	}

	public record TimeoutPolicy(long ms){

		public static final TimeoutPolicy NONE = new TimeoutPolicy(Long.MAX_VALUE);

		public static TimeoutPolicy CUSTOM(long ms){
			return new TimeoutPolicy(ms);
		}

		public long getValue(){
			return ms;
		}

	}

}
