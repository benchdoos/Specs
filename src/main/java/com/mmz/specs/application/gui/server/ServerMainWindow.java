package com.mmz.specs.application.gui.server;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerException;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.application.managers.CommonSettingsManager;
import com.mmz.specs.application.managers.ServerSettingsManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SystemUtils;
import com.mmz.specs.application.utils.validation.UserTypeValidationException;
import com.mmz.specs.application.utils.validation.UsernameValidationException;
import com.mmz.specs.application.utils.validation.ValidationUtils;
import com.mmz.specs.model.ConstantsEntity;
import com.mmz.specs.model.UserTypeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import oshi.hardware.CentralProcessor;
import oshi.software.os.NetworkParams;

import javax.persistence.OptimisticLockException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.*;

public class ServerMainWindow extends JFrame {

    private static final int MONITORING_TIMER_DELAY = 1000;
    private static final int GRAPHICS_LENGTH = 60;
    private static int caretPosition = 0;
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static boolean isUnlocked = false;
    private final String DEGREE = "\u00b0";
    DefaultListModel<Object> usersListModel = new DefaultListModel<>();
    private Timer monitorUiUpdateTimer;
    private ArrayList<Float> memoryValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Float> cpuValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Float> cpuServerValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Float> cpuTemperatureValue = new ArrayList<>(GRAPHICS_LENGTH);
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel monitorPanel;
    private JList<Object> onlineUserList;
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
    private JButton refreshPasswordButton;
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
    private JButton restartServerButton;
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
    private JLabel ipAdressInfoLabel;
    private JLabel totalMemoryInfoLabel;
    private JLabel serverConnectionPortInfoLabel;
    private JLabel jvmInfoLabel;
    private JTable constantsTable;
    private JButton updateServerConstantsButton;
    private JLabel usedCpuByApplicationInfoLabel;
    private JPanel graphicsPanel;
    private JPanel adminServerPanel;
    private JPanel adminSettingsPanel;
    private JPanel adminConstantsPanel;
    private JButton constantsRefreshButton;
    private JButton updateUserListButton;
    private JPanel currentUserPanel;
    private boolean serverOnlineCountLabelCounterShow = true;
    private Date serverStartDate = Calendar.getInstance().getTime();
    private long serverStartDateSeconds = Calendar.getInstance().getTime().getTime() / 1000;
    private JPanel onlyAdminTabsList[];

    public ServerMainWindow() {
        setContentPane(contentPane);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_SERVER);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/logo.png")));

        initGui();


        initKeyboardActions();

        pack();
        setMinimumSize(getSize());

        setLocation(FrameUtils.getFrameOnCenter(null, this));


        setUnlocked(false);

