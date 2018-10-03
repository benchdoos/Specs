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

import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.mmz.specs.application.core.ApplicationConstants.RECENT_ITEMS_LIST_FILE_PATH;

public class RecentItems {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static final int DEFAULT_MAX_VALUE = 10;
    private static final File FILE = new File(RECENT_ITEMS_LIST_FILE_PATH);
    private static final String COMMENT = "Specs recent items list file";
    private static final String PREFIX = "file.";
    private static final String MAX_VALUE_KEY = "max.value";


    private static volatile RecentItems instance;
    private static Properties properties = new Properties();

    private RecentItems() {
        fixFile();
        loadProperties();
    }

    public static RecentItems getInstance() {
        RecentItems localInstance = instance;
        if (localInstance == null) {
            synchronized (RecentItems.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new RecentItems();
                }
            }
        }
        return localInstance;
    }

    private void fixFile() {
        if (!FILE.exists()) {
            properties.setProperty(MAX_VALUE_KEY, DEFAULT_MAX_VALUE + "");
            saveProperties();
        }
    }

    public List<String> getItems() {
        final int maxValue = getMaxValue();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < maxValue; i++) {
            try {
                final String property = properties.getProperty(PREFIX + i);
                if (property != null) {
                    list.add(property);
                }
            } catch (Exception ignore) {
                /*NOP*/
            }
        }
        return list;
    }

    public int getMaxValue() {
        loadProperties();
        final String property = properties.getProperty(MAX_VALUE_KEY);
        if (property != null) {
            try {
                return Integer.valueOf(property);
            } catch (NumberFormatException e) {
                return DEFAULT_MAX_VALUE;
            }
        } else {
            return DEFAULT_MAX_VALUE;
        }
    }

    public void setMaxValue(int value) {
        properties.setProperty(MAX_VALUE_KEY, value + "");
        saveProperties();
    }

    private void loadProperties() {
        try (final FileInputStream fileInputStream = new FileInputStream(FILE)) {
            properties.loadFromXML(fileInputStream);
        } catch (Exception e) {
            log.warn("Could not load recent items", e);
        }
    }

    public void put(String path) {
        Properties prop = new Properties();
        prop.setProperty(MAX_VALUE_KEY, getMaxValue() + "");
        prop.setProperty(PREFIX + 0, path);

        removeDuplicate(path);

        final List<String> items = getItems();
        for (int i = 0; i < items.size(); i++) {
            try {
                String value = items.get(i);
                if (value != null) {
                    prop.setProperty(PREFIX + (i + 1), value);
                }
            } catch (NullPointerException ignore) {
                /*NOP*/
            }
        }
        properties = prop;
        saveProperties();

    }

    public void put(File file) {
        put(file.getAbsolutePath());
    }

    public void removeAll() {
        Properties properties = new Properties();
        properties.setProperty(MAX_VALUE_KEY, getMaxValue() + "");
        RecentItems.properties = properties;
        saveProperties();
    }

    private void removeDuplicate(String path) {
        final Set<String> strings = properties.stringPropertyNames();
        for (String key : strings) {
            if (properties.getProperty(key).equalsIgnoreCase(path)) {
                properties.remove(key);
            }
        }
        saveProperties();
    }

    private void saveProperties() {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(FILE)) {
            properties.storeToXML(fileOutputStream, COMMENT);
        } catch (Exception e) {
            log.warn("Could not save recent items", e);
        }
    }
}
