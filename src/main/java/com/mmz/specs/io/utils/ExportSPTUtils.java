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

package com.mmz.specs.io.utils;

import com.google.common.collect.ComparisonChain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.deserializer.DetailEntityDeserializer;
import com.mmz.specs.deserializer.DetailTitleEntityDeserializer;
import com.mmz.specs.deserializer.MaterialEntityDeserializer;
import com.mmz.specs.io.IOConstants;
import com.mmz.specs.io.SPTreeIOManager;
import com.mmz.specs.io.formats.HibernateProxyTypeAdapter;
import com.mmz.specs.io.formats.SPTFileFormat;
import com.mmz.specs.model.*;
import com.mmz.specs.serializer.DetailEntitySerializer;
import com.mmz.specs.serializer.DetailTitleEntitySerializer;
import com.mmz.specs.serializer.MaterialEntitySerializer;
import com.mmz.specs.service.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static com.mmz.specs.application.utils.SupportedExtensionsConstants.FTP_IMAGE_FILE_EXTENSION;
import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.OPERATING_SYSTEM;
import static com.mmz.specs.io.IOConstants.*;
import static com.mmz.specs.io.formats.SPTFileFormat.*;

public class ExportSPTUtils {


    private static final int DEFAULT_MAX_FILE_LENGTH = 500 * 1000;
    private static final float DEFAULT_COMPRESSION_VALUE = 0.8f;


    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;
    private ProgressManager progressManager;

    public ExportSPTUtils(Session session, ProgressManager progressManager) {
        this.session = session;
        this.progressManager = progressManager;
    }

    public static ArrayList<DetailEntity> listDetailsFromJSON(JsonObject treeJSON) {
        log.debug("Starting searching details in JSON");
        final String jsonType = treeJSON.getAsJsonPrimitive(IOConstants.TYPE).getAsString();
        if (!jsonType.equalsIgnoreCase(SPTFileFormat.DEFAULT_TREE_TYPE)) {
            throw new IllegalArgumentException("JSON File type does not much " + DEFAULT_TREE_TYPE + ", now it is: " + jsonType);
        }

        ArrayList<DetailEntity> result = new ArrayList<>();

        JsonArray tree = treeJSON.getAsJsonArray(IOConstants.TREE);
        log.debug("Found tree size: {}", tree.size());

        for (int i = 0; i < tree.size(); i++) {
            JsonObject record = tree.get(i).getAsJsonObject();
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
            builder.registerTypeAdapter(DetailEntity.class, new DetailEntityDeserializer());
            builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntityDeserializer());
            builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntityDeserializer());
            Gson gson = builder.create();

            final DetailEntity entity = gson.fromJson(record.get("detail"), DetailEntity.class);

//            DetailEntity entity = (DetailEntity) record.get(DETAIL);
            result.add(entity);
            result.addAll(getChildrenFromJSON(record));
        }

        result = removeDuplicates(result);

        log.debug("Totally fount details: {}", result.size());
        for (DetailEntity e : result) {
            System.out.println("Found: " + e.toSimpleString());
        }
        return result;
    }

    private static ArrayList<DetailEntity> removeDuplicates(ArrayList<DetailEntity> result) {
        Set<DetailEntity> set = new LinkedHashSet<>(result);
        result.clear();
        result.addAll(set);
        return result;
    }

    private static ArrayList<DetailEntity> getChildrenFromJSON(JsonObject record) {
        ArrayList<DetailEntity> result = new ArrayList<>();
        final JsonArray children = record.getAsJsonArray(CHILDREN);
        for (int j = 0; j < children.size(); j++) {
            JsonObject object = children.get(j).getAsJsonObject();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
            builder.registerTypeAdapter(DetailEntity.class, new DetailEntityDeserializer());
            builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntityDeserializer());
            builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntityDeserializer());
            Gson gson = builder.create();
            final DetailEntity entity = gson.fromJson(object.get(DETAIL), DetailEntity.class);

