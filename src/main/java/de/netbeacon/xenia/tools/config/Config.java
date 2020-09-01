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

package de.netbeacon.xenia.tools.config;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Reads json config files
 */
public class Config {

    private final JSONObject config;

    /**
     * Creates a new config from a given file
     *
     * @param file File containing the settings in json format
     * @throws IOException if file does not exist or is not readable
     */
    public Config(File file) throws IOException {
        if(!file.exists()){
            throw new IOException("Config File Not Found");
        }else{
            config = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        }
    }

    /**
     * Returns a string value for the given key
     *
     * @param key key
     * @return value
     */
    public String getString(String key){
        try{
            return config.getString(key);
        }catch (Exception e){
            return "";
        }
    }

    /**
     * Returns a long value for the given key
     *
     * @param key key
     * @return value
     */
    public long getLong(String key){
        try{
            return config.getLong(key);
        }catch (Exception e){
            return 0L;
        }
    }

    /**
     * Returns an int value for the given key
     *
     * @param key key
     * @return value
     */
    public int getInt(String key){
        try{
            return config.getInt(key);
        }catch (Exception e){
            return 0;
        }
    }

    /**
     * Returns a bool value for the given key
     *
     * @param key key
     * @return value
     */
    public boolean getBoolean(String key){
        try{
            return config.getBoolean(key);
        }catch (Exception e){
            return false;
        }
    }
}
