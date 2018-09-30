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
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImportSPTUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private ProgressManager progressManager;

    public ImportSPTUtils(ProgressManager progressManager) {
        this.progressManager = progressManager;
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
