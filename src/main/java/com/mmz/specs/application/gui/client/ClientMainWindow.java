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
import com.mmz.specs.application.core.server.service.ClientConnection;
import com.mmz.specs.application.core.server.service.ClientConnectionImpl;
import com.mmz.specs.application.gui.common.AboutApplicationWindow;
import com.mmz.specs.application.gui.common.ButtonTabComponent;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.UserInfoWindow;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.gui.panels.*;
import com.mmz.specs.application.managers.ClientSettingsManager;
import com.mmz.specs.application.utils.*;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.io.SPTreeIOManager;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.mmz.specs.application.core.ApplicationConstants.TMP_IMAGE_FOLDER;


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
    private Timer connectionManagerTimer;
    private int unlockedSeconds;
    private int maxUnlockedSeconds;
    private boolean blockingMessage = false;
    private final Timer blockingMessageTimer = new Timer(5 * 1000, e -> {
        blockingMessage = false;
        updateMessage(null, null);
    });
    private boolean manualDisconnect;

    public ClientMainWindow() {
        try {
            initGui();
            initTimers();
        } catch (Throwable e) {
            log.warn("Could not init Client main window", e);
        }

    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> closeWindow(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

    public void resetUnlockedSeconds() {
        unlockedSeconds = 0;
    }

    private void updateUnlockUiTimer() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            if (currentUser != null) {
                try {
                    ConstantsEntity constant = getConstantsEntity();
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
        openFileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menu.add(openFileMenu);

        /*JMenuItem saveFileMenu = new JMenuItem("Сохранить");
        saveFileMenu.addActionListener(e -> onSaveFileMenu());
        saveFileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menu.add(saveFileMenu);*/

        return menu;
    }

    private void manageConnections() {
        boolean isServerOnline = ClientBackgroundService.getInstance().isConnected();
        boolean isSessionOnline = session != null && session.isConnected();
        boolean isFtpOnline = ftpUtils != null && ftpUtils.isConnected();
        if (!manualDisconnect) {
            if (isServerOnline) {
                if (isSessionOnline) {
                    if (!isFtpOnline) {
                        ftpUtils = null;
                        initFtp();
                    }
                } else {
                    session = null;
                    session = ClientBackgroundService.getInstance().getSession();
                }
            } else {
                session = null;
                ftpUtils = null;
                createConnection();
            }
        }
    }

    private JMenu getEditMenu() {
        final JMenu edit = new JMenu("Правка");
        edit.setEnabled(false);
        return edit;
    }

    private void updateStatusLabel() {
        boolean isServerOnline = ClientBackgroundService.getInstance().isConnected();
        boolean isFtpOnline = ftpUtils != null && ftpUtils.isConnected();
        boolean isSessionOnline = ClientBackgroundService.getInstance().isDBAvailable();
        String serverInfo = isServerOnline ? "Сервер онлайн" : "Сервер оффлайн";
        String ftpInfo = isFtpOnline ? "FTP онлайн" : "FTP оффлайн";
        String sessionInfo = isSessionOnline ? "БД онлайн" : "БД оффлайн";
        statusLabel.setToolTipText(serverInfo + " | " + ftpInfo + " | " + sessionInfo);

        updateStatusLabelIcon(isServerOnline, isFtpOnline, isSessionOnline);

        setButtonsEnabled(isServerOnline && isSessionOnline);
        unlockTabsAndUIs();
    }

    private void setEnabledTab(int i, boolean enabled) {
        clientMainTabbedPane.setEnabledAt(i, enabled);
        if (clientMainTabbedPane.getSelectedIndex() == i && !enabled) {
            clientMainTabbedPane.setSelectedIndex(0);
        }
    }

    private void updateStatusLabelIcon(boolean isServerOnline, boolean isFtpOnline, boolean isSessionOnline) {
        ConnectionStatus status = isServerOnline && isFtpOnline && isSessionOnline ? ConnectionStatus.CONNECTED :
                isServerOnline ? ConnectionStatus.PARTLY_CONNECTED : ConnectionStatus.DISCONNECTED;
        statusLabel.setIcon(getResizedStatusImage(status));
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            createConnection();
        }
        super.setVisible(b);
    }

    private void unlockTabsAndUIs() {
        try {
            unlockTabs();
            unlockUIsForAdmins();
        } catch (Exception e) {
            log.warn("Could not unlock all tabs and UIs", e);
        }
    }

    private void unlockUIsForAdmins() {
        boolean isConnected = ClientBackgroundService.getInstance().isConnected()
                && ClientBackgroundService.getInstance().isDBAvailable();
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
            ClientConnection connection = new ClientConnectionImpl();
            connection.setUser(currentUser);
            UserInfoWindow userInfoWindow = new UserInfoWindow(connection, true);
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
        });
    }

    private void onOpenFileMenu() {
        log.debug("Showing open menu");
        JFileChooser chooser = new JFileChooser(new File(ApplicationConstants.USER_HOME_LOCATION));
        chooser.setDialogTitle("Открытие");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);

        FileFilter fileFilter = new FileNameExtensionFilter("Слепок базы данных",
                SupportedExtensionsConstants.EXPORT_TREE_EXTENSION.replace(".", ""));
        chooser.setFileFilter(fileFilter);
        int returnValue = chooser.showDialog(this, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File file : files) {
                if (file.exists()) {
                    if (file.isFile()) {
                        openFile(file);
                    }
                }
            }
        }
    }

    private void onSaveFileMenu() {
        System.out.println("onSaveFileMenu is not supported yet");
    }

    private void onConnectionSettings() {
        ClientConfigurationWindow configurationWindow = new ClientConfigurationWindow();
        configurationWindow.setLocation(FrameUtils.getFrameOnCenter(this, configurationWindow));
        configurationWindow.setVisible(true);
    }

    private void createConnection() {
        updateMessage("/img/gui/animated/connection.gif", "Устанавливаем соединение с сервером");
        new Thread(() -> {
            try {
                if (!ClientBackgroundService.getInstance().isConnected()) {
                    ClientBackgroundService.getInstance().createConnection();
                }
            } catch (IOException ignore) {
            }
            if (ClientBackgroundService.getInstance().isConnected()) {
                session = ClientBackgroundService.getInstance().getSession();
//                initFtp();
                updateMessage("/img/gui/animated/connection_completed.gif", "Соединение установлено");
                blockMessage();
            }


        }).start();
    }

    private void onConnectToServer() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Сервер уже подключён", "Уведомление",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            try {
                ClientBackgroundService.getInstance().createConnection();
            } catch (IOException e) {
                log.warn("Could not create a socket connection to server", e);
                JOptionPane.showMessageDialog(this,
                        "Не установлена связь с сервером, проверьте настройки.",
                        "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ConstantsEntity getConstantsEntity() {
        ConstantsService service = new ConstantsServiceImpl(session);
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

    private void onDisconnectFromServer() {
        try {
            log.info("Trying to disconnect from server");
            ClientBackgroundService.getInstance().closeConnection();
            log.info("Successfully disconnected from server");
        } catch (IOException e) {
            log.warn("Could not disconnect from server socket ", e);
            JOptionPane.showMessageDialog(this,
                    "Не удалось отключиться от сервера.\n" + e.getLocalizedMessage(),
                    "Ошибка отключения", JOptionPane.ERROR_MESSAGE);
        }

        onDisconnectFromFtp();

        SessionUtils.closeSessionSilently(session);
    }

    public void addTab(String title, Icon icon, JPanel panel, boolean select) {
        clientMainTabbedPane.addTab(title, new ImageIcon(CommonUtils.getScaledImage(CommonUtils.iconToImage(icon), 12, 12)), panel);
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        if (select) {
            clientMainTabbedPane.setSelectedIndex(index);
        }
    }

    private synchronized void initFtp() {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    ftpUtils = FtpUtils.getInstance();

                    Properties constants = CommonUtils.getConstantsToProperties(session);
                    String url = constants.getProperty(DaoConstants.BLOB_CONNECTION_URL_KEY);
                    String username = constants.getProperty(DaoConstants.BLOB_ACCESS_USERNAME_KEY);
                    String password = constants.getProperty(DaoConstants.BLOB_ACCESS_PASSWORD_KEY);
                    String postfix = constants.getProperty(DaoConstants.BLOB_LOCATION_POSTFIX_KEY);

                    if (ftpUtils != null) {
                        new Thread(() -> {
                            ftpUtils.connect(url, username, password);
                            ftpUtils.setPostfix(postfix);
                        }).start();
                    }
                }
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
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/orange16.png")));
                case CONNECTED:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/green16.png")));
                case DISCONNECTED:
                    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/status/red16.png")));
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

    private void initWindowListeners() {
        Component c = this;
        addWindowListener(new WindowAdapter() {


            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }


            @Override
            public void windowClosed(WindowEvent e) {
                saveWindowLocation();
            }

            private void saveWindowLocation() {
                try {
                    log.debug("Closing client window, saving location and dimension");
                    ClientSettingsManager.getInstance().setClientMainWindowLocation(getLocation());

                    final boolean extended = ((ClientMainWindow) c).getExtendedState() == JFrame.MAXIMIZED_BOTH;
                    ClientSettingsManager.getInstance().setClientMainWindowExtended(extended);

                    if (!extended) {
                        ClientSettingsManager.getInstance().setClientMainWindowDimension(getSize());
                    }

                    log.debug("Successfully saved client window location and dimension");
                } catch (IOException e1) {
                    log.warn("Could not save client window location or dimension", e1);
                }
            }
        });
    }

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

    private void closeWindow() {
        try {
            if (getTransactionalTabs().isEmpty()) {
                dispose();
            } else {
                ArrayList<Transactional> transactionalTabList = getTransactionalTabs();
                for (Transactional transactional : transactionalTabList) {
                    if (ClientBackgroundService.getInstance().isConnected()) {
                        if (currentUser != null) {
                            transactional.rollbackTransaction();
                        } else {
                            JOptionPane.showMessageDialog(this, "Невозможно закрыть окно, пока существует транзакционная вкладка\n" +
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
        } catch (HeadlessException e1) {
            log.warn("Could not close window properly... closing anyway", e1);
            dispose();
        }
    }

    private void onViewDetailList(final boolean select) {
        final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/tree/unitOpened.png")));
        log.debug("Adding ViewDetailList tab");
        try {
            updateMessage("/img/gui/animated/sync.gif", "Открываем просмотр вложенности");
            Runnable runnable = () -> {
                addTab("Просмотр вложенности", icon, new DetailListViewPanel(), select);
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

    private void onEditDataButton(boolean select) {
        final MainWindowUtils mainWindowUtils = new MainWindowUtils(session);
        mainWindowUtils.setClientMainWindow(this);
        mainWindowUtils.updateMessage("/img/gui/animated/sync.gif", "Открываем редактирование данных...");
        new Thread(() -> {
            try {
                ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/databaseEdit16.png")));
                addTab("Редактирование данных", icon, new EditDataPanel(), select);
                mainWindowUtils.updateMessage(null, null);
            } catch (IllegalStateException e) {
                log.warn("User tried to add transactional tab ({}), but transaction is already active", EditDataPanel.class.getName(), e);
                JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                        "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
            }
        }).start();
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
        });

        noticeListViewButton.addActionListener(e -> {
            updateMessage("/img/gui/animated/sync.gif", "Открываем информацию о извещениях");
            new Thread(() -> onListNoticeInfo(true)).start();
        });
        noticeListViewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    updateMessage("/img/gui/animated/sync.gif", "Открываем информацию о извещениях");
                    new Thread(() -> onListNoticeInfo(false)).start();
                }
            }
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

        initStatusEaster();

        pack();
        setSize(ClientSettingsManager.getInstance().getClientMainWindowDimension());
        setExtendedState(ClientSettingsManager.getInstance().isClientMainWindowExtended() ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
    }

    private void initStatusEaster() {
        Component component = this;
        statusLabel.addMouseListener(new MouseAdapter() {
            ArrayList<Integer> events = new ArrayList<>();
            ArrayList<Integer> easter = initEasterList();
            Timer timer = new Timer(1000, e -> events.clear());

            private ArrayList<Integer> initEasterList() {
                ArrayList<Integer> result = new ArrayList<>();
                result.add(MouseEvent.BUTTON1);
                result.add(MouseEvent.BUTTON1);
                result.add(MouseEvent.BUTTON1);
                result.add(MouseEvent.BUTTON2);
                result.add(MouseEvent.BUTTON2);
                return result;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                timer.setRepeats(false);
                timer.restart();
                events.add(e.getButton());
                if (events.equals(easter)) {
                    JOptionPane.showMessageDialog(component, "Привет, Юленька!\n" +
                                    "Юляшу не обижать!", "Ня-Ня Пасхалочка", JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/easter/putin.gif"))));
                }
            }
        });
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

    private void onDisconnectFromFtp() {
        if (ftpUtils != null) {
            try {
                ftpUtils.disconnect();
                ftpUtils = null;
            } catch (Exception e) {
                log.warn("Could not disconnect from ftp server: " + ftpUtils, e);
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

        mainMenuBar.add(fileMenu);

        JMenu editMenu = getEditMenu();
        mainMenuBar.add(editMenu);

        JMenu settingsMenu = getSettingsMenu();
        mainMenuBar.add(settingsMenu);

        JMenu about = getHelpMenu();
        mainMenuBar.add(about);

        setJMenuBar(mainMenuBar);
    }

    private JMenu getHelpMenu() {
        JMenu help = new JMenu("Справка");

        JMenuItem userGuide = new JMenuItem("Руководство пользователя");
        userGuide.addActionListener(e -> CommonUtils.openClientGuide());
        userGuide.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/guide.png"))));

        help.add(userGuide);

        JMenuItem about = new JMenuItem("О приложении");
        about.addActionListener(e -> {
            AboutApplicationWindow window = new AboutApplicationWindow();
            window.setLocation(FrameUtils.getFrameOnCenter(this, window));
            window.setVisible(true);
        });
        about.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/clientLogo.png"))));
        help.add(about);

        return help;
    }

    public void blockMessage() {
        this.blockingMessage = true;
        blockingMessageTimer.setRepeats(false);
        blockingMessageTimer.restart();
    }

    public UsersEntity getCurrentUser() {
        return currentUser;
    }

    private void onListNoticeInfo(boolean select) {
        List<NoticeEntity> noticeEntities = new NoticeServiceImpl(session).listNotices();
        Collections.sort(noticeEntities);
        NoticeInfoPanel noticeInfoPanel = new NoticeInfoPanel(session, noticeEntities);
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/notice16.png")));
        addTab("Информация о извещениях", icon, noticeInfoPanel, select);
        updateMessage(null, null);
    }

    public void updateMessageIcon(String imagePath) {
        if (!blockingMessage) {
            if (imagePath != null) {
                try {
                    messageLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(imagePath))
                            .getScaledInstance(16, 16, 1)));
                } catch (Exception e) {
                    log.warn("Could not set image for message label", e);
                    messageLabel.setIcon(null);
                }
            } else {
                messageLabel.setIcon(null);
            }
        }
    }

    private void removeTmpImages() {
        log.debug("Deleting TMP (from clipboard) images on: " + TMP_IMAGE_FOLDER);
        File file = new File(TMP_IMAGE_FOLDER);
        if (file.exists()) {
            CommonUtils.deleteFolder(file);
        }
    }

    @Override
    public void dispose() {
        try {
            onDisconnectFromServer();
            removeTmpImages();

            uiUpdateTimer.stop();
            unlockUiTimer.stop();
            connectionManagerTimer.stop();
            blockingMessageTimer.stop();
        } catch (Exception e) {
            log.warn("Could not stop something", e);
            log.warn("Exiting system permanently, (if some of timers is alive)");
            System.exit(-1);
        }

        super.dispose();
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

        connectionManagerTimer = new Timer(2000, e -> manageConnections());
        if (!connectionManagerTimer.isRunning()) {
            connectionManagerTimer.start();
        }
    }

    private void onLogin() {
        log.debug("User wants to log in/out. Current user is: {}", currentUser);
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
                            log.warn("Could not notify server, that user {} logged in.", user.getUsername());
                        }
                    } else {
                        log.warn("Current user ({}) is admin: {}, is editor: {}, access denied.", user.getUsername(), user.isAdmin(), user.isEditor());
                        JOptionPane.showMessageDialog(this, "Вы должны быть администратором или редактором.",
                                "Доступ запрещен", JOptionPane.WARNING_MESSAGE);
                    }

                    if (user.isAdmin()) {
                        unlockAdminTools(true);
                    }

                } else {
                    log.warn("Current user is active: {}, access denied.", user.isActive());
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

    private JMenu getSettingsMenu() {
        JMenu menu = new JMenu("Параметры");

        JMenuItem settings = getSettingsMenuItem();
        menu.add(settings);

        JMenu connectionMenu = getConnectionMenu();
        menu.add(connectionMenu);

        return menu;
    }

    private JMenuItem getSettingsMenuItem() {
        JMenuItem settings = new JMenuItem("Настройки");
        settings.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/settings.png"))));
        settings.setIconTextGap(0);
        settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK));
        settings.addActionListener(e -> onClientSettings());
        return settings;
    }

    private void onClientSettings() {
        ClientSettingsWindow clientSettingsWindow = new ClientSettingsWindow();
        clientSettingsWindow.setLocation(FrameUtils.getFrameOnCenter(this, clientSettingsWindow));
        clientSettingsWindow.setVisible(true);
    }

    private JMenu getConnectionMenu() {
        JMenu menu = new JMenu("Подключение");
        menu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/connectionSettings.png"))));
        menu.setIconTextGap(0);

        JMenuItem connectionSettings = new JMenuItem("Настройки подключения");
        connectionSettings.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/connectionSettings.png"))));
        connectionSettings.setIconTextGap(0);
        connectionSettings.addActionListener(e -> onConnectionSettings());
        menu.add(connectionSettings);

        JMenuItem connectServer = new JMenuItem("Подключение к серверу");
        connectServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        connectServer.addActionListener(e -> {
            manualDisconnect = false;
            onConnectToServer();
        });
        connectServer.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/connect.png"))));
        connectServer.setIconTextGap(0);
        menu.add(connectServer);

        JMenuItem disconnectServer = new JMenuItem("Отключение от сервера");
        disconnectServer.addActionListener(e -> {
            manualDisconnect = true;
            onDisconnectFromServer();
        });
        disconnectServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        disconnectServer.setIconTextGap(0);
        disconnectServer.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/disconnect.png"))));
        disconnectServer.setEnabled(CommonUtils.getCurrentInetAddress().equalsIgnoreCase("doos-k52jv"));
        menu.add(disconnectServer);

        menu.setIconTextGap(0);
        return menu;
    }

    public void openFile(File file) {
        log.info("Got file to open: {}", file);
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/extensions/sptFileFormat.png")));

        FileViewPanel panel = new FileViewPanel();

        ProgressManager progressManager = new ProgressManager();

        final OpeningFileProcessPanel processPanel = new OpeningFileProcessPanel(file, progressManager);

        addTab(CommonUtils.getSmallFileName(file).toLowerCase(), icon, panel, true);
        panel.setContent(processPanel);

        SPTreeIOManager spTreeIOManager = new SPTreeIOManager(progressManager);
        try {
            final File folder = (File) spTreeIOManager.importData(file);
            progressManager.setTotalProgress(3);
            log.debug("Opening SPT file at  {}", folder);

            SptFileViewPanel sptFileViewPanel = new SptFileViewPanel(folder);
            progressManager.setTotalProgress(4);

            processPanel.prepareToClose();
            panel.setContent(sptFileViewPanel);
        } catch (IOException e) {
            log.warn("Could not import spt file: {}", file, e);
            processPanel.prepareToClose();
            closeTab(panel);
            JOptionPane.showMessageDialog(this, "Не удалось открыть файл " + file.getName(),
                    "Ошибка открытия", JOptionPane.ERROR_MESSAGE);
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
        contentPane.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane = new JTabbedPane();
        clientMainTabbedPane.setTabLayoutPolicy(1);
        contentPane.add(clientMainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane.addTab("Главная", mainPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Функционал"));
        adminButton = new JButton();
        adminButton.setAutoscrolls(false);
        adminButton.setBorderPainted(false);
        adminButton.setContentAreaFilled(true);
        adminButton.setEnabled(false);
        adminButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/adminTools.png")));
        adminButton.setRequestFocusEnabled(true);
        adminButton.setRolloverEnabled(true);
        adminButton.setText("Администрирование");
        adminButton.setMnemonic('А');
        adminButton.setDisplayedMnemonicIndex(0);
        adminButton.setToolTipText("Администрирование сервера");
        panel1.add(adminButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        viewDetailListButton = new JButton();
        viewDetailListButton.setAutoscrolls(false);
        viewDetailListButton.setHorizontalAlignment(2);
        viewDetailListButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png")));
        viewDetailListButton.setText("Просмотр узлов");
        viewDetailListButton.setMnemonic('П');
        viewDetailListButton.setDisplayedMnemonicIndex(0);
        panel2.add(viewDetailListButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editDataButton = new JButton();
        editDataButton.setAutoscrolls(false);
        editDataButton.setEnabled(false);
        editDataButton.setHorizontalAlignment(2);
        editDataButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/databaseEdit16.png")));
        editDataButton.setText("Редактирование данных");
        editDataButton.setMnemonic('Р');
        editDataButton.setDisplayedMnemonicIndex(0);
        editDataButton.setToolTipText("Редактирование списков констант");
        panel2.add(editDataButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeListViewButton = new JButton();
        noticeListViewButton.setAutoscrolls(false);
        noticeListViewButton.setHorizontalAlignment(2);
        noticeListViewButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/notice16.png")));
        noticeListViewButton.setText("Просмотр извещений");
        noticeListViewButton.setMnemonic('М');
        noticeListViewButton.setDisplayedMnemonicIndex(4);
        panel2.add(noticeListViewButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        label1.setText("");
        label1.setToolTipText("Доступно только администраторам");
        panel2.add(label1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        label2.setText("");
        label2.setToolTipText("Доступно только администраторам");
        panel1.add(label2, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 4, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/red16.png")));
        panel4.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applicationVersionTextField = new JTextField();
        applicationVersionTextField.setBackground(new Color(-855310));
        applicationVersionTextField.setEditable(false);
        applicationVersionTextField.setForeground(new Color(-7697782));
        panel4.add(applicationVersionTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameLabel = new JLabel();
        Font usernameLabelFont = this.$$$getFont$$$(null, Font.BOLD, 11, usernameLabel.getFont());
        if (usernameLabelFont != null) usernameLabel.setFont(usernameLabelFont);
        usernameLabel.setText(" ");
        panel5.add(usernameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, -1), null, null, 0, false));
        loginButton = new JButton();
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        loginButton.setMargin(new Insets(2, 2, 2, 2));
        loginButton.setOpaque(false);
        loginButton.setText("");
        loginButton.setToolTipText("Блокировка / разблокировка элементов управления (CTRL + L)");
        panel5.add(loginButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        messageLabel = new JLabel();
        messageLabel.setText("сообщение");
        panel6.add(messageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
