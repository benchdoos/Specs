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

package com.mmz.specs.application.core.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.common.UpdaterWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

public class Updater {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static final String GITHUB_URL = "https://api.github.com/repos/benchdoos/Specs/releases/latest";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static Updater ourInstance = new Updater();
    private HttpsURLConnection connection = null;
    private ApplicationVersion serverVersion;

    private Updater() {
        log.debug("Starting UpdaterWindow instance");
        try {
            connection = getConnection();
        } catch (Throwable t) {
            log.warn("Could not create connection", t);
        }
    }

    public static Updater getInstance() {
        return ourInstance;
    }

    public boolean isUpdateNotAvailable() {
        try {
            getServerApplicationVersion();
            if (serverVersion != null) {
                return !compareVersionsAndUpdate();
            }
        } catch (Throwable t) {
            log.warn("Could not check update", t);
            return true;
        }
        return true;
    }

    public void startUpdate() {

        UpdaterWindow window = new UpdaterWindow(serverVersion);
        SwingUtilities.invokeLater(() -> {
            log.info("Showing message to user about update");
            window.setLocation(FrameUtils.getFrameOnCenter(null, window));
            window.setVisible(true);
        });

        log.info("Starting update");
        try {
            String currentPath = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            File downloadedFile = download();
            if (downloadedFile != null) {
                log.info("New version of file successfully downloaded: {}", downloadedFile.getAbsolutePath());
                log.info("Starting updating to new version.");

               /* String command = "java -jar \"" + downloadedFile.getAbsolutePath() + "\" " + ApplicationArgumentsConstants.UPDATE
                        + " " + currentPath;*/
                String command = downloadedFile.getAbsolutePath() + " /verysilent";
                log.info("Starting new version with command: {}", command);
                window.dispose();
                Runtime.getRuntime().exec(command);
                log.info("Closing current application");
            } else {
                log.warn("Could not download new version, downloaded file: null");
            }

        } catch (Throwable e) {
            log.warn("Could not download file", e);
        }
    }

    private String argsArrayToString(String[] arguments) {
        StringBuilder builder = new StringBuilder();
        if (arguments != null) {
            for (String s : arguments) {
                builder.append("\"").append(s).append("\"").append(" ");
            }
        }
        return builder.toString();
    }

    private File download() {
        File installerFile = new File(ApplicationConstants.APPLICATION_SETTINGS_FOLDER_LOCATION
                + File.separator + ApplicationConstants.APPLICATION_FINAL_NAME);
        if (installerFile.exists()) {
            boolean delete = installerFile.delete();
        }
        try {
            log.info("Starting downloading file from {} to {}", serverVersion.getDownloadUrl(), installerFile.getAbsolutePath());
            FileUtils.copyURLToFile(new URL(serverVersion.getDownloadUrl()), installerFile, 5000, 5000);
            installerFile = new File(ApplicationConstants.APPLICATION_SETTINGS_FOLDER_LOCATION
                    + File.separator + ApplicationConstants.APPLICATION_FINAL_NAME);
        } catch (IOException e) {
            log.warn("Could not download file from {} to {}", serverVersion.getDownloadUrl(), installerFile, e);
        }
        return installerFile;
    }

    private void getServerApplicationVersion() throws IOException { //replace by apache HttpComponents?
        log.debug("Getting current application version");
        String input;
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), DEFAULT_ENCODING));

        input = bufferedReader.readLine();

        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(input).getAsJsonObject();

        serverVersion = new ApplicationVersion();
        fromJsonToApplicationVersion(root);
    }

    private void fromJsonToApplicationVersion(JsonObject root) {
        log.debug("Parsing json to application version");

        final String version = "tag_name";
        final String browser_download_url = "browser_download_url";
        final String assets = "assets";
        final String name = "name";
        final String size = "size";
        final String info = "body";

        serverVersion.setVersion(root.getAsJsonObject().get(version).getAsString());
        serverVersion.setUpdateInfo(root.getAsJsonObject().get(info).getAsString());
        serverVersion.setUpdateTitle(root.getAsJsonObject().get(name).getAsString());

        JsonArray asserts = root.getAsJsonArray(assets);
        for (JsonElement assert_ : asserts) {
            JsonObject userObject = assert_.getAsJsonObject();
            serverVersion.setDownloadUrl(userObject.get(browser_download_url).getAsString());
            serverVersion.setSize(userObject.get(size).getAsLong());
        }
        log.info("Server application version is: {}", serverVersion.toString().replaceAll("\n", " "));
    }

    private void createConnection() {
        if (!connection.getDoOutput()) {
            connection.setDoOutput(true);
        }
        if (!connection.getDoInput()) {
            connection.setDoInput(true);
        }
    }

    private HttpsURLConnection getConnection() throws IOException {
        URL url = new URL(GITHUB_URL);
        log.debug("Creating connection to " + url);

        connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
        createConnection();
        return connection;
    }

    private boolean compareVersionsAndUpdate() {

        String currentVersion = getCurrentVersion();
        log.debug("Current version: {}, server version: {}", currentVersion, serverVersion.getVersion());
        if (Internal.versionCompare(currentVersion, serverVersion.getVersion()) < 0) {
            log.info("Got new version for application: {}, current is: {}.", serverVersion.getVersion(), currentVersion);
            return true;
        }
        return false;
    }

    private String getCurrentVersion() {
        Properties properties = new Properties();
        try {
            properties.load(ClientMainWindow.class.getResourceAsStream("/application.properties"));
            String version = properties.getProperty("application.version");
            String build = properties.getProperty("application.build");
            if (version != null && build != null) {
                String s = build.split(" ")[0];
                return (version + "." + s);
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.warn("Could not load application version info", e);
            return null;
        }
    }

    public void copyMyself(String path) {
        File currentFile = null;
        File destFile = new File(path);
        try {
            currentFile = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            FileUtils.copyFile(currentFile, destFile);
            log.info("File {} successfully copied to {}", currentFile, destFile);
        } catch (URISyntaxException | IOException e) {
            log.warn("Could not copy file {} to {}", currentFile, destFile, e);
        }

    }

    public void runNewVersion(String arg) {
        log.debug("Running new version. File: {}", arg);
        String command = "java -jar " + arg;
        log.debug("Launching command: " + command);
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            log.warn("Could not run cmd command: " + command, e);
        }
    }

    private String[] cutFirstTwoArgs(String[] args) {
        if (args.length > 2) {
            return Arrays.copyOfRange(args, 2, args.length);
        } else return null;
    }

    public void deleteMyself() {
        try {
            File currentFile = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            currentFile.deleteOnExit();
        } catch (URISyntaxException e) {
            log.warn("Could not find myself", e);
        }

    }

    public void notifyUser() {
        JOptionPane.showMessageDialog(null, "Приложение успешно обновлено\n" +
                "Вы можете запустить его снова", "Приложение обновлено", JOptionPane.INFORMATION_MESSAGE);
    }
}
