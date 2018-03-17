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
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;


public class ClientMainWindow extends JFrame {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;
    private JPanel contentPane;
    private JTabbedPane clientMainTabbedPane;
    private JPanel mainPanel;
    private JLabel serverStatusLabel;
    private JButton viewDetailListButton;
    private JButton viewSearchButton;
    private JPanel detailListPanel;
    private JButton loginButton;
    private JLabel usernameLabel;
    private JTree mainTree;
    private JTextField searchTextField;
    private JLabel dbStatusLabel;
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
        setMinimumSize(new Dimension(640, 480));

        removeUnusedPanes();
        initMainMenuBar();
        initListeners();
        initMainTree();
    }

    private void initTimer() {
        uiUpdateTimer = new Timer(1000, e -> updateUserInterface());
        if (!uiUpdateTimer.isRunning()) {
            uiUpdateTimer.start();
        }
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

    private void initListeners() {
        viewDetailListButton.addActionListener(e -> onViewDetailList());
    }

    private void initMainTree() {
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.getUserObject() instanceof DetailEntity) {
                        DetailEntity detailEntity = (DetailEntity) node.getUserObject();
                        return super.getTreeCellRendererComponent(tree, detailEntity.getNumber(), sel, expanded, leaf, row, hasFocus);
                    } else {
                        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                    }
                } else {
                    return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                }
            }
        };
        Icon closedIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon openIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon leafIcon = new ImageIcon(getClass().getResource("/img/gui/tree/detail.png"));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);

        mainTree.setCellRenderer(renderer);

        fillMainTree();
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

        MenuItem connectServer = new MenuItem("Подключение к серверу");
        connectServer.addActionListener(e -> onConnectToServer());
        menu.add(connectServer);

        MenuItem disconnectServer = new MenuItem("Отключение от сервера");
        disconnectServer.addActionListener(e -> onDisconnectFromServer());
        menu.add(disconnectServer);

        return menu;
    }

    private void fillMainTree() {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            List<DetailListEntity> list = service.listDetailLists();
            mainTree.setModel(new DefaultTreeModel(new MainWindowUtils().getDetailListFullTree(session)));
        }
    }

    private void updateUserInterface() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            serverStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/green12.png")));
            serverStatusLabel.setToolTipText("Сервер подключен");

            if (session == null) {
                session = ClientBackgroundService.getInstance().getSession();
            } else {
                if (session.isConnected()) {
                    dbStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/green12.png")));
                    dbStatusLabel.setToolTipText("БД подключена");
                }
            }
        } else {
            serverStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/red12.png")));
            serverStatusLabel.setToolTipText("Сервер отключен");

            if (session != null) {
                if (session.isConnected()) {
                    session.disconnect();
                    session = null;

                    dbStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/red12.png")));
                    dbStatusLabel.setToolTipText("БД отключена");
                }
            }
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

    private void onViewDetailList() {
        clientMainTabbedPane.add("Просмотр вложенности", detailListPanel);
        int index = clientMainTabbedPane.getTabCount() - 1;
        clientMainTabbedPane.setTabComponentAt(index, new ButtonTabComponent(clientMainTabbedPane));
        clientMainTabbedPane.setSelectedIndex(index);
        fillMainTree();
    }

    public void notifyConnectionRefused() {
        JOptionPane.showMessageDialog(this,
                "Не установлена связь с сервером, проверьте настройки.",
                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        onDisconnectFromServer();
        uiUpdateTimer.stop();
        super.dispose();
    }

    private DefaultMutableTreeNode getDetailListNodeByParent(DetailEntity detailEntity) {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(detailEntity.getNumber());
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
        List<DetailListEntity> list = service.getDetailListByParent(detailEntity.getNumber());
        for (DetailListEntity entity : list) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity.getDetailByChildDetailId());
            List<DetailListEntity> child = service.getDetailListByParent(entity.getDetailByChildDetailId());
            if (child != null) {
                if (child.size() > 0) {
                    DefaultMutableTreeNode childTree = getDetailListNodeByParent(entity.getDetailByChildDetailId());
                    node.add(childTree);
                }
            }
            result.add(node);
        }
        return result;
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
        panel1.add(viewDetailListButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        viewSearchButton = new JButton();
        viewSearchButton.setAutoscrolls(false);
        viewSearchButton.setBorderPainted(false);
        viewSearchButton.setContentAreaFilled(true);
        viewSearchButton.setForeground(new Color(-16765749));
        viewSearchButton.setMargin(new Insets(0, 0, 0, 0));
        viewSearchButton.setOpaque(false);
        viewSearchButton.setRequestFocusEnabled(true);
        viewSearchButton.setRolloverEnabled(true);
        viewSearchButton.setText("Поиск детали");
        panel1.add(viewSearchButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JButton button1 = new JButton();
        button1.setAutoscrolls(false);
        button1.setBorderPainted(false);
        button1.setContentAreaFilled(true);
        button1.setForeground(new Color(-16765749));
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setOpaque(false);
        button1.setRequestFocusEnabled(true);
        button1.setRolloverEnabled(true);
        button1.setText("Редактирование данных");
        button1.setToolTipText("Редактирование данных");
        panel1.add(button1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        detailListPanel = new JPanel();
        detailListPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        clientMainTabbedPane.addTab("Просмотр вложенности", detailListPanel);
        searchTextField = new JTextField();
        detailListPanel.add(searchTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Поиск:");
        detailListPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        detailListPanel.add(scrollPane1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(300, -1), new Dimension(300, -1), null, 0, false));
        mainTree = new JTree();
        mainTree.setFocusable(true);
        mainTree.setRootVisible(false);
        scrollPane1.setViewportView(mainTree);
        final Spacer spacer4 = new Spacer();
        detailListPanel.add(spacer4, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel2.add(spacer5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setText("");
        panel2.add(usernameLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loginButton = new JButton();
        loginButton.setText("Вход");
        panel2.add(loginButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverStatusLabel = new JLabel();
        serverStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/gray12.png")));
        panel2.add(serverStatusLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Статус соединения:");
        panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        dbStatusLabel = new JLabel();
        dbStatusLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/status/gray12.png")));
        dbStatusLabel.setText("");
        panel2.add(dbStatusLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        contentPane.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(searchTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
