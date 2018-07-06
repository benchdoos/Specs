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

import com.mmz.specs.application.managers.ServerSettingsManager;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.mmz.specs.connection.HibernateConstants.*;
import static com.mmz.specs.socket.SocketConstants.USER_PC_NAME;

class ServerSocketDialogUtils {

    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private final Socket client;

    ServerSocketDialogUtils(Socket client) {
        this.client = client;
    }

    public String getPcName() {
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());
            out.writeUTF(USER_PC_NAME);
            return in.readUTF();
        } catch (IOException e) {
            /*NOP*/
            log.info("Could not ask client PC name", e);
        }

        return "Unknown";
    }

    void sendSessionInfo() {
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            String serverDbConnectionUrl = ServerSettingsManager.getInstance().getServerDbConnectionUrl();
            /*serverDbConnectionUrl = serverDbConnectionUrl.replace("localhost", ServerSocketService.getInstance().getServerInetAddress());*/
            /*out.writeUTF(serverDbConnectionUrl);
            out.writeUTF(ServerSettingsManager.getInstance().getServerDbUsername());
            out.writeUTF(ServerSettingsManager.getInstance().getServerDbPassword());*/

            JSONObject json = new JSONObject();
            json.put(CP_DB_CONNECTION_URL_KEY, serverDbConnectionUrl);
            json.put(CP_CONNECTION_USERNAME_KEY, ServerSettingsManager.getInstance().getServerDbUsername());
            json.put(CP_CONNECTION_PASSWORD_KEY, ServerSettingsManager.getInstance().getServerDbPassword());
            final String s = json.toString();
            out.writeUTF(s);
        } catch (IOException e) {
            log.warn("Could not send session info for client");
        }
    }

    String getUserName() {
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            return in.readUTF();
        } catch (IOException e) {
            log.info("Could not read client's username", e);
        }
        return "Unknown";
    }
}
