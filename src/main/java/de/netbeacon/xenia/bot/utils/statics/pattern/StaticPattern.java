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

package de.netbeacon.xenia.bot.utils.statics.pattern;

import java.util.regex.Pattern;

/**
 * Contains all regex pattern used
 */
public class StaticPattern {

    public static final Pattern SingleWordPattern = Pattern.compile("^[\\S]*$");
    public static final Pattern WordPattern = Pattern.compile("\\p{L}*"); // only letters
    public static final Pattern NumberPattern = Pattern.compile("\\p{N}*"); // only numbers
    public static final Pattern PunctuationPattern = Pattern.compile("\\p{Punct}");

    public static final Pattern WhiteSpacePattern = Pattern.compile("\\s");
    public static final Pattern MultiWhiteSpacePattern = Pattern.compile("\\s+");
    public static final Pattern LineEndingPattern = Pattern.compile("\n+|\r+|(\r\n)+");
    public static final Pattern LeadingPunctuationPattern = Pattern.compile("^(\\p{Punct}+)");
    public static final Pattern TrailingPunctuationPattern = Pattern.compile("(\\p{Punct}+)$");
    public static final Pattern EnclosingPunctuationPattern = Pattern.compile("^(\\p{Punct}+)[^\\p{Punct}]+(\\p{Punct}+)$");

    public static final Pattern UrlPattern = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

    public static final Pattern ArgPattern = Pattern.compile("(\"(\\X*?)\")|([^\\s]\\X*?(?=\\s|\"|$))");
    public static final Pattern CodeBlock = Pattern.compile("(`{1,3})(.*?\\s)(.*?)(`{1,3})", Pattern.MULTILINE|Pattern.DOTALL);
    public static final Pattern JavaClass = Pattern.compile("(public class)(.*?)(\\{)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]*$");

    public static final Pattern TWITCH_URL_PATTERN = Pattern.compile("https:\\/\\/www\\.twitch\\.tv\\/([a-zA-Z0-9_]{4,25})$");
}
