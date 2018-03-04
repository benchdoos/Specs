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

public class HibernateConstants {
    public static final String DB_CONNECTION_URL_KEY = "hibernate.connection.url";
    public static final String CP_DB_CONNECTION_URL_KEY = "hibernate.hikari.jdbcUrl";
    public static final String CONNECTION_USERNAME_KEY = "hibernate.connection.username";
    public static final String CP_CONNECTION_USERNAME_KEY = "hibernate.hikari.dataSource.user";
    public static final String CONNECTION_PASSWORD_KEY = "hibernate.connection.password";
    public static final String CP_CONNECTION_PASSWORD_KEY = "hibernate.hikari.dataSource.password";
}
