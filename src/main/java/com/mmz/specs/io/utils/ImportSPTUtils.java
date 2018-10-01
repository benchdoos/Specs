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
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.io.formats.SPTFileFormat;
import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.io.formats.TreeSPTRecordBuilder;
import com.mmz.specs.model.DetailEntity;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.mmz.specs.io.formats.SPTFileFormat.DETAIL;

public class ImportSPTUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private ProgressManager progressManager;

    public ImportSPTUtils(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    public static DefaultMutableTreeNode getDefaultTreeModelFromJSONObject(JSONArray array) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (int i = 0; i < array.length(); i++) {
            final JSONObject jsonObject = array.getJSONObject(i);
            TreeSPTRecordBuilder builder = new TreeSPTRecordBuilder();
            /*DetailEntity entity = JsonUtils.getDetailEntity(jsonObject.getString(DETAIL));*/
            DetailEntity entity = null;
            try {
                entity = FromStringBuilder.stringToObject(jsonObject.getString(DETAIL), DetailEntity.class);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
            builder.setDetail(entity)
                    .setQuantity(jsonObject.getInt(SPTFileFormat.QUANTITY))
                    .setInterchangeable(jsonObject.getBoolean(SPTFileFormat.INTERCHANGEABLE));

            final JSONArray jsonArray = jsonObject.getJSONArray(SPTFileFormat.MATERIALS);
            builder.setMaterials(jsonArray.toList());
            final TreeSPTRecord treeSPTRecord = builder.getTreeSPTRecord();
            final DefaultMutableTreeNode detail = new DefaultMutableTreeNode(treeSPTRecord);
            /*if (treeSPTRecord.getDetail().isUnit()) {
            }*/
            root.add(detail);


            /*final JSONArray jsonArray = jsonObject.getJSONArray(SPTFileFormat.CHILDREN);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject o = jsonArray.getJSONObject(i);
                root.add(getDefaultTreeModelFromJSONObject(o));
            }*/

        }
        return root;
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
