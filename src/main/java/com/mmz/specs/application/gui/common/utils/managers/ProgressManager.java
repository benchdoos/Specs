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

package com.mmz.specs.application.gui.common.utils.managers;

public class ProgressManager {
    private int totalProgress = 0;
    private int currentProgress = 0;
    private boolean totalIndeterminate = false;
    private boolean currentIndeterminate = false;

    private int totalMaxValue = 3;

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int totalProgress) {
        this.totalProgress = totalProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public boolean isTotalIndeterminate() {
        return totalIndeterminate;
    }

    public void setTotalIndeterminate(boolean totalIndeterminate) {
        this.totalIndeterminate = totalIndeterminate;
    }

    public boolean isCurrentIndeterminate() {
        return currentIndeterminate;
    }

    public void setCurrentIndeterminate(boolean currentIndeterminate) {
        this.currentIndeterminate = currentIndeterminate;
    }

    public int getTotalMaxValue() {
        return totalMaxValue;
    }

    public void setTotalMaxValue(int totalMaxValue) {
        this.totalMaxValue = totalMaxValue;
    }
}
