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

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Message;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.interactions.button.ButtonStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ButtonRegEntry{

	private final String uuid = UUID.randomUUID().toString();
	private final AllowedOrigins allowedOrigins;
	private final AllowedAccessor allowedAccessor;
	private final AllowedActivations allowedActivations;
	private final TimeoutPolicy timeoutPolicy;
	private final long timeoutAt;
	private final ActionHandler actionHandler;
	private final ExceptionHandler exceptionHandler;
	private int remainingActivations;

	public ButtonRegEntry(AllowedOrigins allowedOrigins, AllowedAccessor allowedAccessor, AllowedActivations allowedActivations, TimeoutPolicy timeoutPolicy, ActionHandler actionHandler, ExceptionHandler exceptionHandler){
		this.allowedOrigins = allowedOrigins;
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

	public boolean isAllowedOrigin(Long... values){
		if(allowedOrigins.equals(AllowedOrigins.ANY)){
			return true;
		}
		return allowedOrigins.getValues().containsAll(List.of(values));
	}

	public boolean isAllowedOrigin(Guild guild, Channel channel, Message message){
		return isAllowedOrigin(guild.getIdLong(), channel.getChannelId(), message.getId());
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

	public boolean isInTime(){
		if(timeoutPolicy.equals(TimeoutPolicy.NONE)){
			return true;
		}
		return System.currentTimeMillis() <= timeoutAt;
	}

	public boolean isAllowedAccessor(Member member){
		return isAllowedAccessor(member.getIdLong()) || member.getRoles().stream().anyMatch(role -> allowedAccessor.getValue() == role.getIdLong());
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
		return isInTime() && (!allowedActivations.equals(AllowedActivations.UNLIMITED) || remainingActivations > 0);
	}

	public record AllowedOrigins(Long... values){

		public static final AllowedOrigins ANY = new AllowedOrigins(-1L);

		public static AllowedOrigins CUSTOM(Long... value){
			return new AllowedOrigins(value);
		}

		public Set<Long> getValues(){
			return new HashSet<>(List.of(values));
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
