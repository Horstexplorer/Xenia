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

    public final static Pattern SingleWordPattern = Pattern.compile("^[\\S]*$");
    public final static Pattern WordPattern = Pattern.compile("\\p{L}*"); // only letters
    public final static Pattern NumberPattern = Pattern.compile("\\p{N}*"); // only numbers
    public final static Pattern PunctuationPattern = Pattern.compile("\\p{Punct}");

    public final static Pattern WhiteSpacePattern = Pattern.compile("\\s");
    public final static Pattern MultiWhiteSpacePattern = Pattern.compile("\\s+");
    public final static Pattern LineEndingPattern = Pattern.compile("\n+|\r+|(\r\n)+");
    public final static Pattern LeadingPunctuationPattern = Pattern.compile("^(\\p{Punct}+)");
    public final static Pattern TrailingPunctuationPattern = Pattern.compile("(\\p{Punct}+)$");
    public final static Pattern EnclosingPunctuationPattern = Pattern.compile("^(\\p{Punct}+)[^\\p{Punct}]+(\\p{Punct}+)$");

    public final static Pattern UrlPattern = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

    public final static Pattern ArgPattern = Pattern.compile("(\"(\\X*?)\")|([^\\s]\\X*?(?=\\s|\"|$))");
    public final static Pattern CodeBlock = Pattern.compile("(`{1,3})(.*?\\s)(.*?)(`{1,3})", Pattern.MULTILINE|Pattern.DOTALL);
    public final static Pattern JavaClass = Pattern.compile("(public class)(.*?)(\\{)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public final static Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]*$");
}
