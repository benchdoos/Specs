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

import java.io.File;

public class SPTreeIOManager implements IOManager {
    private ProgressManager progressManager;
    private boolean isInterrupted;

    public SPTreeIOManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    @Override
    public void exportData(File file) {

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
