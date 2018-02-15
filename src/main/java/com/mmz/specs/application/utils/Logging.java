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

import com.mmz.specs.application.core.ApplicationArgumentsConstants;
import com.mmz.specs.application.core.ApplicationConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.mmz.specs.application.core.ApplicationConstants.LOG_PREFIX_CLIENT;
import static com.mmz.specs.application.core.ApplicationConstants.LOG_PREFIX_SERVER;

/**
 * Created by Eugene Zrazhevsky on 30.10.2016.
 */
public class Logging {
    private static final File LOG_FOLDER = new File(ApplicationConstants.LOG_FOLDER);

    public Logging(String[] args) {
        checkFolders();
        startLogging(manageArguments(args));
    }

    private String manageArguments(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case ApplicationArgumentsConstants.SERVER:
                    return LOG_PREFIX_SERVER;
                case ApplicationArgumentsConstants.CLIENT:
                    return LOG_PREFIX_CLIENT;
                    default:
                        return LOG_PREFIX_CLIENT;
            }
        } else return LOG_PREFIX_CLIENT;
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
        fixLoggingFolder(LOG_FOLDER);
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
    private void startLogging(String prefix) {
        System.setProperty(ApplicationConstants.APP_LOG_PROPERTY_KEY, ApplicationConstants.LOG_FOLDER + prefix );
        System.out.println("Logging starts at: " + LOG_FOLDER);
        Logger log = LogManager.getLogger(getCurrentClassName());
        log.trace("Logging started at: " + LOG_FOLDER);
        log.debug("Logging started at: " + LOG_FOLDER);
        log.info("Logging started at: " + LOG_FOLDER);
        log.warn("Logging started at: " + LOG_FOLDER);
        log.error("Logging started at: " + LOG_FOLDER);
        log.fatal("Logging started at: " + LOG_FOLDER);
    }

}
