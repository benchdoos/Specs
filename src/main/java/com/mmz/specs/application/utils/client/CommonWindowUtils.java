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
import com.mmz.specs.application.utils.CoreUtils;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.swing.JOptionPane.*;

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
        String s = CoreUtils.getApplicationVersionString();
        if (s != null) {
            textField.setText(s);
            textField.setComponentPopupMenu(getCopyPopupMenu(textField.getText()));
        } else {
            textField.setText(null);
            textField.setVisible(false);
        }
    }

    public static JPopupMenu getCopyPopupMenu(String text) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = getCopyMenuItem(text);
        menu.add(copy);
        return menu;
    }

    private static JMenuItem getCopyMenuItem(String text) {
        JMenuItem copy = new JMenuItem("Копировать");
        copy.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
        });
        copy.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(CommonWindowUtils.class.getResource("/img/gui/edit/copy.png"))));
        return copy;
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

    private static boolean notContainsEntityInParent(Component parentComponent, TreePath selectionPath, DetailEntity entity) {
        final Object o = selectionPath.getLastPathComponent();
        if (o != null) {
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    final Object userObject = childNode.getUserObject();
                    if (userObject instanceof DetailEntity) {
                        DetailEntity current = (DetailEntity) userObject;
                        if (current.getId() == entity.getId()) {
                            showMessageDialog(parentComponent, "Нельзя добавить деталь/узел "
                                    + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                                    "т.к. он/она уже содержится в узле.", "Ошибка добавления", ERROR_MESSAGE);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean notContainsEntityInParents(Component parentComponent, TreePath selectionPath, DetailEntity entity) {
        for (Object o : selectionPath.getPath()) {
            if (o != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                if (node.getUserObject() instanceof DetailEntity) {
                    DetailEntity current = (DetailEntity) node.getUserObject();
                    if (current.equals(entity)) {
                        showMessageDialog(parentComponent, "Нельзя добавить узел "
                                + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                                "т.к. он не может содержать самого себя.", "Ошибка добавления", ERROR_MESSAGE);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean notContainsEntityInChildren(Component parentComponent, JTree mainTree, TreePath path, DetailEntity entity) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
        for (int i = 0; i < node.getChildCount(); i++) {
            final Object child = mainTree.getModel().getChild(node, i);
            if (child != null) {
                if (child instanceof DefaultMutableTreeNode) {
                    DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) child).getUserObject();
                    if (detailEntity != null && entity != null) {
                        if (detailEntity.getId() == entity.getId()) {
                            showMessageDialog(parentComponent, "Нельзя добавить деталь / узел "
                                    + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                                    "т.к. он(а) уже содержится в узле.", "Ошибка добавления", ERROR_MESSAGE);
                            return false;
                        }
                    } else return false;
                }
            }
        }
        return true;
    }

    public static String getCanonicalProfile(String text) {
        //I was too lazy to make it work perfect (use array)
        if (text.toLowerCase().contains("круг")) {
            text = text.replaceAll("круг", "Ø");
            text = text.replaceAll("Круг", "Ø");
        }
        return text;
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

    public DetailTitleEntity onCreateNewTitle(Component component) {
        Window window = FrameUtils.findWindow(component);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();


            if (addTitle(component, currentUser)) {
                return getDetailTitleEntity(component);
            }
        } else {
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(component), loginWindow));
            loginWindow.setVisible(true);
            UsersEntity user = loginWindow.getAuthorizedUser();

            if (addTitle(component, user)) {
                return getDetailTitleEntity(component);
            }
        }
        return null;
    }

    private boolean addTitle(Component component, UsersEntity user) {
        if (user != null) {
            if (user.isActive()) {
                if (user.isAdmin()) {
                    return true;
                } else {
                    FrameUtils.shakeFrame(component);
                    showMessageDialog(component,
                            "Вам необходимо быть администратором,\n" +
                                    "чтобы проводить изменения.", "Ошибка доступа", WARNING_MESSAGE);
                }
            } else {
                FrameUtils.shakeFrame(component);
                showMessageDialog(component,
                        "Пользователь не активен,\n" +
                                "обратитесь к администратору для продолжения.", "Ошибка доступа", WARNING_MESSAGE);
            }
        } else {
            FrameUtils.shakeFrame(component);
            showMessageDialog(component,
                    "Необходимо выполнить вход, чтобы продолжить", "Ошибка входа", WARNING_MESSAGE);
        }
        return false;
    }

    private DetailTitleEntity getDetailTitleEntity(Component component) {
        EditTitleWindow editTitleWindow = new EditTitleWindow(session, null);
        editTitleWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(component), editTitleWindow));
        editTitleWindow.setVisible(true);
        return editTitleWindow.getDetailTitleEntity();
    }
}
