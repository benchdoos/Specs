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
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.dao.ConstantsDaoImpl;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
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
    private JPanel detailListPanel;
    private JButton loginButton;
    private JLabel usernameLabel;
    private JTree mainTree;
    private JTextField searchTextField;
    private JLabel dbStatusLabel;
    private JLabel numberLabel;
    private JLabel finishedWeightLabel;
    private JLabel workpieceWeightLabel;
    private JLabel isActiveLabel;
    private JButton editButton;
    private JLabel detailIconLabel;
    private JPanel detailInfoPanel;
    private JLabel titleLabel;
    private JLabel techProcessLabel;
    private JLabel unitLabel;
    private JButton noticeInfoButton;
    private JButton editDataButton;
    private JButton noticeListViewButton;
    private JButton refreshSessionButton;
    private JButton button1;
    private Timer uiUpdateTimer;

    private void initGui() {
        setMinimumSize(new Dimension(860, 480));

        initConnectionLabels();

        removeUnusedPanes();
        initMainMenuBar();
        initListeners();
        initMainTree();
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

    private void removeUnusedPanes() {
        clientMainTabbedPane.remove(detailListPanel);
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

    public ClientMainWindow() {
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_CLIENT);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/clientLogo.png")));

        initGui();
        initTimer();


        pack();

    }

    private void initListeners() {
        viewDetailListButton.addActionListener(e -> onViewDetailList());
        noticeInfoButton.addActionListener(e -> onNoticeInfo());
        loginButton.addActionListener(e -> onLogin());
        usernameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onUsernameInfo();
            }
        });
        noticeListViewButton.addActionListener(e -> onListNoticeInfo());


        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            String searchText = "";
            final Timer searchTimer = new Timer(1000, e -> {
                if (!searchText.isEmpty()) {
                    searchText = searchText.replace(",", ".");
                    System.out.println("timer works: " + searchText);
                    fillMainTreeBySearch(searchText);
                } else {
                    fillMainTreeFully();
                }
            });

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }
        });

        refreshSessionButton.addActionListener(e -> onRefreshSession());

    }

    private void unlock(boolean status) {
        if (status) {
            loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/unlocked.png"))));
        } else {
            loginButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/locked.png"))));
        }
        usernameLabel.setText(status ? (currentUser != null ? currentUser.getUsername() : "") : "");

        editButton.setEnabled(status);
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

    private void onViewDetailList() {
        clientMainTabbedPane.add("Просмотр вложенности", detailListPanel);
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        clientMainTabbedPane.setSelectedIndex(index);

        updateDetailInfoPanelWithEmptyEntity();

        fillMainTreeFully();
    }

    private void onNoticeInfo() {
        DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent != null) {
            if (lastSelectedPathComponent.getUserObject() instanceof DetailEntity) {
                DetailEntity entityFromTree = (DetailEntity) lastSelectedPathComponent.getUserObject();

                DetailListService detailListService = new DetailListServiceImpl(new DetailListDaoImpl(session));
                List<DetailListEntity> list;

                if (detailListService.getDetailListByParent(entityFromTree).size() > 0) {
                    list = detailListService.getDetailListByParent(entityFromTree);
                } else {
                    list = detailListService.getDetailListByChild(entityFromTree);

                }
                if (list != null) {
                    if (list.size() > 0) {
                        List<NoticeEntity> noticeEntities = getUniqueNoticeList(list);
                        NoticeInfoWindow noticeInfoWindow = new NoticeInfoWindow(session, noticeEntities);
                        noticeInfoWindow.setLocation(FrameUtils.getFrameOnCenter(this, noticeInfoWindow));
                        noticeInfoWindow.setVisible(true);
                    }
                }
            } else {
                log.warn("Not DetailEntity: " + mainTree.getLastSelectedPathComponent().getClass().getName());
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
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ваш аккаунт отключён, вы не можете продолжить. Обратитесь к администратору.",
                            "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                unlock(false);
            }
        } else {
            unlock(false);
            currentUser = null;
        }
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

    private void fillMainTreeBySearch(String searchText) {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).getDetailListTreeByDetailList(service.getDetailListBySearch(searchText))));
        }
    }

    private void fillMainTreeFully() {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).getDetailListFullTree(service.listDetailLists())));
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private void updateDetailInfoPanel(DetailEntity selectedComponent) {
        updateDetailInfoPanelWithEmptyEntity();
        if (selectedComponent != null) {
            updateDetailInfoPanelWithEntity(selectedComponent);
        }
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

    private void updateDetailInfoPanelWithEmptyEntity() {
        numberLabel.setText("");
        titleLabel.setText("");

        detailIconLabel.setIcon(null);
        if (detailIconLabel.getMouseListeners().length > 0) {
            for (MouseListener listener : detailIconLabel.getMouseListeners()) {
                detailIconLabel.removeMouseListener(listener);
            }
        }

        unitLabel.setText("");
        finishedWeightLabel.setText("");
        workpieceWeightLabel.setText("");

        techProcessLabel.setToolTipText("");
        techProcessLabel.setText("");

        isActiveLabel.setText("");
    }

    private void initMainTree() {
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            private Color backgroundSelectionColor = new Color(0, 120, 215);
            private Color backgroundNonSelectionColor = new Color(255, 255, 255);

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) { //todo optimize this!!!

                if (selected) {
                    setBackground(backgroundSelectionColor);
                } else {
                    setBackground(backgroundNonSelectionColor);
                }

                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.getUserObject() instanceof DetailEntity) {
                        DetailEntity child = (DetailEntity) node.getUserObject();
                        if (row >= 0) {
                            if (tree.getPathForRow(row) != null) {
                                Object[] pathForRow = tree.getPathForRow(row).getParentPath().getPath();
                                if (pathForRow.length > 1) {
                                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) pathForRow[pathForRow.length - 1];
                                    DetailEntity parent = (DetailEntity) mutableTreeNode.getUserObject();

                                    MainWindowUtils mainWindowUtils = new MainWindowUtils(session);

                                    List<DetailListEntity> result = mainWindowUtils.getDetailListEntitiesByParentAndChild(parent, child);

                                    setOpaque(true);

                                    if (result.size() > 0) {
                                        DetailListEntity detailListEntity = mainWindowUtils.getLatestDetailListEntity(result);
                                        if (detailListEntity.isInterchangeableNode()) {
                                            if (!selected) {
                                                setBackground(Color.GRAY.brighter());
                                            } else {
                                                setBackground(backgroundSelectionColor);
                                            }
                                            setToolTipText("Взаимозаменяемая деталь");
                                        }

                                        if (!detailListEntity.isActive()) {
                                            if (!selected) {
                                                setBackground(Color.RED.darker());
                                            } else {
                                                setBackground(backgroundSelectionColor);
                                            }
                                        }

                                        String value1 = child.getCode() + " (" + detailListEntity.getQuantity() + ") " + child.getDetailTitleByDetailTitleId().getTitle();
                                        return super.getTreeCellRendererComponent(tree, value1, selected, true, leaf, row, hasFocus);
                                    }
                                }
                            }
                        }
                        return super.getTreeCellRendererComponent(tree,
                                child.getCode() + " " + child.getDetailTitleByDetailTitleId().getTitle(), selected, expanded, leaf, row, hasFocus); //spaces fixes
                        // issue when (detailListEntity.getQuantity()) does not work... fix it some how????
                    }
                }
                return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                FontMetrics fm = getFontMetrics(getFont());
                char[] chars = getText().toCharArray();

                int w = getIconTextGap() + 32;
                for (char ch : chars) {
                    w += fm.charWidth(ch);
                }
                w += getText().length();
                dim.width = w;
                return dim;
            }
        };
        Icon closedIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon openIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon leafIcon = new ImageIcon(getClass().getResource("/img/gui/tree/detail.png"));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);

        mainTree.setCellRenderer(renderer);

        mainTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();

            if (lastSelectedPathComponent != null) {
                if (lastSelectedPathComponent.getUserObject() instanceof DetailEntity) {
                    DetailEntity entityFromTree = (DetailEntity) lastSelectedPathComponent.getUserObject();

                    DetailServiceImpl detailService = new DetailServiceImpl(new DetailDaoImpl(session));
                    DetailEntity loadedEntity = detailService.getDetailById(entityFromTree.getId());

                    updateDetailInfoPanel(loadedEntity);
                } else {
                    log.warn("Not DetailEntity: " + mainTree.getLastSelectedPathComponent().getClass().getName());
                }
            }
        });

        mainTree.setExpandsSelectedPaths(true);

        fillMainTreeFully();
    }

    private List<NoticeEntity> getUniqueNoticeList(List<DetailListEntity> list) {
        List<NoticeEntity> result = new ArrayList<>();
        List<String> noticeList = new ArrayList<>(); //todo fix this shit
        for (DetailListEntity entity : list) {
            NoticeEntity noticeEntity = entity.getNoticeByNoticeId();

            if (!noticeList.contains(noticeEntity.getNumber())) {
                noticeList.add(noticeEntity.getNumber());
                result.add(noticeEntity);
            }
        }
        Collections.sort(result);
        return result;
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

    private void updateDetailInfoPanelWithEntity(DetailEntity selectedComponent) {
        numberLabel.setText(CommonUtils.substring(25, selectedComponent.getCode()));

        if (selectedComponent.getDetailTitleByDetailTitleId() != null) {
            String title = selectedComponent.getDetailTitleByDetailTitleId().getTitle();
            titleLabel.setToolTipText(title);
            titleLabel.setText(CommonUtils.substring(25, title));
        }

        unitLabel.setText(Boolean.toString(selectedComponent.isUnit())
                .replace("false", "нет").replace("true", "да"));

        if (selectedComponent.getFinishedWeight() != null) {
            finishedWeightLabel.setText((Double.toString(selectedComponent.getFinishedWeight())));
        }

        if (selectedComponent.getWorkpieceWeight() != null) {
            workpieceWeightLabel.setText(Double.toString(selectedComponent.getWorkpieceWeight()));
        }

        FtpUtils ftp = FtpUtils.getInstance();
        BufferedImage image = ftp.getImage(selectedComponent.getId());

        if (image != null) {
            BufferedImage scaledImage = Scalr.resize(image, 128);
            detailIconLabel.setIcon(new ImageIcon(scaledImage));
            detailIconLabel.setText("");
            detailIconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    FrameUtils.onShowImage(ClientMainWindow.super.getOwner(), image, "Изображение " + selectedComponent.getCode());
                }
            });
        } else {
            detailIconLabel.setIcon(null);
            detailIconLabel.setText("Нет изображения");
        }

        if (selectedComponent.getTechProcessByTechProcessId() != null) {
            String process = selectedComponent.getTechProcessByTechProcessId().getProcess();
            techProcessLabel.setToolTipText(process);
            techProcessLabel.setText(CommonUtils.substring(25, process));
        }

        isActiveLabel.setText(Boolean.toString(!selectedComponent.isActive())
                .replace("false", "нет").replace("true", "да"));
    }

    private void onRefreshSession() {
        //todo add expansion for selected path
        ClientBackgroundService.getInstance().refreshSession();
        fillMainTreeFully();
        searchTextField.setText("");
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
        viewDetailListButton.setHorizontalTextPosition(2);
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
        editDataButton.setHorizontalTextPosition(2);
        editDataButton.setMargin(new Insets(0, 0, 0, 0));
        editDataButton.setOpaque(false);
        editDataButton.setRequestFocusEnabled(true);
        editDataButton.setRolloverEnabled(true);
        editDataButton.setText("Редактирование данных");
        editDataButton.setToolTipText("Редактирование данных");
        panel1.add(editDataButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JButton button2 = new JButton();
        button2.setAutoscrolls(false);
        button2.setBorderPainted(false);
        button2.setContentAreaFilled(true);
        button2.setForeground(new Color(-16765749));
        button2.setMargin(new Insets(0, 0, 0, 0));
        button2.setOpaque(false);
        button2.setRequestFocusEnabled(true);
        button2.setRolloverEnabled(true);
        button2.setText("Администрирование");
        button2.setToolTipText("Редактирование данных");
        panel1.add(button2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeListViewButton = new JButton();
        noticeListViewButton.setAutoscrolls(false);
        noticeListViewButton.setBorderPainted(false);
        noticeListViewButton.setContentAreaFilled(true);
        noticeListViewButton.setForeground(new Color(-16765749));
        noticeListViewButton.setHorizontalTextPosition(2);
        noticeListViewButton.setMargin(new Insets(0, 0, 0, 0));
        noticeListViewButton.setOpaque(false);
        noticeListViewButton.setRequestFocusEnabled(true);
        noticeListViewButton.setRolloverEnabled(true);
        noticeListViewButton.setText("Просмотр извещений");
        panel1.add(noticeListViewButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailListPanel = new JPanel();
        detailListPanel.setLayout(new GridLayoutManager(12, 2, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane.addTab("Просмотр вложенности", detailListPanel);
        detailInfoPanel = new JPanel();
        detailInfoPanel.setLayout(new GridLayoutManager(13, 6, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel.add(detailInfoPanel, new GridConstraints(0, 1, 11, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchTextField = new JTextField();
        detailInfoPanel.add(searchTextField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Поиск:");
        detailInfoPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Индекс:");
        detailInfoPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Наименование:");
        detailInfoPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Узел:");
        detailInfoPanel.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Масса готовой детали:");
        detailInfoPanel.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Масса заготовки:");
        detailInfoPanel.add(label6, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Аннулирована:");
        detailInfoPanel.add(label7, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Технический процесс:");
        detailInfoPanel.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberLabel = new JLabel();
        numberLabel.setText("    ");
        detailInfoPanel.add(numberLabel, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleLabel = new JLabel();
        titleLabel.setText("    ");
        detailInfoPanel.add(titleLabel, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("    ");
        detailInfoPanel.add(unitLabel, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightLabel = new JLabel();
        finishedWeightLabel.setText("    ");
        detailInfoPanel.add(finishedWeightLabel, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightLabel = new JLabel();
        workpieceWeightLabel.setText("    ");
        detailInfoPanel.add(workpieceWeightLabel, new GridConstraints(7, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessLabel = new JLabel();
        techProcessLabel.setText("     ");
        detailInfoPanel.add(techProcessLabel, new GridConstraints(8, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveLabel = new JLabel();
        isActiveLabel.setText("   ");
        detailInfoPanel.add(isActiveLabel, new GridConstraints(9, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailIconLabel = new JLabel();
        detailIconLabel.setForeground(new Color(-3684409));
        detailIconLabel.setHorizontalAlignment(0);
        detailIconLabel.setHorizontalTextPosition(0);
        detailIconLabel.setText("Нет изображения");
        detailInfoPanel.add(detailIconLabel, new GridConstraints(2, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Последнее извещение:");
        detailInfoPanel.add(label9, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeInfoButton = new JButton();
        noticeInfoButton.setBorderPainted(false);
        noticeInfoButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/info.png")));
        noticeInfoButton.setIconTextGap(0);
        noticeInfoButton.setMargin(new Insets(2, 2, 2, 2));
        noticeInfoButton.setOpaque(false);
        noticeInfoButton.setText("");
        detailInfoPanel.add(noticeInfoButton, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        detailInfoPanel.add(spacer4, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        detailInfoPanel.add(spacer5, new GridConstraints(3, 5, 10, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        detailInfoPanel.add(spacer6, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel.add(panel2, new GridConstraints(0, 0, 11, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(300, -1), new Dimension(300, -1), null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Узлы"));
        mainTree = new JTree();
        mainTree.setFocusable(true);
        mainTree.setRootVisible(false);
        scrollPane1.setViewportView(mainTree);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(0);
        panel2.add(toolBar1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        refreshSessionButton = new JButton();
        refreshSessionButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        refreshSessionButton.setMargin(new Insets(2, 2, 2, 2));
        refreshSessionButton.setText("");
        refreshSessionButton.setToolTipText("Обновить данные с базы данных");
        toolBar1.add(refreshSessionButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        button1 = new JButton();
        button1.setEnabled(false);
        button1.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        button1.setText("");
        toolBar1.add(button1);
        editButton = new JButton();
        editButton.setEnabled(false);
        editButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editButton.setText("");
        toolBar1.add(editButton);
        final Spacer spacer7 = new Spacer();
        detailListPanel.add(spacer7, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel3.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        usernameLabel = new JLabel();
        Font usernameLabelFont = this.$$$getFont$$$(null, Font.BOLD, 11, usernameLabel.getFont());
        if (usernameLabelFont != null) usernameLabel.setFont(usernameLabelFont);
        usernameLabel.setText("");
        panel3.add(usernameLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loginButton = new JButton();
        loginButton.setBorderPainted(false);
        loginButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        loginButton.setMargin(new Insets(2, 2, 2, 2));
        loginButton.setOpaque(false);
        loginButton.setText("");
        panel3.add(loginButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 4, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serverStatusLabel = new JLabel();
        panel4.add(serverStatusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dbStatusLabel = new JLabel();
        dbStatusLabel.setText("");
        panel4.add(dbStatusLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        contentPane.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(searchTextField);
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
