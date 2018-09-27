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
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.io.utils.ExportSPTUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static com.mmz.specs.application.core.ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION;
import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.OPERATING_SYSTEM;

public class SPTreeIOManager implements IOManager {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final Session session;

    private ProgressManager progressManager;
    private boolean isInterrupted;

    public SPTreeIOManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
        this.session = ServerDBConnectionPool.getInstance().getSession();
    }

    @Override
    public void exportData(File file) throws IOException {
        log.info("Starting export of tree (SPT) to file: {}", file);
        checkCreate(file);

        createTreeJSON(file);
        progressManager.setTotalProgress(1);

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

    private void createTreeJSON(File file) {
        log.info("Creating JSON file");
        progressManager.setText("Формирование структуры базы данных");
        progressManager.setCurrentProgress(0);
        JSONObject root = new JSONObject();
        root.put("type", "com.mmz.specs.tree");
        root.put("timestamp", Calendar.getInstance().getTime());
        root.put("author", OPERATING_SYSTEM.getNetworkParams().getHostName());

        final ExportSPTUtils exportSPTUtils = new ExportSPTUtils(session, progressManager);

        final JSONArray fullTree = exportSPTUtils.getFullTree();
        root.put("tree", fullTree);
        System.out.println("TREE:\n" + root.toString(1));

        progressManager.setCurrentIndeterminate(false);

    }

    @Override
    public Object importData(File file) {
        return null;
    }

    @Override
    public void interrupt() {
        isInterrupted = true;
    }
}
