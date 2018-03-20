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

package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.core.server.service.ServerBackgroundService;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.UsersEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

class ServerIcon extends TrayIcon {
    ServerIcon(Image image) {
        super(image);
        initIcon();
    }

    private void initIcon() {
        setImageAutoSize(true);
        addMenu();

        addActionListener(e -> onIconClick());
    }

    private void addMenu() {
        PopupMenu menu = new PopupMenu();

        MenuItem open = new MenuItem("Открыть");
        open.addActionListener(e -> onIconClick());
        menu.add(open);

        MenuItem shutDown = new MenuItem("Выключить");
        shutDown.addActionListener(e -> onShutDownClick());
        menu.add(shutDown);

        setPopupMenu(menu);
    }

    private void onShutDownClick() {
        LoginWindow loginWindow = new LoginWindow(ServerDBConnectionPool.getInstance().getSession());
        loginWindow.setLocation(FrameUtils.getFrameOnCenter(null, loginWindow));
        loginWindow.setVisible(true);
        UsersEntity user = loginWindow.getAuthorizedUser();

        if (user != null) {
            if (user.isActive() && user.isAdmin()) {
                ServerBackgroundService backgroundService = ServerBackgroundService.getInstance();
                backgroundService.stopServerMainBackgroundService();
                removeIcon();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Пользователь должен быть активным и быть администратором сервера.",
                        "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Необходимо войти в систему, чтобы продолжить",
                    "Ошибка входа", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onIconClick() {
        removeListeners();
        removeIcon();
        ServerMainWindow serverMainWindow = new ServerMainWindow();
        serverMainWindow.setVisible(true);
    }

    private void removeListeners() {
        for (ActionListener listener : getActionListeners()) {
            removeActionListener(listener);
        }
    }

    private void removeIcon() {
        SystemTray.getSystemTray().remove(this);
    }

}
