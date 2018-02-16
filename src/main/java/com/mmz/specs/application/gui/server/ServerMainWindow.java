package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SystemUtils;
import com.mmz.specs.dao.entity.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.CentralProcessor;
import oshi.software.os.NetworkParams;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.*;

public class ServerMainWindow extends JFrame {

    private static final int MONITORING_TIMER_DELAY = 1000;
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
    private JTextField connectionUrlTextField;
    private JTextField connectionLoginTextField;
    private JPasswordField connectionPasswordTextField;
    private JLabel osInfoLabel;
    private JLabel usedProcessMemoryInfoLabel;
    private JLabel processorInfoLabel;
    private JLabel temperatureInfoLabel;
    private JLabel networkNameInfoLabel;
    private JLabel ipAdressInfoLabel;
    private JLabel totalMemoryInfoLabel;
    private JLabel serverConnectionPortInfoLabel;
    private JLabel jvmInfoLabel;
    private JTable constantsTable;
    private JButton updateServerConstantsButton;
    private JLabel usedProcessCpuInfoLabel;
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

            Timer timer = new Timer(MONITORING_TIMER_DELAY, e -> {
                updateOnlineUsersCount(e);
                updateServerOnlineTimeLabel();
                updateActiveThreadCounterLabel();
                updateProcessorInfoLabel();
                updateUsedProcessCpuInfoLabel();
                updateUsedJvmMemoryInfoLabel();
                updateTemperatureInfoLabel();
            });

            private void updateTemperatureInfoLabel() {
                final String DEGREE = "\u00b0";

                if (getCpuTemperature() > 90.0d) {
                    temperatureInfoLabel.setForeground(Color.RED);
                } else {
                    temperatureInfoLabel.setForeground(Color.BLACK);
                }

                if (getCpuTemperature() > 95.0d) {
                    temperatureInfoLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/warning.png")));
                } else {
                    temperatureInfoLabel.setIcon(null);
                }

                temperatureInfoLabel.setText("ЦП: " + getCpuTemperature() + " C" + DEGREE + " VOL: " + getCpuVoltage()
                        + " FAN-SPEED: " + Arrays.toString(getCpuFanSpeeds()));
            }

            private void updateUsedJvmMemoryInfoLabel() {
                String memoryInfo = "JVM: " + getRuntimeUsedMemory() + " / " + getRuntimeTotalMemory() + " МБ. ";
                if (getRuntimeUsedMemory() > (getRuntimeMaxMemory() - 0.2 * getRuntimeMaxMemory())) {
                    usedProcessMemoryInfoLabel.setForeground(Color.RED);
                } else {
                    usedProcessMemoryInfoLabel.setForeground(Color.BLACK);
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
        if (cpuLoad >= 60.0d) {
            processorInfoLabel.setForeground(Color.RED);
        } else {
            processorInfoLabel.setForeground(Color.BLACK);
        }
        processorInfoLabel.setText(physicalProcessorCount + " (" + logicalProcessorCount + ") " + cpuLoadString);
    }

    private void updateUsedProcessCpuInfoLabel() {
        final double cpuUsageByApplication = getCpuUsageByApplication();
        String processInfo = cpuUsageByApplication + "%";
        if (cpuUsageByApplication >= 60.0d) {
            usedProcessCpuInfoLabel.setForeground(Color.RED);
        } else {
            usedProcessCpuInfoLabel.setForeground(Color.BLACK);
        }
        usedProcessCpuInfoLabel.setText(processInfo);

    }

    String getServerOnlineString(long differenceInSeconds) {
        final String MAXIMUM_SUPPORTED_ALIFE_TIME = "> 24855д.";
        if (differenceInSeconds >= 0) {
            long seconds = differenceInSeconds % 60;
            long minutes = (differenceInSeconds / 60) % 60;
            long hours = (differenceInSeconds / 60 / 60) % 24;
            long days = (differenceInSeconds / 60 / 60 / 24);
            return days + "д. " + hours + "ч. " + minutes + "м. " + seconds + "с.";
        } else {
            return MAXIMUM_SUPPORTED_ALIFE_TIME;
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
