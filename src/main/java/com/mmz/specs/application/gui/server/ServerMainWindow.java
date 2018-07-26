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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerConstants;
import com.mmz.specs.application.core.server.ServerException;
import com.mmz.specs.application.core.server.service.*;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.application.gui.common.UserInfoWindow;
import com.mmz.specs.application.managers.CommonSettingsManager;
import com.mmz.specs.application.managers.ServerSettingsManager;
import com.mmz.specs.application.utils.*;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.validation.UserTypeValidationException;
import com.mmz.specs.application.utils.validation.UsernameValidationException;
import com.mmz.specs.application.utils.validation.ValidationUtils;
import com.mmz.specs.connection.DaoConstants;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.ConstantsEntity;
import com.mmz.specs.model.UserTypeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import oshi.software.os.NetworkParams;

import javax.persistence.OptimisticLockException;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.*;

public class ServerMainWindow extends JFrame {

    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static int lastActionTimeAgoCounter = 0;
    private final String DEGREE = "\u00b0";
    private final long logicalProcessorCount = SystemMonitoringInfoUtils.getProcessor().getLogicalProcessorCount();
    private final long physicalProcessorCount = SystemMonitoringInfoUtils.getProcessor().getPhysicalProcessorCount();
    private final long runtimeMaxMemory = getRuntimeMaxMemory();
    private final Date serverStartDate = Calendar.getInstance().getTime();
    private final ArrayList<ServerLogMessage> logMessages = new ArrayList<>(10000);
    private boolean isMonitoringActive = true;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel monitorPanel;
    private JList<ClientConnection> onlineUserList;
    private JLabel onlineUsersCountLabel;
    private JLabel threadsCount;
    private JLabel serverOnlineTimeLabel;
    private JButton buttonForceUserDisconnect;
    private JButton powerServerButton;
    private JButton buttonAdminLock;
    private JTextPane logTextPane;
    private JPanel controlPanel;
    private JPanel logPanel;
    private JList<UsersEntity> registeredUserList;
    private JButton resetPasswordButton;
    private JTextField nameTextField;
    private JTextField patronymicTextField;
    private JTextField surnameTextField;
    private JTextField usernameTextField;
    private JCheckBox isEditorCheckBox;
    private JCheckBox isAdminCheckBox;
    private JCheckBox isActiveCheckBox;
    private JComboBox<UserTypeEntity> userTypeComboBox;
    private JButton saveUserButton;
    private JTabbedPane adminPane;
    private JButton addUserButton;
    private JPanel adminUsersPanel;
    private JButton buttonUserInfo;
    private JButton openLogFolderButton;
    private JLabel userIdLabel;
    private JLabel onlineUsersCount2;
    private JButton saveSettingsToButton;
    private JTextField connectionUrlTextField;
    private JTextField connectionLoginTextField;
    private JPasswordField connectionPasswordField;
    private JLabel osInfoLabel;
    private JLabel usedProcessMemoryInfoLabel;
    private JLabel usedCpuBySystemInfoLabel;
    private JLabel temperatureInfoLabel;
    private JLabel networkNameInfoLabel;
    private JLabel ipAddressInfoLabel;
    private JLabel totalMemoryInfoLabel;
    private JLabel serverSocketPortInfoLabel;
    private JLabel jvmInfoLabel;
    private JTable constantsTable;
    private JButton updateServerConstantsButton;
    private JLabel usedCpuByApplicationInfoLabel;
    private JPanel graphicsDataPanel;
    private JPanel adminServerPanel;
    private JPanel adminSettingsPanel;
    private JPanel adminConstantsPanel;
    private JButton constantsRefreshButton;
    private JButton refreshRegisteredUsersButton;
    private JPanel currentUserPanel;
    private JLabel authorizedUserName;
    private JButton buttonForceAllUsersDisconnect;
    private JButton removeUserButton;
    private JButton switchMonitoringButton;
    private JScrollPane logTextScrollPane;
    private JTextField applicationVersionArea;
    private JButton openClientGuideButton;
    private JButton openServerGuideButton;
    private JPanel graphicsPanel;
    final ServerMonitoringGraphics serverMonitoringGraphics = new ServerMonitoringGraphics();
    private JCheckBox ramServerCheckBox;
    private JCheckBox cpuServerCheckBox;
    private JCheckBox temperatureCpuServerCheckBox;
    private JCheckBox usersCheckBox;
    private boolean serverOnlineCountLabelCounterShow = true;
    private JPanel onlyAdminTabsList[];
    private Timer monitorUiUpdateTimer;
    private Timer userActionsUpdateTimer;
    private boolean isWindowClosing = false;
    private UsersEntity currentLoggedInUser = null;
    private Session session;
    private JCheckBox cpuSystemCheckBox;


    public ServerMainWindow() {
        session = ServerDBConnectionPool.getInstance().getSession();
        initGui();

        initKeyBindings();

        pack();
        setMinimumSize(getSize());

        setLocation(FrameUtils.getFrameOnCenter(null, this));

        setUnlocked(false);

        initThreads();
    }

