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

package com.mmz.specs.application.utils.client;

import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.client.EditTitleWindow;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.DetailListServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class CommonWindowUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Session session;

    public CommonWindowUtils(Session session) {
        this.session = session;
    }

    public static String createDelimiter(String longMark, String longProfile) {
        if (longMark != null && longProfile != null) {
            if (longMark.length() > longProfile.length()) {
                return getDelimiter(longMark.length());
            } else return getDelimiter(longProfile.length());
        } else return "";
    }

    private static String getDelimiter(int size) {
        size = (int) (size * 1.4);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size; i++) {
            result.append("-");
        }
        return result.toString();
    }

    public static void initApplicationVersionArea(JTextField textField) {
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new JPanel().getBackground());
        Properties properties = new Properties();
        try {
            properties.load(ClientMainWindow.class.getResourceAsStream("/application.properties"));
            String name = properties.getProperty("application.name");
            String version = properties.getProperty("application.version");
            String build = properties.getProperty("application.build");
            if (version != null && build != null) {
                textField.setText(name + " v." + version + " (" + build + ")");
            } else {
                textField.setText(null);
                textField.setVisible(false);
            }
        } catch (Exception e) {
            log.warn("Could not load application version info", e);
        }
    }

    public DefaultListModel<DetailEntity> getEffectList(int id) {
        DefaultListModel<DetailEntity> model = new DefaultListModel<>();

        List<DetailListEntity> list = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByNoticeId(id);
        ArrayList<String> unique = new ArrayList<>();

        for (DetailListEntity entity : list) {
            if (!unique.contains(entity.getDetailByParentDetailId().getCode())) {
                unique.add(entity.getDetailByParentDetailId().getCode());
                model.addElement(entity.getDetailByParentDetailId());
            }

            if (!unique.contains(entity.getDetailByChildDetailId().getCode())) {
                unique.add(entity.getDetailByChildDetailId().getCode());
                model.addElement(entity.getDetailByChildDetailId());
            }
        }

        Collections.sort(unique);
        model = sortModel(unique, model);

        return model;
    }

    private DefaultListModel<DetailEntity> sortModel(ArrayList<String> unique, DefaultListModel<DetailEntity> model) {
        DefaultListModel<DetailEntity> result = new DefaultListModel<>();
        for (String name : unique) {
            for (int i = 0; i < model.getSize(); i++) {
                DetailEntity entity = model.getElementAt(i);
                if (name.equalsIgnoreCase(entity.getCode())) {
                    result.addElement(entity);
                }
            }
        }
        return result;
    }

    public static int getIndexByValue(JList list, Object value) {
        System.out.println(">>> " + list.getModel().getSize() + " " + value);
        DefaultListModel model = (DefaultListModel) list.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object current = model.get(i);
            if (current.equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private boolean addTitle(Component component, UsersEntity user) {
        if (user != null) {
            if (user.isActive()) {
                if (user.isAdmin()) {
                    return true;
                } else {
                    showMessageDialog(component,
                            "Вам необходимо быть администратором,\n" +
                                    "чтобы проводить изменения.", "Ошибка доступа", WARNING_MESSAGE);
                }
            } else {
                showMessageDialog(component,
                        "Пользователь не активен,\n" +
                                "обратитесь к администратору для продолжения.", "Ошибка доступа", WARNING_MESSAGE);
            }
        } else {
            showMessageDialog(component,
                    "Необходимо выполнить вход, чтобы продолжить", "Ошибка входа", WARNING_MESSAGE);
        }
        return false;
    }

    public DetailTitleEntity onCreateNewTitle(Component component) {
        Window window = FrameUtils.findWindow(component);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();


            EditTitleWindow editTitleWindow = new EditTitleWindow(null);
            editTitleWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(component), editTitleWindow));
            editTitleWindow.setVisible(true);
            DetailTitleEntity titleEntity = editTitleWindow.getDetailTitleEntity();

            if (addTitle(component, currentUser)) return titleEntity;
        } else {
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(component), loginWindow));
            loginWindow.setVisible(true);
            UsersEntity user = loginWindow.getAuthorizedUser();


            EditTitleWindow editTitleWindow = new EditTitleWindow(null);
            editTitleWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(component), editTitleWindow));
            editTitleWindow.setVisible(true);
            DetailTitleEntity titleEntity = editTitleWindow.getDetailTitleEntity();

            if (addTitle(component, user)) return titleEntity;
        }
        return null;
    }
}
