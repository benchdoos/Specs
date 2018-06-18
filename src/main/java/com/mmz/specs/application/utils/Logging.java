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

package com.mmz.specs.application.utils;

import com.mmz.specs.application.core.ApplicationArgumentsConstants;
import com.mmz.specs.application.core.ApplicationConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Calendar;

import static com.mmz.specs.application.core.ApplicationConstants.LOG_PREFIX_CLIENT;
import static com.mmz.specs.application.core.ApplicationConstants.LOG_PREFIX_SERVER;

/**
 * Created by Eugene Zrazhevsky on 30.10.2016.
 */
public class Logging {
    private static final File LOG_FOLDER = new File(ApplicationConstants.LOG_FOLDER);

    public Logging(String[] args) {
        startLogging(manageArguments(args));
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
     * Creates a property for Log4j
     * Warning! Run this before any log implementation.
     */
    private void startLogging(String prefix) {
        System.setProperty(ApplicationConstants.APP_LOG_PROPERTY_KEY, ApplicationConstants.LOG_FOLDER + prefix);
        final String applicationVersionString = CoreUtils.getApplicationVersionString();

        System.out.println("Logging starting at: " + LOG_FOLDER + " (" + applicationVersionString + ")");

        Logger log = LogManager.getLogger(getCurrentClassName());
        log.info("Logging started on: " + Calendar.getInstance().getTime() + " at: " + LOG_FOLDER);
        log.info("Application: " + applicationVersionString);
    }

}
