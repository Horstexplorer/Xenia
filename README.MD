# Xenia
#### Discord Bot
Xenia provides various tools to improve the experience in your guild:

- Keep track of important things
  ```~notification create "6h 20m" "meet with friends"```
- Never miss when your favorites are streaming on twitch
  ```~twitch create https://www.twitch.tv/<usr>```
- Where did the last message go?
  ```~last deleted```
- Can u upload that file to hastebin?
  ```~hastebin```
- What a nice cat video i found
  ```~tag create "smallcat" "https://www.youtube.com/watch?v=W86cTIoMv2U"```
- Lets chat somewhere!*
  ```~chatbot setup```
  And more

Not sure how the current command group works? just add `help` to the end
```~twitch help```
or ```~help``` to see all command-/groups
___
# Settings and configuration
## Language
Most responses have been translated. Available languages are:
```
en_us - English
en_uwu - A more UwU version of English
de_de - German
fr_fr - Frensh
es_es - Spanish
ja_jp - Japanese
```
To set your personal response language, use  
```~settings self language LOCALE_ID```  
To enforce a language for your guild, use  
```~settings general language LOCALE_ID```  
```~settings general settings ENFORCE_LANGUAGE true```
## Guild Settings
### General
Some settings for the guild in general are available  
```~settings general settings OPTION true/false```  
Options:
```
COMMAND_AUTO_CORRECT - Disables the typo response and executes the command it thinks is correct on typos
ENFORCE_LANGUAGE - Enforce the set language as response language
VPERM_ENABLE - Use the virtuale role/permission system provided by Xenia to check member/(role) permissions instead of the Discord one
```
### Prefix
The prefix of the bot (default ~) can be changed to any 1-4 characters you want  
```~settings general prefix PREFIX```
### License
Some features of the bot are limited to a certain amount to keep usage reasonable. Those limits can be extended by asking us for a license key if u have the need for more. This will raise the limits as desired.  
```~settings general upgradelicense LICENSEKEY```  
The current limits can be viewed on the general overview  
```~settings general```
## Channel Settings
### Access mode
Xenia has various modes on how to act (and not to act) in a specific (or the current) channel  
```~settings channel accessmode ACCESSMODE #optional_channel_mention```  
Options:
```
ACTIVE - Xenia will properly interact
INACTIVE - Xenia wont respond to commands but logging is still active
DISABLED - Xenia will ignore this channel
```
### Logging
Xenia is able to log messages to find out if something has been edited or deleted. This can be turned off/on on a per channel basis and allows to just keep track off those changes or log them to a channel.  
```~settings channel logging true/false #channel_mention #optional_log_channel```
## Role Settings
It is possible to create and assign virtual roles to a member to use the virtual permission system of Xenia.  
This feature hasnt been implemented yet
___
Some features might not work properly at the moment or have not yet been implemented

*This feature is only available in English and is intended for entertainment purposes only. Responses are generated from unverified messages - Some answers may seem inappropriate in certain contexts. Learn more about the origin of the root datasets [here](href="https://github.com/Horstexplorer/Xenia/blob/master/src/main/resources/d43z1.index)