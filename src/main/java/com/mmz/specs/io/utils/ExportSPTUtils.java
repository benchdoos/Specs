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

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.io.IOConstants;
import com.mmz.specs.io.SPTreeIOManager;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.MaterialListEntity;
import com.mmz.specs.service.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

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

public class ExportSPTUtils {
    public static final String DETAIL = "detail";
    public static final String QUANTITY = "quantity";
    public static final String MATERIALS = "materials";
    public static final String INTERCHANGEABLE = "interchangeable";
    public static final String CHILDREN = "children";

    private static final int DEFAULT_MAX_FILE_LENGTH = 500 * 1000;
    private static final float DEFAULT_COMPRESSION_VALUE = 0.8f;


    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;
    private ProgressManager progressManager;

    public ExportSPTUtils(Session session, ProgressManager progressManager) {
        this.session = session;
        this.progressManager = progressManager;
    }

    public static ArrayList<DetailEntity> listDetailsFromJSON(JSONObject treeJSON) {
        log.debug("Starting searching details in JSON");
        final String jsonType = treeJSON.getString(IOConstants.TYPE);
        if (!jsonType.equalsIgnoreCase(IOConstants.DEFAULT_TREE_TYPE)) {
            throw new IllegalArgumentException("JSON File type does not much " + DEFAULT_TREE_TYPE + ", now it is: " + jsonType);
        }

        ArrayList<DetailEntity> result = new ArrayList<>();

        JSONArray tree = treeJSON.getJSONArray(IOConstants.TREE);
        log.debug("Found tree size: {}", tree.length());

        for (int i = 0; i < tree.length(); i++) {
            JSONObject record = tree.getJSONObject(i);
            DetailEntity entity = (DetailEntity) record.get(DETAIL);
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

    private static ArrayList<DetailEntity> getChildrenFromJSON(JSONObject record) {
        ArrayList<DetailEntity> result = new ArrayList<>();
        final JSONArray children = record.getJSONArray(CHILDREN);
        for (int j = 0; j < children.length(); j++) {
            JSONObject object = children.getJSONObject(j);
            DetailEntity entity = (DetailEntity) object.get(DETAIL);

            result.add(entity);

            if (entity.isUnit()) {
                result.addAll(getChildrenFromJSON(object));
            }
        }
        return result;
    }

    public JSONObject createTreeJSON() {
        log.info("Creating JSON file");
        progressManager.setText("Формирование структуры базы данных");
        progressManager.setCurrentProgress(0);

        JSONObject root = new JSONObject();
        root.put(TYPE, DEFAULT_TREE_TYPE);
        root.put(TIMESTAMP, Calendar.getInstance().getTime());
        root.put(AUTHOR, OPERATING_SYSTEM.getNetworkParams().getHostName());

        if (!Thread.currentThread().isInterrupted()) {
            final JSONArray fullTree = getFullTree();
            root.put(TREE, fullTree);
            System.out.println("TREE:\n" + root.toString(1));

            progressManager.setCurrentIndeterminate(false);
        }
        return root;
    }

    private JSONArray getFullTree() {
        log.info("Creating root entities");
        progressManager.setText("Формирование корневых каталогов");

        ArrayList<DetailEntity> root = CommonServiceUtils.getRootObjects(session);

        log.debug("Root entities: ({}) {}", root.size(), root);

        JSONArray array = new JSONArray();
        if (root != null) {
            for (int i = 0; i < root.size(); i++) {
                if (!Thread.currentThread().isInterrupted()) {
                    DetailEntity entity = root.get(i);

                    log.debug("Creating tree for root entity: {}", entity.toSimpleString());

                    progressManager.setText("Формирование корневого каталога: " + (i + 1) + " из " + root.size());

                    JSONObject object = new JSONObject();
                    object.put(DETAIL, entity);
                    object.put(QUANTITY, 1);
                    final JSONArray allChildrenForEntity = getAllChildrenForEntity(entity);
                    log.debug("Children for {} (size: {}): {}", entity.toSimpleString(), allChildrenForEntity.length(), allChildrenForEntity);
                    object.put(CHILDREN, allChildrenForEntity);

                    array.put(object);

                    int progress = (int) (((double) i / (root.size() - 1)) * 100);
                    progressManager.setCurrentProgress(progress);
                } else {
                    break;
                }
            }
        } else {
            progressManager.setText("Не удалось найти корневые каталоги");
        }
        return array;
    }

    private JSONArray getAllChildrenForEntity(DetailEntity parent) {
        JSONArray result = new JSONArray();
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
                                    JSONObject record = new JSONObject();
                                    record.put(DETAIL, child);
                                    record.put(QUANTITY, lastDetailListEntity.getQuantity());
                                    record.put(MATERIALS, getAllMaterialsForEntity(child));
                                    record.put(INTERCHANGEABLE, lastDetailListEntity.isInterchangeableNode());
                                    if (child.isUnit()) {
                                        record.put(CHILDREN, getAllChildrenForEntity(child));
                                    }
                                    result.put(record);
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

    private JSONArray getAllMaterialsForEntity(DetailEntity child) {
        JSONArray result = new JSONArray();
        MaterialListService service = new MaterialListServiceImpl(session);
        final List<MaterialListEntity> materialListByDetail = service.getMaterialListByDetail(child);
        for (MaterialListEntity entity : materialListByDetail) {
            if (entity.isActive()) {
                if (entity.getMaterialByMaterialId().isActive()) {
                    result.put(entity.getMaterialByMaterialId());
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

    public File downloadImages(File folder, JSONObject treeJSON) {
        File result = new File(folder, SPTreeIOManager.IMAGES_FOLDER_FILE_NAME);
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

    private void compressImage(File inputFile, File outputFile, float compresstion) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        OutputStream os = new FileOutputStream(outputFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compresstion);  // Change the quality value you prefer
        writer.write(null, new IIOImage(image, null, null), param);

        os.close();
        ios.close();
        writer.dispose();
    }
}
