package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SystemUtils;
import com.mmz.specs.dao.entity.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ServerMainWindow extends JFrame {
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final OperatingSystem OPERATING_SYSTEM = SYSTEM_INFO.getOperatingSystem();

    private static final HardwareAbstractionLayer HARDWARE_ABSTRACTION_LAYER = SYSTEM_INFO.getHardware();
    private static final int MEGABYTE = 1024 * 1024;

    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static boolean isUnlocked = false;
    Thread monitorUiUpdateThread;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel monitorPanel;
    private JList onlineUserList;
    private JLabel onlineUsersCountLabel;
    private JLabel threadsCount;
    private JLabel serverOnlineTimeLabel;
    private JButton buttonForceUserDisconnect;
    private JButton buttonPower;
    private JButton buttonAdminLock;
    private JTextPane logTextPane;
    private JPanel controlPanel;
    private JPanel logPanel;
    private JList userList;
    private JButton refreshPasswordButton;
    private JTextField nameTextField;
    private JTextField lastnameTextField;
    private JTextField surnameTextField;
    private JTextField usernameTextField;
    private JCheckBox isEditorCheckBox;
    private JCheckBox isAdminCheckBox;
    private JCheckBox isActiveCheckBox;
    private JComboBox userTypeComboBox;
    private JButton saveButton;
    private JTabbedPane controlPane;
    private JButton addUserButton;
    private JPanel usersControlPanel;
    private JButton buttonUserInfo;
    private JButton restartServerButton;
    private JButton openLogFolderButton;
    private JLabel userIdLabel;
    private JLabel onlineUsersCount2;
    private JButton saveConfigurationToButton;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JLabel osInfoLabel;
    private JLabel usedMemoryInfoLabel;
    private JLabel processorInfoLabel;
    private JLabel temperatureInfoLabel;
    private JLabel networkNameInfoLabel;
    private JLabel ipAdressInfoLabel;
    private JLabel totalMemoryInfoLabel;
    private JLabel serverConnectionPortInfoLabel;
    private JLabel jvmInfoLabel;
    private boolean serverOnlineCountLabelCounterShow = true;
    private Date serverStartDate = Calendar.getInstance().getTime();
    private long serverStartDateSeconds = Calendar.getInstance().getTime().getTime() / 1000;
    private JPanel onlyAdminTabsList[] = new JPanel[]{controlPanel};


    public ServerMainWindow() {
        setContentPane(contentPane);
        setTitle(ApplicationConstants.APPLICATION_NAME + ApplicationConstants.APPLICATION_NAME_POSTFIX_SERVER);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/application/logo.png")));

        initGui();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        initKeyboardActions();

        pack();
        setMinimumSize(getSize());

        setLocation(FrameUtils.getFrameOnCenter(null, this));

        setUnlocked(false);

        initThreads();


    }

    private void initThreads() {
        monitorUiUpdateThread = new Thread(new Runnable() {

            Timer timer = new Timer(1000, e -> {
                updateOnlineUsersCount(e);
                updateServerOnlineTimeLabel();
                updateActiveThreadCounterLabel();
                updateUsedMemoryInfoLabel();
                updateTemperatureInfoLabel();
            });

            private void updateTemperatureInfoLabel() {
                final String DEGREE = "\u00b0";
                Sensors sensors = HARDWARE_ABSTRACTION_LAYER.getSensors();
                double cpuTemperature = sensors.getCpuTemperature();
                double cpuVoltage = sensors.getCpuVoltage();
                int[] fanSpeeds = sensors.getFanSpeeds();

                temperatureInfoLabel.setText("ЦП: " + cpuTemperature + " C" + DEGREE + " VOL: " + cpuVoltage
                        + " FAN-SPEED: " + Arrays.toString(fanSpeeds));
            }

            private void updateUsedMemoryInfoLabel() {
                Runtime runtime = Runtime.getRuntime();


                long runtimeTotalMemory = runtime.totalMemory() / MEGABYTE;
                long runtimeUsedMemory = runtimeTotalMemory - (runtime.freeMemory() / MEGABYTE);

                String memoryInfo = "JVM: " + runtimeUsedMemory + " / " + runtimeTotalMemory + " МБ. ";

                usedMemoryInfoLabel.setText(memoryInfo);
            }

            private void updateActiveThreadCounterLabel() {
                threadsCount.setText(Thread.activeCount() + "");
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

            private void updateOnlineUsersCount(ActionEvent e) {
                onlineUsersCountLabel.setText(onlineUserList.getModel().getSize() + "");// TODO make manager mby???? or something to update everything
                onlineUsersCount2.setText(onlineUserList.getModel().getSize() + "");// TODO make manager mby???? or something to update everything
                if (Thread.currentThread().isInterrupted()) {
                    ((Timer) e.getSource()).stop();
                }
            }

            @Override
            public void run() {
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        monitorUiUpdateThread.start();
    }

    private void updateTotalMemoryLabel() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = HARDWARE_ABSTRACTION_LAYER.getMemory().getTotal() / MEGABYTE;

        long runtimeMaxMemory = runtime.maxMemory() / MEGABYTE;
        totalMemoryInfoLabel.setText("JVM: " + runtimeMaxMemory + " МБ. ОЗУ: " + totalMemory + " МБ.");
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
        processorInfoLabel.setText(physicalProcessorCount + " (" + logicalProcessorCount + ")");
    }

    String getServerOnlineString(long differenceInSeconds) {
        if (differenceInSeconds >= 0) {
            long seconds = differenceInSeconds % 60;
            long minutes = (differenceInSeconds / 60) % 60;
            long hours = (differenceInSeconds / 60 / 60) % 24;
            long days = (differenceInSeconds / 60 / 60 / 24);
            return days + "д. " + hours + "ч. " + minutes + "м. " + seconds + "с.";
        } else {
            return "> 24855д.";
        }
    }

    private void initKeyboardActions() {
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> onButtonAdminLock(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void updateSystemInfoLabel() {
        String osInfo = OPERATING_SYSTEM.getManufacturer() + " " + OPERATING_SYSTEM.getFamily()
                + " " + OPERATING_SYSTEM.getVersion() + " x" + SystemUtils.getRealSystemArch();
        osInfoLabel.setText(osInfo);
    }

    private void initGui() {
        updateSystemInfoLabel();
        updateProcessorInfoLabel();
        updateNetworkInfoPanel();
        updateTotalMemoryLabel();
        updateJvmInfoLabel();

        //test
        DefaultListModel listModel = new DefaultListModel<>();

        for (int i = 0; i < 100; i++) {
            listModel.addElement("User: " + i);
        }
        onlineUserList.setModel(listModel);

        buttonForceUserDisconnect.addActionListener(e -> {
            onForceUserDisconnect(listModel);
        });

        buttonAdminLock.addActionListener(e -> {
            onButtonAdminLock();
        });

        serverOnlineTimeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onServerOnlineCountLabel();
            }
        });

        refreshPasswordButton.addActionListener(e -> {
            onRefreshPasswordButton();
        });

        openLogFolderButton.addActionListener(e -> {
            onOpenLogFolder();
        });
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


    private void onForceUserDisconnect(DefaultListModel listModel) {
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
            if (user != null) { // TODO !=null and admin...
                System.out.println("Hello, " + user.getUsername() + ", " + "p:" + user.getPassword());
                if (user.isAdmin()) {
                    System.out.println("Hello admin");
                    setUnlocked(user.isAdmin());
                }
            }
        }
    }

    private void onRefreshPasswordButton() {
        //TODO get user from list
        UsersEntity user = new UsersEntity();
        user.setUsername("user");
        user.setPassword("somePath");
        PasswordChangeWindow passwordChangeWindow = new PasswordChangeWindow(user);
        passwordChangeWindow.setLocation(FrameUtils.getFrameOnCenter(this, passwordChangeWindow));
        passwordChangeWindow.setVisible(true);
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

    @Override
    public void dispose() {
        monitorUiUpdateThread.interrupt();
        super.dispose();
    }
}
