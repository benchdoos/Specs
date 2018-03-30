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

package com.mmz.specs.application.gui.client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.common.ButtonTabComponent;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.UserInfoWindow;
import com.mmz.specs.application.gui.panels.DetailListPanel;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.dao.ConstantsDaoImpl;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.ConstantsServiceImpl;
import com.mmz.specs.service.NoticeServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class ClientMainWindow extends JFrame {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int STATUS_IMAGE_SIZE = 6;

    private UsersEntity currentUser;
    private FtpUtils ftpUtils;
    private Session session;
    private JPanel contentPane;
    private JTabbedPane clientMainTabbedPane;
    private JPanel mainPanel;
    private JLabel serverStatusLabel;
    private JButton viewDetailListButton;
    private JButton loginButton;
    private JLabel usernameLabel;
    private JLabel dbStatusLabel;
    private JButton editDataButton;
    private JButton noticeListViewButton;
    private JButton adminButton;
    private Timer uiUpdateTimer;

    public ClientMainWindow() {
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_CLIENT);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/clientLogo.png")));

        initGui();
        initTimer();

        pack();

    }

    private void initGui() {
        setMinimumSize(new Dimension(860, 480));

        initConnectionLabels();

        initMainMenuBar();
        initListeners();
        unlock(false);
    }

    private void initTimer() {
        uiUpdateTimer = new Timer(1000, e -> updateUserInterface());
        if (!uiUpdateTimer.isRunning()) {
            uiUpdateTimer.start();
        }
    }

    private void initConnectionLabels() {
        serverStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.UNKNOWN));
        serverStatusLabel.setToolTipText("Соединение с сервером не установлено");
        dbStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.UNKNOWN));
        dbStatusLabel.setToolTipText("Соединение с базой данных не установлено");
    }

    private void initMainMenuBar() {
        MenuBar mainMenuBar = new MenuBar();

        Menu fileMenu = getFileMenu();

        mainMenuBar.add(fileMenu);

        Menu editMenu = getEditMenu();
        mainMenuBar.add(editMenu);

        Menu connectionMenu = getConnectionMenu();
        mainMenuBar.add(connectionMenu);

        setMenuBar(mainMenuBar);
    }

    private void initListeners() {
        viewDetailListButton.addActionListener(e -> onViewDetailList());
        loginButton.addActionListener(e -> onLogin());
        usernameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onUsernameInfo();
            }
        });
        noticeListViewButton.addActionListener(e -> onListNoticeInfo());
    }

    private void unlock(boolean status) {
        if (status) {
            loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/unlocked.png"))));
        } else {
            loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/locked.png"))));
        }
        usernameLabel.setText(status ? (currentUser != null ? currentUser.getUsername() : "") : "");
    }

    private void updateUserInterface() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            serverStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.CONNECTED));
            serverStatusLabel.setToolTipText("Сервер подключен");

            if (session == null) {
                session = ClientBackgroundService.getInstance().getSession();
                initFtp();
            } else {
                if (session.isConnected()) {
                    dbStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.CONNECTED));
                    dbStatusLabel.setToolTipText("БД подключена");
                }
            }

            setButtonsEnabled(true);
            setTabsEnabled(true);

        } else {
            serverStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.DISCONNECTED));
            serverStatusLabel.setToolTipText("Сервер отключен");

            if (session != null) {
                if (session.isConnected()) {
                    session.disconnect();
                    session = null;

                    dbStatusLabel.setIcon(getResizedStatusImage(ConnectionStatus.DISCONNECTED));
                    dbStatusLabel.setToolTipText("БД отключена");
                }
            }

            setButtonsEnabled(false);
            setTabsEnabled(false);
        }


    }

    private ImageIcon getResizedStatusImage(ConnectionStatus status) {
        try {
            BufferedImage resized;
            switch (status) {
                case UNKNOWN:
                    resized = Scalr.resize(ImageIO.read(getClass().getResource("/img/gui/status/gray12.png")), STATUS_IMAGE_SIZE);
                    return new ImageIcon(resized);
                case CONNECTED:
                    resized = Scalr.resize(ImageIO.read(getClass().getResource("/img/gui/status/green12.png")), STATUS_IMAGE_SIZE);
                    return new ImageIcon(resized);
                case DISCONNECTED:
                    resized = Scalr.resize(ImageIO.read(getClass().getResource("/img/gui/status/red12.png")), STATUS_IMAGE_SIZE);
                    return new ImageIcon(resized);
                default:
                    resized = Scalr.resize(ImageIO.read(getClass().getResource("/img/gui/status/gray12.png")), STATUS_IMAGE_SIZE);
                    return new ImageIcon(resized);
            }
        } catch (IOException e) {
            log.warn("Could not set icons for status label for status: " + status, e);
            return null;
        }
    }

    private Menu getFileMenu() {
        Menu menu = new Menu("Файл");

        MenuItem openFileMenu = new MenuItem("Открыть");
        openFileMenu.addActionListener(e -> onOpenFileMenu());
        menu.add(openFileMenu);

        MenuItem saveFileMenu = new MenuItem("Сохранить");
        saveFileMenu.addActionListener(e -> onSaveFileMenu());
        menu.add(saveFileMenu);

        return menu;
    }

    private Menu getEditMenu() {
        Menu menu = new Menu("Правка");
        MenuItem restoreFromDb = new MenuItem("Восстановить с базы данных");
        restoreFromDb.addActionListener(e -> onRestoreFromDb());

        menu.add(restoreFromDb);

        return menu;
    }

    private Menu getConnectionMenu() {
        Menu menu = new Menu("Подключение");

        MenuItem connectionSettings = new MenuItem("Настройки подключения");
        connectionSettings.addActionListener(e -> onConnectionSettings());
        menu.add(connectionSettings);

        MenuItem connectServer = new MenuItem("Подключение к серверу", new MenuShortcut(KeyEvent.VK_C, true));
        connectServer.addActionListener(e -> onConnectToServer());
        menu.add(connectServer);

        MenuItem disconnectServer = new MenuItem("Отключение от сервера", new MenuShortcut(KeyEvent.VK_D, true));
        disconnectServer.addActionListener(e -> onDisconnectFromServer());
        menu.add(disconnectServer);

        return menu;
    }

    private void setTabsEnabled(boolean enabled) {
        for (Component component : clientMainTabbedPane.getComponents()) {
            if (component instanceof DetailListPanel) {
                try {
                    int componentZOrder = clientMainTabbedPane.indexOfComponent(component);
                    clientMainTabbedPane.setEnabledAt(componentZOrder, enabled);
                } catch (Exception e) {/*NOP*/}
            }
        }

        if (!enabled) {
            if (clientMainTabbedPane.getSelectedComponent() instanceof DetailListPanel) {
                clientMainTabbedPane.setSelectedIndex(0);
            }
        }
    }

    private void onLogin() {
        if (currentUser == null) {
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
            loginWindow.setVisible(true);
            UsersEntity user = loginWindow.getAuthorizedUser();
            if (user != null) {
                if (user.isActive()) {
                    if (user.isEditor() || user.isAdmin()) {
                        currentUser = user;
                        unlock(true);
                    }

                    if (user.isAdmin()) {
                        unlockAdminTools(true);
                    }

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ваш аккаунт отключён, вы не можете продолжить. Обратитесь к администратору.",
                            "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                unlock(false);
                unlockAdminTools(false);
            }
        } else {
            unlock(false);
            unlockAdminTools(false);
            currentUser = null;
        }
    }

    private void unlockAdminTools(boolean unlock) {
        adminButton.setEnabled(unlock);

        unlockDetailLists(unlock);
    }

    private void onUsernameInfo() {
        if (currentUser != null) {
            UserInfoWindow userInfoWindow = new UserInfoWindow();
            userInfoWindow.setUsersEntity(currentUser);
            userInfoWindow.setLocation(FrameUtils.getFrameOnCenter(this, userInfoWindow));
            userInfoWindow.pack();
            userInfoWindow.setVisible(true);
        }
    }

    private void onListNoticeInfo() {

        List<NoticeEntity> noticeEntities = new NoticeServiceImpl(new NoticeDaoImpl(session)).listNotices();
        Collections.sort(noticeEntities);
        NoticeInfoWindow noticeInfoWindow = new NoticeInfoWindow(session, noticeEntities);
        noticeInfoWindow.setLocation(FrameUtils.getFrameOnCenter(this, noticeInfoWindow));
        noticeInfoWindow.setVisible(true);
    }

    private void initFtp() {
        if (session != null) {
            ftpUtils = FtpUtils.getInstance();

            String url = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_CONNECTION_URL_KEY).getValue();
            String username = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_ACCESS_USERNAME_KEY).getValue();
            String password = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_ACCESS_PASSWORD_KEY).getValue();
            String postfix = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_LOCATION_POSTFIX_KEY).getValue();

            ftpUtils.connect(url, username, password);

            ftpUtils.setPostfix(postfix);
        }
    }

    private void onOpenFileMenu() {
        System.out.println("Open file menu is not supported yet");
    }

    private void onSaveFileMenu() {
        System.out.println("onSaveFileMenu is not supported yet");
    }

    private void onRestoreFromDb() {
        System.out.println("onRestoreFromDb is not supported yet");
    }

    private void onConnectionSettings() {
        ClientConfigurationWindow configurationWindow = new ClientConfigurationWindow();
        configurationWindow.setLocation(FrameUtils.getFrameOnCenter(this, configurationWindow));
        configurationWindow.setVisible(true);
    }

    private void onConnectToServer() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            JOptionPane.showMessageDialog(null,
                    "Сервер уже подключён", "Уведомление",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            ClientBackgroundService.getInstance().createConnection();
        }
    }

    private void onDisconnectFromServer() {
        try {
            log.info("Trying to disconnect from server");
            ClientBackgroundService.getInstance().closeConnection();
            log.info("Successfully disconnected from server");
        } catch (IOException e) {
            log.warn("Could not disconnect from server", e);
            JOptionPane.showMessageDialog(this,
                    "Не удалось отключиться от сервера.\n" + e.getLocalizedMessage(),
                    "Ошибка отключения", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void notifyConnectionRefused() {
        JOptionPane.showMessageDialog(this,
                "Не установлена связь с сервером, проверьте настройки.",
                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        onDisconnectFromServer();
        onDisconnectFromFtp();
        uiUpdateTimer.stop();
        super.dispose();
    }

    private void onDisconnectFromFtp() {
        try {
            ftpUtils.disconnect();
        } catch (IOException e) {
            log.warn("Could not disconnect from ftp server: " + ftpUtils, e);
        }
    }

    public void notifyConnectionError() {
        JOptionPane.showMessageDialog(this,
                "Не установлена связь с сервером, ошибка. Звоните фиксикам.",
                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
    }

    private void unlockDetailLists(boolean unlock) {
        for (Component component : clientMainTabbedPane.getComponents()) {
            if (component instanceof DetailListPanel) {
                DetailListPanel panel = (DetailListPanel) component;
                panel.enableAdminButtons(unlock);
            }
        }
    }

    private void onViewDetailList() {
       /* clientMainTabbedPane.add("Просмотр вложенности", new DetailListPanel());
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        clientMainTabbedPane.setSelectedIndex(index);*/
        addTab("Просмотр вложенности", new DetailListPanel());

        if (currentUser != null) {
            if (currentUser.isAdmin()) {
                unlockDetailLists(true);
            } else {
                unlock(false);
            }
        } else {
            unlockDetailLists(false);
        }
    }

    public void addTab(String title, JPanel panel) {
        clientMainTabbedPane.add(title, panel);
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        clientMainTabbedPane.setSelectedIndex(index);
    }

    private void setButtonsEnabled(final boolean enabled) {

        adminButton.setEnabled(enabled);

        viewDetailListButton.setEnabled(enabled);
        noticeListViewButton.setEnabled(enabled);
        editDataButton.setEnabled(enabled);

    }

    public JTabbedPane getTabbedPane() {
        return clientMainTabbedPane;
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
        contentPane.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane = new JTabbedPane();
        contentPane.add(clientMainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane.addTab("Главная", mainPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Функционал"));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        viewDetailListButton = new JButton();
        viewDetailListButton.setAutoscrolls(false);
        viewDetailListButton.setBorderPainted(false);
        viewDetailListButton.setContentAreaFilled(true);
        viewDetailListButton.setForeground(new Color(-16765749));
        viewDetailListButton.setMargin(new Insets(0, 0, 0, 0));
        viewDetailListButton.setOpaque(false);
        viewDetailListButton.setRequestFocusEnabled(true);
        viewDetailListButton.setRolloverEnabled(true);
        viewDetailListButton.setText("Просмотр вложенности узлов");
        panel1.add(viewDetailListButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        editDataButton = new JButton();
        editDataButton.setAutoscrolls(false);
        editDataButton.setBorderPainted(false);
        editDataButton.setContentAreaFilled(true);
        editDataButton.setForeground(new Color(-16765749));
        editDataButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        editDataButton.setMargin(new Insets(0, 0, 0, 0));
        editDataButton.setOpaque(false);
        editDataButton.setRequestFocusEnabled(true);
        editDataButton.setRolloverEnabled(true);
        editDataButton.setText("Редактирование данных");
        editDataButton.setToolTipText("Редактирование списков констант");
        panel1.add(editDataButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adminButton = new JButton();
        adminButton.setAutoscrolls(false);
        adminButton.setBorderPainted(false);
        adminButton.setContentAreaFilled(true);
        adminButton.setForeground(new Color(-16765749));
        adminButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        adminButton.setMargin(new Insets(0, 0, 0, 0));
        adminButton.setOpaque(false);
        adminButton.setRequestFocusEnabled(true);
        adminButton.setRolloverEnabled(true);
        adminButton.setText("Администрирование");
        adminButton.setToolTipText("Редактирование данных");
        panel1.add(adminButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeListViewButton = new JButton();
        noticeListViewButton.setAutoscrolls(false);
        noticeListViewButton.setBorderPainted(false);
        noticeListViewButton.setContentAreaFilled(true);
        noticeListViewButton.setForeground(new Color(-16765749));
        noticeListViewButton.setMargin(new Insets(0, 0, 0, 0));
        noticeListViewButton.setOpaque(false);
        noticeListViewButton.setRequestFocusEnabled(true);
        noticeListViewButton.setRolloverEnabled(true);
        noticeListViewButton.setText("Просмотр извещений");
        panel1.add(noticeListViewButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        usernameLabel = new JLabel();
        Font usernameLabelFont = this.$$$getFont$$$(null, Font.BOLD, 11, usernameLabel.getFont());
        if (usernameLabelFont != null) usernameLabel.setFont(usernameLabelFont);
        usernameLabel.setText("");
        panel2.add(usernameLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loginButton = new JButton();
        loginButton.setBorderPainted(false);
        loginButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        loginButton.setMargin(new Insets(2, 2, 2, 2));
        loginButton.setOpaque(false);
        loginButton.setText("");
        panel2.add(loginButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 4, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serverStatusLabel = new JLabel();
        panel3.add(serverStatusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dbStatusLabel = new JLabel();
        dbStatusLabel.setText("");
        panel3.add(dbStatusLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        contentPane.add(spacer5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    enum ConnectionStatus {
        UNKNOWN, CONNECTED, DISCONNECTED
    }
}