//            DetailEntity entity = (DetailEntity) object.get(DETAIL);

            result.add(entity);

            if (entity.isUnit()) {
                result.addAll(getChildrenFromJSON(object));
            }
        }
        return result;
    }

    public JsonObject createTreeJSON() {
        log.info("Creating JSON file");
        progressManager.setText("Формирование структуры базы данных");
        progressManager.setCurrentProgress(0);

        JsonObject root = new JsonObject();
        root.addProperty(TYPE, DEFAULT_TREE_TYPE);
        root.addProperty(TIMESTAMP, Calendar.getInstance().getTime().getTime());
        root.addProperty(AUTHOR, OPERATING_SYSTEM.getNetworkParams().getHostName());

        if (!Thread.currentThread().isInterrupted()) {
            final JsonArray fullTree = getFullTree();
            root.add(TREE, fullTree);
            System.out.println("TREE:\n" + root.toString());

            progressManager.setCurrentIndeterminate(false);
        }
        return root;
    }

    private JsonArray getFullTree() {
        log.info("Creating root entities");
        progressManager.setText("Формирование корневых каталогов");

        ArrayList<DetailEntity> root = CommonServiceUtils.getRootObjects(session);

        log.debug("Root entities: ({}) {}", root.size(), root);

        JsonArray array = new JsonArray();
        for (int i = 0; i < root.size(); i++) {
            if (!Thread.currentThread().isInterrupted()) {
                DetailEntity entity = root.get(i);

                log.debug("Creating tree for root entity: {}", entity.toSimpleString());

                progressManager.setText("Формирование корневого каталога: " + (i + 1) + " из " + root.size());

                JsonObject object = new JsonObject();

                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
                builder.registerTypeAdapter(DetailEntity.class, new DetailEntitySerializer());
                builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntitySerializer());
                builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntitySerializer());
                Gson gson = builder.create();

                object.add(DETAIL, gson.toJsonTree(entity));

                object.addProperty(QUANTITY, 1);
                final JsonArray allChildrenForEntity = getAllChildrenForEntity(entity);
                log.debug("Children for {} (size: {}): {}", entity.toSimpleString(), allChildrenForEntity.size(), allChildrenForEntity);
                object.add(CHILDREN, allChildrenForEntity);

                array.add(object);

                int progress = (int) (((double) i / (root.size() - 1)) * 100);
                progressManager.setCurrentProgress(progress);
            } else {
                break;
            }
        }
        return array;
    }

    private JsonArray getAllChildrenForEntity(DetailEntity parent) {
        JsonArray result = new JsonArray();
        DetailListService service = new DetailListServiceImpl(session);
        final ArrayList<DetailEntity> totalChildrenList = (ArrayList<DetailEntity>) service.listChildren(parent);
        if (totalChildrenList.size() > 0) {
            log.trace("Parent {} has children count: {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), totalChildrenList.size());
            CommonServiceUtils.sortDetailEntityArray(totalChildrenList);

            for (DetailEntity child : totalChildrenList) {
                if (!Thread.currentThread().isInterrupted()) {
                    if (child != null) {
                        if (child.isActive()) {
                            DetailListEntity lastDetailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, child);
                            if (lastDetailListEntity != null) {
                                if (lastDetailListEntity.isActive()) {
                                    JsonObject record = new JsonObject();

                                    GsonBuilder builder = new GsonBuilder();
                                    builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
                                    builder.registerTypeAdapter(DetailEntity.class, new DetailEntitySerializer());
                                    builder.registerTypeAdapter(DetailTitleEntity.class, new DetailTitleEntitySerializer());
                                    builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntitySerializer());
                                    Gson gson = builder.create();

                                    record.add(DETAIL, gson.toJsonTree(child));
                                    record.addProperty(QUANTITY, lastDetailListEntity.getQuantity());


                                    record.add(MATERIALS, getAllMaterialsForEntity(child));
                                    record.addProperty(INTERCHANGEABLE, lastDetailListEntity.isInterchangeableNode());
                                    if (child.isUnit()) {
                                        record.add(CHILDREN, getAllChildrenForEntity(child));
                                    }
                                    result.add(record);
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }

        return result;
    }

    private JsonArray getAllMaterialsForEntity(DetailEntity child) {
        JsonArray result = new JsonArray();
        MaterialListService service = new MaterialListServiceImpl(session);
        final List<MaterialListEntity> materialListByDetail = service.getMaterialListByDetail(child);
        materialListByDetail.sort((o1, o2) -> {
            if (o2 != null) {
                return ComparisonChain.start()
                        .compareTrueFirst(o1.isMainMaterial(), o2.isMainMaterial())
                        .compare(o1.getMaterialByMaterialId().getLongMark(), o2.getMaterialByMaterialId().getLongMark())
                        .compare(o1.getMaterialByMaterialId().getLongProfile(), o2.getMaterialByMaterialId().getLongProfile())
                        .result();
            } else {
                return -1;
            }
        });

        for (MaterialListEntity entity : materialListByDetail) {
            if (entity.isActive()) {
                if (entity.getMaterialByMaterialId().isActive()) {

                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
                    builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntitySerializer());
                    Gson gson = builder.create();
                    result.add(gson.toJsonTree(entity.getMaterialByMaterialId()));
                }
            }
        }
        return result;
    }

    public void downloadFromFTP(File result, ArrayList<DetailEntity> details) {
        Properties constants = CommonUtils.getConstantsToProperties(session);
        String url = constants.getProperty(DaoConstants.BLOB_CONNECTION_URL_KEY);
        String username = constants.getProperty(DaoConstants.BLOB_ACCESS_USERNAME_KEY);
        String password = constants.getProperty(DaoConstants.BLOB_ACCESS_PASSWORD_KEY);
        String postfix = constants.getProperty(DaoConstants.BLOB_LOCATION_POSTFIX_KEY);

        final FtpUtils ftpUtils = FtpUtils.getInstance();
        ftpUtils.connect(url, username, password);
        ftpUtils.setPostfix(postfix);

        for (int i = 0; i < details.size(); i++) {
            if (!Thread.currentThread().isInterrupted()) {
                DetailEntity entity = details.get(i);

                int progress = (int) (((double) i / (details.size() - 1)) * 100);
                progressManager.setCurrentProgress(progress);


                String fileName = entity.getId() + FTP_IMAGE_FILE_EXTENSION;
                File localFile = new File(result + File.separator + fileName);
                try {
                    ftpUtils.downloadFile(entity.getId(), localFile);
                } catch (IOException e) {
                    log.warn("Could not write a file to {}", localFile, e);
                }
            } else {
                break;
            }
        }
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            log.warn("Could not close ftp connection", e);
        }
    }

    public File downloadImages(File folder, JsonObject treeJSON) {
        File result = new File(folder, SPTreeIOManager.IMAGES_FOLDER_FILE_NAME);
        log.debug("Starting downloading images to folder: {}", folder);
        progressManager.setCurrentProgress(0);
        progressManager.setText("Определяем список деталей");
        progressManager.setCurrentIndeterminate(true);

        final String jsonType = treeJSON.getAsJsonPrimitive(TYPE).getAsString();
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

    public void createSPTFile(File file, File jsonFile, File imagesFolder) throws ZipException {
        ZipFile zipFile = new ZipFile(file);

        ZipParameters parameters = getDefaultZipParameters();

        zipFile.addFile(jsonFile, parameters);
        zipFile.addFolder(imagesFolder, parameters);
    }

    private ZipParameters getDefaultZipParameters() {
        ZipParameters parameters = new ZipParameters();
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        parameters.setPassword(ApplicationConstants.INTERNAL_FULL_NAME);
        return parameters;
    }

    public void optimizeImages(File folder) {
        log.info("Starting optimization for images at folder: {}", folder);
        progressManager.setCurrentProgress(0);
        progressManager.setText("Оптимизация изображений");

        if (folder.isDirectory()) {
            final File[] files = folder.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    progressManager.setText("Оптимизация изображения " + (i + 1) + " из " + files.length);
                    File file = files[i];
                    final long length = file.length();
                    log.debug("File length: {}/max:{} ({})", length, DEFAULT_MAX_FILE_LENGTH, file);
                    if (length > DEFAULT_MAX_FILE_LENGTH) {
                        try {
                            compressImage(file, file, DEFAULT_COMPRESSION_VALUE);
                        } catch (IOException e) {
                            log.warn("Could not compress file: {}", file, e);
                        }
                    }
                    int progress = (int) (((double) i / (files.length - 1)) * 100);
                    progressManager.setCurrentProgress(progress);
                }
            }
        }

        progressManager.setCurrentProgress(100);
    }

    private void compressImage(File inputFile, File outputFile, float compression) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        OutputStream os = new FileOutputStream(outputFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compression);  // Change the quality value you prefer
        writer.write(null, new IIOImage(image, null, null), param);

        os.close();
        ios.close();
        writer.dispose();
    }
}
