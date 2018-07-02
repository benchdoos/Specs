/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.application.managers;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerConstants;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CommonSettingsManager {

    public static String getServerSettingsFilePath() throws IOException {
        String result;
        Properties properties = new Properties();
        properties.loadFromXML(new FileInputStream(ApplicationConstants.SETTINGS_FILE_PATH));
        result = properties.getProperty(ServerConstants.SERVER_SETTINGS_FILE_LOCATION_KEY);
        return result;
    }

    public static void setServerSettingsFilePath(String path) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(ServerConstants.SERVER_SETTINGS_FILE_LOCATION_KEY, path);
        properties.storeToXML(new FileOutputStream(ApplicationConstants.SETTINGS_FILE_PATH),
                ApplicationConstants.INTERNAL_FULL_NAME + " settings file locations",
                ApplicationConstants.DEFAULT_FILE_ENCODING);
    }
}
