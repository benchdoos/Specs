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

import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SupportedExtensionsConstants;
import com.mmz.specs.io.utils.ExportSPTUtils;
import com.mmz.specs.io.utils.ImportSPTUtils;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.mmz.specs.application.core.ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION;

public class SPTreeIOManager implements IOManager {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    public static final String JSON_FILE_NAME = "tree.json";
    public static final String IMAGES_FOLDER_FILE_NAME = "images";


    private Session session = null;
    private String datePattern = "dd.MM.yyyy HH.mm";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
    private ProgressManager progressManager;


    public SPTreeIOManager(Session session, ProgressManager progressManager) {
        this.progressManager = progressManager;
        this.session = session;
    }

    public SPTreeIOManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
        this.session = null;
    }

    @Override
    public void exportData(File file) throws IOException, ZipException {
        log.info("Starting export of tree (SPT) to file: {}", file);
        checkCreate(file);

        final File folder = createFolder(file);

        final JSONObject treeJSON;
        final File jsonFile;
        if (!Thread.currentThread().isInterrupted()) {
            final ExportSPTUtils exportSPTUtils = new ExportSPTUtils(session, progressManager);
            treeJSON = exportSPTUtils.createTreeJSON();
            jsonFile = exportTree(folder, treeJSON);

            progressManager.setTotalProgress(1);

            if (!Thread.currentThread().isInterrupted()) {
                File imagesFolder = exportSPTUtils.downloadImages(folder, treeJSON);
                progressManager.setTotalProgress(2);

                if (!Thread.currentThread().isInterrupted()) {
                    exportSPTUtils.createSPTFile(file, jsonFile, imagesFolder);
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
        final File jsonFile = new File(folder + File.separator + JSON_FILE_NAME);
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
            log.warn("File exists: {}", file.exists());
            throw new IOException("Can not write data to file: " + file + ". File exists: " + file.exists());
        } else {
            log.debug("Testing file deleted: " + file.delete());
        }
    }


    @Override
    public Object importData(File sptFile) throws IOException {
        log.info("Starting import of sptFile: {}", sptFile);
        if (!isFileSPT(sptFile)) {
            throw new IOException("Can not open sptFile: " + sptFile);
        }


        final ImportSPTUtils importSPTUtils = new ImportSPTUtils(progressManager);
        try {
            final File folder = importSPTUtils.openSPTFile(sptFile);
            log.info("SPT sptFile {} extracted to {}", sptFile, folder);
            return folder;
        } catch (ZipException e) {
            log.warn("Could not extract spt sptFile: {}", sptFile, e);
        }
        return null;
    }

    private boolean isFileSPT(File file) {
        if (file != null) {
            if (file.exists()) {
                return CommonUtils.getFileExtension(file).equalsIgnoreCase(
                        SupportedExtensionsConstants.EXPORT_TREE_EXTENSION);
            }
        }
        return false;
    }
}
