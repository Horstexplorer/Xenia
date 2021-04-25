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

package de.netbeacon.xenia.bot.commands.chat.structure.settings.roles;

import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.bot.commands.chat.objects.CommandGroup;
import de.netbeacon.xenia.bot.commands.chat.objects.HybridCommand;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class HYBRIDRoles extends HybridCommand{

	public HYBRIDRoles(CommandGroup parent){
		super(parent, "roles", false, new CommandCooldown(CommandCooldown.Type.Guild, 2000),
			null,
			new HashSet<>(List.of(Permission.MANAGE_SERVER)),
			new HashSet<>(List.of(Role.Permissions.Bit.GUILD_SETTINGS_OVERRIDE)),
			null
		);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception{
		Guild guild = commandEvent.getBackendDataPack().getbGuild();
		StringBuilder stringBuilder = new StringBuilder();
		for(Role role : guild.getRoleCache().getAllAsList()){
			stringBuilder.append(role.getId()).append(" ").append(role.getRoleName()).append(" ").append(Arrays.toString(role.getPermissions().getBits().toArray()));
		}
		String roleS = stringBuilder.toString();
		if(roleS.isEmpty()){
			roleS = translationPackage.getTranslation(getClass().getName() + ".response.is_empty");
		}
		commandEvent.getEvent().getChannel().sendMessage(
			EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation(getClass(), "response.title"), commandEvent.getEvent().getAuthor())
				.setDescription(roleS)
				.build()
		).queue();
	}

}
