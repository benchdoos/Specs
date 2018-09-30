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

package com.mmz.specs.application.utils;

import java.util.Arrays;
import java.util.List;

public class SupportedExtensionsConstants {
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = {"jpg", "png", "bmp", "gif"};


    public static final String FTP_IMAGE_FILE_EXTENSION = ".spi";

    public static final String NOTICE_DUMP_FILE_EXTENSION = ".spn";

    public static final String EXPORT_TREE_EXTENSION = ".spt";

    public static final String EXPORT_GDB_EXTENSION = ".gdb";

    public static final String EXPORT_SQL_EXTENSION = ".sql";

    public static final String EXPORT_BACKUP_EXTENSION = ".spb";

    public static final String[] SUPPORTED_FILE_EXTENSIONS = {EXPORT_TREE_EXTENSION, FTP_IMAGE_FILE_EXTENSION};

    public static boolean contains(String[] extensions, String extension) {
        extension = extension.toLowerCase();
        final List<String> list = Arrays.asList(extensions);
        return list.contains(extension);
    }
}
