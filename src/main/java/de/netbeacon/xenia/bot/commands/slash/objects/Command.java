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

package de.netbeacon.xenia.bot.commands.slash.objects;

import de.netbeacon.utils.statistics.AverageCounter;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.BackendException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.slash.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class Command {

    private final String alias;
    private final HashMap<String, Command> childCommands = new HashMap<>();
    private final CommandUpdateAction.CommandData commandData;
    private final CommandUpdateAction.SubcommandData subcommandData;
    private final CommandUpdateAction.SubcommandGroupData subcommandGroupData;
    private final List<CmdArgDef> options = new ArrayList<>();
    private CommandCooldown commandCooldown;
    private final HashSet<Permission> memberPrimaryPermissions = new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ));
    private final HashSet<Role.Permissions.Bit> memberSecondaryPermissions = new HashSet<>(Collections.singletonList(Role.Permissions.Bit.BOT_INTERACT));
    private final HashSet<Permission> botPermissions = new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ));
    private final AverageCounter processingAvgCounter = new AverageCounter();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Basic command with options
     * @param alias
     * @param description
     * @param commandCooldown
     * @param botPermissions
     * @param memberPrimaryPermissions
     * @param memberSecondaryPermission
     * @param options
     */
    public Command(String alias, String description, CommandCooldown commandCooldown, HashSet<Permission> botPermissions, HashSet<Permission> memberPrimaryPermissions, HashSet<Role.Permissions.Bit> memberSecondaryPermission, List<CmdArgDef> options){
        this.alias = alias;
        this.commandData = new CommandUpdateAction.CommandData(alias, description);
        this.subcommandData = new CommandUpdateAction.SubcommandData(alias, description);
        this.subcommandGroupData = null;
        this.commandCooldown = commandCooldown;
        if(botPermissions != null){
            this.botPermissions.addAll(botPermissions);
        }
        if(memberPrimaryPermissions != null){
            this.memberPrimaryPermissions.addAll(memberPrimaryPermissions);
        }
        if(memberSecondaryPermission != null){
            this.memberSecondaryPermissions.addAll(memberSecondaryPermission);
        }
        if(options != null){
            for(CmdArgDef option : options){
                this.options.add(option);
                commandData.addOption(option.getOptionData());
                subcommandData.addOption(option.getOptionData());
            }
        }
    }

    /**
     * Command Group
     * @param alias
     * @param description
     * @param subCommands
     */
    public Command(String alias, String description, Command...subCommands){
        this.alias = alias;
        this.commandData = new CommandUpdateAction.CommandData(alias, description);
        this.subcommandData = null;
        this.subcommandGroupData = new CommandUpdateAction.SubcommandGroupData(alias, description);
        for(Command command : subCommands){
            if(command == null) continue;
            subcommandGroupData.addSubcommand(command.getSubCommandData());
            childCommands.put(command.getAlias(), command);
        }
    }

    /**
     * Command RootGroup
     * @param alias
     * @param description
     * @param subCommands
     */
    public Command(String alias, String description, boolean areGroups, Command...subCommands){
        this.alias = alias;
        this.commandData = new CommandUpdateAction.CommandData(alias, description);
        this.subcommandData = new CommandUpdateAction.SubcommandData(alias, description);
        this.subcommandGroupData = null;
        for(Command command : subCommands){
            if(command == null) continue;
            if(areGroups){
                commandData.addSubcommandGroup(command.getSubcommandGroupData());
            } else {
                commandData.addSubcommand(command.getSubCommandData());
            }
            childCommands.put(command.getAlias(), command);
        }
    }

    public String getAlias(){ return alias; }

    public boolean isCommandGroup(){ return !childCommands.isEmpty(); }

    /**
     * Returns the required permissions of the bot
     *
     * @return list of the required permissions
     */
    public HashSet<Permission> getBotPermissions(){
        return botPermissions;
    }

    /**
     * Returns the required permissions of the member
     *
     * @return list of the required permissions
     */
    public HashSet<Permission> getMemberPrimaryPermissions(){
        return memberPrimaryPermissions;
    }

    public HashSet<Role.Permissions.Bit> getMemberSecondaryPermissions(){
        return memberSecondaryPermissions;
    }

    /**
     * Returns the command cooldown
     * @return CommandCooldown
     */
    public CommandCooldown getCommandCooldown(){
        return commandCooldown;
    }

    public List<Command> getChildCommands(){ return new ArrayList<>(childCommands.values()); }

    public Command getChildCommand(String alias){ return childCommands.get(alias); }

    public CommandUpdateAction.CommandData getCommandData(){ return commandData; }

    public CommandUpdateAction.SubcommandData getSubCommandData(){ return subcommandData; }

    public CommandUpdateAction.SubcommandGroupData getSubcommandGroupData(){ return subcommandGroupData; }

    public List<CmdArgDef> getOptions() { return options; }

    /**
     * Used to execute the command
     * @param args remaining arguments
     * @param commandEvent CommandEvent
     */
    public void execute(List<String> args, CommandEvent commandEvent){
        if(!isCommandGroup()){
            TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(commandEvent.getBackendDataPack().getbGuild(), commandEvent.getBackendDataPack().getbMember());
            if(translationPackage == null){
                commandEvent.getEvent().reply("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue();
                return;
            }
            long guildId = commandEvent.getEvent().getGuild().getIdLong(); // should not throw as we check for this before in the listener
            long authorId = commandEvent.getEvent().getUser().getIdLong();
            // check bot permissions
            if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(commandEvent.getEvent().getTextChannel(), getBotPermissions())){
                // bot does not have the required permissions
                if(commandEvent.getEvent().getGuild().getSelfMember().hasPermission(commandEvent.getEvent().getTextChannel(), Permission.MESSAGE_WRITE)){
                    commandEvent.getEvent().reply(onMissingBotPerms(translationPackage)).queue();
                }
                return;
            }

            Guild bGuild = commandEvent.getBackendDataPack().getbGuild();
            Member bMember = commandEvent.getBackendDataPack().getbMember();
            net.dv8tion.jda.api.entities.Member member = commandEvent.getEvent().getMember();
            if(
                    !(bMember.metaIsOwner() || XeniaCore.getInstance().getOwnerID() == authorId)
                            &&
                            (
                                    (bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE) && (member == null || bMember.getRoles().stream()//e
                                            .filter(r->r.getPermissions().hasAllPermission(getMemberSecondaryPermissions().toArray(Role.Permissions.Bit[]::new)))
                                            .findFirst().isEmpty()))
                                            ||
                                            (!bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE) && (member == null || (!member.hasPermission(getMemberPrimaryPermissions()))))
                            )
            ){
                // invalid permission
                commandEvent.getEvent().reply(onMissingMemberPerms(translationPackage, bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE))).queue();
                return;
            }
            // check cooldown
            if(commandCooldown != null){
                // process cd
                if(!commandCooldown.allow(guildId, authorId)){
                    // cd running
                    commandEvent.getEvent().reply(onCooldownActive(translationPackage)).queue();
                    return;
                }
                // activate cd
                commandCooldown.deny(guildId, authorId);
            }
            // check arguments
            CmdArgs cmdArgs;
            try{
                cmdArgs = CmdArgFactory.getArgs((s) -> commandEvent.getEvent().getOption(s), options);
            }catch (CmdArgFactory.Exception e){
                commandEvent.getEvent().reply(onBadOptions(translationPackage)).queue();
                return;
            }
            // everything alright
            long startTime = System.currentTimeMillis();
            try{
                commandEvent.addProcessingTime(processingAvgCounter.getAvg());
                boolean ackRequired = commandEvent.getEstimatedProcessingTime() > 2.5; // we should ack if it takes longer than 3 seconds, we plan in some buffer
                onExecution(cmdArgs, commandEvent, translationPackage, ackRequired);
            }catch (Exception e){
                logger.error("Unhandled exception: ", e);
                commandEvent.getEvent().reply(onUnhandledException(translationPackage, e)).queue();
            }finally {
                processingAvgCounter.add(System.currentTimeMillis()-startTime);
            }
        }else if(isCommandGroup()){
            Command command = getChildCommand(args.get(0));
            if(command != null){
                args.remove(0);
                command.execute(args, commandEvent);
            }
        }
    }

    /**
     * Returns an message embed if the execution of the command is not allowed due to it being rate limited
     *
     * @return MessageEmbed
     */
    public MessageEmbed onCooldownActive(TranslationPackage translationPackage){
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onCooldownActive.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription(translationPackage.getTranslation("default.onCooldownActive.description"))
                .build();
    }

    /**
     * Returns an message embed if the execution of the command is missing permissions on the bot side
     *
     * @return MessageEmbed
     */
    public MessageEmbed onMissingBotPerms(TranslationPackage translationPackage){
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingBotPerms.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription(translationPackage.getTranslation("default.onMissingBotPerms.description"))
                .addField(translationPackage.getTranslation("default.onMissingBotPerms.requiredPerms.fn"), Arrays.toString(botPermissions.toArray()), false)
                .build();
    }

    /**
     * Returns an message embed if the execution of the command is missing permissions on the user side
     *
     * @param vPerms show vPerms or default
     * @return MessageEmbed
     */
    public MessageEmbed onMissingMemberPerms(TranslationPackage translationPackage, boolean vPerms){
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingMemberPerms.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription(translationPackage.getTranslation("default.onMissingMemberPerms.description"));
        if(vPerms){
            embedBuilder.addField(translationPackage.getTranslation("default.onMissingMemberPerms.requiredPerms.fn"), Arrays.toString(memberSecondaryPermissions.toArray()), false);
        }else{
            embedBuilder.addField(translationPackage.getTranslation("default.onMissingMemberPerms.requiredPerms.fn"), Arrays.toString(memberPrimaryPermissions.toArray()), false);
        }
        return embedBuilder.build();
    }

    /**
     * Returns an message embed if the execution of the command is missing an options or invalid data has been provided
     *
     * @return MessageEmbed
     */
    public MessageEmbed onBadOptions(TranslationPackage translationPackage){
        StringBuilder usage = new StringBuilder().append("<> ").append(alias).append(" ");
        for(CmdArgDef s : options){
            usage.append("<").append(s.getName()).append(">").append(" ");
        }
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingArgs.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription(translationPackage.getTranslation("default.onMissingArgs.description"))
                .addField(translationPackage.getTranslation("default.onMissingArgs.usage.fn"), usage.toString(), false);
        for(CmdArgDef s : options){
            embedBuilder.addField("<"+s.getName()+">", s.getExtendedDescription(), false);
        }
        return embedBuilder.build();
    };

    /**
     * Returns an message embed which can be used to tell that something is wrong
     *
     * @param message the message which should be displayed
     * @return MessageEmbed
     */
    public MessageEmbed onError(TranslationPackage translationPackage, String message){
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onError.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .setDescription(message)
                .build();
    }

    /**
     * Returns an message embed which can be used to tell that things went good
     *
     * @param message the message which should be displayed
     * @return MessageEmbed
     */
    public MessageEmbed onSuccess(TranslationPackage translationPackage, String message){
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onSuccess.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.GREEN)
                .setDescription(message)
                .build();
    }

    /**
     * Returns an message embed which can be used to indicate internal errors to the user
     *
     * @param e exception
     * @return MessageEmbed
     */
    public MessageEmbed onUnhandledException(TranslationPackage translationPackage, Exception e){
        EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onUnhandledException.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser());
        if(e instanceof DataException){
            if(((DataException) e).getType().equals(DataException.Type.HTTP)){
                embedBuilder.setDescription(translationPackage.getTranslationWithPlaceholders("default.onUnhandledException.dataexception.msg", ((DataException) e).getType().name()+" ("+((DataException) e).getCode()+")"));
            }else{
                embedBuilder.setDescription(translationPackage.getTranslationWithPlaceholders("default.onUnhandledException.dataexception.msg", ((DataException) e).getType().name()));
            }
        }else if(e instanceof CacheException){
            embedBuilder.setDescription(translationPackage.getTranslationWithPlaceholders("default.onUnhandledException.cacheexception.msg", ((CacheException) e).getType().name()));
        }else if(e instanceof BackendException){
            embedBuilder.setDescription(translationPackage.getTranslationWithPlaceholders("default.onUnhandledException.backendexception.msg.msg", ((BackendException) e).getId()));
        }else {
            embedBuilder.setDescription(translationPackage.getTranslation("default.onUnhandledException.exception.msg"));
        }
        return embedBuilder.build();
    }


    /**
     * Called on execution of the command
     * @param commandEvent CommandEvent
     * @param translationPackage translation package which should be used
     */
    public abstract void onExecution(CmdArgs cmdArgs, CommandEvent commandEvent, TranslationPackage translationPackage, boolean ackRequired) throws Exception;
}
