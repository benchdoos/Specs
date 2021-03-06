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

package com.mmz.specs.application.core.client;

import java.awt.*;

public class ClientConstants {
    public static final String CLIENT_SERVER_ADDRESS_KEY = "server.address";
    public static final String MAIN_WINDOW_DIMENSION = "window.dimension";
    public static final String MAIN_WINDOW_POSITION = "window.position";
    public static final String MAIN_WINDOW_EXTENDED = "window.extended";

    public static final String IMAGE_PREVIEW_WINDOW_DIMENSION = "preview.extended";
    public static final String IMAGE_PREVIEW_WINDOW_POSITION = "preview.position";


    public static final String BOOST_ROOT_UNITS_LOADING = "settings.tree.loading.boosted";

    public static final Dimension MAIN_WINDOW_DEFAULT_DIMENSION = new Dimension(640, 540);
    public static final boolean MAIN_WINDOW_DEFAULT_EXTENDED = false;
    public static final Dimension IMAGE_PREVIEW_WINDOW_DEFAULT_DIMENSION = new Dimension(640, 480);
    public static final boolean BOOST_ROOT_UNITS_DEFAULT_LOADING = true;

    public static final String AUTO_UPDATE_ENABLED = "application.update";
    public static final boolean AUTO_UPDATE_DEFAULT_ENABLED = true;


    public static final String IMAGE_REMOVE_KEY = "remove";
    public static final String OFFLINE_MODE = "--offline";
    public static final String NEW_IMAGE_VIEWER = "preview.image.viewer.new";
    public static final boolean NEW_IMAGE_VIEWER_DEFAULT_VALUE = false;
}
