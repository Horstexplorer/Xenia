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

package de.netbeacon.xenia.bot.commands.objects;

import de.netbeacon.d43z.one.algo.LiamusJaccard;
import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.backend.client.objects.external.Guild;
import de.netbeacon.xenia.backend.client.objects.external.Member;
import de.netbeacon.xenia.backend.client.objects.external.Role;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.BackendException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.CacheException;
import de.netbeacon.xenia.backend.client.objects.internal.exceptions.DataException;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgDef;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgFactory;
import de.netbeacon.xenia.bot.commands.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationManager;
import de.netbeacon.xenia.bot.commands.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Command {

    private final String alias;
    private final LiamusJaccard.BitArray64 aliasBitArray;
    private boolean isCommandHandler;
    private CommandCooldown commandCooldown;
    private final HashSet<Permission> memberPrimaryPermissions = new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ));
    private final HashSet<Role.Permissions.Bit> memberSecondaryPermissions = new HashSet<>(Collections.singletonList(Role.Permissions.Bit.BOT_INTERACT));
    private final HashSet<Permission> botPermissions = new HashSet<>(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ));
    private final List<CmdArgDef> requiredArgs = new ArrayList<>();
    private final HashMap<String, Command> children = new HashMap<>();
    private boolean isHybrid = false;

    /**
     * Creates a new instance of the command as command
     *
     * @param alias of the command
     * @param commandCooldown cooldown of the command
     * @param botPermissions required for the user
     * @param memberPrimaryPermissions required for the member using discord perms
     * @param memberSecondaryPermission required for the member using v perms
     * @param commandArgs for the command
     */
    public Command(String alias, CommandCooldown commandCooldown, HashSet<Permission> botPermissions, HashSet<Permission> memberPrimaryPermissions, HashSet<Role.Permissions.Bit> memberSecondaryPermission, List<CmdArgDef> commandArgs){
        this.alias = alias;
        this.aliasBitArray = LiamusJaccard.hashString(alias, 1);
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
        if(commandArgs != null){
            this.requiredArgs.addAll(commandArgs);
        }
        this.isCommandHandler = false;
    }

    /**
     * Creates a new instance of the command as command handler
     *
     * @param alias of the command handler
     */
    public Command(String alias){
        this.alias = alias;
        this.aliasBitArray = LiamusJaccard.hashString(alias, 1);
        this.isCommandHandler = true;
    }

    /**
     * Returns the alias of the command
     *
     * @return alias
     */
    public String getAlias(){
        return alias.toLowerCase();
    }

    /**
     * Returns the description of the command
     *
     * @return description
     */
    public String getDescription(TranslationPackage translationPackage) {
        return translationPackage.getTranslation(getClass().getName()+".description");
    }

    /**
     * Returns whether this objects acts like a command or command handler
     *
     * @return true if it is a command handler
     */
    public boolean isCommandHandler(){
        return isCommandHandler;
    }

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
     * Returns a list of required args to use this command
     *
     * @return required args
     */
    public List<CmdArgDef> getCommandArgs(){
        return requiredArgs;
    }

    public CommandCooldown getCommandCooldown(){
        return commandCooldown;
    }

    /**
     * Returns the number of args required to use this command
     *
     * @return number of args required
     */
    public int getRequiredArgCount(){
        return requiredArgs.size();
    }

    /**
     * Used to get child commands
     *
     * @return Child commands
     */
    public HashMap<String, Command> getChildCommands(){return children;}

    /**
     * Used to add child commands to this command
     *
     * This wont do anything if this object is not a command handler
     */
    public void addChildCommand(Command command){
        if(isCommandHandler){
            children.put(command.getAlias(), command);
        }
    }

    /**
     * Enables hybrid mode for commands so that they can act as command groups aswell
     */
    protected void activateHybridMode(){
        isCommandHandler = true;
        isHybrid = true;
    }

    /**
     * Check if this command is hybrid
     * @return boolean
     */
    public boolean isHybrid(){
        return isHybrid;
    }

    public LiamusJaccard.BitArray64 getAliasBitArray(){
        return aliasBitArray;
    }

    /**
     * Used to execute the command
     * @param args remaining arguments
     * @param commandEvent CommandEvent
     */
    public void execute(List<String> args, CommandEvent commandEvent){
        execute(args, commandEvent, false);
    }

    /**
     * Used to execute the command
     * @param args remaining arguments
     * @param commandEvent CommandEvent
     * @param s2 true on recursive call to check with the alternate mode
     */
    private void execute(List<String> args, CommandEvent commandEvent, boolean s2){
        if(!isCommandHandler || (s2 && isHybrid)){
            TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(commandEvent.getBackendDataPack().getbGuild(), commandEvent.getBackendDataPack().getbMember());
            if(translationPackage == null){
                commandEvent.getEvent().getChannel().sendMessage("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue();
                return;
            }
            long guildId = commandEvent.getEvent().getGuild().getIdLong();
            long authorId = commandEvent.getEvent().getAuthor().getIdLong();
            // check required args
            CmdArgs cmdArgs = CmdArgFactory.getArgs(args, getCommandArgs());
            //if(getRequiredArgCount() > args.size() || !cmdArgs.verify()){
            if(!cmdArgs.verify()){
                // missing args
                commandEvent.getEvent().getChannel().sendMessage(onMissingArgs(translationPackage)).queue();
                return;
            }
            // check bot permissions
            if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(commandEvent.getEvent().getChannel(),getBotPermissions())){
                // bot does not have the required permissions
                if(commandEvent.getEvent().getGuild().getSelfMember().hasPermission(commandEvent.getEvent().getChannel(), Permission.MESSAGE_WRITE)){
                    commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms(translationPackage)).queue();
                }
                return;
            }

            Guild bGuild = commandEvent.getBackendDataPack().getbGuild();
            Member bMember = commandEvent.getBackendDataPack().getbMember();
            net.dv8tion.jda.api.entities.Member member = commandEvent.getEvent().getMember();
            if(
                    !(bMember.metaIsOwner() || XeniaCore.getInstance().getOwnerID() == commandEvent.getEvent().getAuthor().getIdLong())
                    &&
                    (
                            (bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE) && (member == null || bMember.getRoles().stream()//e
                                    .filter(r->r.getPermissions().hasAllPermission(memberSecondaryPermissions.toArray(Role.Permissions.Bit[]::new)))
                                    .findFirst().isEmpty()))
                            ||
                            (!bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE) && (member == null || (!member.hasPermission(getMemberPrimaryPermissions()))))
                    )
            ){
                // invalid permission
                commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms(translationPackage, bGuild.getSettings().has(Guild.GuildSettings.Settings.VPERM_ENABLE))).queue();
                return;
            }
            if(commandCooldown != null){
                // process cd
                if(!commandCooldown.allow(guildId, authorId)){
                    // cd running
                    commandEvent.getEvent().getChannel().sendMessage(onCooldownActive(translationPackage)).queue();
                    return;
                }
                // activate cd
                commandCooldown.deny(guildId, authorId);
            }
            // everything alright
            try{
                onExecution(cmdArgs, commandEvent, translationPackage);
            }catch (Exception e){
                commandEvent.getEvent().getChannel().sendMessage(onUnhandledException(translationPackage, e)).queue();
            }
        }else{
            if(args.size() > 0) {
                Command command = children.get(args.get(0).toLowerCase());
                if (command != null) {
                    args.remove(0);
                    command.execute(args, commandEvent);
                }else if(isHybrid){
                    execute(args, commandEvent, true); // execute handler again
                }else {
                    List<Command> estimatedCommands = Command.getBestMatch(args.get(0), getChildCommands());
                    if(estimatedCommands.isEmpty()){
                        return;
                    }
                    TranslationPackage translationPackage = TranslationManager.getInstance().getTranslationPackage(commandEvent.getBackendDataPack().getbGuild(), commandEvent.getBackendDataPack().getbMember());
                    if(translationPackage == null){
                        commandEvent.getEvent().getChannel().sendMessage("Internal Error - Language Not Available.\nTry again, check the language settings or contact an administrator if the error persists.").queue();
                        return;
                    }
                    if(this instanceof CommandGroup){
                        StringBuilder commandPathBuilder = new StringBuilder();
                        CommandGroup current = ((CommandGroup) this).getParent();
                        while(current != null){
                            commandPathBuilder.insert(0, current.getAlias() + " ");
                            current = current.getParent();
                        }
                        commandPathBuilder.append(" ").append(this.getAlias());
                        String commandPath = commandPathBuilder.toString().trim();
                        commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslationWithPlaceholders("default.estimatedCommand.msg", commandPath+" "+args.get(0), commandPath+" "+estimatedCommands.get(0).getAlias()))).queue();
                    }else{
                        commandEvent.getEvent().getChannel().sendMessage(onError(translationPackage, translationPackage.getTranslationWithPlaceholders("default.estimatedCommand.msg", args.get(0), estimatedCommands.get(0).getAlias()))).queue();
                    }
                }
            }else if (isHybrid){
                if(!requiredArgs.isEmpty()){
                    children.get("help").execute(args, commandEvent);
                }else{
                    execute(args, commandEvent, true); // execute handler again
                }
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
     * Returns an message embed if the execution of the command is missing arguments
     *
     * @return MessageEmbed
     */
    public MessageEmbed onMissingArgs(TranslationPackage translationPackage){
        StringBuilder usage = new StringBuilder().append("<> ").append(alias).append(" ");
        for(CmdArgDef s : requiredArgs){
            usage.append("<").append(s.getName()).append(">").append(" ");
        }
        return EmbedBuilderFactory.getDefaultEmbed(translationPackage.getTranslation("default.onMissingArgs.title"), XeniaCore.getInstance().getShardManager().getShards().get(0).getSelfUser())
                .setColor(Color.RED)
                .appendDescription(translationPackage.getTranslation("default.onMissingArgs.description"))
                .addField(translationPackage.getTranslation("default.onMissingArgs.usage.fn"), usage.toString(), false)
                .build();
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
            embedBuilder.setDescription(translationPackage.getTranslationWithPlaceholders("default.onUnhandledException.dataexception.msg", ((DataException) e).getType().name()));
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
     * Used to find the best matching command to the input string
     *
     * @param arg the estimated command name
     * @param commandMap a map of the available commands
     * @return List<Command>
     */
    public static List<Command> getBestMatch(String arg, Map<String, Command> commandMap){
        LiamusJaccard.BitArray64 argBitArray = LiamusJaccard.hashString(arg.toLowerCase(), 1);
        List<Command> commands = new ArrayList<>(commandMap.values());
        return commands.stream()
                .map(command -> new Pair<>(command, LiamusJaccard.similarityCoefficient(argBitArray, command.getAliasBitArray())))
                .sorted((o1, o2) -> o2.getValue2().compareTo(o1.getValue2()))
                .filter(f -> {
                    System.out.println(f.getValue1().alias+" "+f.getValue2());
                    return true;
                })
                .map(Pair::getValue1)
                .collect(Collectors.toList());
    }

    /**
     * Called on execution of the command
     *  @param args remaining arguments of the message
     * @param commandEvent CommandEvent
     * @param translationPackage translation package which should be used
     */
    public abstract void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage) throws Exception;
}
