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

package com.mmz.specs.io;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.io.utils.ExportSPTUtils;
import com.mmz.specs.model.DetailEntity;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.mmz.specs.application.core.ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION;
import static com.mmz.specs.io.IOConstants.DEFAULT_TREE_TYPE;
import static com.mmz.specs.io.IOConstants.TYPE;

public class SPTreeIOManager implements IOManager {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private final Session session;
    private String datePattern = "dd.MM.yyyy HH.mm";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
    private ProgressManager progressManager;


    public SPTreeIOManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
        this.session = ServerDBConnectionPool.getInstance().getSession();
    }

    @Override
    public void exportData(File file) throws IOException, ZipException {
        log.info("Starting export of tree (SPT) to file: {}", file);
        checkCreate(file);

        final File folder = createFolder(file);

        final JSONObject treeJSON;
        final File jsonFile;
        if (!Thread.currentThread().isInterrupted()) {
            treeJSON = new ExportSPTUtils(session, progressManager).createTreeJSON();
            jsonFile = exportTree(folder, treeJSON);

            progressManager.setTotalProgress(1);

            if (!Thread.currentThread().isInterrupted()) {
                File imagesFolder = downloadImages(folder, treeJSON);
                progressManager.setTotalProgress(2);

                if (!Thread.currentThread().isInterrupted()) {
                    createSPTFile(file, jsonFile, imagesFolder);
                    progressManager.setTotalProgress(3);
                }

                if (!Thread.currentThread().isInterrupted()) {
                    removeTrash(folder);
                    progressManager.setText("Экспорт успено проведён");
                    progressManager.setTotalProgress(4);
                    progressManager.setCurrentProgress(100);
                }
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            removeTrash(folder);
            progressManager.reset();
        }
    }

    private void removeTrash(File folder) {
        log.debug("Deleting temp folder: {}", folder);
        progressManager.setText("Удаляем временные файлы");
        progressManager.setCurrentProgress(0);
        try {
            FileUtils.deleteDirectory(folder);
            progressManager.setCurrentProgress(100);
        } catch (IOException e) {
            log.warn("Could not delete folder: {}", folder, e);
        }
    }

    private File exportTree(File folder, JSONObject treeJSON) throws IOException {
        progressManager.setText("Экспорт дерева");
        final File jsonFile = new File(folder + File.separator + "tree.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            treeJSON.write(writer);
        }
        return jsonFile;
    }

    private File createFolder(File file) {
        final File folder = new File(file.getParentFile() + File.separator + dateFormatter.format(Calendar.getInstance().getTime()).replace(" ", "_"));
        folder.mkdirs();
        return folder;
    }

    private void checkCreate(File file) throws IOException {
        log.debug("Checking ability to create file: {}", file);
        progressManager.setText("Проверка доступности файла");

        new File(APPLICATION_EXPORT_FOLDER_LOCATION).mkdirs();

        if (!file.createNewFile()) {
            log.warn("Can not create file: {}", file);
            throw new IOException("Can not write data to file: " + file);
        } else {
            log.debug("Testing file deleted: " + file.delete());
        }
    }

    private File downloadImages(File folder, JSONObject treeJSON) {
        File result = new File(folder, "images");
        log.debug("Starting downloading images to folder: {}", folder);
        progressManager.setCurrentProgress(0);
        progressManager.setText("Определяем список деталей");
        progressManager.setCurrentIndeterminate(true);

        final String jsonType = treeJSON.getString(TYPE);
        if (!jsonType.equalsIgnoreCase(DEFAULT_TREE_TYPE)) {
            throw new IllegalArgumentException("JSON File type does not much " + DEFAULT_TREE_TYPE + ", now it is: " + jsonType);
        }
        log.debug("Starting finding all details in JSON");
        ArrayList<DetailEntity> details = ExportSPTUtils.listDetailsFromJSON(treeJSON);

        progressManager.setCurrentIndeterminate(false);
        progressManager.setText("Загружаем изображения с FTP");

        if (details != null) {
            log.debug("Got details: {}", details.size());
            log.debug("Starting downloading {} details images to {}", details.size(), result);
            result.mkdirs();

            final ExportSPTUtils exportSPTUtils = new ExportSPTUtils(session, progressManager);
            exportSPTUtils.downloadFromFTP(result, details);

            return result;
        }
        return null;

    }

    private void createSPTFile(File file, File jsonFile, File imagesFolder) throws ZipException {
        ZipFile zipFile = new ZipFile(file);

        ZipParameters parameters = new ZipParameters();
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        parameters.setPassword(ApplicationConstants.INTERNAL_FULL_NAME);

        zipFile.addFile(jsonFile, parameters);
        zipFile.addFolder(imagesFolder, parameters);
    }


    @Override
    public Object importData(File file) {
        return null;
    }
}
