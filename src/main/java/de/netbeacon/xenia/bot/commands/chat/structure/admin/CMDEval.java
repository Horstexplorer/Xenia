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

package de.netbeacon.xenia.bot.commands.chat.structure.admin;

import de.netbeacon.xenia.bot.commands.chat.objects.misc.cmdargs.CmdArgs;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.cooldown.CommandCooldown;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.event.CommandEvent;
import de.netbeacon.xenia.bot.commands.chat.objects.misc.translations.TranslationPackage;
import de.netbeacon.xenia.bot.core.XeniaCore;
import de.netbeacon.xenia.bot.utils.embedfactory.EmbedBuilderFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.CodeBlock;
import static de.netbeacon.xenia.bot.utils.statics.pattern.StaticPattern.JavaClass;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class CMDEval extends AdminCommand{

	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private final Logger logger = LoggerFactory.getLogger(CMDEval.class);

	public CMDEval(){
		super("eval", new CommandCooldown(CommandCooldown.Type.User, 1000), null, null, null, null);
	}

	@Override
	public void onExecution(CmdArgs args, CommandEvent commandEvent, TranslationPackage translationPackage){
		// parse java things
		String msg = commandEvent.getEvent().getMessage().getContentRaw();
		Matcher matcher = CodeBlock.matcher(msg);
		if(matcher.find()){
			try{
				String code = matcher.group(3).trim();
				// send eval running embed
				try{
					// simple eval or java code to run
					Matcher classMatcher = JavaClass.matcher(code);
					if(classMatcher.find()){
						// eval as java
						Message message = commandEvent.getEvent().getChannel().sendMessageEmbeds(getRequestEmbed("Java", code)).complete();
						if(message == null){
							throw new Exception("Error Sending Eval Message");
						}
						evalJavaCode(commandEvent, message, code);
					}
					else{
						// eval as js
						String engine = matcher.group(2).trim();
						Message message = commandEvent.getEvent().getChannel().sendMessageEmbeds(getRequestEmbed(((!engine.isBlank()) ? engine : "Default"), code)).complete();
						if(message == null){
							throw new Exception("Error Sending Eval Message");
						}
						evalJavaScriptCode(commandEvent, message, engine, code);
					}
				}
				catch(Exception e){
					commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.RED, "Failed", null, -1, code, e.getMessage())).queue(s -> {}, ex -> {});
				}
			}
			catch(Exception e){
				commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.RED, "Failed", null, -1, "Unknown", e.getMessage())).queue(s -> {}, ex -> {});
			}
		}
	}

	/**
	 * Evaluates JavaScript code
	 *
	 * @param commandEvent CommandEvent
	 * @param message      the eval message
	 * @param engine       code engine
	 * @param code         code
	 */
	public void evalJavaScriptCode(CommandEvent commandEvent, Message message, String engine, String code){
		if(engine == null || engine.isBlank()){
			engine = "groovy";
		}
		logger.info("! EVAL RUNNING ! Engine: \"" + engine + "\"");
		long start = System.currentTimeMillis();
		try{
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engine);
			scriptEngine.put("commandEvent", commandEvent); // this might be dangerous
			Object o = scriptEngine.eval(code);
			message.editMessage(getResultEmbed(Color.GREEN, "Success", scriptEngine.getFactory().getEngineName(), System.currentTimeMillis() - start, code, o.toString())).queue(s -> {}, e -> {
				commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.GREEN, "Success", scriptEngine.getFactory().getEngineName(), System.currentTimeMillis() - start, code, o.toString())).queue(s -> {}, ex -> {});
			});
		}
		catch(Exception e){
			String finalEngine = engine;
			message.editMessage(getResultEmbed(Color.RED, "Failed", "\"" + finalEngine + "\"", System.currentTimeMillis() - start, code, e.getMessage())).queue(s -> {}, ex -> {
				commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.RED, "Failed", "\"" + finalEngine + "\"", System.currentTimeMillis() - start, code, e.getMessage())).queue(s -> {}, exe -> {});
			});
		}
		logger.info("! EVAL FINISHED ! Engine: \"" + engine + "\"");
	}

	/**
	 * Evaluates Java code
	 * <p>
	 * This requires an EvalHelper.jar {@link "https://github.com/Horstexplorer/EvalHelper"} to be located in the same directory as the bot is executed from
	 *
	 * @param commandEvent CommandEvent
	 * @param message      the eval message
	 * @param code         code
	 */
	public void evalJavaCode(CommandEvent commandEvent, Message message, String code){
		try{
			logger.info("! EVAL RUNNING ! Engine: Java");
			String filename = String.valueOf(Math.abs(new Random().nextLong()));
			// write to file
			File path = new File(XeniaCore.getInstance().getConfig().getString("evalTmpPath"));
			if(!path.exists()){
				path.mkdirs();
			}
			Files.write(Path.of(XeniaCore.getInstance().getConfig().getString("evalTmpPath") + filename), code.getBytes(), CREATE_NEW);
			// start process
			long start = System.currentTimeMillis();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("java", "-jar", "EvalHelper.jar", XeniaCore.getInstance().getConfig().getString("evalTmpPath") + filename);
			Process process = processBuilder.start();
			// start process timeout
			ScheduledFuture<?> timeout = executorService.schedule(process::destroyForcibly, 15, TimeUnit.SECONDS);
			// read output
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while((line = bufferedReader.readLine()) != null){
				if(!line.contains("[EVAL][INFO]")){
					stringBuilder.append(line).append("\n");
				}
			}
			process.waitFor();
			timeout.cancel(true);
			if(process.exitValue() != 0){
				message.editMessage(getResultEmbed(Color.RED, "Failed", "Java", System.currentTimeMillis() - start, code, stringBuilder.toString())).queue(s -> {}, ex -> {
					commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.RED, "Failed", "Java", System.currentTimeMillis() - start, code, stringBuilder.toString())).queue();
				});
			}
			else{
				message.editMessage(getResultEmbed(Color.GREEN, "Success", "Java", System.currentTimeMillis() - start, code, stringBuilder.toString())).queue(s -> {}, ex -> {
					commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.GREEN, "Success", "Java", System.currentTimeMillis() - start, code, stringBuilder.toString())).queue();
				});
			}
		}
		catch(Exception e){
			message.editMessage(getResultEmbed(Color.RED, "Failed", "Java", -1, code, e.getMessage())).queue(s -> {}, ex -> {
				commandEvent.getEvent().getChannel().sendMessageEmbeds(getResultEmbed(Color.RED, "Failed", "Java", -1, code, e.getMessage())).queue();
			});
		}
		finally{
			logger.info("! EVAL FINISHED ! Engine: Java");
		}
	}

	private MessageEmbed getRequestEmbed(String engine, String code){
		String status = "Running";
		if(engine == null || engine.isBlank()){
			engine = "Unknown";
		}
		if(code == null || code.isBlank()){
			code = "Unknown";
		}
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Eval")
			.setColor(Color.ORANGE)
			.addField("Engine:", engine, true)
			.addField("Status:", status, true)
			.addField("Timeout:", 15000 + "ms", true);
		if(code.length() >= 1900){
			int i = code.length() - 1900;
			code = code.substring(0, 1900) + "....\nCode shortened by " + i + " chars.";
		}
		embedBuilder.addField("Code:", "```" + code + "```", false);
		return embedBuilder.build();
	}

	private MessageEmbed getResultEmbed(Color color, String status, String engine, long duration, String code, String result){
		if(status == null || status.isBlank()){
			status = "Unknown";
		}
		if(result == null || result.isBlank()){
			result = "Unknown";
		}
		if(engine == null || engine.isBlank()){
			engine = "Unknown";
		}
		EmbedBuilder embedBuilder = EmbedBuilderFactory.getDefaultEmbed("Eval")
			.setColor(color)
			.addField("Engine:", engine, true)
			.addField("Status:", status, true)
			.addField("Duration:", duration + "ms", true);
		if(code.length() >= 1900){
			int i = code.length() - 1900;
			code = code.substring(0, 1900) + "....\nCode shortened by " + i + " chars.";
		}
		embedBuilder.addField("Code:", "```" + code + "```", false);
		if(result.length() >= 1900){
			int i = result.length() - 1900;
			result = result.substring(0, 1900) + "....\nResult shortened by " + i + " chars.";
		}
		embedBuilder.addField("Result:", result, false);
		return embedBuilder.build();
	}

}
