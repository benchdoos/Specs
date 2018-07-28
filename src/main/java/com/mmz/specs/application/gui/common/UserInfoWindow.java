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

package com.mmz.specs.application.gui.common;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.core.server.service.ClientConnection;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.model.UsersEntity;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserInfoWindow extends JDialog {
    private JPanel contentPane;
    private JLabel usernameLabel;
    private JLabel userTypeLabel;
    private JLabel connectionAddressLabel;
    private JLabel nameLabel;
    private JLabel surnameLabel;
    private JCheckBox isEditorCheckBox;
    private JCheckBox isAdminCheckBox;
    private JCheckBox isActiveCheckBox;
    private JLabel patronymicLabel;
    private JLabel userIconLabel;
    private JButton resetPasswordButton;
    private ClientConnection clientConnection;

    public UserInfoWindow(ClientConnection clientConnection, boolean enableResetButton) {
        this.clientConnection = clientConnection;

        if (clientConnection != null) {
            initGui();

            initListeners();

            initKeyBindings();

            fillUserInformation(clientConnection.getUser());

        }
        if (enableResetButton) {
            initResetPasswordButton();
        }
        pack();
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initListeners() {
        resetPasswordButton.addActionListener(e -> onResetPassword());
    }

    private void initGui() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/user16.png")));
        setContentPane(contentPane);
        setModal(true);

        updateTitle();
        updateClientConnectionAddressLabel();

        setResizable(false);

    }

    private void initResetPasswordButton() {
        if (clientConnection != null) {
            if (clientConnection.getUser() != null) {
                resetPasswordButton.setVisible(true);
            }
        }
    }

    private void onResetPassword() {

        try (final Session session = ClientBackgroundService.getInstance().getSession()) {//only for client
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
            loginWindow.setVisible(true);
            UsersEntity user = loginWindow.getAuthorizedUser();
            if (user != null) {
                if (clientConnection != null) {
                    final UsersEntity usersEntity = clientConnection.getUser();
                    if (usersEntity != null) {
                        if (user.equals(usersEntity) || (user.isAdmin() && user.isActive())) {
                            PasswordChangeWindow passwordChangeWindow = new PasswordChangeWindow(usersEntity, session);
                            passwordChangeWindow.setLocation(FrameUtils.getFrameOnCenter(this, passwordChangeWindow));
                            passwordChangeWindow.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Необходимо войти как пользователь " + usersEntity.getUsername() + "\n" +
                                            "или как администратор", "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private void updateUserIcon(UsersEntity entity) {
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/user128.png")));
        if (entity != null) {
            updateIcon(icon);

            if (entity.isAdmin()) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/admin128.png")));
                updateIcon(icon);
            }

            if (!entity.isActive()) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/notActive_128.png")));
                updateIcon(icon);

            }
        } else {
            icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/notActive_128.png")));
            userIconLabel.setIcon(icon);
        }
    }

    private void updateClientConnectionAddressLabel() {
        if (clientConnection != null) {
            connectionAddressLabel.setText(clientConnection.getSocket().getInetAddress() + ":" + clientConnection.getSocket().getPort());
        }
    }

    private void updateIcon(ImageIcon icon) {
        userIconLabel.setIcon(icon);
        setIconImage(icon.getImage());
    }

    private void updateTitle() {
        String title = "Информация о пользователе: ";
        if (clientConnection != null) {
            if (isUserEntityAvailable()) {
                setTitle(title + clientConnection.getUser().getUsername());
            } else {
                if (clientConnection.getSocket() != null) {
                    setTitle(title + clientConnection.getSocket().getInetAddress() + ":" + clientConnection.getSocket().getPort());
                }
            }
        }
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
        initGui();
    }

    private boolean isUserEntityAvailable() {
        return clientConnection != null && clientConnection.getUser() != null;
    }

    private void fillUserInformation(UsersEntity entity) {
        final String NO_DATA = "нет данных";
        if (entity != null) {
            updateUserIcon(entity);
            usernameLabel.setText(entity.getUsername() == null ? NO_DATA : entity.getUsername());
            surnameLabel.setText(entity.getSurname() == null ? NO_DATA : entity.getSurname());
            nameLabel.setText(entity.getName() == null ? NO_DATA : entity.getName());
            patronymicLabel.setText(entity.getPatronymic() == null ? NO_DATA : entity.getPatronymic());
            userTypeLabel.setText(entity.getUserType().getName() == null ? NO_DATA : entity.getUserType().getName());
            isEditorCheckBox.setSelected(entity.isEditor());
            isAdminCheckBox.setSelected(entity.isAdmin());
            isActiveCheckBox.setSelected(entity.isActive());
        } else {
            usernameLabel.setText(NO_DATA);
            surnameLabel.setText(NO_DATA);
            nameLabel.setText(NO_DATA);
            patronymicLabel.setText(NO_DATA);
            userTypeLabel.setText(NO_DATA);
            isEditorCheckBox.setSelected(false);
            isAdminCheckBox.setSelected(false);
            isActiveCheckBox.setSelected(false);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        resetPasswordButton = new JButton();
        resetPasswordButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningOrange16.png")));
        resetPasswordButton.setIconTextGap(2);
        resetPasswordButton.setMargin(new Insets(2, 5, 2, 5));
        resetPasswordButton.setText("Сбросить пароль");
        resetPasswordButton.setMnemonic('С');
        resetPasswordButton.setDisplayedMnemonicIndex(0);
        panel1.add(resetPasswordButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(14, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Имя пользователя:");
        panel2.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setText("Не известно");
        panel2.add(usernameLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userTypeLabel = new JLabel();
        userTypeLabel.setText("Не известно");
        panel2.add(userTypeLabel, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Тип пользователя");
        panel2.add(label2, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Имя:");
        panel2.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Не известно");
        panel2.add(nameLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Отчество:");
        panel2.add(label4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        patronymicLabel = new JLabel();
        patronymicLabel.setText("Не известно");
        panel2.add(patronymicLabel, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Адрес подключения:");
        panel2.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Фамилия:");
        panel2.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        surnameLabel = new JLabel();
        surnameLabel.setText("Не известно");
        panel2.add(surnameLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel2.add(separator1, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        userIconLabel = new JLabel();
        userIconLabel.setHorizontalAlignment(0);
        userIconLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/user128.png")));
        userIconLabel.setText("");
        panel2.add(userIconLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel2.add(separator2, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        isEditorCheckBox = new JCheckBox();
        isEditorCheckBox.setBorderPainted(false);
        isEditorCheckBox.setBorderPaintedFlat(false);
        isEditorCheckBox.setEnabled(false);
        isEditorCheckBox.setFocusCycleRoot(false);
        isEditorCheckBox.setHorizontalAlignment(10);
        isEditorCheckBox.setHorizontalTextPosition(2);
        isEditorCheckBox.setRolloverEnabled(true);
        isEditorCheckBox.setText("");
        panel2.add(isEditorCheckBox, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isAdminCheckBox = new JCheckBox();
        isAdminCheckBox.setEnabled(false);
        isAdminCheckBox.setHorizontalTextPosition(2);
        isAdminCheckBox.setOpaque(true);
        isAdminCheckBox.setText("");
        panel2.add(isAdminCheckBox, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setEnabled(false);
        isActiveCheckBox.setHorizontalTextPosition(2);
        isActiveCheckBox.setText("");
        panel2.add(isActiveCheckBox, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Редактор:");
        panel2.add(label7, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Администратор:");
        panel2.add(label8, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Действующий:");
        panel2.add(label9, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectionAddressLabel = new JLabel();
        connectionAddressLabel.setText("Не известно");
        panel2.add(connectionAddressLabel, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
