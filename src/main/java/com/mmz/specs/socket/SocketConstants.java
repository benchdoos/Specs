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

package com.mmz.specs.socket;

public interface SocketConstants {
    String TESTING_CONNECTION_COMMAND = "testing";
    String QUIT_COMMAND = "quit";

    String USER_PC_NAME = "user.pc.name";
    String GIVE_SESSION = "give_session";

    String USER_LOGIN = "user.login";
    String USER_LOGOUT = "user.logout";


    String TRANSACTION_BIND = "transaction.bind";
    String TRANSACTION_UNBIND = "transaction.unbind";
    String TRANSACTION_ACCESS_GRANTED = "transaction.access.granted";
    String TRANSACTION_ACCESS_DENIED = "transaction.access.denied";

}
