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
import com.mmz.specs.application.core.server.service.ServerBackgroundService;
import com.mmz.specs.application.gui.server.ServerConfigurationWindow;
import com.mmz.specs.application.gui.server.ServerMainWindow;
import com.mmz.specs.application.managers.ClientManager;
import com.mmz.specs.application.managers.CommonSettingsManager;
import com.mmz.specs.application.managers.ModeManager;
import com.mmz.specs.application.managers.ServerSettingsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;

public class CoreUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static void manageArguments(final String[] args) {
        if (args.length > 0) {
            final String firstArgument = args[0].toLowerCase();
            log.debug("Got arguments: " + Arrays.toString(args));
            switch (firstArgument) {
                case ApplicationArgumentsConstants.CLIENT:
                    log.debug("Argument is for " + ModeManager.MODE.CLIENT);

                    ClientManager clientManager = ClientManager.getInstance();
                    break;
                case ApplicationArgumentsConstants.SERVER:
                    log.debug("Argument is for " + ModeManager.MODE.SERVER);

                    loadServerSettings();

                    ServerBackgroundService service = ServerBackgroundService.getInstance();

                    ServerMainWindow serverMainWindow = new ServerMainWindow();
                    serverMainWindow.setVisible(true);

                    break;
                default:
                    log.debug("Unknown argument: " + firstArgument);
                    log.debug("Starting default mode: " + ModeManager.DEFAULT_MODE);
                    break;
            }
        } else {
            log.debug("Found no arguments. Starting default mode: " + ModeManager.DEFAULT_MODE);
            ModeManager.setCurrentMode(ModeManager.DEFAULT_MODE);
        }
    }

    private static void loadServerSettings() {
        log.info("Loading server settings");
        ServerSettingsManager settingsManager = ServerSettingsManager.getInstance();
        try {
            settingsManager.setServerSettings(CommonSettingsManager.getServerSettingsFilePath());
            try {
                settingsManager.loadSettingsFile();
            } catch (IOException e) {
                log.warn("Could not load settings file at: " + CommonSettingsManager.getServerSettingsFilePath(), e);
            }
        } catch (Exception e) {
            log.warn("Could not set new server settings", e);
            ServerConfigurationWindow serverConfigurationWindow = new ServerConfigurationWindow();
            serverConfigurationWindow.setLocation(FrameUtils.getFrameOnCenter(null, serverConfigurationWindow));

            serverConfigurationWindow.setVisible(true);
            //loadServerSettings();//TODO check this properly
        }

    }

    /**
     * Enables LookAndFeel for current OS.
     *
     * @see javax.swing.UIManager.LookAndFeelInfo
     */
    public static void enableLookAndFeel() {
        try {
            log.debug("Trying to enable LookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            log.info("Successfully enabled LookAndFeel");
        } catch (Exception e) {
            log.warn("Unable to enable LookAndFeel", e);
        }
    }

    public static void localizeFileChooser() { //add some more or choose system fileChooser
        log.debug("Localizing FileChooser");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.lookInLabelText", "Смотреть в");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла");

        UIManager.put("FileChooser.saveButtonText", "Сохранить");
        UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.openButtonToolTipText", "Открыть");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена");

        UIManager.put("FileChooser.lookInLabelText", "Папка");
        UIManager.put("FileChooser.saveInLabelText", "Папка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов");

        UIManager.put("FileChooser.upFolderToolTipText", "На один уровень вверх");
        UIManager.put("FileChooser.newFolderToolTipText", "Создание новой папки");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблица");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
        UIManager.put("FileChooser.fileDateHeaderText", "Изменен");
        UIManager.put("FileChooser.fileAttrHeaderText", "Атрибуты");

        UIManager.put("FileChooser.acceptAllFileFilterText", "Все файлы");
    }


}
