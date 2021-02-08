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

package de.netbeacon.d43z.one.gen;

import de.netbeacon.d43z.one.objects.base.Content;
import de.netbeacon.d43z.one.objects.base.ContentContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class OPENSUB {

    private static final Pattern NEWLINE = Pattern.compile("\n");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern XML_CHARS = Pattern.compile("[<>].*");
    private static final Pattern NOT_GOOD_CHARS = Pattern.compile("[^a-zA-Z0-9\\.\\,\\?\\!\\s\\'\\\"\\`]");

    public static void main(String...args) throws IOException {
        try{
            File inDir = new File("F:\\D31\\opensub\\OpenSubtitles\\raw\\en");
            var files = FileUtils.listFiles(inDir, new String[]{"xml"}, true);
            int max = files.size();
            int x = 0;
            long l = 0;
            for(File file : files){
               try{
                   byte[] bytes = Files.readAllBytes(file.toPath());
                   List<Content> contentList = new LinkedList<>();
                   StringBuilder linebuffer = new StringBuilder();
                   for(String line : NEWLINE.split(new String(bytes))){
                       if(line.isEmpty() || XML_CHARS.matcher(line).find()){
                           continue;
                       }
                       line = NOT_GOOD_CHARS.matcher(line).replaceAll(" ");
                       line = SPACES.matcher(line).replaceAll(" ").trim();
                       if(line.endsWith(",")){
                           linebuffer.append(" ").append(line);
                           continue;
                       }else if(linebuffer.length() > 0){
                          line = linebuffer +" "+line;
                          linebuffer = new StringBuilder();
                       }
                       contentList.add(new Content(line));
                       l++;
                   }
                   ContentContext contentContext = new ContentContext("OPENSUBTITLES-NLP.EU_"+x++, new HashSet<>(), contentList);
                   File out = new File("F:\\D31\\OUT\\opensubtitles\\"+contentContext.getUUID().toString()+".cc.json");
                   Files.write(out.toPath(), contentContext.asJSON().toString(1).getBytes());
                   if(x%10000 == 0){
                       System.out.println(x+"     "+l);
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
