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

package de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs;

import de.netbeacon.xenia.backend.client.objects.external.Channel;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.HumanTime;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.specialtypes.Mention;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.KEY_PATTERN;
import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.TWITCH_URL_PATTERN;

public class CmdArgDefStatics{

	// LAST

	// NOTIFICATION CMD
	public static final CmdArgDef<Long> NOTIFICATION_ID_DEF = new CmdArgDef.Builder<>("id", Long.class)
		.addPredicateDescription("number")
		.buildAllMatch();
	public static final CmdArgDef<HumanTime> NOTIFICATION_TARGET_TIME_DEF = new CmdArgDef.Builder<>("time", HumanTime.class)
		.addPredicateDescription("\"#h #m #s\" or \"60\" (in minutes) or \"yyyy-MM-dd hh:mm:ss\"")
		.buildAllMatch();
	public static final CmdArgDef<String> NOTIFICATION_MESSAGE_DEF = new CmdArgDef.Builder<>("message", String.class)
		.addPredicateDescription("text")
		.buildAllMatch();
	// TWITCH NOTIFICATION
	public static final CmdArgDef<String> TWITCH_NOTIFICATION_CHANNEL_URL = new CmdArgDef.Builder<>("twitch link", String.class)
		.predicateAddPredicate(string -> TWITCH_URL_PATTERN.matcher(string).matches())
		.addPredicateDescription("twitch channel url")
		.buildAllMatch();
	public static final CmdArgDef<String> TWITCH_NOTIFICATION_CUSTOM_MESSAGE_OPTIONAL = new CmdArgDef.Builder<>("custom message*", String.class)
		.predicateAddStringLengthRange(1, 512)
		.addPredicateDescription("text, 1-512 chars, supports placeholders, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<String> TWITCH_NOTIFICATION_CUSTOM_MESSAGE = new CmdArgDef.Builder<>("custom message", String.class)
		.predicateAddStringLengthRange(1, 512)
		.addPredicateDescription("text, 1-512 chars, supports placeholders")
		.buildAllMatch();
	public static final CmdArgDef<Long> TWITCH_NOTIFICATION_ID = new CmdArgDef.Builder<>("id", Long.class)
		.addPredicateDescription("number")
		.buildAllMatch();

	// TAG CMD
	public static final CmdArgDef<String> TAG_NAME_DEF = new CmdArgDef.Builder<>("name", String.class)
		.predicateAddStringLengthRange(3, 32)
		.predicateAddPredicate(t -> KEY_PATTERN.matcher(t).matches())
		.predicateAddPredicate(t -> !(t.equalsIgnoreCase("create") || t.equalsIgnoreCase("modify") || t.equalsIgnoreCase("delete")))
		.addPredicateDescription("text, 3-32 chars, only alphanumeric chars")
		.buildAllMatch();
	public static final CmdArgDef<String> TAG_CONTENT_DEF = new CmdArgDef.Builder<>("content", String.class)
		.predicateAddStringLengthRange(1, 1500)
		.addPredicateDescription("text, 1-1500 chars")
		.buildAllMatch();
	// SETTINGS
	public static final CmdArgDef<String> SELF_LANGUAGE_ID_DEF = new CmdArgDef.Builder<>("language", String.class)
		.addPredicateDescription("language identifier " + Arrays.toString(TranslationManager.getInstance().getLanguageIds().toArray(new String[0])))
		.buildAllMatch();
	public static final CmdArgDef<String> GUILD_SETTINGS_SETTING_DEF = new CmdArgDef.Builder<>("setting", String.class)
		.addPredicateDescription("name of the setting " + Arrays.toString(Guild.GuildSettings.Settings.values()))
		.buildAllMatch();
	public static final CmdArgDef<Boolean> GUILD_SETTINGS_SETTING_MODE_DEF = new CmdArgDef.Builder<>("enable", Boolean.class)
		.addPredicateDescription("true or false")
		.buildAllMatch();
	public static final CmdArgDef<String> GUILD_LANGUAGE_ID_DEF = new CmdArgDef.Builder<>("language", String.class)
		.addPredicateDescription("language identifier " + Arrays.toString(TranslationManager.getInstance().getLanguageIds().toArray(new String[0])))
		.buildAllMatch();
	public static final CmdArgDef<String> LICENSE_KEY_DEF = new CmdArgDef.Builder<>("licensekey", String.class)
		.predicateAddStringLengthRange(64, 64)
		.addPredicateDescription("license key, 64 chars")
		.buildAllMatch();
	public static final CmdArgDef<String> GUILD_PREFIX_DEF = new CmdArgDef.Builder<>("prefix*", String.class)
		.predicateAddStringLengthRange(1, 4)
		.addPredicateDescription("guild prefix, 1-4 chars, optional")
		.buildAllMatch()
		.setOptional(true);
	// Chatbot settings
	public static final CmdArgDef<Boolean> CB_CHANNEL_ENABLE = new CmdArgDef.Builder<>("enable", Boolean.class)
		.addPredicateDescription("true or false")
		.buildAllMatch();
	public static final CmdArgDef<Mention> CB_CHANNEL_ID_OPTIONAL = new CmdArgDef.Builder<>("channel*", Mention.class)
		.predicateAddCompare(Mention::getMentionType, Message.MentionType.CHANNEL, 0)
		.addPredicateDescription("channel mention, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<String> CB_CHATBOT_MODE = new CmdArgDef.Builder<>("mode", String.class)
		.addPredicateDescription("mode " + Arrays.toString(Guild.D43Z1Mode.Modes.values()))
		.buildAllMatch();
	// CHANNEL SETTINGS
	public static final CmdArgDef<Mention> CHANNEL_ID_OPTIONAL = new CmdArgDef.Builder<>("channel*", Mention.class)
		.predicateAddCompare(Mention::getMentionType, Message.MentionType.CHANNEL, 0)
		.addPredicateDescription("channel mention, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<Mention> CHANNEL_ID_TMPLOGGING_OPTIONAL = new CmdArgDef.Builder<>("tmp logging channel*", Mention.class)
		.predicateAddCompare(Mention::getMentionType, Message.MentionType.CHANNEL, 0)
		.addPredicateDescription("channel mention, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<String> CHANNEL_ACCESS_MODE = new CmdArgDef.Builder<>("access mode", String.class)
		.addPredicateDescription("access mode " + Arrays.toString(Channel.AccessMode.Mode.values()))
		.buildAllMatch();
	public static final CmdArgDef<Boolean> CHANNEL_LOGGING_ENABLE = new CmdArgDef.Builder<>("enable", Boolean.class)
		.addPredicateDescription("true or false")
		.buildAllMatch();

	// ADMIN
	public static final CmdArgDef<Mention> ADMIN_CHATLOG_CHANNEL = new CmdArgDef.Builder<>("channel*", Mention.class)
		.predicateAddCompare(Mention::getMentionType, Message.MentionType.CHANNEL, 0)
		.addPredicateDescription("channel mention, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<Boolean> ADMIN_CHATLOG_LIMIT = new CmdArgDef.Builder<>("respLimit*", Boolean.class)
		.addPredicateDescription("true or false, optional")
		.buildAllMatch()
		.setOptional(true);
	public static final CmdArgDef<String> ADMIN_D43Z1_INPUT = new CmdArgDef.Builder<>("input", String.class)
		.addPredicateDescription("text")
		.buildAllMatch();

	// ANIME
	public static final CmdArgDef<Mention> ANIME_OPTIONAL_USER = new CmdArgDef.Builder<>("user*", Mention.class)
		.predicateAddCompare(Mention::getMentionType, Message.MentionType.USER, 0)
		.addPredicateDescription("user mention, optional")
		.buildAllMatch()
		.setOptional(true);

}
