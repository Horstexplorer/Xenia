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

package de.netbeacon.xenia.bot.commands.objects.misc.cmdargs;

import java.time.LocalDateTime;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.KEY_PATTERN;

public class CmdArgDefStatics {

    // NOTIFICATION CMD
    public static final CmdArgDef<Long> NOTIFICATION_ID_DEF = new CmdArgDef.Builder<Long>("id", Long.class)
            .buildAllMatch();
    public static final CmdArgDef<LocalDateTime> NOTIFICATION_TARGET_TIME_DEF = new CmdArgDef.Builder<LocalDateTime>("time", LocalDateTime.class)
            .buildAllMatch();
    public static final CmdArgDef<String> NOTIFICATION_MESSAGE_DEF = new CmdArgDef.Builder<String>("message", String.class)
            .buildAllMatch();

    // TAG CMD
    public static final CmdArgDef<String> TAG_NAME_DEF = new CmdArgDef.Builder<String>("name", String.class)
            .predicateAddStringLengthRange(3, 32)
            .predicateAddPredicate(t-> KEY_PATTERN.matcher(t).matches())
            .predicateAddPredicate(t-> !(t.equalsIgnoreCase("create") || t.equalsIgnoreCase("modify") || t.equalsIgnoreCase("delete")))
            .buildAllMatch();
    public static final CmdArgDef<String> TAG_CONTENT_DEF = new CmdArgDef.Builder<String>("content", String.class)
            .predicateAddStringLengthRange(1, 1500)
            .buildAllMatch();
}