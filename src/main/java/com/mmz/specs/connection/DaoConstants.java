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

package com.mmz.specs.connection;

public class DaoConstants {
    public static final String BLOB_LOCATION_POSTFIX_KEY = "blob.location.postfix";
    public static final String BLOB_CONNECTION_URL_KEY = "blob.connection.url";
    public static final String BLOB_ACCESS_USERNAME_KEY = "blob.access.username";
    public static final String BLOB_ACCESS_PASSWORD_KEY = "blob.access.password";

    public static final String USER_ADMIN_TIMEOUT = "user.admin.timeout";
    public static final int USER_ADMIN_TIMEOUT_DEFAULT = 60;
    public static final int USER_ADMIN_TIMEOUT_MINIMUM = 10; //10 sec
    public static final int USER_ADMIN_TIMEOUT_MAXIMUM = 60 * 60; // 1 hour

    public static final String USER_EDITOR_TIMEOUT = "user.editor.timeout";
    public static final int USER_EDITOR_TIMEOUT_DEFAULT = 60;
    public static final int USER_EDITOR_TIMEOUT_MINIMUM = 10;
    public static final int USER_EDITOR_TIMEOUT_MAXIMUM = 60 * 60;
}
