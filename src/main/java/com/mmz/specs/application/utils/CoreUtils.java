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

import com.mmz.specs.application.ApplicationException;
import com.mmz.specs.application.core.ApplicationArgumentsConstants;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.service.ServerBackgroundService;
import com.mmz.specs.application.core.updater.Updater;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.server.ServerConfigurationWindow;
import com.mmz.specs.application.gui.server.ServerMainWindow;
import com.mmz.specs.application.managers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import static com.mmz.specs.application.core.ApplicationArgumentsConstants.CLIENT;
import static com.mmz.specs.application.core.ApplicationArgumentsConstants.SERVER;

public class CoreUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static ClientManager clientManager;


    public static void manageArguments(final String[] args) {
        if (args.length > 0) {
            final String firstArgument = args[0].toLowerCase();
            log.debug("Got arguments: " + Arrays.toString(args));
            switchMode(args, firstArgument);
        } else {
            final boolean updateAvailable = Updater.getInstance().isUpdateAvailable();
            log.info("Update available: {}", updateAvailable);
            if (updateAvailable) {
                loadClientWithUpdate();
            } else {
                log.debug("Found no arguments. Starting default mode: " + ModeManager.DEFAULT_MODE);
                ModeManager.setCurrentMode(ModeManager.DEFAULT_MODE);
                startClient();
            }
        }
    }

    private static void switchMode(String[] args, String firstArgument) {
        switch (firstArgument) {
            case CLIENT: {
                initClient();
                break;
            }
            case SERVER: {
                final boolean updateAvailable = Updater.getInstance().isUpdateAvailable();
                log.info("Update available: {}", updateAvailable);
                if (updateAvailable) {
                    try {
                        Updater.getInstance().startUpdate();
                    } catch (ApplicationException e) {
                        startServer();
                    }
                } else {
                    startServer();
                }
                break;
            }

            case ApplicationArgumentsConstants.UPDATE: {
                Updater.getInstance().notifyUser();
                Updater.getInstance().deleteInstaller();
                if (args.length > 1) {
                    final String arg = args[1];
                    if (arg.equalsIgnoreCase(SERVER)) {
                        switchMode(null, SERVER);
                    }
                } else {
                    switchMode(null, CLIENT);
                }
                break;
            }
            default: {
                log.debug("Unknown argument: " + firstArgument);

                final boolean supportedFile = isSupportedFile(firstArgument);

                log.debug("Starting default mode: " + ModeManager.DEFAULT_MODE);
                initClient();
                if (supportedFile) {
                    clientManager.openFile(new File(firstArgument));
                }
                break;
            }
        }
    }

    private static boolean isSupportedFile(String firstArgument) {
        log.info("Checking if argument: {} is file", firstArgument);
        final File file = new File(firstArgument);
        if (file.exists() && file.isFile()) {
            final String fileExtension = CommonUtils.getFileExtension(file);
            if (SupportedExtensionsConstants.contains(SupportedExtensionsConstants.SUPPORTED_FILE_EXTENSIONS,
                    fileExtension)) {
                return fileExtension.equalsIgnoreCase(SupportedExtensionsConstants.EXPORT_TREE_EXTENSION);
            }
        }
        return false;
    }

    private static void initClient() {
        final boolean updateAvailable = Updater.getInstance().isUpdateAvailable();
        log.info("Update available: {}", updateAvailable);
        if (updateAvailable) {
            loadClientWithUpdate();
        } else {
            startClient();
        }
    }

    private static void loadClientWithUpdate() {
        final boolean autoUpdateEnabled = ClientSettingsManager.getInstance().isAutoUpdateEnabled();
        if (autoUpdateEnabled) {
            try {
                Updater.getInstance().startUpdate();
            } catch (ApplicationException e) {
                startClient();
            }
        } else {
            ClientManager clientManager = ClientManager.getInstance();
            final ClientMainWindow clientMainWindow = clientManager.getClientMainWindow();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(clientMainWindow,
                    "Доступно новое обновление, но вы отключили авто-обновление\n" +
                            "приложения, что крайне не рекомендуется!\n" +
                            "Рекомендуется открыть настройки приложения и включить\n" +
                            "авто-обновление приложения!\n",
                    "Обновление приложения " + ApplicationConstants.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE));
        }
    }

    private static void startServer() {
        log.debug("Argument is for " + ModeManager.MODE.SERVER);

        loadServerSettings();

        ServerBackgroundService service = ServerBackgroundService.getInstance();

        ServerMainWindow serverMainWindow = new ServerMainWindow();
        serverMainWindow.setVisible(true);
    }

    private static void startClient() {
        log.debug("Argument is for " + ModeManager.MODE.CLIENT);

        clientManager = ClientManager.getInstance();
    }

    private static void loadServerSettings() {
        log.info("Loading server settings");
        ServerSettingsManager settingsManager = ServerSettingsManager.getInstance();
        try {
            settingsManager.setServerSettings(CommonSettingsManager.getServerSettingsFilePath());
            settingsManager.loadSettingsFile();
        } catch (Exception e) {
            log.warn("Could not set new server settings", e);
            ServerConfigurationWindow serverConfigurationWindow = new ServerConfigurationWindow();
            serverConfigurationWindow.setLocation(FrameUtils.getFrameOnCenter(null, serverConfigurationWindow));

            serverConfigurationWindow.setVisible(true);
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

    public static String getApplicationVersionString() {
        Properties properties = new Properties();
        try {
            properties.load(ClientMainWindow.class.getResourceAsStream("/application.properties"));
            String name = properties.getProperty("application.name");
            String version = properties.getProperty("application.version");
            String build = properties.getProperty("application.build");

            if (version != null && build != null) {
                return name + " v." + version + "." + build;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.warn("Could not load application version info", e);
            return null;
        }
    }


    public static void localizeUI() {
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        Locale locale = new Locale("ru");
        JOptionPane.setDefaultLocale(locale);

    }

    public static void drawUI() {
        UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("ProgressBar.selectionBackground", new ColorUIResource(Color.BLACK));
    }
}
