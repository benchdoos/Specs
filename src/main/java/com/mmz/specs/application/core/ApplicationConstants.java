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

package com.mmz.specs.application.core;

import java.io.File;

public class ApplicationConstants {
    public static final String INTERNAL_FULL_NAME = "com.mmz.specs";
    public static final String APPLICATION_NAME = "MMZ - Спецификации";
    public static final String APPLICATION_NAME_POSTFIX_SERVER = " - Сервер";
    public static final String USER_HOME_LOCATION = System.getProperty("user.home");
    public static final String LOG_FOLDER = System
            .getProperty("java.io.tmpdir") + File.separator + INTERNAL_FULL_NAME + File.separator + "logs";
    public static final String APP_LOG_PROPERTY_KEY = "com.mmz.specs:log.folder";

    public static final String SETTINGS_FILE_PATH = new File(LOG_FOLDER).getParent() + File.separator + "settings.xml";
    public static final String DEFAULT_FILE_ENCODING = "UTF-8";

    public static final String LOG_PREFIX_SERVER = File.separator + "server_";
    public static final String LOG_PREFIX_CLIENT = File.separator + "client_";
}
