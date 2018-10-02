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

import com.google.gson.*;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.io.SPTreeIOManager;
import com.mmz.specs.io.formats.SPTFileFormat;
import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.io.formats.TreeSPTRecordBuilder;
import com.mmz.specs.io.serialization.deserializer.MaterialEntityDeserializer;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialEntity;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ImportSPTUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private ProgressManager progressManager;

    public ImportSPTUtils(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    public static DefaultMutableTreeNode getDefaultTreeModelFromJsonObject(DefaultMutableTreeNode root, JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            final JsonObject jsonObject = array.get(i).getAsJsonObject();
            TreeSPTRecordBuilder treeBuilder = new TreeSPTRecordBuilder();

            Gson gson = SPTreeIOManager.getDefaultGson();

            final DetailEntity entity = gson.fromJson(jsonObject.get("detail"), DetailEntity.class);


            treeBuilder.setDetail(entity)
                    .setQuantity(jsonObject.get(SPTFileFormat.QUANTITY).getAsInt())
                    .setInterchangeable(jsonObject.get(SPTFileFormat.INTERCHANGEABLE).getAsBoolean());

            final JsonArray materialsArray = jsonObject.getAsJsonArray(SPTFileFormat.MATERIALS);
            treeBuilder.setMaterials(jsonArrayToMaterialList(materialsArray));
            final TreeSPTRecord treeSPTRecord = treeBuilder.getTreeSPTRecord();
            final DefaultMutableTreeNode detail = new DefaultMutableTreeNode(treeSPTRecord);
            if (treeSPTRecord.getDetail().isUnit()) {
                final JsonArray jsonArray = jsonObject.getAsJsonArray(SPTFileFormat.CHILDREN);
                root.add(getDefaultTreeModelFromJsonObject(detail, jsonArray));
            }
            root.add(detail);
        }
        return root;
    }

    private static List<Object> jsonArrayToMaterialList(JsonArray jsonArray) {
        List<Object> result = new ArrayList<>();
        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(MaterialEntity.class, new MaterialEntityDeserializer());
                Gson gson = builder.create();

                final MaterialEntity entity = gson.fromJson(element.getAsJsonObject(), MaterialEntity.class);
                result.add(entity);
            }
        }
        return result;
    }

    public File openSPTFile(File file) throws ZipException, IOException {
        progressManager.setTotalProgress(0);

        ZipFile zipFile = new ZipFile(file);
        Path tempDirectory = Files.createTempDirectory("sptTMPFolder");

        log.debug("Created tmp folder: {} ", tempDirectory);
        progressManager.setTotalProgress(1);


        zipFile.setPassword(ApplicationConstants.INTERNAL_FULL_NAME);
        zipFile.extractAll(tempDirectory.toString());
        progressManager.setTotalProgress(2);
        final File tmpFolder = tempDirectory.toFile();
        tmpFolder.deleteOnExit();
        return tmpFolder;
    }
}
