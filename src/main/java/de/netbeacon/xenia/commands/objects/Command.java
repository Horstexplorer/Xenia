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

package de.netbeacon.xenia.commands.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class Command {

    private final String alias;
    private final String description;
    private final boolean isCommandHandler;
    private HashSet<Permission> memberPermissions;
    private HashSet<Permission> botPermissions;
    private List<String> requiredArgs;
    private final HashMap<String, Command> children = new HashMap<>();

    /**
     * Creates a new instance of the command as command
     *
     * @param alias of the command
     * @param description of the command
     * @param botPermissions required for the user
     * @param memberPermissions required for the member
     * @param requiredArgs required for the command
     */
    public Command(String alias, String description, HashSet<Permission> botPermissions, HashSet<Permission> memberPermissions, List<String> requiredArgs){
        this.alias = alias;
        this.description = description;
        this.botPermissions = botPermissions;
        this.memberPermissions = memberPermissions;
        this.requiredArgs = requiredArgs;
        this.isCommandHandler = false;
    }

    /**
     * Creates a new instance of the command as command handler
     *
     * @param alias of the command handler
     * @param description of the command handler
     */
    public Command(String alias, String description){
        this.alias = alias;
        this.description = description;
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
    public String getDescription() {
        return description;
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
    public HashSet<Permission> getMemberPermissions(){
        return memberPermissions;
    }

    /**
     * Returns a list of required args to use this command
     *
     * @return required args
     */
    public List<String> getRequiredArgs(){
        return requiredArgs;
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
     * Used to check if the command requires the author to be resolved to a member object
     *
     * @return boolean
     */
    public boolean requiresAuthorResolved(){return false;}

    /**
     * Used to execute the command
     * @param args remaining arguments
     * @param commandEvent CommandEvent
     */
    public void execute(List<String> args, CommandEvent commandEvent){
        if(!isCommandHandler){
            // check required args
            if(getRequiredArgCount() > args.size()){
                // missing args
                commandEvent.getEvent().getChannel().sendMessage(onMissingArgs()).queue(s->{},e->{});
                return;
            }
            // check bot permissions
            if(!commandEvent.getEvent().getGuild().getSelfMember().hasPermission(getBotPermissions())){
                // bot does not have the required permissions
                commandEvent.getEvent().getChannel().sendMessage(onMissingBotPerms()).queue(s->{},e->{});
                return;
            }
            if(!(getMemberPermissions().isEmpty() || (getMemberPermissions().containsAll(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) && getMemberPermissions().size() == 2)) || requiresAuthorResolved()){
                // retrieve member
                Member member = commandEvent.getEvent().getGuild().retrieveMember(commandEvent.getEvent().getAuthor()).complete();
                if(member == null || !member.hasPermission(getMemberPermissions())){
                    // invalid permission
                    commandEvent.getEvent().getChannel().sendMessage(onMissingMemberPerms()).queue(s->{},e->{});
                    return;
                }
                commandEvent.setRetrievedAuthor(member);
            }
            // everything alright
            onExecution(args, commandEvent);
        }else if(args.size() > 0){
            Command command = children.get(args.get(0).toLowerCase());
            if(command != null){
                args.remove(0);
                command.execute(args, commandEvent);
            }
        }
    }

    /**
     * Returns an message embed if the execution of the command failed due to bad arguments
     *
     * @return MessageEmbed
     */
    public MessageEmbed onHelpNeeded(){
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Help <"+alias+">")
                .setColor(Color.CYAN);
        StringBuilder help = new StringBuilder();
        if(isCommandHandler){
            for(Map.Entry<String, Command> entry : children.entrySet()){
                Command c = entry.getValue();
                help.append("<> ").append(c.getAlias()).append(" ");
                if(c.isCommandHandler()){
                    help.append("#");
                }else{
                    for(String s : c.getRequiredArgs()){
                        help.append("<").append(s).append(">").append(" ");
                    }
                }
                help.append("- ").append(description);
                help.append("\n");
            }
            embedBuilder.addField("Commands", help.toString(), false);
        }else{
            help.append("<> ").append(alias).append(" ");
            for(String s : requiredArgs){
                help.append("<").append(s).append(">").append(" ");
            }
            help.append("\n");
            embedBuilder.addField("Syntax", help.toString(), false);
        }
        return embedBuilder.build();
    }

    /**
     * Returns an message embed if the execution of the command is missing permissions on the bot side
     *
     * @return MessageEmbed
     */
    public MessageEmbed onMissingBotPerms(){
        return new EmbedBuilder()
                .setTitle("Failed: Bot Is Missing Permissions")
                .setColor(Color.RED)
                .appendDescription("I am unable to execute the command due to missing permissions!")
                .addField("Required Permissions", Arrays.toString(botPermissions.toArray()), false)
                .build();
    }

    /**
     * Returns an message embed if the execution of the command is missing permissions on the user side
     *
     * @return MessageEmbed
     */
    public MessageEmbed onMissingMemberPerms(){
        return new EmbedBuilder()
                .setTitle("Failed: Not Enough Permissions")
                .setColor(Color.RED)
                .appendDescription("You are not allowed to do this !")
                .addField("Required Permissions", Arrays.toString(botPermissions.toArray()), false)
                .build();
    }

    /**
     * Returns an message embed if the execution of the command is missing arguments
     *
     * @return MessageEmbed
     */
    public MessageEmbed onMissingArgs(){
        StringBuilder usage = new StringBuilder().append("<> ").append(alias).append(" ");
        for(String s : requiredArgs){
            usage.append("<").append(s).append(">").append(" ");
        }
        return new EmbedBuilder()
                .setTitle("Failed: Not Enough Arguments")
                .setColor(Color.RED)
                .appendDescription("This command requires more arguments.")
                .addField("Usage", usage.toString(), false)
                .build();
    };

    /**
     * Called on execution of the command
     *
     * @param args remaining arguments of the message
     * @param commandEvent CommandEvent
     */
    public abstract void onExecution(List<String> args, CommandEvent commandEvent);
}
