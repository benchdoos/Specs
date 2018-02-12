/*
 * Copyright 2018 Eugeny Zrazhevsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mmz.specs.application.utils;

import com.mmz.specs.application.core.ApplicationConstants;

import java.io.File;

/**
 * Created by Eugene Zrazhevsky on 30.10.2016.
 */
public class Logging {
    private static final File LOG_FOLDER = new File(ApplicationConstants.getLogFolder());

    public Logging() {
        checkFolders();
        startLogging();
    }

    /**
     * Returns the name of called class. Made for usage in static methods.
     * Created to minimize hardcoded code.
     *
     * @return a name of called class.
     */
    public static String getCurrentClassName() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            return e.getStackTrace()[1].getClassName();
        }
    }

    /**
     * Checks folders for Log4j, creates them, if needed.
     */
    private void checkFolders() {

        File infoLogFolder = new File(LOG_FOLDER + File.separator + "INFO");
        File warnLogFolder = new File(LOG_FOLDER + File.separator + "WARN");
        File debugLogFolder = new File(LOG_FOLDER + File.separator + "DEBUG");

        fixLoggingFolder(LOG_FOLDER);
        fixLoggingFolder(infoLogFolder);
        fixLoggingFolder(warnLogFolder);
        fixLoggingFolder(debugLogFolder);

    }

    private void fixLoggingFolder(File infoLogFolder) {
        if (!infoLogFolder.exists()) {
            boolean success = infoLogFolder.mkdirs();
            if (!success) {
                System.err.println("Could not create file: " + infoLogFolder.getAbsolutePath());
            }
        }
    }

    /**
     * Creates a property for Log4j
     * Warning! Run this before any log implementation.
     */
    private void startLogging() {
        System.setProperty(ApplicationConstants.getAppLogPropertyKey(), ApplicationConstants.getLogFolder());
        System.out.println("Logging starts at: " + LOG_FOLDER);
    }

}
