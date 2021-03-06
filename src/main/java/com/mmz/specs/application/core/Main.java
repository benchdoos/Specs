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

package com.mmz.specs.application.core;

import com.mmz.specs.application.utils.Logging;

public class Main {
    public Main(String[] args) {
        try {
            new Logging(args);
            Application application = new Application();
            application.initApplication(args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Got an fatal exception: " + e.getLocalizedMessage());
            System.err.println("Exiting application with code: -1");
            System.exit(-1);
        }
    }

    public static void main(final String[] args) {
        new Main(args);
    }
}