    private void initListeners() {

        powerServerButton.addActionListener(e -> onPowerServerButton());

        buttonUserInfo.addActionListener(e -> onUserInfoButton());

        buttonForceUserDisconnect.addActionListener(e -> onForceDisconnectUserButton());

        buttonForceAllUsersDisconnect.addActionListener(e -> onForceDisconnectAllUsersButton());

        buttonAdminLock.addActionListener(e -> onButtonAdminLock());

        serverOnlineTimeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onServerOnlineCountLabel();
            }
        });


        logTextPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem clear = new JMenuItem("Очистить");
                    clear.addActionListener(e1 -> ServerMonitoringBackgroundService.getInstance().clearServerLogMessages());
                    menu.add(clear);
                    logTextPane.add(menu);
                }
            }
        });

        openLogFolderButton.addActionListener(e -> onOpenLogFolder());

        openClientGuideButton.addActionListener(e -> CommonUtils.openClientGuide());

        openServerGuideButton.addActionListener(e -> CommonUtils.openServerGuide());


        constantsRefreshButton.addActionListener(e -> fillAdminConstantsPanel());

        updateServerConstantsButton.addActionListener(e -> onSaveAdminConstantsPanel());

        refreshRegisteredUsersButton.addActionListener(e -> onUpdateUserListButton());

        addUserButton.addActionListener(e -> onAddNewUserButton());

        removeUserButton.addActionListener(e -> onRemoveUserButton());
        switchMonitoringButton.addActionListener(e -> onSwitchMonitoring());

        initUserInfoPanelListeners();

        initGraphicsPanelListeners();
    }

    private void initGraphicsPanelListeners() {
        cpuSystemCheckBox.addActionListener(e -> serverMonitoringGraphics.setCpuSystemShow(cpuSystemCheckBox.isSelected()));
        temperatureCpuServerCheckBox.addActionListener(e -> serverMonitoringGraphics.setTemperatureServerShow(temperatureCpuServerCheckBox.isSelected()));
        cpuServerCheckBox.addActionListener(e -> serverMonitoringGraphics.setCpuServerShow(cpuServerCheckBox.isSelected()));
        ramServerCheckBox.addActionListener(e -> serverMonitoringGraphics.setRamServerShow(ramServerCheckBox.isSelected()));
        usersCheckBox.addActionListener(e -> serverMonitoringGraphics.setUsersShow(usersCheckBox.isSelected()));
    }

    private void fillAdminConstantsPanel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && super.isCellEditable(row, column);
            }
        };
        model.addColumn("Параметр");
        model.addColumn("Значение");
        SessionUtils.refreshSession(session, ConstantsEntity.class);
        ConstantsService constantsService = new ConstantsServiceImpl(session);
        List<ConstantsEntity> list = constantsService.listConstants();

        for (ConstantsEntity entity : list) {
            String key = entity.getKey();
            String value = entity.getValue();
            model.addRow(new Object[]{key, value});
        }
        constantsTable.setModel(model);
    }

    private void onUpdateUserListButton() {
        clearCurrentUserPanel();
        fillAdminRegisteredUsersPanel();
        restoreTextFieldsColors();
    }

    private void updateOnlineUsersCount() {
        int onlineUsersCount = ServerBackgroundService.getInstance().getOnlineUsersCount();
        onlineUsersCountLabel.setText(onlineUsersCount + "");
        onlineUsersCount2.setText(onlineUsersCount + "");
    }

    private void updateTotalMemoryLabel() {
        long runtimeTotalMemory = getSystemTotalMemory();
        long runtimeMaxMemory = getRuntimeMaxMemory();
        totalMemoryInfoLabel.setText("JVM: " + runtimeMaxMemory + " МБ. ОЗУ: " + runtimeTotalMemory + " МБ.");
    }

    private void updateNetworkInfoPanel() {
        NetworkParams networkParams = OPERATING_SYSTEM.getNetworkParams();
        networkNameInfoLabel.setText(networkParams.getHostName());
        ipAddressInfoLabel.setText(networkParams.getIpv4DefaultGateway() + " / " + networkParams.getIpv6DefaultGateway());
    }

    private void updateProcessorInfoLabel() {
        ArrayList<Float> cpuLoadValues = ServerMonitoringBackgroundService.getInstance().getCpuLoadValues();
        final int index = cpuLoadValues.size() - 1;
        double cpuLoad = cpuLoadValues.size() > index && index >= 0 ? cpuLoadValues.get(index) : 0;
        String cpuLoadString = CommonUtils.round(cpuLoad, 1) + "%";

        if (cpuLoad >= 60.0d) {
            setWarningMode(usedCpuBySystemInfoLabel, true);
        } else {
            setWarningMode(usedCpuBySystemInfoLabel, false);
        }
        usedCpuBySystemInfoLabel.setText(physicalProcessorCount + " (" + logicalProcessorCount + ") " + cpuLoadString);
    }

    private void updateUsedProcessCpuInfoLabel() {
        ArrayList<Float> cpuLoadByServerValues = ServerMonitoringBackgroundService.getInstance().getCpuLoadByServerValues();

        final int index = cpuLoadByServerValues.size() - 1;
        final double cpuUsageByApplication = cpuLoadByServerValues.size() > index && index >= 0 ? cpuLoadByServerValues.get(index) : 0;

        String processInfo = CommonUtils.round(cpuUsageByApplication, 1) + "%";

        if (cpuUsageByApplication >= 60.0d) {
            setWarningMode(usedCpuByApplicationInfoLabel, true);

        } else {
            setWarningMode(usedCpuByApplicationInfoLabel, false);

        }
        usedCpuByApplicationInfoLabel.setText(processInfo);

    }

    private void setWarningMode(JLabel label, boolean value) {
        if (value) {
            label.setForeground(Color.RED);
            try {
                label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/warningRed12.png"))));
            } catch (NullPointerException e) {
                /*NOP*/
            }
        } else {
            label.setForeground(Color.BLACK);
            label.setIcon(null);
        }
    }

    String getServerOnlineString(long differenceInSeconds) {
        final String MAXIMUM_SUPPORTED_LIFE_TIME = "> 24855д.";
        if (differenceInSeconds >= 0) {
            long seconds = differenceInSeconds % 60;
            long minutes = (differenceInSeconds / 60) % 60;
            long hours = (differenceInSeconds / 60 / 60) % 24;
            long days = (differenceInSeconds / 60 / 60 / 24);
            return days + "д. " + hours + "ч. " + minutes + "м. " + seconds + "с.";
        } else {
            return MAXIMUM_SUPPORTED_LIFE_TIME;
        }
    }

    private void updateSystemInfoLabel() {
        String osInfo = OPERATING_SYSTEM.getManufacturer() + " " + OPERATING_SYSTEM.getFamily()
                + " " + OPERATING_SYSTEM.getVersion() + " x" + SystemUtils.getRealSystemArch();
        osInfoLabel.setText(osInfo);
    }

    private void initGui() {
        setContentPane(contentPane);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_SERVER);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/serverLogo.png")));

        initUserTypeComboBox();

        initLockTimer();

        updateSystemInfoLabel();
        updateNetworkInfoPanel();
        updateTotalMemoryLabel();
        updateJvmInfoLabel();

        updateServerSocketPortInfoLabel();

        initAdminSettingsPanel();

        initAdminConstantsPanel();
        fillAdminConstantsPanel();

        initAdminRegisteredUsersPanel();
        fillAdminRegisteredUsersPanel();

        clearCurrentUserPanel();

        initOnlineUsersList();

        initListeners();

        CommonWindowUtils.initApplicationVersionArea(applicationVersionArea);

    }

    private void updateServerSocketPortInfoLabel() {
        serverSocketPortInfoLabel.setText(ServerConstants.SERVER_DEFAULT_SOCKET_PORT + "");
    }

    private int getAdminLockDelayFromConstants() {
        ConstantsService service = new ConstantsServiceImpl(session);
        ConstantsEntity constant = service.getConstantByKey(DaoConstants.USER_ADMIN_TIMEOUT);
        try {
            int result = Integer.parseInt(constant.getValue());
            if (result < 10) {
                result = DaoConstants.USER_ADMIN_TIMEOUT_MINIMUM;
            } else if (result > DaoConstants.USER_ADMIN_TIMEOUT_MAXIMUM) {
                result = DaoConstants.USER_ADMIN_TIMEOUT_MAXIMUM;
            }
            return result;
        } catch (NumberFormatException e) {
            log.warn("Constant for " + DaoConstants.USER_ADMIN_TIMEOUT + " is not set correctly: " + constant.getValue() + ", setting default");
            constant.setValue(Integer.toString(DaoConstants.USER_ADMIN_TIMEOUT_DEFAULT));
            service.getConstantsDao().getSession().getTransaction().begin();
            service.updateConstant(constant);
            service.getConstantsDao().getSession().getTransaction().commit();
            log.info("Constant for " + DaoConstants.USER_ADMIN_TIMEOUT + " was set: " + constant.getValue());
            return Integer.parseInt(constant.getValue());
        }
    }

    private void updateCurrentSelectedUserInformation(UsersEntity usersEntity) {
        if (usersEntity != null) {
            updateCurrentUserPanel(usersEntity);
        } else {
            clearCurrentUserPanel();
        }
    }

    private void clearCurrentUserPanel() {
        userIdLabel.setText("");

        for (Component component : currentUserPanel.getComponents()) {
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                textField.setText("");
                component.setEnabled(false);
            }
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                checkBox.setSelected(false);
                component.setEnabled(false);
            }

            if (component instanceof JButton) {
                component.setEnabled(false);
            }
        }
        userTypeComboBox.setSelectedItem(null);
        userTypeComboBox.setEnabled(false);

        restoreTextFieldsColors();

    }

    private void initUserTypeComboBox() {
        DefaultComboBoxModel<UserTypeEntity> model = new DefaultComboBoxModel<>();

        UserTypeService userTypeService = new UserTypeServiceImpl(session);
        List<UserTypeEntity> userTypeEntities = userTypeService.listUserTypes();
        for (UserTypeEntity entity : userTypeEntities) {
            model.addElement(entity);
        }

        userTypeComboBox.setModel(model);
        userTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof UserTypeEntity) {
                    UserTypeEntity entity = (UserTypeEntity) value;
                    String name = entity.getName();
                    return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
    }

    private void initLockTimer() {
        int timerTimeout = getAdminLockDelayFromConstants();
        ActionListener listener = e -> lastActionTimeAgoCounter = 0;

        FrameUtils.addActionListenerToAll(contentPane, listener);

        userActionsUpdateTimer = new Timer(1000, e -> {
            if (!isWindowClosing) {
                if (lastActionTimeAgoCounter >= timerTimeout || lastActionTimeAgoCounter < 0) {
                    if (currentLoggedInUser != null) {
                        setUnlocked(false);
                        currentLoggedInUser = null;
                    }
                }
                lastActionTimeAgoCounter++;
            }
        });

        userActionsUpdateTimer.setRepeats(true);
        if (!userActionsUpdateTimer.isRunning()) {
            userActionsUpdateTimer.start();
        }
    }

    private void createServerTrayIcon() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(new ServerIcon(getIconImage()));
        } catch (AWTException e) {
            log.warn("Could not create system icon", e);
        }
    }

    private void onPowerServerButton() {
        ServerBackgroundService.getInstance().stopServerMainBackgroundService();
        if (monitorUiUpdateTimer != null) {
            if (monitorUiUpdateTimer.isRunning()) {
                monitorUiUpdateTimer.stop();
            }
        }

        if (userActionsUpdateTimer != null) {
            if (userActionsUpdateTimer.isRunning()) {
                userActionsUpdateTimer.stop();
            }
        }

        isWindowClosing = true;
        dispose();
    }

    private void initThreads() {
        int MONITORING_TIMER_DELAY = 1000;
        monitorUiUpdateTimer = new Timer(MONITORING_TIMER_DELAY, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateOnlineUsersCount();
                updateOnlineUsersList();

                updateServerOnlineTimeLabel();

                updateActiveThreadCounterLabel();
                updateProcessorInfoLabel();
                updateUsedProcessCpuInfoLabel();
                updateUsedJvmMemoryInfoLabel();

                updateTemperatureInfoLabel();
                createGraphics();

                updateLogTextPane();

            }

            private void updateServerOnlineTimeLabel() {
                long onlineNanoSeconds = ServerMonitoringBackgroundService.getInstance().getServerOnlineTimeInSeconds();
                if (serverOnlineCountLabelCounterShow) {
                    String text = getServerOnlineString(onlineNanoSeconds);
                    serverOnlineTimeLabel.setText(text);
                } else {
                    serverOnlineTimeLabel.setText(serverStartDate.toString());
                }
            }

            private void updateActiveThreadCounterLabel() {
                threadsCount.setText(Integer.toString(getApplicationCurrentThreads()));
            }

            private void updateUsedJvmMemoryInfoLabel() {
                final long runtimeUsedMemory = getRuntimeUsedMemory();
                final long runtimeTotalMemory = getRuntimeTotalMemory();

                String memoryInfo = "JVM: " + runtimeUsedMemory + " / " + runtimeTotalMemory + " МБ. ";

                if (runtimeUsedMemory > (runtimeMaxMemory - 0.2 * runtimeMaxMemory)) {
                    setWarningMode(usedProcessMemoryInfoLabel, true);
                } else {
                    setWarningMode(usedProcessMemoryInfoLabel, false);
                }
                usedProcessMemoryInfoLabel.setText(memoryInfo);
            }

            private void updateTemperatureInfoLabel() {
                ArrayList<Float> cpuTemperatureValue = ServerMonitoringBackgroundService.getInstance().getCpuTemperatureValues();

                final int index = cpuTemperatureValue.size() - 1;
                final double cpuTemperature = cpuTemperatureValue.size() > index && index >= 0 ? cpuTemperatureValue.get(index) : 0;

                if (cpuTemperature > 90.0d) {

                    setWarningMode(temperatureInfoLabel, true);

                } else {
                    setWarningMode(temperatureInfoLabel, false);

                }

                temperatureInfoLabel.setText("ЦП: " + cpuTemperature + " C" + DEGREE);
            }

            private void updateLogTextPane() {
                ArrayList<ServerLogMessage> serverLogMessages = ServerMonitoringBackgroundService.getInstance().getServerLogMessages();
                if (serverLogMessages.size() > logMessages.size()) {
                    int startCaretPosition = logTextPane.getCaretPosition();
                    boolean change = false;

                    if (startCaretPosition == logTextPane.getDocument().getLength()) {
                        change = true;
                    }

                    int messagesToLoad = serverLogMessages.size() - logMessages.size();
                    for (int i = 0; i < messagesToLoad; i++) {
                        int j = messagesToLoad - i;
                        ServerLogMessage message = serverLogMessages.get(serverLogMessages.size() - j);
                        logMessages.add(message);
                        appendTextToTextPane(message);
                    }

                    if (change) {
                        logTextPane.setCaretPosition(logTextPane.getDocument().getLength());
                    }
                } else if (serverLogMessages.size() < logMessages.size()) {
                    logMessages.clear();
                    logTextPane.setText("");
                }
            }

            private void appendTextToTextPane(ServerLogMessage message) {

                StyledDocument doc = logTextPane.getStyledDocument();


                SimpleAttributeSet newMessage = new SimpleAttributeSet();
                if (message.getLevel().equals(ServerLogMessage.ServerLogMessageLevel.SUCCESS)) {
                    StyleConstants.setForeground(newMessage, Color.GREEN.darker().darker());
                } else if (message.getLevel().equals(ServerLogMessage.ServerLogMessageLevel.WARN)) {
                    StyleConstants.setForeground(newMessage, Color.WHITE);
                    StyleConstants.setBackground(newMessage, Color.RED);
                }
                StyleConstants.setBold(newMessage, true);


                try {
                    doc.insertString(doc.getLength(), message.getFormattedMessage() + "\n", newMessage);
                } catch (Exception e) {
                    log.warn("Could not add message", e);
                }
            }


        });
        if (!monitorUiUpdateTimer.isRunning()) {
            monitorUiUpdateTimer.start();
        }
    }

    private void restoreTextFieldsColors() {
        usernameTextField.setBackground(Color.WHITE);
    }

    private void onRemoveUserButton() {
        try (Session session = ServerDBConnectionPool.getInstance().getSession()) {
            if (registeredUserList.getSelectedIndex() >= 0) {
                UsersEntity entity = registeredUserList.getSelectedValue();
                if (entity != null) {
                    NoticeService noticeService = new NoticeServiceImpl(session);
                    if (noticeService.listNoticesByUser(entity).size() > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Нельзя удалять пользователей, которые вносили изменения.\n" +
                                        "Вместо этого вы можете сделать их не активными.",
                                "Невозможно удалить пользователя", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (entity.getId() >= 0) {
                            UsersService usersService = new UsersServiceImpl(session);
                            usersService.getUsersDao().getSession().beginTransaction();
                            usersService.removeUser(entity.getId());
                            usersService.getUsersDao().getSession().getTransaction().commit();
                            JOptionPane.showMessageDialog(this, "Пользователь успешно удален!",
                                    "Успех",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }

                        DefaultListModel model = (DefaultListModel) registeredUserList.getModel();
                        int selectedIndex = registeredUserList.getSelectedIndex();
                        if (selectedIndex != -1) {
                            model.remove(selectedIndex);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Пользователь не может быть пустым, index: " + registeredUserList.getSelectedIndex());
                }
            }
        }
    }

    private void initAdminRegisteredUsersPanel() {
        registeredUserList.setCellRenderer(getRegisteredUserListCellRenderer());

        registeredUserList.addListSelectionListener(e -> {
            final int selectedIndex = registeredUserList.getSelectedIndex();
            if (selectedIndex >= 0) {
                UsersEntity usersEntity = registeredUserList.getModel().getElementAt(selectedIndex);
                updateCurrentSelectedUserInformation(usersEntity);
            } else {
                clearCurrentUserPanel();
            }
        });

    }

    private void fillAdminRegisteredUsersPanel() {
        SessionUtils.refreshSession(session, UsersEntity.class);
        DefaultListModel<UsersEntity> model = new DefaultListModel<>();
        UsersService usersService = new UsersServiceImpl(session);
        List<UsersEntity> usersEntityList = usersService.listUsers();

        Collections.sort(usersEntityList);

        for (UsersEntity user : usersEntityList) {
            model.addElement(user);
        }
        registeredUserList.setModel(model);
    }

    private DefaultListCellRenderer getRegisteredUserListCellRenderer() {
        return new DefaultListCellRenderer() {
            private final String newUserTitle = "новый пользователь";

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof UsersEntity) {
                    UsersEntity usersEntity = (UsersEntity) value;
                    String username;
                    if (usersEntity.getUsername() != null) {
                        if (!usersEntity.getUsername().isEmpty()) {
                            username = usersEntity.getUsername();
                        } else {
                            username = newUserTitle;
                        }
                    } else {
                        username = newUserTitle;
                    }
                    Component listCellRendererComponent = super.getListCellRendererComponent(list, username, index, isSelected, cellHasFocus);
                    try {
                        if (usersEntity.getId() < 0) {
                            if (isSelected) {
                                listCellRendererComponent.setForeground(Color.GREEN.brighter().brighter());
                            } else {
                                listCellRendererComponent.setForeground(Color.GREEN.darker().darker());
                            }
                        } else {
                            if (!usersEntity.isActive()) {
                                if (isSelected) {
                                    listCellRendererComponent.setBackground(Color.DARK_GRAY);
                                } else {
                                    listCellRendererComponent.setBackground(Color.GRAY);
                                }
                            }
                        }
                    } catch (NullPointerException e) {
                        /*NOP*/
                    }

                    return listCellRendererComponent;
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        };
    }

    private void onSaveAdminConstantsPanel() {
        DefaultTableModel model = (DefaultTableModel) constantsTable.getModel();
        try (Session session = ServerDBConnectionPool.getInstance().getSession()) {
            ConstantsService constantsService = new ConstantsServiceImpl(session);
            session.beginTransaction();
            for (int i = 0; i < model.getRowCount(); i++) {

                final String key = model.getValueAt(i, 0).toString();
                final String value = model.getValueAt(i, 1).toString();


                ConstantsEntity entity = constantsService.getConstantByKey(key);
                if (entity != null) {
                    entity.setValue(value);
                    constantsService.updateConstant(entity);
                }
            }
            session.getTransaction().commit();
            JOptionPane.showMessageDialog(this, "Настройки успешно изменены, все изменения \r\n" +
                    "будут применены после перезагрузки сервера.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (HibernateException e) {
            log.warn("Could not save constants to DB", e);
            JOptionPane.showMessageDialog(this, "Не удалось сохранить настройки:\n" + e.getLocalizedMessage(),
                    "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initUserInfoPanelListeners() {

        DocumentListener usernameTextFieldDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }

            private void updateData() {
                UsersEntity selectedValue = registeredUserList.getSelectedValue();
                if (selectedValue != null) {
                    final int VARCHAR_LENGTH = 20;
                    if (usernameTextField.getText().length() > VARCHAR_LENGTH) {
                        String text = usernameTextField.getText();
                        text = text.substring(0, VARCHAR_LENGTH) + "...";
                        selectedValue.setUsername(text);
                    } else {
                        selectedValue.setUsername(usernameTextField.getText());
                    }
                    SwingUtilities.invokeLater(() -> registeredUserList.updateUI());
                    try {
                        ValidationUtils.validateUserName(selectedValue.getUsername());
                        usernameTextField.setBackground(Color.WHITE);
                    } catch (UsernameValidationException e) {
                        usernameTextField.setBackground(Color.RED);
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateData();
            }
        };

        usernameTextField.getDocument().addDocumentListener(usernameTextFieldDocumentListener);

        resetPasswordButton.addActionListener(e -> {
            UsersEntity selectedValue = registeredUserList.getSelectedValue();
            onResetPasswordButton(selectedValue);
        });

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setName(nameTextField.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setName(nameTextField.getText());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setName(nameTextField.getText());
                }
            }
        });

        patronymicTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setPatronymic(patronymicTextField.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setPatronymic(patronymicTextField.getText());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setPatronymic(patronymicTextField.getText());
                }
            }
        });

        surnameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setSurname(surnameTextField.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setSurname(surnameTextField.getText());
                }

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (registeredUserList.getSelectedValue() != null) {
                    registeredUserList.getSelectedValue().setSurname(surnameTextField.getText());
                }
            }
        });

        userTypeComboBox.addActionListener(e -> {
            Object selectedItem = userTypeComboBox.getSelectedItem();
            if (selectedItem != null) {
                registeredUserList.getSelectedValue().setUserType((UserTypeEntity) selectedItem);
            }
        });

        isEditorCheckBox.addItemListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setEditor(isEditorCheckBox.isSelected());
            }
        });
        isAdminCheckBox.addItemListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setAdmin(isAdminCheckBox.isSelected());
            }
        });
        isActiveCheckBox.addItemListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setActive(isActiveCheckBox.isSelected());
            }
        });

        saveUserButton.addActionListener(e -> {
            UsersEntity user = registeredUserList.getSelectedValue();
            int selectedIndex = registeredUserList.getSelectedIndex();
            try {
                if (ValidationUtils.validateUserEntity(user)) {
                    restoreTextFieldsColors();
                    try {
                        onSaveUserButton(user);

                        clearCurrentUserPanel();
                        fillAdminRegisteredUsersPanel();

                        if (selectedIndex >= 0) {
                            registeredUserList.setSelectedIndex(selectedIndex);
                        }
                    } catch (OptimisticLockException e1) {
                        JOptionPane.showMessageDialog(this,
                                "Пользователь " + user.getUsername() + " уже существует",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (UsernameValidationException e1) {
                usernameTextField.setBackground(Color.red);
                String message = "Имя пользователя должно:\n" +
                        "быть больше 2 и меньше 20 символов\n" +
                        "не должно:\n" +
                        "начинаться с пробела";
                JOptionPane.showMessageDialog(this, message,
                        "Некорректное имя пользователя", JOptionPane.ERROR_MESSAGE);
            } catch (UserTypeValidationException e1) {
                JOptionPane.showMessageDialog(this, "Тип пользователя должен быть указан",
                        "Некорректный тип пользователя", JOptionPane.ERROR_MESSAGE);
            }

        });
    }

    private void initAdminConstantsPanel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && super.isCellEditable(row, column);
            }
        };
        model.addColumn("Параметр");
        model.addColumn("Значение");
    }

    private void initAdminSettingsPanel() {
        connectionUrlTextField.setText(ServerSettingsManager.getInstance().getServerDbConnectionUrl());
        connectionLoginTextField.setText(ServerSettingsManager.getInstance().getServerDbUsername());
        connectionPasswordField.setText(ServerSettingsManager.getInstance().getServerDbPassword());
        saveSettingsToButton.addActionListener(e -> onSaveSettingsToButton());
    }

    private void onForceDisconnectUserButton() {
        DefaultListModel<ClientConnection> listModel = new DefaultListModel<>();
        for (int i = 0; i < onlineUserList.getModel().getSize(); i++) {
            if (onlineUserList.getModel().getElementAt(i) != null) {
                listModel.addElement(onlineUserList.getModel().getElementAt(i));
            }
        }

        if (onlineUserList.getSelectedIndex() >= 0 && onlineUserList.getSelectedIndex() < listModel.getSize()) {
            int selectedIndex = onlineUserList.getSelectedIndex();
            ClientConnection connection = listModel.get(selectedIndex);
            try {
                ServerSocketService.getInstance().closeClientConnection(connection);
                listModel.remove(selectedIndex);
            } catch (IOException e) {
                log.warn("Could not close connection from GUI: " + connection, e);
                ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                        "Не удалось закрыть socket-соединение с: " + connection.getSocket().getInetAddress(),
                        ServerLogMessage.ServerLogMessageLevel.WARN));
            }

            updateOnlineUsersList();

            if (listModel.getSize() > selectedIndex) {
                onlineUserList.setSelectedIndex(selectedIndex);
            } else {
                if (listModel.getSize() > 0 && listModel.getSize() > selectedIndex - 1) {
                    onlineUserList.setSelectedIndex(selectedIndex - 1);
                }
            }
        }
    }

    private void updateJvmInfoLabel() {
        jvmInfoLabel.setText(System.getProperty("java.specification.version")
                + "(" + System.getProperty("java.version") + ")");
    }

    private void onOpenLogFolder() {
        try {
            Desktop.getDesktop().open(new File(ApplicationConstants.LOG_FOLDER));
        } catch (IOException e1) {
            log.warn("Could not open log folder:" + ApplicationConstants.LOG_FOLDER, e1);
        }
    }

    private void onForceDisconnectAllUsersButton() {
        if (ServerSocketService.getInstance().getConnectedClientsCount() > 0) {
            ServerSocketService.getInstance().closeAll();
            updateOnlineUsersList();
        }
    }

    private void initAdminAccessArray() {
        onlyAdminTabsList = new JPanel[]{controlPanel};
    }

    private void initKeyBindings() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> onButtonAdminLock(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> {
                    if (currentLoggedInUser != null) {
                        onPowerServerButton();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        adminServerPanel.registerKeyboardAction(e -> onForceDisconnectUserButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adminServerPanel.registerKeyboardAction(e -> onForceDisconnectAllUsersButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adminServerPanel.registerKeyboardAction(e -> onUserInfoButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        adminUsersPanel.registerKeyboardAction(e -> onAddNewUserButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
                JOptionPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adminUsersPanel.registerKeyboardAction(e -> onRemoveUserButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JOptionPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adminUsersPanel.registerKeyboardAction(e -> initAdminRegisteredUsersPanel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
                JOptionPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onResetPasswordButton(UsersEntity usersEntity) {
        try (final Session session = ServerDBConnectionPool.getInstance().getSession()) {
            PasswordChangeWindow passwordChangeWindow = new PasswordChangeWindow(usersEntity, session);
            passwordChangeWindow.setLocation(FrameUtils.getFrameOnCenter(this, passwordChangeWindow));
            passwordChangeWindow.setVisible(true);
            final UsersEntity userWithNewPassword = passwordChangeWindow.getUserWithNewPassword();
            if (userWithNewPassword != null) {
                onUpdateUserListButton();
                JOptionPane.showMessageDialog(this, "Пароль успешно обновлен", "Обновление пароля", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Не удалось обновить пароль\n" + e.getLocalizedMessage(),
                    "Обновление пароля", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onServerOnlineCountLabel() {
        serverOnlineCountLabelCounterShow = !serverOnlineCountLabelCounterShow;
        if (serverOnlineCountLabelCounterShow) {
            long serverOnlineTimeInSeconds = ServerMonitoringBackgroundService.getInstance().getServerOnlineTimeInSeconds();
            serverOnlineTimeLabel.setText(getServerOnlineString(serverOnlineTimeInSeconds));
        } else {
            serverOnlineTimeLabel.setText(serverStartDate.toString());
        }
    }

    private void setUnlocked(boolean isUnlocked) {
        initAdminAccessArray();


        if (isUnlocked) {
            buttonAdminLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/unlocked.png"))));
            authorizedUserName.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/authorizedUser128.png"))
                    .getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
            authorizedUserName.setToolTipText("Авторизированный пользователь");
            lastActionTimeAgoCounter = 0;
        } else {
            buttonAdminLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/locked.png"))));
            authorizedUserName.setText(null);
            authorizedUserName.setIcon(null);
            authorizedUserName.setToolTipText(null);
            selectCommonAvailableTab();
        }

        setTabsEnabled(isUnlocked);

    }

    private void setTabsEnabled(boolean enabled) {
        for (JPanel tab : onlyAdminTabsList) {
            tabbedPane.setEnabledAt(tabbedPane.getComponentZOrder(tab), enabled);
        }
    }

    private void selectCommonAvailableTab() {
        for (JPanel tab : onlyAdminTabsList) {
            if (tabbedPane.getSelectedComponent().equals(tab)) {
                if (tabbedPane.isEnabledAt(tabbedPane.getComponentZOrder(monitorPanel))) {
                    tabbedPane.setSelectedComponent(monitorPanel);
                } else {
                    tabbedPane.setSelectedComponent(logPanel);
                }
            }
        }

    }

    private void onUserInfoButton() {
        if (onlineUserList.getSelectedIndex() >= 0) {
            ClientConnection connection = onlineUserList.getSelectedValue();
            UserInfoWindow userInfoWindow = new UserInfoWindow(connection.getUser());
            if (connection.getUser() == null) {
                userInfoWindow.setClientConnection(connection);
            }
            userInfoWindow.pack();
            userInfoWindow.setLocation(FrameUtils.getFrameOnCenter(this, userInfoWindow));
            userInfoWindow.setVisible(true);
        }
    }

    private void onAddNewUserButton() {
        DefaultListModel<UsersEntity> model = new DefaultListModel<>();
        for (int i = 0; i < registeredUserList.getModel().getSize(); i++) {
            model.addElement(registeredUserList.getModel().getElementAt(i));
        }
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setId(-1);
        usersEntity.setUserType(new UserTypeServiceImpl(session).getUserTypeById(1));
        model.addElement(usersEntity);
        registeredUserList.setModel(model);
        int index = registeredUserList.getModel().getSize() - 1;
        registeredUserList.setSelectedIndex(index);
    }

    private void onButtonAdminLock() {
        if (currentLoggedInUser != null) {
            setUnlocked(false);
            ServerMonitoringBackgroundService.getInstance().addMessage(
                    new ServerLogMessage("Администратор вышел из системы сервера",
                            ServerLogMessage.ServerLogMessageLevel.SUCCESS));
            currentLoggedInUser = null;
        } else {
            final Session session = ServerDBConnectionPool.getInstance().getSession();
            LoginWindow loginWindow = new LoginWindow(session);
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
            loginWindow.setVisible(true);
            try {
                session.close();
            } catch (HibernateException e) {
                log.warn("Could not close session");
            }
            UsersEntity user = loginWindow.getAuthorizedUser();
            if (user != null) {
                log.info("User to log in as administrator: " + user);
                if (user.isActive()) {
                    if (user.isAdmin()) {
                        log.info("User successfully authorized as administrator: " + user.getUsername());
                        authorizedUserName.setText(user.getUsername());
                        setUnlocked(user.isAdmin());

                        ServerMonitoringBackgroundService.getInstance().addMessage(
                                new ServerLogMessage("Администратор " + user.getUsername()
                                        + " (" + user.getName() + " " + user.getSurname() + ") успешно выполнил вход на сервере",
                                        ServerLogMessage.ServerLogMessageLevel.SUCCESS));

                        currentLoggedInUser = user;
                    } else {
                        log.warn("User could not be authorized as administrator: " + user.getUsername() + ", he is not an admin: " + user);
                        JOptionPane.showMessageDialog(this,
                                "Вход разрешен только администраторам сервера",
                                "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                        ServerMonitoringBackgroundService.getInstance().addMessage(
                                new ServerLogMessage("Пользоватесь " + user.getUsername()
                                        + " (" + user.getName() + " " + user.getSurname() + ") пытался выполнить вход в администраторскую часть, не имея права администратора.",
                                        ServerLogMessage.ServerLogMessageLevel.WARN));
                    }
                } else {
                    log.warn("User" + user.getUsername() + " can not be authorized, he is not active.");
                    JOptionPane.showMessageDialog(this,
                            "Вход разрешен только активным пользователям",
                            "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
                    ServerMonitoringBackgroundService.getInstance().addMessage(
                            new ServerLogMessage("Не действующий пользователь " + user.getUsername()
                                    + " (" + user.getName() + " " + user.getSurname() + ") пытался выполнить вход в администраторскую часть.",
                                    ServerLogMessage.ServerLogMessageLevel.WARN));
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Необходимо войти в систему, чтобы продолжить",
                        "Ошибка входа", JOptionPane.ERROR_MESSAGE);
                ServerMonitoringBackgroundService.getInstance().addMessage(
                        new ServerLogMessage("Кто-то пытался войти в администраторскую часть, " +
                                "но судя по всему пароль не подошёл.",
                                ServerLogMessage.ServerLogMessageLevel.WARN));
            }
        }
    }

    @Override
    public void dispose() {
        if (monitorUiUpdateTimer != null) {
            if (monitorUiUpdateTimer.isRunning()) {
                monitorUiUpdateTimer.stop();
            }
        }

        if (userActionsUpdateTimer != null) {
            if (userActionsUpdateTimer.isRunning()) {
                userActionsUpdateTimer.stop();
            }
        }
        if (!isWindowClosing) {
            createServerTrayIcon();
        }
        try {
            session.close();
        } catch (Exception e) {
            log.warn("Could not close session", e);
        }
        super.dispose();
    }

    private void updateOnlineUsersList() {
        List<ClientConnection> connections = ServerBackgroundService.getInstance().getConnectedClientsList();
        DefaultListModel<ClientConnection> model = new DefaultListModel<>();
        if (connections != null) {
            for (ClientConnection connection : connections) {
                model.addElement(connection);
            }
        }


        int selectedIndex = onlineUserList.getSelectedIndex();
        onlineUserList.setModel(model);

        if (selectedIndex >= 0) {
            onlineUserList.setSelectedIndex(selectedIndex);
        }
    }

    private void onSwitchMonitoring() {
        if (!isMonitoringActive) {
            ServerMonitoringBackgroundService.getInstance().startMonitoring();
            tabbedPane.setEnabledAt(tabbedPane.getComponentZOrder(monitorPanel), true);
            tabbedPane.setEnabledAt(tabbedPane.getComponentZOrder(graphicsPanel), true);
            if (tabbedPane.getSelectedIndex() == tabbedPane.getComponentZOrder(monitorPanel)) {
                tabbedPane.setSelectedIndex(2);
            }
            switchMonitoringButton.setText("Отключить мониторинг");
        } else {
            ServerMonitoringBackgroundService.getInstance().stopMonitoring();
            tabbedPane.setEnabledAt(tabbedPane.getComponentZOrder(monitorPanel), false);
            tabbedPane.setEnabledAt(tabbedPane.getComponentZOrder(graphicsPanel), false);
            switchMonitoringButton.setText("Включить мониторинг");
        }
        isMonitoringActive = !isMonitoringActive;


    }

    private void updateCurrentUserPanel(UsersEntity entity) {
        for (Component component : currentUserPanel.getComponents()) {
            component.setEnabled(true);
        }

        userIdLabel.setText(Integer.toString(entity.getId()));
        usernameTextField.setText(entity.getUsername());
        nameTextField.setText(entity.getName());
        patronymicTextField.setText(entity.getPatronymic());
        surnameTextField.setText(entity.getSurname());
        isEditorCheckBox.setSelected(entity.isEditor());
        isAdminCheckBox.setSelected(entity.isAdmin());
        isActiveCheckBox.setSelected(entity.isActive());
        userTypeComboBox.setSelectedItem(entity.getUserType());

        resetPasswordButton.setEnabled(entity.getId() >= 0);
    }

    private void onSaveUserButton(UsersEntity usersEntity) {
        try (Session session = ServerDBConnectionPool.getInstance().getSession()) {
            UsersService service = new UsersServiceImpl(session);
            try {
                service.getUserById(usersEntity.getId());
                if ((currentLoggedInUser.getId() == usersEntity.getId()) && usersEntity.isAdmin() && usersEntity.isActive()) {
                    updateUser(usersEntity);
                } else if (currentLoggedInUser.getId() != usersEntity.getId()) {
                    updateUser(usersEntity);
                } else {
                    log.warn("User wanted to grand down his permissions (admin wanted to become user), it was blocked. User: " + usersEntity);
                    JOptionPane.showMessageDialog(this,
                            "Вы не можете сделать себя обычным пользователем\n" +
                                    "либо отключить профиль.\n" +
                                    "Это может сделать только другой администратор.",
                            "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ObjectNotFoundException e) {
                createNewUser(usersEntity);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось обновить пользователя: " + usersEntity.getUsername()
                                + "\n Ошибка: " + e.getLocalizedMessage(),
                        "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
                try {
                    service.getUsersDao().getSession().getTransaction().rollback();
                } catch (Exception e1) {
                    log.warn("Could not rollback transaction", e);
                }
            }
        }
    }

    private void createNewUser(UsersEntity entity) {
        if (entity != null) {
            try (Session session = ServerDBConnectionPool.getInstance().getSession()) {
                try {
                    UsersService service = new UsersServiceImpl(session);
                    session.getTransaction().begin();

                    service.addUser(entity);

                    session.getTransaction().commit();

                    JOptionPane.showMessageDialog(this,
                            "Пользователь успешно сохранён: " + entity.getUsername(),
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (ConstraintViolationException e) {
                    log.warn("Could not save user: {}", entity, e);
                    try {
                        session.getTransaction().rollback();
                    } catch (HibernateException ex) {
                        log.warn("Could not rollback transaction", ex);
                    }
                    JOptionPane.showMessageDialog(this, "Не удалось сохранить пользователя: " + entity.getUsername(),
                            "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
                }
            } finally {
                SessionUtils.refreshSession(this.session, UsersEntity.class);
            }
        }
    }

    private void initOnlineUsersList() {
        DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                setOpaque(true);
                if (value instanceof ClientConnection) {
                    ClientConnection client = (ClientConnection) value;
                    String username;
                    UsersEntity userEntity = client.getUser();

                    if (userEntity != null) {
                        if (!userEntity.getUsername().isEmpty()) {
                            username = userEntity.getUsername() + " (" + userEntity.getName() + " " + userEntity.getSurname() + ")";
                            if (isSelected) {
                                setBackground(Color.GREEN.brighter().brighter());
                            } else {
                                setBackground(Color.GREEN.darker().darker());
                            }
                        } else {
                            username = "Some incorrect user, id: " + userEntity.getId();
                            if (isSelected) {
                                setBackground(Color.RED.brighter().brighter());
                            } else {
                                setBackground(Color.RED.darker().darker());
                            }
                        }
                    } else {
                        username = client.getSocket().getInetAddress().getHostName() + ":" + client.getSocket().getPort();
                        if (isSelected) {
                            setBackground(Color.DARK_GRAY.brighter().brighter());
                        } else {
                            setBackground(Color.DARK_GRAY.darker().darker());
                        }
                    }
                    return super.getListCellRendererComponent(list, username, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        };
        onlineUserList.setCellRenderer(cellRenderer);
    }

    private void updateUser(UsersEntity entity) {
        try (Session session = ServerDBConnectionPool.getInstance().getSession()) {
            if (entity != null) {
                UsersService service = new UsersServiceImpl(session);
                service.getUsersDao().getSession().getTransaction().begin();

                service.updateUser(entity);

                service.getUsersDao().getSession().getTransaction().commit();
                JOptionPane.showMessageDialog(this,
                        "Пользователь успешно сохранён: " + entity.getUsername(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            log.warn("Could not update user: {}", e);
            JOptionPane.showMessageDialog(this, "Не удалось обновить пользователя: " + entity.getUsername() + "\n"
                            + e.getLocalizedMessage(),
                    "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
        } finally {
            SessionUtils.refreshSession(this.session, UsersEntity.class);
        }
    }

    private void onSaveSettingsToButton() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить файл конфигурации (.xml)");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(new File(ApplicationConstants.USER_HOME_LOCATION));


        FileFilter fileFilter = new FileNameExtensionFilter("Файл конфигурации", "xml");
        chooser.setFileFilter(fileFilter);
        int returnValue = chooser.showDialog(this, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isDirectory()) {
                final String fileName = chooser.getSelectedFile() + File.separator + "server_configuration.xml";
                try {
                    ServerSettingsManager.getInstance().setServerSettings(fileName);
                    ServerSettingsManager.getInstance().setServerDbConnectionUrl(connectionUrlTextField.getText());
                    ServerSettingsManager.getInstance().setServerDbUsername(connectionLoginTextField.getText());
                    ServerSettingsManager.getInstance().setServerDbPassword(new String(connectionPasswordField.getPassword()));
                    CommonSettingsManager.setServerSettingsFilePath(fileName);

                    JOptionPane.showMessageDialog(this,
                            "Файл успешно сохранен:\n" + fileName,
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (ServerException | IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Невозможно сохранить файл:\n" + e.getLocalizedMessage(),
                            "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }

    private void createGraphics() {
        final int width = graphicsDataPanel.getWidth();
        final int height = 210; // must to be hardcoded or it will rise

        XYChart chart = serverMonitoringGraphics.getChart(width, height);
        XChartPanel<XYChart> graphXChartPanel = new XChartPanel<>(chart);
        if (graphicsDataPanel.getComponents().length > 0) {
            graphicsDataPanel.removeAll();
        }
        graphicsDataPanel.add(graphXChartPanel);
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAdminLock = new JButton();
        buttonAdminLock.setBorderPainted(false);
        buttonAdminLock.setContentAreaFilled(false);
        buttonAdminLock.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        buttonAdminLock.setMargin(new Insets(2, 2, 2, 2));
        buttonAdminLock.setOpaque(false);
        buttonAdminLock.setSelected(false);
        buttonAdminLock.setText("");
        buttonAdminLock.setToolTipText("Разблокировать / заблокировать интерфейс (CTRL+L)");
        panel2.add(buttonAdminLock, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        authorizedUserName = new JLabel();
        Font authorizedUserNameFont = this.$$$getFont$$$(null, Font.BOLD, 11, authorizedUserName.getFont());
        if (authorizedUserNameFont != null) authorizedUserName.setFont(authorizedUserNameFont);
        authorizedUserName.setText("username");
        panel2.add(authorizedUserName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(1001, 248), null, 0, false));
        tabbedPane = new JTabbedPane();
        tabbedPane.setEnabled(true);
        panel3.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Мониторинг", monitorPanel);
        final Spacer spacer2 = new Spacer();
        monitorPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        monitorPanel.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(2, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel4.add(spacer4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel4.add(spacer5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setName("");
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder("Система"));
        final JLabel label1 = new JLabel();
        label1.setText("ОС:");
        panel6.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        osInfoLabel = new JLabel();
        osInfoLabel.setText("_");
        osInfoLabel.setToolTipText("Версия операционной системы");
        panel6.add(osInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Процессор:");
        panel6.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedCpuBySystemInfoLabel = new JLabel();
        usedCpuBySystemInfoLabel.setText("_");
        usedCpuBySystemInfoLabel.setToolTipText("Физических (логических), нагрузка ЦП %");
        panel6.add(usedCpuBySystemInfoLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Температура:");
        panel6.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        temperatureInfoLabel = new JLabel();
        temperatureInfoLabel.setText("0");
        temperatureInfoLabel.setToolTipText("Температура процессора");
        panel6.add(temperatureInfoLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Всего доступно:");
        panel6.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalMemoryInfoLabel = new JLabel();
        totalMemoryInfoLabel.setText("0");
        totalMemoryInfoLabel.setToolTipText("Доступно для JVM / для системы");
        panel6.add(totalMemoryInfoLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("JVM:");
        panel6.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jvmInfoLabel = new JLabel();
        jvmInfoLabel.setText("_");
        jvmInfoLabel.setToolTipText("Версия Java Virtual Machine");
        panel6.add(jvmInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder("Сеть"));
        final JLabel label6 = new JLabel();
        label6.setText("Сетевое имя:");
        panel7.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkNameInfoLabel = new JLabel();
        networkNameInfoLabel.setText("_");
        panel7.add(networkNameInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("IP (v4/v6):");
        panel7.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipAddressInfoLabel = new JLabel();
        ipAddressInfoLabel.setText("_");
        panel7.add(ipAddressInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel7.add(spacer6, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel7.add(spacer7, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder("Сервер"));
        final JLabel label8 = new JLabel();
        label8.setText("Сервер онлайн:");
        panel8.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverOnlineTimeLabel = new JLabel();
        serverOnlineTimeLabel.setText("0д. 0ч. 0м. 0с.");
        panel8.add(serverOnlineTimeLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), new Dimension(180, -1), new Dimension(180, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Потоков:");
        panel8.add(label9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        threadsCount = new JLabel();
        threadsCount.setText("0");
        panel8.add(threadsCount, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Пользователей онлайн:");
        panel8.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        onlineUsersCountLabel = new JLabel();
        onlineUsersCountLabel.setText("0");
        panel8.add(onlineUsersCountLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Порт:");
        panel8.add(label11, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverSocketPortInfoLabel = new JLabel();
        serverSocketPortInfoLabel.setText("0000");
        panel8.add(serverSocketPortInfoLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Использовано памяти (JVM):");
        panel8.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedProcessMemoryInfoLabel = new JLabel();
        usedProcessMemoryInfoLabel.setText("0");
        usedProcessMemoryInfoLabel.setToolTipText("Использовано / зарезервировано");
        panel8.add(usedProcessMemoryInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Нагрузка ЦП:");
        panel8.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedCpuByApplicationInfoLabel = new JLabel();
        usedCpuByApplicationInfoLabel.setText("0%");
        panel8.add(usedCpuByApplicationInfoLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Версия:");
        panel8.add(label14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applicationVersionArea = new JTextField();
        applicationVersionArea.setBackground(new Color(-855310));
        applicationVersionArea.setEditable(false);
        panel8.add(applicationVersionArea, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        graphicsPanel = new JPanel();
        graphicsPanel.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("График", graphicsPanel);
        graphicsDataPanel = new JPanel();
        graphicsDataPanel.setLayout(new BorderLayout(0, 0));
        graphicsPanel.add(graphicsDataPanel, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(500, 210), null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        graphicsPanel.add(spacer8, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cpuSystemCheckBox = new JCheckBox();
        cpuSystemCheckBox.setSelected(true);
        cpuSystemCheckBox.setText("ЦП (система)");
        cpuSystemCheckBox.setMnemonic('П');
        cpuSystemCheckBox.setDisplayedMnemonicIndex(1);
        graphicsPanel.add(cpuSystemCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ramServerCheckBox = new JCheckBox();
        ramServerCheckBox.setSelected(true);
        ramServerCheckBox.setText("ОЗУ (сервер)");
        ramServerCheckBox.setMnemonic('О');
        ramServerCheckBox.setDisplayedMnemonicIndex(0);
        graphicsPanel.add(ramServerCheckBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cpuServerCheckBox = new JCheckBox();
        cpuServerCheckBox.setSelected(true);
        cpuServerCheckBox.setText("ЦП (сервер)");
        cpuServerCheckBox.setMnemonic('Ц');
        cpuServerCheckBox.setDisplayedMnemonicIndex(0);
        graphicsPanel.add(cpuServerCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usersCheckBox = new JCheckBox();
        usersCheckBox.setSelected(true);
        usersCheckBox.setText("Пользователи");
        graphicsPanel.add(usersCheckBox, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        temperatureCpuServerCheckBox = new JCheckBox();
        temperatureCpuServerCheckBox.setSelected(true);
        temperatureCpuServerCheckBox.setText("Температура С° ЦП");
        temperatureCpuServerCheckBox.setMnemonic('Т');
        temperatureCpuServerCheckBox.setDisplayedMnemonicIndex(0);
        graphicsPanel.add(temperatureCpuServerCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logPanel = new JPanel();
        logPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("История сеанса", logPanel);
        logTextScrollPane = new JScrollPane();
        logTextScrollPane.setAutoscrolls(true);
        logPanel.add(logTextScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        logTextScrollPane.setViewportView(logTextPane);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        controlPanel.setEnabled(false);
        tabbedPane.addTab("Администрирование", controlPanel);
        adminPane = new JTabbedPane();
        controlPanel.add(adminPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        adminServerPanel = new JPanel();
        adminServerPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        adminPane.addTab("Сервер", adminServerPanel);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        adminServerPanel.add(panel9, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel9.setBorder(BorderFactory.createTitledBorder("Управление текущими сеансами"));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        panel9.add(scrollPane1, new GridConstraints(0, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        onlineUserList = new JList();
        onlineUserList.setLayoutOrientation(0);
        onlineUserList.setSelectionMode(0);
        scrollPane1.setViewportView(onlineUserList);
        final Spacer spacer9 = new Spacer();
        panel9.add(spacer9, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(1);
        panel9.add(toolBar1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        buttonForceUserDisconnect = new JButton();
        buttonForceUserDisconnect.setIcon(new ImageIcon(getClass().getResource("/img/gui/disconnect.png")));
        buttonForceUserDisconnect.setMargin(new Insets(2, 2, 2, 2));
        buttonForceUserDisconnect.setText("");
        buttonForceUserDisconnect.setToolTipText("Отключить пользователя (DELETE)");
        toolBar1.add(buttonForceUserDisconnect);
        buttonForceAllUsersDisconnect = new JButton();
        buttonForceAllUsersDisconnect.setIcon(new ImageIcon(getClass().getResource("/img/gui/disconnect_total.png")));
        buttonForceAllUsersDisconnect.setToolTipText("Отключить всех пользователей(CTRL+SHIFT+DELETE)");
        toolBar1.add(buttonForceAllUsersDisconnect);
        buttonUserInfo = new JButton();
        buttonUserInfo.setIcon(new ImageIcon(getClass().getResource("/img/gui/info.png")));
        buttonUserInfo.setMargin(new Insets(2, 2, 2, 2));
        buttonUserInfo.setText("");
        buttonUserInfo.setToolTipText("Информация о подключенном пользователе (CTRL+I)");
        toolBar1.add(buttonUserInfo);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/user16.png")));
        label15.setText("");
        label15.setToolTipText("Онлайн");
        panel10.add(label15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        panel10.add(spacer10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        onlineUsersCount2 = new JLabel();
        onlineUsersCount2.setText("0");
        panel10.add(onlineUsersCount2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(50, -1), 0, false));
        final Spacer spacer11 = new Spacer();
        panel9.add(spacer11, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        adminServerPanel.add(spacer12, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(4, 2, new Insets(10, 0, 0, 0), -1, -1));
        adminServerPanel.add(panel11, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel11.setBorder(BorderFactory.createTitledBorder("Управление сервером"));
        final Spacer spacer13 = new Spacer();
        panel11.add(spacer13, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        powerServerButton = new JButton();
        powerServerButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/power.png")));
        powerServerButton.setText("Выключить");
        powerServerButton.setMnemonic('В');
        powerServerButton.setDisplayedMnemonicIndex(0);
        powerServerButton.setToolTipText("Выключить сервер (CTRL+Q)");
        panel11.add(powerServerButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer14 = new Spacer();
        panel11.add(spacer14, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(521, 11), null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel12.setBorder(BorderFactory.createTitledBorder("Анализ"));
        openLogFolderButton = new JButton();
        openLogFolderButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/extensions/logFileFormat.png")));
        openLogFolderButton.setText("Открыть папку с логами (.log)");
        openLogFolderButton.setMnemonic('О');
        openLogFolderButton.setDisplayedMnemonicIndex(0);
        openLogFolderButton.setToolTipText("Открыть папку с логами для анализа работы сервера и поиска ошибок");
        panel12.add(openLogFolderButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        panel12.add(spacer15, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        switchMonitoringButton = new JButton();
        switchMonitoringButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/monitoring16.png")));
        switchMonitoringButton.setText("Отключить мониторинг");
        switchMonitoringButton.setMnemonic('Т');
        switchMonitoringButton.setDisplayedMnemonicIndex(1);
        panel12.add(switchMonitoringButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel13, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel13.setBorder(BorderFactory.createTitledBorder("Руководство пользователя"));
        openClientGuideButton = new JButton();
        openClientGuideButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/extensions/pdfFileFormat.png")));
        openClientGuideButton.setText("Клиент");
        panel13.add(openClientGuideButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer16 = new Spacer();
        panel13.add(spacer16, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        openServerGuideButton = new JButton();
        openServerGuideButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/extensions/pdfFileFormat.png")));
        openServerGuideButton.setText("Сервер");
        panel13.add(openServerGuideButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        adminUsersPanel = new JPanel();
        adminUsersPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        adminPane.addTab("Пользователи", adminUsersPanel);
        final JScrollPane scrollPane2 = new JScrollPane();
        adminUsersPanel.add(scrollPane2, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        registeredUserList = new JList();
        registeredUserList.setSelectionMode(0);
        scrollPane2.setViewportView(registeredUserList);
        final Spacer spacer17 = new Spacer();
        adminUsersPanel.add(spacer17, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        currentUserPanel = new JPanel();
        currentUserPanel.setLayout(new GridLayoutManager(10, 4, new Insets(0, 0, 0, 0), -1, -1));
        adminUsersPanel.add(currentUserPanel, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        currentUserPanel.setBorder(BorderFactory.createTitledBorder("Пользователь"));
        final JLabel label16 = new JLabel();
        label16.setText("Имя пользователя:");
        label16.setToolTipText("Имя пользователя");
        currentUserPanel.add(label16, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer18 = new Spacer();
        currentUserPanel.add(spacer18, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Пароль:");
        currentUserPanel.add(label17, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Имя:");
        currentUserPanel.add(label18, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        nameTextField.setToolTipText("Имя пользователя");
        currentUserPanel.add(nameTextField, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        resetPasswordButton = new JButton();
        resetPasswordButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/securityShield16.png")));
        resetPasswordButton.setText("Сбросить");
        resetPasswordButton.setMnemonic('Б');
        resetPasswordButton.setDisplayedMnemonicIndex(1);
        resetPasswordButton.setToolTipText("Сбросить пароль пользователя");
        currentUserPanel.add(resetPasswordButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Отчество:");
        currentUserPanel.add(label19, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        patronymicTextField = new JTextField();
        patronymicTextField.setToolTipText("Отчество пользователя");
        currentUserPanel.add(patronymicTextField, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Фамилия:");
        currentUserPanel.add(label20, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        surnameTextField = new JTextField();
        surnameTextField.setToolTipText("Фамилия пользователя");
        currentUserPanel.add(surnameTextField, new GridConstraints(5, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("ID:");
        currentUserPanel.add(label21, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userIdLabel = new JLabel();
        userIdLabel.setText("id");
        currentUserPanel.add(userIdLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usernameTextField = new JTextField();
        usernameTextField.setText("");
        usernameTextField.setToolTipText("Имя пользователя в системе (login)");
        currentUserPanel.add(usernameTextField, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        isEditorCheckBox = new JCheckBox();
        isEditorCheckBox.setHorizontalTextPosition(10);
        isEditorCheckBox.setText("Редактор:");
        isEditorCheckBox.setMnemonic('Р');
        isEditorCheckBox.setDisplayedMnemonicIndex(0);
        isEditorCheckBox.setToolTipText("Редактор может вносить изменения в базе, за исключенем администрирования пользователей и сервера");
        currentUserPanel.add(isEditorCheckBox, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isAdminCheckBox = new JCheckBox();
        isAdminCheckBox.setHorizontalTextPosition(10);
        isAdminCheckBox.setText("Администратор:");
        isAdminCheckBox.setMnemonic('Д');
        isAdminCheckBox.setDisplayedMnemonicIndex(1);
        isAdminCheckBox.setToolTipText("Администратор имеет самые большие полномочия");
        currentUserPanel.add(isAdminCheckBox, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userTypeComboBox = new JComboBox();
        userTypeComboBox.setToolTipText("Тип пользователя");
        currentUserPanel.add(userTypeComboBox, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("Тип пользователя:");
        currentUserPanel.add(label22, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveUserButton = new JButton();
        saveUserButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        saveUserButton.setText("Сохранить");
        saveUserButton.setMnemonic('С');
        saveUserButton.setDisplayedMnemonicIndex(0);
        saveUserButton.setToolTipText("Сохранить пользователя");
        currentUserPanel.add(saveUserButton, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setHorizontalTextPosition(10);
        isActiveCheckBox.setText("Действующий");
        isActiveCheckBox.setMnemonic('Ю');
        isActiveCheckBox.setDisplayedMnemonicIndex(7);
        isActiveCheckBox.setToolTipText("Пользователь активный или нет (вместо удаления, для архивирования)");
        currentUserPanel.add(isActiveCheckBox, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer19 = new Spacer();
        adminUsersPanel.add(spacer19, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        adminUsersPanel.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addUserButton = new JButton();
        addUserButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addUserButton.setMargin(new Insets(2, 2, 2, 2));
        addUserButton.setText("");
        addUserButton.setToolTipText("Добавить пользователя (INSERT)");
        toolBar2.add(addUserButton);
        removeUserButton = new JButton();
        removeUserButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeUserButton.setText("");
        removeUserButton.setToolTipText("Удалить пользователя (только если он не внес никаких изменений) (DELETE)");
        toolBar2.add(removeUserButton);
        refreshRegisteredUsersButton = new JButton();
        refreshRegisteredUsersButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        refreshRegisteredUsersButton.setText("");
        refreshRegisteredUsersButton.setToolTipText("Обновить список (CTRL+R)");
        toolBar2.add(refreshRegisteredUsersButton);
        adminSettingsPanel = new JPanel();
        adminSettingsPanel.setLayout(new GridLayoutManager(6, 4, new Insets(10, 10, 10, 10), -1, -1));
        adminPane.addTab("Настройки", adminSettingsPanel);
        final JLabel label23 = new JLabel();
        label23.setText("URL подключения:");
        adminSettingsPanel.add(label23, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer20 = new Spacer();
        adminSettingsPanel.add(spacer20, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Логин:");
        adminSettingsPanel.add(label24, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("Пароль:");
        adminSettingsPanel.add(label25, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveSettingsToButton = new JButton();
        saveSettingsToButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        saveSettingsToButton.setText("Сохранить в...");
        saveSettingsToButton.setMnemonic('Р');
        saveSettingsToButton.setDisplayedMnemonicIndex(3);
        adminSettingsPanel.add(saveSettingsToButton, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectionUrlTextField = new JTextField();
        adminSettingsPanel.add(connectionUrlTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        connectionLoginTextField = new JTextField();
        adminSettingsPanel.add(connectionLoginTextField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        connectionPasswordField = new JPasswordField();
        adminSettingsPanel.add(connectionPasswordField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label26 = new JLabel();
        label26.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningOrange16.png")));
        label26.setText("");
        label26.setToolTipText("Важная настройка системы. Будьте осторожны, применяя изменения.");
        adminSettingsPanel.add(label26, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer21 = new Spacer();
        adminSettingsPanel.add(spacer21, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adminConstantsPanel = new JPanel();
        adminConstantsPanel.setLayout(new GridLayoutManager(3, 4, new Insets(10, 10, 10, 10), -1, -1));
        adminPane.addTab("Константы", adminConstantsPanel);
        final Spacer spacer22 = new Spacer();
        adminConstantsPanel.add(spacer22, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        updateServerConstantsButton = new JButton();
        updateServerConstantsButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        updateServerConstantsButton.setText("Сохранить");
        updateServerConstantsButton.setMnemonic('С');
        updateServerConstantsButton.setDisplayedMnemonicIndex(0);
        adminConstantsPanel.add(updateServerConstantsButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer23 = new Spacer();
        adminConstantsPanel.add(spacer23, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label27 = new JLabel();
        label27.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningOrange16.png")));
        label27.setText("");
        label27.setToolTipText("Важная настройка системы. Будьте осторожны, применяя изменения.");
        adminConstantsPanel.add(label27, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        constantsRefreshButton = new JButton();
        constantsRefreshButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        constantsRefreshButton.setText("Обновить");
        constantsRefreshButton.setMnemonic('Б');
        constantsRefreshButton.setDisplayedMnemonicIndex(1);
        constantsRefreshButton.setToolTipText("Обновить");
        adminConstantsPanel.add(constantsRefreshButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        adminConstantsPanel.add(scrollPane3, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        constantsTable = new JTable();
        scrollPane3.setViewportView(constantsTable);
        label14.setLabelFor(applicationVersionArea);
        label16.setLabelFor(usernameTextField);
        label18.setLabelFor(nameTextField);
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
}