        initThreads();

    }

    private void initAdminAccessArray() {
        onlyAdminTabsList = new JPanel[]{controlPanel};
    }

    private void initThreads() {
        monitorUiUpdateTimer = new Timer(MONITORING_TIMER_DELAY, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateOnlineUsersCount();
                updateServerOnlineTimeLabel();
                updateActiveThreadCounterLabel();
                updateProcessorInfoLabel();
                updateUsedProcessCpuInfoLabel();
                updateUsedJvmMemoryInfoLabel();

                updateTemperatureInfoLabel();
                createGraphics();

                caretPosition++;
            }

            private void updateTemperatureInfoLabel() {
                final double cpuTemperature = getCpuTemperature();

                if (cpuTemperature > 90.0d) {

                    setWarningMode(temperatureInfoLabel, true);

                } else {
                    setWarningMode(temperatureInfoLabel, false);

                }

                cpuTemperatureValue = updateGraphicValue(cpuTemperatureValue, getCpuTemperature());

                temperatureInfoLabel.setText("ЦП: " + cpuTemperature + " C" + DEGREE);
            }


            private void updateUsedJvmMemoryInfoLabel() {
                final long runtimeUsedMemory = getRuntimeUsedMemory();
                final long runtimeMaxMemory = getRuntimeMaxMemory();
                String memoryInfo = "JVM: " + runtimeUsedMemory + " / " + getRuntimeTotalMemory() + " МБ. ";

                double usedMemory = CommonUtils.round(runtimeUsedMemory / (double) runtimeMaxMemory * 100, 2);

                memoryValues = updateGraphicValue(memoryValues, usedMemory);

                if (runtimeUsedMemory > (runtimeMaxMemory - 0.2 * runtimeMaxMemory)) {
                    setWarningMode(usedProcessMemoryInfoLabel, true);
                } else {
                    setWarningMode(usedProcessMemoryInfoLabel, false);
                }
                usedProcessMemoryInfoLabel.setText(memoryInfo);
            }

            private void updateActiveThreadCounterLabel() {
                threadsCount.setText(getApplicationCurrentThreads() + "");
            }

            private void updateServerOnlineTimeLabel() {
                long onlineNanoSeconds = Calendar.getInstance().getTime().getTime() / 1000 - serverStartDateSeconds;
                if (serverOnlineCountLabelCounterShow) {
                    String text = getServerOnlineString(onlineNanoSeconds);
                    System.out.println(text);
                    serverOnlineTimeLabel.setText(text);
                } else {
                    serverOnlineTimeLabel.setText(serverStartDate.toString());
                }
            }

            private void updateOnlineUsersCount() {
                onlineUsersCountLabel.setText(onlineUserList.getModel().getSize() + "");
                onlineUsersCount2.setText(onlineUserList.getModel().getSize() + "");
            }
        });
        if (!monitorUiUpdateTimer.isRunning()) {
            monitorUiUpdateTimer.start();
        }
    }

    private void updateTotalMemoryLabel() {
        long runtimeTotalMemory = getSystemTotalMemory();
        long runtimeMaxMemory = getRuntimeMaxMemory();
        totalMemoryInfoLabel.setText("JVM: " + runtimeMaxMemory + " МБ. ОЗУ: " + runtimeTotalMemory + " МБ.");
    }

    private void updateNetworkInfoPanel() {
        NetworkParams networkParams = OPERATING_SYSTEM.getNetworkParams();
        networkNameInfoLabel.setText(networkParams.getHostName());
        ipAdressInfoLabel.setText(networkParams.getIpv4DefaultGateway() + " / " + networkParams.getIpv6DefaultGateway());
    }

    private void updateProcessorInfoLabel() {
        CentralProcessor processor = HARDWARE_ABSTRACTION_LAYER.getProcessor();
        long logicalProcessorCount = processor.getLogicalProcessorCount();
        long physicalProcessorCount = processor.getPhysicalProcessorCount();
        double cpuLoad = getProcessCpuLoad();
        String cpuLoadString = CommonUtils.round(cpuLoad, 1) + "%";

        cpuValues = updateGraphicValue(cpuValues, getProcessCpuLoad());

        if (cpuLoad >= 60.0d) {
            setWarningMode(usedCpuBySystemInfoLabel, true);
        } else {
            setWarningMode(usedCpuBySystemInfoLabel, false);
        }
        usedCpuBySystemInfoLabel.setText(physicalProcessorCount + " (" + logicalProcessorCount + ") " + cpuLoadString);
    }

    private void updateUsedProcessCpuInfoLabel() {
        final double cpuUsageByApplication = getCpuUsageByApplication();
        String processInfo = cpuUsageByApplication + "%";

        cpuServerValues = updateGraphicValue(cpuServerValues, cpuUsageByApplication);


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
            label.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningRed12.png")));
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

    private void initKeyboardActions() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> onButtonAdminLock(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adminServerPanel.registerKeyboardAction(e -> onForceUserDisconnect(usersListModel),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void updateSystemInfoLabel() {
        String osInfo = OPERATING_SYSTEM.getManufacturer() + " " + OPERATING_SYSTEM.getFamily()
                + " " + OPERATING_SYSTEM.getVersion() + " x" + SystemUtils.getRealSystemArch();
        osInfoLabel.setText(osInfo);
    }

    private void initGui() {
        updateSystemInfoLabel();
        updateNetworkInfoPanel();
        updateTotalMemoryLabel();
        updateJvmInfoLabel();

        updateAdminSettingsPanel();
        updateAdminConstantsPanel();
        updateAdminRegisteredUsersPanel();
        clearCurrentUserPanel();


        updateUserTypeComboBox();

        //test

        for (int i = 0; i < 10 * 1000; i++) {
            usersListModel.addElement("User: " + i);
        }
        onlineUserList.setModel(usersListModel);

        initListeners();
    }

    private void updateAdminRegisteredUsersPanel() {
        DefaultListModel<UsersEntity> model = new DefaultListModel<>();
        UsersService usersService = new UsersServiceImpl();
        List<UsersEntity> usersEntityList = usersService.listUsers();
        for (UsersEntity user : usersEntityList) {
            model.addElement(user);
        }
        registeredUserList.setModel(model);
        registeredUserList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof UsersEntity) {
                    UsersEntity usersEntity = (UsersEntity) value;
                    String username = " ";
                    if (usersEntity.getUsername() != null) {
                        if (!usersEntity.getUsername().isEmpty()) {
                            username = usersEntity.getUsername();
                        }
                    }
                    Component listCellRendererComponent = super.getListCellRendererComponent(list, username, index, isSelected, cellHasFocus);
                    try {
                        if (usersEntity.getId() < 0) {
                            if (isSelected) {
                                setForeground(Color.GREEN.brighter().brighter());
                            } else {
                                setForeground(Color.GREEN.darker().darker());
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
        });

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
    }

    private void updateCurrentUserPanel(UsersEntity usersEntity) {
        for (Component component : currentUserPanel.getComponents()) {
            component.setEnabled(true);
        }

        userIdLabel.setText(Integer.toString(usersEntity.getId()));
        usernameTextField.setText(usersEntity.getUsername());
        nameTextField.setText(usersEntity.getName());
        patronymicTextField.setText(usersEntity.getPatronymic());
        surnameTextField.setText(usersEntity.getSurname());
        isEditorCheckBox.setSelected(usersEntity.isEditor());
        isAdminCheckBox.setSelected(usersEntity.isAdmin());
        isActiveCheckBox.setSelected(usersEntity.isActive());
        userTypeComboBox.setSelectedItem(usersEntity.getUserType());
    }

    private void updateUserTypeComboBox() {
        DefaultComboBoxModel<UserTypeEntity> model = new DefaultComboBoxModel<>();

        UserTypeService userTypeService = new UserTypeServiceImpl();
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

    private void initListeners() {
        buttonForceUserDisconnect.addActionListener(e -> onForceUserDisconnect(usersListModel));

        buttonAdminLock.addActionListener(e -> onButtonAdminLock());

        serverOnlineTimeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onServerOnlineCountLabel();
            }
        });


        openLogFolderButton.addActionListener(e -> onOpenLogFolder());

        constantsRefreshButton.addActionListener(e -> updateAdminConstantsPanel());

        updateServerConstantsButton.addActionListener(e -> onSaveAdminConstantsPanel());

        updateUserListButton.addActionListener(e -> {
            clearCurrentUserPanel();
            updateAdminRegisteredUsersPanel();
            restoreTextFieldsColors();
        });

        addUserButton.addActionListener(e -> addNewUser());

        initUserInfoPanelListeners();
    }

    private void initUserInfoPanelListeners() {

        DocumentListener usernameTextFieldDocumentListener = new DocumentListener() {
            private void updateData() {
                UsersEntity selectedValue = registeredUserList.getSelectedValue();
                if (selectedValue != null) {
                    selectedValue.setUsername(usernameTextField.getText());
                    registeredUserList.updateUI();
                    try {
                        ValidationUtils.validateUserName(selectedValue.getUsername());
                        usernameTextField.setBackground(Color.WHITE);
                    } catch (UsernameValidationException e) {
                        usernameTextField.setBackground(Color.RED);
                    }
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateData();
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

        refreshPasswordButton.addActionListener(e -> onRefreshPasswordButton(registeredUserList.getSelectedValue()));

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

        isEditorCheckBox.addChangeListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setEditor(isEditorCheckBox.isSelected());
            }
        });
        isAdminCheckBox.addChangeListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setAdmin(isAdminCheckBox.isSelected());
            }
        });
        isActiveCheckBox.addChangeListener(e -> {
            if (registeredUserList.getSelectedValue() != null) {
                registeredUserList.getSelectedValue().setActive(isActiveCheckBox.isSelected());
            }
        });

        saveUserButton.addActionListener(e -> {
            UsersEntity user = registeredUserList.getSelectedValue();
            try {
                if (ValidationUtils.validateUserEntity(user)) {
                    restoreTextFieldsColors();
                    try {
                        onSaveUserButton(user);
                        clearCurrentUserPanel();
                        updateAdminRegisteredUsersPanel();
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

    private void restoreTextFieldsColors() {
        usernameTextField.setBackground(Color.WHITE);
    }

    private UsersEntity onSaveUserButton(UsersEntity usersEntity) {
        UsersService usersService = new UsersServiceImpl();
        usersService.getUserDao().getSession().getTransaction().begin();

        System.out.println("Entity to save: " + usersEntity);

        UsersEntity entity = usersService.getUserByUsername(usersEntity.getUsername());
        if (entity != null) {
            usersService.updateUser(usersEntity);
        } else {
            entity = usersService.getUserById(usersService.addUser(usersEntity));
        }
        //usersService.addUser(usersEntity);
        usersService.getUserDao().getSession().getTransaction().commit();
        return entity;
    }

    private void addNewUser() {
        DefaultListModel<UsersEntity> model = new DefaultListModel<>();
        for (int i = 0; i < registeredUserList.getModel().getSize(); i++) {
            model.addElement(registeredUserList.getModel().getElementAt(i));
        }
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setId(-1);
        usersEntity.setUsername("<new user>");
        usersEntity.setUserType(new UserTypeServiceImpl().getUserTypeById(1));
        model.addElement(usersEntity);
        registeredUserList.setModel(model);
        int index = registeredUserList.getModel().getSize() - 1;
        System.out.println("index:> " + index);
        registeredUserList.setSelectedIndex(index);
    }

    private void onSaveAdminConstantsPanel() {
        DefaultTableModel model = (DefaultTableModel) constantsTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            ConstantsService constantsService = new ConstantsServiceImpl();

            constantsService.getSession().beginTransaction();

            final String key = model.getValueAt(i, 0).toString();
            final String value = model.getValueAt(i, 1).toString();

            ConstantsEntity entity = constantsService.getConstantByKey(key);
            if (entity != null) {
                entity.setValue(value);
                constantsService.updateConstant(entity);
            }
            constantsService.getSession().getTransaction().commit();
        }
    }

    private void updateAdminConstantsPanel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && super.isCellEditable(row, column);
            }
        };
        model.addColumn("key");
        model.addColumn("value");


        ConstantsService constantsService = new ConstantsServiceImpl();
        List<ConstantsEntity> list = constantsService.listConstants();
        for (ConstantsEntity entity : list) {
            String key = entity.getKey();
            String value = entity.getValue();
            model.addRow(new Object[]{key, value});
        }
        constantsTable.setModel(model);
    }

    private void updateAdminSettingsPanel() {
        connectionUrlTextField.setText(ServerSettingsManager.getInstance().getServerDbConnectionUrl());
        connectionLoginTextField.setText(ServerSettingsManager.getInstance().getServerDbUsername());
        connectionPasswordField.setText(ServerSettingsManager.getInstance().getServerDbPassword());
        saveSettingsToButton.addActionListener(e -> {
            onSaveSettingsToButton();
        });
    }

    private void onSaveSettingsToButton() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить файл конфигурации (.xml)");
        chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
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
        final int width = graphicsPanel.getWidth();
        final int height = 210; // need to be hardcoded or it will rise

        XYChart chart = getChart(width, height);
        XChartPanel<XYChart> graphXChartPanel = new XChartPanel<>(chart);
        if (graphicsPanel.getComponents().length > 0) {
            graphicsPanel.removeAll();
        }
        graphicsPanel.add(graphXChartPanel);
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

    private void onForceUserDisconnect(DefaultListModel<Object> listModel) {
        if (onlineUserList.getSelectedIndex() >= 0 && onlineUserList.getSelectedIndex() < listModel.getSize()) {
            int selectedIndex = onlineUserList.getSelectedIndex();
            listModel.remove(selectedIndex);
            if (listModel.getSize() > selectedIndex) {
                onlineUserList.setSelectedIndex(selectedIndex);
            } else {
                if (listModel.getSize() > 0 && listModel.getSize() > selectedIndex - 1) {
                    onlineUserList.setSelectedIndex(selectedIndex - 1);
                }
            }
        }
    }

    private void onButtonAdminLock() {
        if (ServerMainWindow.isUnlocked) {
            setUnlocked(false);
        } else {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
            UsersEntity user = loginWindow.getAuthorizedUser();
            if (user != null) {
                System.out.println("Hello, " + user.getUsername() + ", " + "p:" + user.getPassword());
                if (user.isAdmin()) {
                    System.out.println("Hello admin");
                    setUnlocked(user.isAdmin());
                }
            }
        }
    }

    private UsersEntity onRefreshPasswordButton(UsersEntity usersEntity) {
        PasswordChangeWindow passwordChangeWindow = new PasswordChangeWindow(usersEntity);
        passwordChangeWindow.setLocation(FrameUtils.getFrameOnCenter(this, passwordChangeWindow));
        passwordChangeWindow.setVisible(true);
        return passwordChangeWindow.getUserWithNewPassword();
    }

    private void onServerOnlineCountLabel() {
        serverOnlineCountLabelCounterShow = !serverOnlineCountLabelCounterShow;
        if (serverOnlineCountLabelCounterShow) {
            serverOnlineTimeLabel.setText(getServerOnlineString(Calendar.getInstance().getTime().getTime() / 1000 - serverStartDateSeconds));
        } else {
            serverOnlineTimeLabel.setText(serverStartDate.toString());
        }
    }

    private void setUnlocked(boolean isUnlocked) {
        ServerMainWindow.isUnlocked = isUnlocked;
        initAdminAccessArray();

        if (isUnlocked) {
            buttonAdminLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/unlocked.png"))));
        } else {
            buttonAdminLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/admin/locked.png"))));
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
                tabbedPane.setSelectedComponent(monitorPanel);
            }
        }

    }

    private XYChart getChart(int width, int height) {
        final Color BACKGROUND_COLOR = new Color(242, 242, 242);
        Font defaultFont = new JLabel().getFont();

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(height).yAxisTitle("Нагрузка (%)").theme(Styler.ChartTheme.Matlab).build();

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendSeriesLineLength(12);
        chart.getStyler().setLegendPadding(5);
        chart.getStyler().setLegendBorderColor(new Color(1f, 0f, 0f, 0f));

        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        chart.getStyler().setYAxisDecimalPattern("###.##");

        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(1);


        chart.getStyler().setChartBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setLegendBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setPlotBackgroundColor(BACKGROUND_COLOR);

        chart.getStyler().setLegendFont(defaultFont);
        chart.getStyler().setBaseFont(defaultFont);
        chart.getStyler().setAnnotationsFont(defaultFont);
        chart.getStyler().setAxisTitleFont(defaultFont);
        chart.getStyler().setChartTitleFont(defaultFont);
        chart.getStyler().setAxisTickLabelsFont(defaultFont);

        // Series
        // @formatter:off

        ArrayList<Float> xAges = getGraphicXAges();

        ArrayList<Float> memoryData = this.memoryValues;

        ArrayList<Float> cpuData = this.cpuValues;

        ArrayList<Float> cpuServerData = this.cpuServerValues;

        ArrayList<Float> cpuTemperatureData = this.cpuTemperatureValue;
        // @formatter:on

        XYSeries memory = chart.addSeries("% ОЗУ (cервер)", xAges, memoryData);
        memory.setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN);

        XYSeries cpuServer = chart.addSeries("% ЦП (сервер)", xAges, cpuServerData);
        cpuServer.setMarker(SeriesMarkers.NONE).setLineColor(Color.BLUE);
        cpuServer.setLineStyle(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        XYSeries temperature = chart.addSeries(DEGREE + "C ЦП", xAges, cpuTemperatureData);
        temperature.setMarker(SeriesMarkers.NONE).setLineColor(Color.ORANGE);
        temperature.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        XYSeries cpuSystem = chart.addSeries("% ЦП (система)", xAges, cpuData);
        cpuSystem.setMarker(SeriesMarkers.NONE).setLineColor(Color.RED);
        cpuSystem.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        return chart;
    }

    private ArrayList<Float> getGraphicXAges() {
        ArrayList<Float> result = new ArrayList<>(GRAPHICS_LENGTH);
        for (int i = 0; i < GRAPHICS_LENGTH; i++) {
            result.add((float) i);
        }
        return result;
    }

    @Override
    public void dispose() {
        if (monitorUiUpdateTimer.isRunning()) {
            monitorUiUpdateTimer.stop();
        }
        super.dispose();
    }

    private ArrayList<Float> updateGraphicValue(ArrayList<Float> oldValues, double newValue) {
        if (oldValues.size() != GRAPHICS_LENGTH) {
            for (int i = 0; i < GRAPHICS_LENGTH; i++) {
                oldValues.add(0f);
            }
        }

        ArrayList<Float> result = new ArrayList<>(GRAPHICS_LENGTH);

        if (oldValues.size() == GRAPHICS_LENGTH) {
            for (int i = 1; i < oldValues.size(); i++) {
                result.add(oldValues.get(i));
            }
            result.add((float) newValue);
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAdminLock = new JButton();
        buttonAdminLock.setBorderPainted(false);
        buttonAdminLock.setIcon(new ImageIcon(getClass().getResource("/img/gui/admin/locked.png")));
        buttonAdminLock.setMargin(new Insets(2, 2, 2, 2));
        buttonAdminLock.setOpaque(false);
        buttonAdminLock.setSelected(false);
        buttonAdminLock.setText("");
        buttonAdminLock.setToolTipText("Разблокировать / заблокировать интерфейс (CTRL+L)");
        panel2.add(buttonAdminLock, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(1001, 248), null, 0, false));
        tabbedPane = new JTabbedPane();
        tabbedPane.setEnabled(true);
        panel3.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 0, 0), -1, -1));
        tabbedPane.addTab("Мониторинг", monitorPanel);
        final Spacer spacer2 = new Spacer();
        monitorPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(8, 7, new Insets(0, 0, 0, 0), -1, -1));
        monitorPanel.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setName("");
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Система"));
        final JLabel label1 = new JLabel();
        label1.setText("ОС:");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        osInfoLabel = new JLabel();
        osInfoLabel.setText("_");
        panel5.add(osInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Процессор:");
        panel5.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedCpuBySystemInfoLabel = new JLabel();
        usedCpuBySystemInfoLabel.setText("_");
        usedCpuBySystemInfoLabel.setToolTipText("Физических (логических), нагрузка ЦП %");
        panel5.add(usedCpuBySystemInfoLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Температура:");
        panel5.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        temperatureInfoLabel = new JLabel();
        temperatureInfoLabel.setText("0");
        temperatureInfoLabel.setToolTipText("Температура процессора");
        panel5.add(temperatureInfoLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Всего доступно:");
        panel5.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalMemoryInfoLabel = new JLabel();
        totalMemoryInfoLabel.setText("0");
        totalMemoryInfoLabel.setToolTipText("Доступно для JVM / для системы");
        panel5.add(totalMemoryInfoLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("JVM:");
        panel5.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jvmInfoLabel = new JLabel();
        jvmInfoLabel.setText("_");
        panel5.add(jvmInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder("Сеть"));
        final JLabel label6 = new JLabel();
        label6.setText("Сетевое имя:");
        panel6.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkNameInfoLabel = new JLabel();
        networkNameInfoLabel.setText("_");
        panel6.add(networkNameInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("IP (v4/v6):");
        panel6.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipAdressInfoLabel = new JLabel();
        ipAdressInfoLabel.setText("_");
        panel6.add(ipAdressInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel6.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel4.add(spacer5, new GridConstraints(3, 3, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel4.add(spacer6, new GridConstraints(4, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(0, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        graphicsPanel = new JPanel();
        graphicsPanel.setLayout(new BorderLayout(0, 0));
        panel7.add(graphicsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(500, 210), null, new Dimension(-1, 210), 0, false));
        graphicsPanel.setBorder(BorderFactory.createTitledBorder("График"));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel8, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder("Сервер"));
        final JLabel label8 = new JLabel();
        label8.setText("Сервер онлайн:");
        panel8.add(label8, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverOnlineTimeLabel = new JLabel();
        serverOnlineTimeLabel.setText("0д. 0ч. 0м. 0с.");
        panel8.add(serverOnlineTimeLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(180, -1), new Dimension(180, -1), new Dimension(180, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Потоков:");
        panel8.add(label9, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        threadsCount = new JLabel();
        threadsCount.setText("0");
        panel8.add(threadsCount, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Пользователей онлайн:");
        panel8.add(label10, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        onlineUsersCountLabel = new JLabel();
        onlineUsersCountLabel.setText("0");
        panel8.add(onlineUsersCountLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Порт:");
        panel8.add(label11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverConnectionPortInfoLabel = new JLabel();
        serverConnectionPortInfoLabel.setText("0000");
        panel8.add(serverConnectionPortInfoLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Использовано памяти (JVM):");
        panel8.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedProcessMemoryInfoLabel = new JLabel();
        usedProcessMemoryInfoLabel.setText("0");
        usedProcessMemoryInfoLabel.setToolTipText("Использовано / зарезервировано");
        panel8.add(usedProcessMemoryInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Нагрузка ЦП:");
        panel8.add(label13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedCpuByApplicationInfoLabel = new JLabel();
        usedCpuByApplicationInfoLabel.setText("0%");
        panel8.add(usedCpuByApplicationInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel4.add(spacer7, new GridConstraints(1, 2, 7, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel4.add(spacer8, new GridConstraints(2, 3, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel4.add(spacer9, new GridConstraints(2, 1, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logPanel = new JPanel();
        logPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("История сеанса", logPanel);
        final JScrollPane scrollPane1 = new JScrollPane();
        logPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        scrollPane1.setViewportView(logTextPane);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        controlPanel.setEnabled(false);
        tabbedPane.addTab("Администрирование", controlPanel);
        adminPane = new JTabbedPane();
        controlPanel.add(adminPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        adminServerPanel = new JPanel();
        adminServerPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        adminPane.addTab("Сервер", adminServerPanel);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        adminServerPanel.add(panel9, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel9.setBorder(BorderFactory.createTitledBorder("Управление текущими сеансами"));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setHorizontalScrollBarPolicy(31);
        panel9.add(scrollPane2, new GridConstraints(0, 0, 3, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        onlineUserList = new JList();
        onlineUserList.setLayoutOrientation(0);
        onlineUserList.setSelectionMode(0);
        scrollPane2.setViewportView(onlineUserList);
        final Spacer spacer10 = new Spacer();
        panel9.add(spacer10, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(1);
        panel9.add(toolBar1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        buttonForceUserDisconnect = new JButton();
        buttonForceUserDisconnect.setIcon(new ImageIcon(getClass().getResource("/img/gui/disconnect.png")));
        buttonForceUserDisconnect.setMargin(new Insets(2, 2, 2, 2));
        buttonForceUserDisconnect.setText("");
        buttonForceUserDisconnect.setToolTipText("Отключить пользователя (delete)");
        toolBar1.add(buttonForceUserDisconnect);
        buttonUserInfo = new JButton();
        buttonUserInfo.setIcon(new ImageIcon(getClass().getResource("/img/gui/info.png")));
        buttonUserInfo.setMargin(new Insets(2, 2, 2, 2));
        buttonUserInfo.setText("");
        buttonUserInfo.setToolTipText("Информация о подключенном пользователе (ctrl+i)");
        toolBar1.add(buttonUserInfo);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/user.png")));
        label14.setText("");
        label14.setToolTipText("Онлайн");
        panel10.add(label14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        panel10.add(spacer11, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        onlineUsersCount2 = new JLabel();
        onlineUsersCount2.setText("0");
        panel10.add(onlineUsersCount2, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(50, -1), 0, false));
        final Spacer spacer12 = new Spacer();
        panel9.add(spacer12, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer13 = new Spacer();
        adminServerPanel.add(spacer13, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(4, 4, new Insets(10, 0, 0, 0), -1, -1));
        adminServerPanel.add(panel11, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel11.setBorder(BorderFactory.createTitledBorder("Управление сервером"));
        final Spacer spacer14 = new Spacer();
        panel11.add(spacer14, new GridConstraints(2, 0, 2, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        powerServerButton = new JButton();
        powerServerButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/power.png")));
        powerServerButton.setText("Выключить");
        powerServerButton.setToolTipText("Выключить сервер");
        panel11.add(powerServerButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        restartServerButton = new JButton();
        restartServerButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/reboot.png")));
        restartServerButton.setText("Перезапустить");
        restartServerButton.setToolTipText("Перезапустить сервер");
        panel11.add(restartServerButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        panel11.add(spacer15, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel12.setBorder(BorderFactory.createTitledBorder("Анализ"));
        openLogFolderButton = new JButton();
        openLogFolderButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/extensions/log-file-format.png")));
        openLogFolderButton.setText("Открыть папку с логами (.log)");
        openLogFolderButton.setMnemonic('О');
        openLogFolderButton.setDisplayedMnemonicIndex(0);
        openLogFolderButton.setToolTipText("Открыть папку с логами для анализа работы сервера и поиска ошибок");
        panel12.add(openLogFolderButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer16 = new Spacer();
        panel12.add(spacer16, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adminUsersPanel = new JPanel();
        adminUsersPanel.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        adminPane.addTab("Пользователи", adminUsersPanel);
        final JScrollPane scrollPane3 = new JScrollPane();
        adminUsersPanel.add(scrollPane3, new GridConstraints(2, 0, 4, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        registeredUserList = new JList();
        registeredUserList.setSelectionMode(0);
        scrollPane3.setViewportView(registeredUserList);
        final Spacer spacer17 = new Spacer();
        adminUsersPanel.add(spacer17, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        currentUserPanel = new JPanel();
        currentUserPanel.setLayout(new GridLayoutManager(10, 5, new Insets(0, 0, 0, 0), -1, -1));
        adminUsersPanel.add(currentUserPanel, new GridConstraints(2, 2, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        currentUserPanel.setBorder(BorderFactory.createTitledBorder("Пользователь"));
        final JLabel label15 = new JLabel();
        label15.setText("Имя пользователя:");
        label15.setToolTipText("Имя пользователя");
        currentUserPanel.add(label15, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer18 = new Spacer();
        currentUserPanel.add(spacer18, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Пароль:");
        currentUserPanel.add(label16, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Имя:");
        currentUserPanel.add(label17, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        nameTextField.setToolTipText("Имя пользователя");
        currentUserPanel.add(nameTextField, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        refreshPasswordButton = new JButton();
        refreshPasswordButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/security-shield.png")));
        refreshPasswordButton.setText("Сбросить");
        refreshPasswordButton.setMnemonic('Б');
        refreshPasswordButton.setDisplayedMnemonicIndex(1);
        refreshPasswordButton.setToolTipText("Сбросить пароль пользователя");
        currentUserPanel.add(refreshPasswordButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Отчество:");
        currentUserPanel.add(label18, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        patronymicTextField = new JTextField();
        patronymicTextField.setToolTipText("Отчество пользователя");
        currentUserPanel.add(patronymicTextField, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Фамилия:");
        currentUserPanel.add(label19, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        surnameTextField = new JTextField();
        surnameTextField.setToolTipText("Фамилия пользователя");
        currentUserPanel.add(surnameTextField, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("ID:");
        currentUserPanel.add(label20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userIdLabel = new JLabel();
        userIdLabel.setText("id");
        currentUserPanel.add(userIdLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer19 = new Spacer();
        currentUserPanel.add(spacer19, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        usernameTextField = new JTextField();
        usernameTextField.setText("");
        usernameTextField.setToolTipText("Имя пользователя в системе (login)");
        currentUserPanel.add(usernameTextField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        final JLabel label21 = new JLabel();
        label21.setText("Тип пользователя:");
        currentUserPanel.add(label21, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveUserButton = new JButton();
        saveUserButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        saveUserButton.setText("Сохранить");
        saveUserButton.setMnemonic('С');
        saveUserButton.setDisplayedMnemonicIndex(0);
        saveUserButton.setToolTipText("Сохранить пользователя");
        currentUserPanel.add(saveUserButton, new GridConstraints(8, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setHorizontalTextPosition(10);
        isActiveCheckBox.setText("Действующий");
        isActiveCheckBox.setMnemonic('Ю');
        isActiveCheckBox.setDisplayedMnemonicIndex(7);
        isActiveCheckBox.setToolTipText("Пользователь активный или нет (вместо удаления, для архивирования)");
        currentUserPanel.add(isActiveCheckBox, new GridConstraints(7, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer20 = new Spacer();
        adminUsersPanel.add(spacer20, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer21 = new Spacer();
        adminUsersPanel.add(spacer21, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        adminUsersPanel.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addUserButton = new JButton();
        addUserButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addUserButton.setMargin(new Insets(2, 2, 2, 2));
        addUserButton.setText("");
        addUserButton.setToolTipText("Добавить пользователя");
        toolBar2.add(addUserButton);
        updateUserListButton = new JButton();
        updateUserListButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        updateUserListButton.setText("");
        updateUserListButton.setToolTipText("Обновить список");
        toolBar2.add(updateUserListButton);
        adminSettingsPanel = new JPanel();
        adminSettingsPanel.setLayout(new GridLayoutManager(6, 4, new Insets(10, 10, 10, 10), -1, -1));
        adminPane.addTab("Настройки", adminSettingsPanel);
        final JLabel label22 = new JLabel();
        label22.setText("URL подключения:");
        adminSettingsPanel.add(label22, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer22 = new Spacer();
        adminSettingsPanel.add(spacer22, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("Логин:");
        adminSettingsPanel.add(label23, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Пароль:");
        adminSettingsPanel.add(label24, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveSettingsToButton = new JButton();
        saveSettingsToButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        saveSettingsToButton.setText("Сохранить в...");
        adminSettingsPanel.add(saveSettingsToButton, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectionUrlTextField = new JTextField();
        adminSettingsPanel.add(connectionUrlTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        connectionLoginTextField = new JTextField();
        adminSettingsPanel.add(connectionLoginTextField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        connectionPasswordField = new JPasswordField();
        adminSettingsPanel.add(connectionPasswordField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningOrange16.png")));
        label25.setText("");
        label25.setToolTipText("Важная настройка системы. Будьте осторожны, применяя изменения.");
        adminSettingsPanel.add(label25, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer23 = new Spacer();
        adminSettingsPanel.add(spacer23, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adminConstantsPanel = new JPanel();
        adminConstantsPanel.setLayout(new GridLayoutManager(3, 4, new Insets(10, 10, 10, 10), -1, -1));
        adminPane.addTab("Константы", adminConstantsPanel);
        constantsTable = new JTable();
        constantsTable.setAutoCreateRowSorter(true);
        adminConstantsPanel.add(constantsTable, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer24 = new Spacer();
        adminConstantsPanel.add(spacer24, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        updateServerConstantsButton = new JButton();
        updateServerConstantsButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/save.png")));
        updateServerConstantsButton.setText("Сохранить");
        adminConstantsPanel.add(updateServerConstantsButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer25 = new Spacer();
        adminConstantsPanel.add(spacer25, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label26 = new JLabel();
        label26.setIcon(new ImageIcon(getClass().getResource("/img/gui/warningOrange16.png")));
        label26.setText("");
        label26.setToolTipText("Важная настройка системы. Будьте осторожны, применяя изменения.");
        adminConstantsPanel.add(label26, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        constantsRefreshButton = new JButton();
        constantsRefreshButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        constantsRefreshButton.setText("Обновить");
        constantsRefreshButton.setToolTipText("Обновить");
        adminConstantsPanel.add(constantsRefreshButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label15.setLabelFor(usernameTextField);
        label17.setLabelFor(nameTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
