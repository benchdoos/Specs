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
import com.mmz.specs.application.core.client.ClientConstants;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.common.AboutApplicationWindow;
import com.mmz.specs.application.gui.common.ButtonTabComponent;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.UserInfoWindow;
import com.mmz.specs.application.gui.panels.*;
import com.mmz.specs.application.managers.ClientSettingsManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.dao.ConstantsDaoImpl;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.ConstantsEntity;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.ConstantsService;
import com.mmz.specs.service.ConstantsServiceImpl;
import com.mmz.specs.service.NoticeServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ClientMainWindow extends JFrame {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private UsersEntity currentUser;
    private FtpUtils ftpUtils;
    private Session session;
    private JPanel contentPane;
    private JTabbedPane clientMainTabbedPane;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JButton viewDetailListButton;
    private JButton loginButton;
    private JLabel usernameLabel;
    private JButton editDataButton;
    private JButton noticeListViewButton;
    private JButton adminButton;
    private JTextField applicationVersionTextField;
    private JLabel messageLabel;
    private Timer uiUpdateTimer;
    private Timer unlockUiTimer;
    private int unlockedSeconds;
    private int maxUnlockedSeconds;
    private boolean blockingMessage = false;

    public ClientMainWindow() {
        try {
            initGui();
            initTimers();
        } catch (Throwable e) {
            log.warn("Could not init Client main window", e);
        }

    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onLogin(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> closeCurrentTab(),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initHomeTab() {
        clientMainTabbedPane.setIconAt(0,
                new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/home12.png"))));
    }

    private void initTimers() {
        uiUpdateTimer = new Timer(300, e -> updateStatusLabel());
        if (!uiUpdateTimer.isRunning()) {
            uiUpdateTimer.start();
        }
        unlockUiTimer = new Timer(1000, e -> updateUnlockUiTimer());
        if (!unlockUiTimer.isRunning()) {
            unlockUiTimer.start();
        }
    }

    public void resetUnlockedSeconds() {
        unlockedSeconds = 0;
    }

    private void updateUnlockUiTimer() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            if (currentUser != null) {
                try {
                    ConstantsService service = new ConstantsServiceImpl(session);
                    ConstantsEntity constant;
                    constant = getConstantsEntity(service);
                    if (constant != null) {
                        String value = constant.getValue();
                        maxUnlockedSeconds = Integer.valueOf(value);
                        if (unlockedSeconds > maxUnlockedSeconds) {
                            unlockedSeconds = 0;
                            lockUI();
                        } else {
                            unlockedSeconds++;
                        }
                    }
                } catch (Exception ignore) {
                }
            } else {
                unlockedSeconds = 0;
            }
        } else {
            unlockedSeconds = 0;
            lockUI();
        }
        uiUpdateTimer.restart();
    }

    private void initConnectionLabels() {
        statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.DISCONNECTED));
        statusLabel.setToolTipText("Соединение с сервером и БД не установлено");
    }

    private void unlockButtonIconUpdate(boolean status) {
        try {
            if (status) {
                loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/unlocked.png"))));
            } else {
                loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/locked.png"))));
            }
            usernameLabel.setText(status ? (currentUser != null ? currentUser.getUsername() : "") : "");
        } catch (Exception e) {
            log.warn("Could not update icon for login button", e);
        }
    }

    private JMenu getFileMenu() {
        JMenu menu = new JMenu("Файл");

        JMenuItem openFileMenu = new JMenuItem("Открыть");
        openFileMenu.addActionListener(e -> onOpenFileMenu());
        menu.add(openFileMenu);

        JMenuItem saveFileMenu = new JMenuItem("Сохранить");
        saveFileMenu.addActionListener(e -> onSaveFileMenu());
        menu.add(saveFileMenu);

        return menu;
    }

    private JMenu getEditMenu() {
        JMenu menu = new JMenu("Правка");
        JMenuItem restoreFromDb = new JMenuItem("Восстановить с базы данных");
        restoreFromDb.addActionListener(e -> onRestoreFromDb());

        menu.add(restoreFromDb);

        return menu;
    }

    private JMenu getConnectionMenu() {
        JMenu menu = new JMenu("Подключение");

        JMenuItem connectionSettings = new JMenuItem("Настройки подключения");
        connectionSettings.setIconTextGap(0);
        connectionSettings.addActionListener(e -> onConnectionSettings());
        menu.add(connectionSettings);

        JMenuItem connectServer = new JMenuItem("Подключение к серверу");
        connectServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        connectServer.addActionListener(e -> onConnectToServer());
        connectServer.setIconTextGap(0);
        menu.add(connectServer);

        JMenuItem disconnectServer = new JMenuItem("Отключение от сервера");
        disconnectServer.addActionListener(e -> onDisconnectFromServer());
        disconnectServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        disconnectServer.setIconTextGap(0);
        disconnectServer.setEnabled(false); //not able for users
        menu.add(disconnectServer);

        menu.setIconTextGap(0);
        return menu;
    }

    private void updateStatusLabel() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            Runnable runnable = () -> {
                statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.PARTLY_CONNECTED));
                statusLabel.setToolTipText("Сервер подключен");

                if (session == null) {
                    Runnable getSessionRunnable = () -> {
                        session = ClientBackgroundService.getInstance().getSession();
                        initFtp();
                    };
                    SwingUtilities.invokeLater(getSessionRunnable);
                } else {
                    if (session.isConnected()) {
                        statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.CONNECTED));
                        statusLabel.setToolTipText(statusLabel.getToolTipText() + " | БД подключена");
                    } else {
                        statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.PARTLY_CONNECTED));
                        statusLabel.setToolTipText(statusLabel.getToolTipText() + " | БД не подключена");
                    }
                }

                setButtonsEnabled(true);
                unlockTabsAndUIs();
            };
            SwingUtilities.invokeLater(runnable);
        } else {
            Runnable runnable = () -> {
                statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.DISCONNECTED));
                statusLabel.setToolTipText("Сервер отключен");

                if (session != null) {
                    if (session.isConnected()) {
                        try {
                            session.disconnect();
                            session = null;
                        } catch (Exception e) {
                            log.warn("Could not disconnect", e);
                        }

                        statusLabel.setIcon(getResizedStatusImage(ConnectionStatus.DISCONNECTED));
                        statusLabel.setToolTipText(statusLabel.getToolTipText() + " | БД отключена");
                    }
                }

                setButtonsEnabled(false);
                unlockTabsAndUIs();
                unlockAdminTools(false);
                if (currentUser != null) {
                    onLogin();
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    private void setEnabledTab(int i, boolean enabled) {
        clientMainTabbedPane.setEnabledAt(i, enabled);
        if (clientMainTabbedPane.getSelectedIndex() == i && !enabled) {
            clientMainTabbedPane.setSelectedIndex(0);
        }
    }

    private void unlockTabsAndUIs() {
        try {
            unlockTabs();
            unlockUIsForAdmins();
            unlockUIsForConnection();
        } catch (Exception e) {
            log.warn("Could not unlock all tabs and UIs", e);
        }
    }

    private void unlockUIsForConnection() {
        //-----------------
        boolean isConnected = ClientBackgroundService.getInstance().isConnected();
        unlockRestoreFromDBjMenuItem(isConnected);
    }

    private void unlockRestoreFromDBjMenuItem(boolean isConnected) {
        JMenuBar jMenuBar = getJMenuBar();
        JMenu menu = jMenuBar.getMenu(1);
        JMenuItem item = menu.getItem(0);
        item.setEnabled(isConnected);
    }

    private void unlockUIsForAdmins() {
        boolean isConnected = ClientBackgroundService.getInstance().isConnected();
        if (isConnected) {
            if (currentUser != null) {
                setEnabledUis(currentUser.isActive() && currentUser.isAdmin());
            } else {
                setEnabledUis(false);
            }
        } else {
            setEnabledUis(false);
        }
    }

    private void setEnabledUis(boolean unlock) {
        editDataButton.setEnabled(unlock);
        adminButton.setEnabled(unlock);
    }

    private void unlockTabs() {
        for (int i = 0; i < clientMainTabbedPane.getTabCount(); i++) {
            final Component tabComponentAt = clientMainTabbedPane.getComponentAt(i);
            try {
                AccessPolicy policyTab = (AccessPolicy) tabComponentAt;
                final AccessPolicyManager policyManager = policyTab.getPolicyManager();

                boolean isEditor = currentUser != null && currentUser.isActive() && currentUser.isEditor();
                boolean isAdmin = currentUser != null && currentUser.isActive() && currentUser.isAdmin();
                boolean isConnected = ClientBackgroundService.getInstance().isConnected();

                if (policyManager.isAvailableForEditor() && policyManager.isAvailableForConnectionOnly()) {
                    if (!policyManager.isAvailableOnlyForAdmin()) {
                        setEnabledTab(i, isEditor && isConnected);
                    } else {
                        if (isAdmin) {
                            setEnabledTab(i, isConnected);
                        } else {
                            setEnabledTab(i, false);
                        }
                    }
                }
                if (policyManager.isAvailableForEditor() && !policyManager.isAvailableForConnectionOnly()) {
                    if (!policyManager.isAvailableOnlyForAdmin()) {
                        setEnabledTab(i, isEditor);
                    } else {
                        if (isAdmin) {
                            setEnabledTab(i, true);
                        } else {
                            setEnabledTab(i, false);
                        }
                    }
                }

                if (!policyManager.isAvailableForEditor() && policyManager.isAvailableForConnectionOnly()) {
                    if (!policyManager.isAvailableOnlyForAdmin()) {
                        setEnabledTab(i, isConnected);
                    } else {
                        if (isAdmin) {
                            setEnabledTab(i, isConnected);
                        } else {
                            setEnabledTab(i, false);
                        }
                    }
                }

                if (!policyManager.isAvailableForEditor() && !policyManager.isAvailableForConnectionOnly()) {
                    if (!policyManager.isAvailableOnlyForAdmin()) {
                        setEnabledTab(i, true);
                    } else {
                        if (isAdmin) {
                            setEnabledTab(i, true);
                        } else {
                            setEnabledTab(i, false);
                        }
                    }
                }

                policyTab.setUIEnabled(isEditor || isAdmin);
            } catch (Exception ignore) {/*NOP*/}
        }
    }

    private void onUsernameInfo() {
        if (currentUser != null) {
            UserInfoWindow userInfoWindow = new UserInfoWindow(currentUser);
            userInfoWindow.pack();
            userInfoWindow.setLocation(FrameUtils.getFrameOnCenter(this, userInfoWindow));
            userInfoWindow.setVisible(true);
        }
    }

    private void initListeners() {
        initWindowListeners();

        initMainTabListeners();

        initUserListeners();

    }

    private void initUserListeners() {
        loginButton.addActionListener(e -> onLogin());
        usernameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onUsernameInfo();
            }
            //
            ///**      * Method generated by IntelliJ IDEA GUI Designer
        });
    }

    private void initMainTabListeners() {
        viewDetailListButton.addActionListener(e -> onViewDetailList(true));
        viewDetailListButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    onViewDetailList(false);
                }
            }
            //
            ///**      * Method generated by IntelliJ IDEA GUI Designer
        });

        noticeListViewButton.addActionListener(e -> onListNoticeInfo(true));
        noticeListViewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    onListNoticeInfo(false);
                }
            }
            //
            ///**      * Method generated by IntelliJ IDEA GUI Designer
        });

        editDataButton.addActionListener(e -> onEditDataButton(true));
        editDataButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    onEditDataButton(false);
                }
            }
        });
    }

    private void onOpenFileMenu() {
        System.out.println("Open file menu is not supported yet");
    }

    private void onSaveFileMenu() {
        System.out.println("onSaveFileMenu is not supported yet");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private void onConnectionSettings() {
        ClientConfigurationWindow configurationWindow = new ClientConfigurationWindow();
        configurationWindow.setLocation(FrameUtils.getFrameOnCenter(this, configurationWindow));
        configurationWindow.setVisible(true);
    }

    private void onConnectToServer() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            JOptionPane.showMessageDialog(this,
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

    private ConstantsEntity getConstantsEntity(ConstantsService service) {
        if (maxUnlockedSeconds == 0) {
            ConstantsEntity constant = null;
            if (currentUser.isAdmin()) {
                constant = service.getConstantByKey(DaoConstants.USER_ADMIN_TIMEOUT);
            } else if (currentUser.isEditor()) {
                constant = service.getConstantByKey(DaoConstants.USER_EDITOR_TIMEOUT);
            }
            try {
                if (constant != null) {
                    maxUnlockedSeconds = Integer.valueOf(constant.getValue());
                }
            } catch (Exception ignore) {
            }
            return constant;
        } else {
            final ConstantsEntity constantsEntity = new ConstantsEntity();
            constantsEntity.setValue(maxUnlockedSeconds + "");
            return constantsEntity;
        }
    }

    private void onDisconnectFromFtp() {
        if (ftpUtils != null) {
            try {
                ftpUtils.disconnect();
            } catch (Exception e) {
                log.warn("Could not disconnect from ftp server: " + ftpUtils, e);
            }
        }
    }

    public void notifyConnectionError() {
        JOptionPane.showMessageDialog(this,
                "Не установлена связь с сервером, ошибка. Звоните фиксикам.",
                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
    }

    public void addTab(String title, Icon icon, JPanel panel, boolean select) {
        clientMainTabbedPane.addTab(title, new ImageIcon(CommonUtils.getScaledImage(CommonUtils.iconToImage(icon), 12, 12)), panel);
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        if (select) {
            clientMainTabbedPane.setSelectedIndex(index);
        }
    }

    private void initFtp() {
        try {
            if (session != null) {
                ftpUtils = FtpUtils.getInstance();

                String url = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_CONNECTION_URL_KEY).getValue();
                String username = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_ACCESS_USERNAME_KEY).getValue();
                String password = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_ACCESS_PASSWORD_KEY).getValue();
                String postfix = new ConstantsServiceImpl(new ConstantsDaoImpl(session)).getConstantByKey(DaoConstants.BLOB_LOCATION_POSTFIX_KEY).getValue();
                log.info("Creating ftp connection at: " + url + " user: " + username + " password: " + password.length() + " postfix: " + postfix);
                ftpUtils.connect(url, username, password);

                ftpUtils.setPostfix(postfix);
            }
        } catch (Exception e) {
            log.warn("Can not init ftp again", e);
        }
    }

    private ImageIcon getResizedStatusImage(ConnectionStatus status) {
        try {
            switch (status) {
                case UNKNOWN:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/gray12.png")));
                case PARTLY_CONNECTED:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/orange12.png")));
                case CONNECTED:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/green12.png")));
                case DISCONNECTED:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/red12.png")));
                default:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/gray12.png")));
            }
        } catch (Throwable t) {
            log.warn("Could not set icons for status label for status: " + status, t);
            return null;
        }
    }

    private void setButtonsEnabled(final boolean enabled) {

        adminButton.setEnabled(enabled);

        viewDetailListButton.setEnabled(enabled);
        noticeListViewButton.setEnabled(enabled);
        editDataButton.setEnabled(enabled);

    }

    private void closeCurrentTab() {
        int selectedIndex = clientMainTabbedPane.getSelectedIndex();
        if (selectedIndex > 0) {

            try {
                Transactional transactional = (Transactional) clientMainTabbedPane.getComponentAt(selectedIndex);
                transactional.rollbackTransaction();
            } catch (ClassCastException e) {
                clientMainTabbedPane.remove(selectedIndex);
            } catch (Exception e) {
                log.warn("Could not close tab at: {} and close transaction", selectedIndex);
                clientMainTabbedPane.remove(selectedIndex);
            }

        }
    }

    public void closeTab(Component component) {
        clientMainTabbedPane.remove(component);
        final int selectedIndex = clientMainTabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            clientMainTabbedPane.getComponentAt(selectedIndex).requestFocus();
        }
    }

    private void unlockAdminTools(boolean unlock) {
        adminButton.setEnabled(unlock);
    }

    private void onRestoreFromDb() {
        ClientBackgroundService.getInstance().refreshSession(session);
        //todo refresh all GUIs //use interface to update data for each panel
    }

    private void initWindowListeners() {
        Component c = this;
        addWindowListener(new WindowAdapter() {
            private ArrayList<Transactional> getTransactionalTabs() {
                ArrayList<Transactional> result = new ArrayList<>();
                for (int i = 0; i < clientMainTabbedPane.getTabCount(); i++) {
                    try {
                        Component component = clientMainTabbedPane.getComponentAt(i);
                        Transactional transactional = (Transactional) component;
                        result.add(transactional);
                    } catch (ClassCastException ignore) {
                        /*NOP*/
                    }
                }
                return result;
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (getTransactionalTabs().isEmpty()) {
                    dispose();
                } else {
                    ArrayList<Transactional> transactionalTabList = getTransactionalTabs();
                    for (Transactional transactional : transactionalTabList) {
                        if (ClientBackgroundService.getInstance().isConnected()) {
                            if (currentUser != null) {
                                transactional.rollbackTransaction();
                            } else {
                                JOptionPane.showMessageDialog(c, "Невозможно закрыть окно, пока существует транзакционная вкладка\n" +
                                        "Войдите в систему, чтобы закрыть её.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            transactional.rollbackTransaction();
                        }
                    }
                    if (getTransactionalTabs().isEmpty()) {
                        dispose();
                    }
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    log.debug("Closing client window, saving location and dimension");
                    ClientSettingsManager.getInstance().setClientMainWindowLocation(getLocation());
                    ClientSettingsManager.getInstance().setClientMainWindowDimension(getSize());
                    log.debug("Successfully saved client window location and dimension");
                } catch (IOException e1) {
                    log.warn("Could not save client window location or dimension", e);
                }
            }
        });
    }

    private void onViewDetailList(final boolean select) {
        final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/tree/unitOpened.png")));
        log.debug("Adding ViewDetailList tab");
        try {
            updateMessage("/img/gui/animated/sync.gif", "Открываем просмотр вложенности");
            Runnable runnable = () -> {
                addTab("Просмотр вложенности", icon, new DetailListPanel(), select);
                updateMessage(null, null);
            };
            new Thread(runnable).start();

            if (currentUser != null) {
                if (currentUser.isActive()) {
                    unlockTabsAndUIs();
                } else {
                    unlockButtonIconUpdate(false);
                }
            } else {
                unlockTabsAndUIs();
            }
        } catch (Throwable e) {
            log.warn("Could not add ViewDetailList tab", e);
        }
    }

    @Override
    public void dispose() {
        onDisconnectFromServer();
        onDisconnectFromFtp();
        uiUpdateTimer.stop();
        unlockUiTimer.stop();

        session.close();
        super.dispose();
    }

    private void onLogin() {
        log.debug("Used onLogin. Current user is: {}", currentUser);
        if (currentUser == null) {
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
            loginWindow.setVisible(true);
            UsersEntity user = loginWindow.getAuthorizedUser();
            if (user != null) {
                if (user.isActive()) {
                    if (user.isEditor() || user.isAdmin()) {
                        currentUser = user;
                        unlockButtonIconUpdate(true);
                        unlockTabsAndUIs();
                        try {
                            ClientBackgroundService.getInstance().userLogin(currentUser);
                        } catch (Throwable e) {
                            log.warn("Could not notify server, that user {} logged in.", currentUser.getUsername());
                        }
                    } else {
                        log.warn("Current user is admin: {}, is editor: {}, access denied.", currentUser.isAdmin(), currentUser.isEditor());
                        JOptionPane.showMessageDialog(this, "Вы должны быть администратором или редактором.",
                                "Доступ запрещен", JOptionPane.WARNING_MESSAGE);
                    }

                    if (user.isAdmin()) {
                        unlockAdminTools(true);
                    }

                } else {
                    log.warn("Current user is active: {}, access denied.", currentUser.isActive());
                    JOptionPane.showMessageDialog(this,
                            "Ваш аккаунт отключён, вы не можете продолжить. Обратитесь к администратору.",
                            "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                log.debug("Locking UI, authorized user is null");
                unlockButtonIconUpdate(false);
                unlockAdminTools(false);
                unlockTabsAndUIs();
            }
        } else {
            lockUI();
        }
    }

    private void onEditDataButton(boolean select) {
        try {
            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/databaseEdit16.png")));
            addTab("Редактирование данных", icon, new EditDataPanel(), select);
        } catch (IllegalStateException e) {
            log.warn("User tried to add transactional tab ({}), but transaction is already active", EditDataPanel.class.getName(), e);
            JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                    "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onListNoticeInfo(boolean select) {
        List<NoticeEntity> noticeEntities = new NoticeServiceImpl(new NoticeDaoImpl(session)).listNotices();
        Collections.sort(noticeEntities);
        NoticeInfoPanel noticeInfoPanel = new NoticeInfoPanel(session, noticeEntities);
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/notice16.png")));

        updateMessage("/img/gui/animated/sync.gif", "Открываем информацию о извещениях");
        Runnable runnable = () -> {
            addTab("Информация о извещениях", icon, noticeInfoPanel, select);
            updateMessage(null, null);
        };
        new Thread(runnable).start();
    }

    private void initGui() {
        setContentPane(contentPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_CLIENT);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/clientLogo.png")));
        setMinimumSize(ClientConstants.MAIN_WINDOW_DEFAULT_DIMENSION);

        initHomeTab();
        initConnectionLabels();

        initMainMenuBar();
        initListeners();
        initKeyBindings();
        unlockButtonIconUpdate(false);

        CommonWindowUtils.initApplicationVersionArea(applicationVersionTextField);

        updateMessage(null, null);

        pack();
        setSize(ClientSettingsManager.getInstance().getClientMainWindowDimension());
    }

    public void updateMessage(final String imagePath, final String message) {
        if (!blockingMessage) {
            updateMessageIcon(imagePath);
            updateMessageText(message);
        }
    }

    public void updateMessageText(String message) {
        if (!blockingMessage) {
            messageLabel.setText(message);
        }
    }

    public void updateMessageIcon(String imagePath) {
        if (!blockingMessage) {
            if (imagePath != null) {
                try {
                    messageLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imagePath))
                            .getScaledInstance(16, 16, 1)));
                } catch (Exception e) {
                    messageLabel.setIcon(null);
                }
            } else {
                messageLabel.setIcon(null);
            }
        }
    }

    private void lockUI() {
        if (currentUser != null) {
            log.debug("User called locking UI, {} login out.", currentUser.getUsername());
        }
        unlockButtonIconUpdate(false);
        unlockAdminTools(false);
        unlockTabsAndUIs();
        try {
            ClientBackgroundService.getInstance().userLogout(currentUser);
        } catch (IOException e) {
            log.warn("Could not notify server, that user {} logged out.", currentUser.getUsername());
        }
        currentUser = null;
    }

    private void initMainMenuBar() {
        JMenuBar mainMenuBar = new JMenuBar();

        JMenu fileMenu = getFileMenu();
        fileMenu.setEnabled(false);//unsupported yet!

        mainMenuBar.add(fileMenu);

        JMenu editMenu = getEditMenu();
        mainMenuBar.add(editMenu);

        JMenu connectionMenu = getConnectionMenu();
        mainMenuBar.add(connectionMenu);

        JMenu about = getHelpMenu();
        mainMenuBar.add(about);

        setJMenuBar(mainMenuBar);
    }

    private JMenu getHelpMenu() {
        JMenu help = new JMenu("Справка");

        JMenuItem userGuide = new JMenuItem("Руководство пользователя");
        userGuide.addActionListener(e -> {
            CommonUtils.openClientGuide();
        });
        help.add(userGuide);

        JMenuItem about = new JMenuItem("О приложении");
        about.addActionListener(e -> {
            AboutApplicationWindow window = new AboutApplicationWindow();
            window.setLocation(FrameUtils.getFrameOnCenter(this, window));
            window.setVisible(true);
        });

        help.add(about);

        return help;
    }

    public void blockMessage() {
        this.blockingMessage = true;
        new Timer(5 * 1000, e -> {
            blockingMessage = false;
            updateMessage(null, null);
        }).start();
    }

    public UsersEntity getCurrentUser() {
        return currentUser;
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
        clientMainTabbedPane.setTabLayoutPolicy(1);
        contentPane.add(clientMainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane.addTab("Главная", mainPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Функционал"));
        viewDetailListButton = new JButton();
        viewDetailListButton.setAutoscrolls(false);
        viewDetailListButton.setBorderPainted(false);
        viewDetailListButton.setContentAreaFilled(true);
        viewDetailListButton.setRequestFocusEnabled(true);
        viewDetailListButton.setRolloverEnabled(true);
        viewDetailListButton.setText("Просмотр вложенности узлов");
        viewDetailListButton.setMnemonic('П');
        viewDetailListButton.setDisplayedMnemonicIndex(0);
        panel1.add(viewDetailListButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editDataButton = new JButton();
        editDataButton.setAutoscrolls(false);
        editDataButton.setBorderPainted(false);
        editDataButton.setContentAreaFilled(true);
        editDataButton.setEnabled(false);
        editDataButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        editDataButton.setRequestFocusEnabled(true);
        editDataButton.setRolloverEnabled(true);
        editDataButton.setText("Редактирование данных");
        editDataButton.setMnemonic('Р');
        editDataButton.setDisplayedMnemonicIndex(0);
        editDataButton.setToolTipText("Редактирование списков констант");
        panel1.add(editDataButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adminButton = new JButton();
        adminButton.setAutoscrolls(false);
        adminButton.setBorderPainted(false);
        adminButton.setContentAreaFilled(true);
        adminButton.setEnabled(false);
        adminButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        adminButton.setRequestFocusEnabled(true);
        adminButton.setRolloverEnabled(true);
        adminButton.setText("Администрирование");
        adminButton.setMnemonic('А');
        adminButton.setDisplayedMnemonicIndex(0);
        adminButton.setToolTipText("Редактирование данных");
        panel1.add(adminButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeListViewButton = new JButton();
        noticeListViewButton.setAutoscrolls(false);
        noticeListViewButton.setBorderPainted(false);
        noticeListViewButton.setContentAreaFilled(true);
        noticeListViewButton.setRequestFocusEnabled(true);
        noticeListViewButton.setRolloverEnabled(true);
        noticeListViewButton.setText("Просмотр извещений");
        noticeListViewButton.setMnemonic('М');
        noticeListViewButton.setDisplayedMnemonicIndex(4);
        panel1.add(noticeListViewButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 4, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/red12.png")));
        panel3.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applicationVersionTextField = new JTextField();
        applicationVersionTextField.setBackground(new Color(-855310));
        applicationVersionTextField.setEditable(false);
        applicationVersionTextField.setForeground(new Color(-7697782));
        panel3.add(applicationVersionTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameLabel = new JLabel();
        Font usernameLabelFont = this.$$$getFont$$$(null, Font.BOLD, 11, usernameLabel.getFont());
        if (usernameLabelFont != null) usernameLabel.setFont(usernameLabelFont);
        usernameLabel.setText(" ");
        panel4.add(usernameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loginButton = new JButton();
        loginButton.setBorderPainted(false);
        loginButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        loginButton.setMargin(new Insets(2, 2, 2, 2));
        loginButton.setOpaque(false);
        loginButton.setText("");
        loginButton.setToolTipText("Блокировка / разблокировка элементов управления (CTRL + L)");
        panel4.add(loginButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        messageLabel = new JLabel();
        messageLabel.setText("сообщение");
        panel5.add(messageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        contentPane.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        UNKNOWN, PARTLY_CONNECTED, CONNECTED, DISCONNECTED
    }
}
