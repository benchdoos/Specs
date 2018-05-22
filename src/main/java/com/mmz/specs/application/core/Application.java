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

import com.mmz.specs.application.utils.CoreUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Application {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public void initApplication(final String[] args) {
        try {
            CoreUtils.enableLookAndFeel();
            CoreUtils.localizeFileChooser();
            CoreUtils.manageArguments(args);
        } catch (Throwable throwable) {
            JOptionPane.showMessageDialog(null,
                    "Фатальная ошибка приложения.\n" + throwable.getLocalizedMessage(), "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            log.fatal("Got exception in application: " + throwable.getLocalizedMessage(), throwable);
            log.fatal("Exiting application with exception: " + throwable.getLocalizedMessage());
            System.exit(-1); //fixme this should not exit like this...
        }
    }
}