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

import com.google.gson.*;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SupportedExtensionsConstants;
import com.mmz.specs.io.formats.HibernateProxyTypeAdapter;
import com.mmz.specs.io.serialization.deserializer.DetailEntityDeserializer;
import com.mmz.specs.io.serialization.deserializer.DetailTitleEntityDeserializer;
import com.mmz.specs.io.serialization.deserializer.MaterialEntityDeserializer;
import com.mmz.specs.io.serialization.serializer.DetailEntitySerializer;
import com.mmz.specs.io.serialization.serializer.DetailTitleEntitySerializer;
import com.mmz.specs.io.serialization.serializer.MaterialEntitySerializer;
import com.mmz.specs.io.utils.ExportSPTUtils;
import com.mmz.specs.io.utils.ImportSPTUtils;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.model.MaterialEntity;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.mmz.specs.application.core.ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION;

public class SPTreeIOManager implements IOManager {
    public static final String JSON_FILE_NAME = "tree.json";
    public static final String IMAGES_FOLDER_FILE_NAME = "images";
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session = null;
    private String datePattern = "dd.MM.yyyy HH.mm";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
    private ProgressManager progressManager;


    public SPTreeIOManager(Session session, ProgressManager progressManager) {
        progressManager.setTotalMaxValue(5);
        this.progressManager = progressManager;
        this.session = session;
    }

    public SPTreeIOManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
        this.session = null;
    }

    public static JsonObject loadJsonFromFile(File file) throws IOException {
        String content = FileUtils.readFileToString(file, ApplicationConstants.DEFAULT_FILE_ENCODING);
        JsonElement element = new JsonParser().parse(content);
        return element.getAsJsonObject();

    }

    public static Gson getDefaultGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
        builder.registerTypeAdapter(DetailEntity.class, new DetailEntitySerializer());
        builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntitySerializer());
        builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntitySerializer());
        builder.registerTypeAdapter(DetailEntity.class, new DetailEntityDeserializer());
        builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntityDeserializer());
        builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntityDeserializer());
        return builder.create();
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

    private File createFolder(File file) {
        final File folder = new File(file.getParentFile() + File.separator + dateFormatter.format(Calendar.getInstance().getTime()).replace(" ", "_"));
        folder.mkdirs();
        return folder;
    }

    @Override
    public void exportData(File file) throws IOException, ZipException {
        log.info("Starting export of tree (SPT) to file: {}", file);
        checkCreate(file);

        final File folder = createFolder(file);

        final JsonObject treeJSON;
        final File jsonFile;
        if (!Thread.currentThread().isInterrupted()) {
            final ExportSPTUtils exportSPTUtils = new ExportSPTUtils(session, progressManager);
            treeJSON = exportSPTUtils.createTreeJSON();
            jsonFile = exportTree(folder, treeJSON);

            progressManager.setTotalProgress(progressManager.getTotalProgress() + 1);

            if (!Thread.currentThread().isInterrupted()) {
                File imagesFolder = exportSPTUtils.downloadImages(folder, treeJSON);
                progressManager.setTotalProgress(progressManager.getTotalProgress() + 1);


                if (!Thread.currentThread().isInterrupted()) {
                    exportSPTUtils.optimizeImages(new File(folder + File.separator + IMAGES_FOLDER_FILE_NAME));
                    progressManager.setTotalProgress(progressManager.getTotalProgress() + 1);
                }

                if (!Thread.currentThread().isInterrupted()) {
                    exportSPTUtils.createSPTFile(file, jsonFile, imagesFolder);
                    progressManager.setTotalProgress(progressManager.getTotalProgress() + 1);
                }

                if (!Thread.currentThread().isInterrupted()) {
                    removeTrash(folder);
                    progressManager.setText("Экспорт успено проведён");
                    progressManager.setTotalProgress(progressManager.getTotalProgress() + 1);
                    progressManager.setCurrentProgress(100);
                }
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            removeTrash(folder);
            progressManager.reset();
        }
    }

    private File exportTree(File folder, JsonObject treeJSON) throws IOException {
        progressManager.setText("Экспорт дерева");
        final File jsonFile = new File(folder + File.separator + JSON_FILE_NAME);
        /*try (FileWriter writer = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(treeJSON, writer);
        }*/
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(jsonFile, true),
                        StandardCharsets.UTF_8))) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(treeJSON, writer);
        }
        return jsonFile;
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
}
