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

package com.mmz.specs.application.managers;

public class ModeManager {
    public static final MODE DEFAULT_MODE = MODE.CLIENT;
    private static MODE currentMode;

    public static MODE getCurrentMode() {
        return currentMode;
    }

    public static void setCurrentMode(MODE currentMode) {
        ModeManager.currentMode = currentMode;
    }

    public enum MODE {CLIENT, SERVER}
}
