package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SystemUtils;
import com.mmz.specs.model.UsersEntity;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.*;

public class ServerMainWindow extends JFrame {

    private static final int MONITORING_TIMER_DELAY = 1000;
    private static final int GRAPHICS_LENGTH = 60;
    private static int caretPosition = 0;
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static boolean isUnlocked = false;
    private Timer monitorUiUpdateTimer;
    private final String DEGREE = "\u00b0";
    private Thread monitorUiUpdateThread;
    private ArrayList<Double> memoryValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Double> cpuValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Double> cpuServerValues = new ArrayList<>(GRAPHICS_LENGTH);
    private ArrayList<Double> cpuTemperatureValue = new ArrayList<>(GRAPHICS_LENGTH);
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel monitorPanel;
    private JList<Object> onlineUserList;
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
    private JPanel graphicsPanel;
    private JLabel voltageInfoLabel;
    private JLabel fanSpeedInfoLabel;
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
        monitorUiUpdateTimer = new Timer(MONITORING_TIMER_DELAY, new ActionListener() {
            long counter = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                long t0 = System.nanoTime();
                updateOnlineUsersCount(e);
                long t1 = System.nanoTime();
                updateServerOnlineTimeLabel();
                long t2 = System.nanoTime();
                updateActiveThreadCounterLabel();
                long t3 = System.nanoTime();
                updateProcessorInfoLabel();
                long t4 = System.nanoTime();
                updateUsedProcessCpuInfoLabel();
                long t5 = System.nanoTime();
                updateUsedJvmMemoryInfoLabel();
                long t6 = System.nanoTime();

                if (counter % 10 == 0) {
                    updateTemperatureInfoLabel();
                    updateVoltageInfoLabel();
                    updateFanSpeedInfoLabel();
                }

                System.out.println(">>> updateOnlineUsersCount:" + getTime(t0, t1) + " updateServerOnlineTimeLabel:" + getTime(t1, t2)
                        + " updateActiveThreadCounterLabel:" + getTime(t2, t3) + " updateProcessorInfoLabel:" + getTime(t3, t4)
                        + " updateUsedProcessCpuInfoLabel:" + getTime(t4, t5) + " updateUsedJvmMemoryInfoLabel:" + getTime(t5, t6));
                counter++;
            }

            private long getTime(long t1, long t2) {
                return (t2 - t1) / 1000;
            }

            private void updateTemperatureInfoLabel() {
                final String DEGREE = "\u00b0";

                final double cpuTemperature = getCpuTemperature();

                if (cpuTemperature > 90.0d) {
                    temperatureInfoLabel.setForeground(Color.RED);
                } else {
                    temperatureInfoLabel.setForeground(Color.BLACK);
                }

                if (cpuTemperature > 95.0d) {
                    temperatureInfoLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/warning.png")));
                } else {
                    temperatureInfoLabel.setIcon(null);
                }

                cpuTemperatureValue = updateGraphicValue(cpuTemperatureValue, getCpuTemperature());

                temperatureInfoLabel.setText("ЦП: " + cpuTemperature + " C" + DEGREE);
            }

            private void updateFanSpeedInfoLabel() {
                fanSpeedInfoLabel.setText(Arrays.toString(getCpuFanSpeeds()));
            }

            private void updateVoltageInfoLabel() {
                voltageInfoLabel.setText(Double.toString(getCpuVoltage()));
            }

            private void updateUsedJvmMemoryInfoLabel() {
                String memoryInfo = "JVM: " + getRuntimeUsedMemory() + " / " + getRuntimeTotalMemory() + " МБ. ";
                double usedMemory = CommonUtils.round(getRuntimeUsedMemory() / (double) getRuntimeMaxMemory() * 100, 2);

                memoryValues = updateGraphicValue(memoryValues, usedMemory);

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
            processorInfoLabel.setForeground(Color.RED);
        } else {
            processorInfoLabel.setForeground(Color.BLACK);
        }
        processorInfoLabel.setText(physicalProcessorCount + " (" + logicalProcessorCount + ") " + cpuLoadString);
    }

    private void updateUsedProcessCpuInfoLabel() {
        final double cpuUsageByApplication = getCpuUsageByApplication();
        String processInfo = cpuUsageByApplication + "%";

        updateGraphicValue(cpuServerValues, getCpuUsageByApplication());


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
        DefaultListModel<Object> listModel = new DefaultListModel<>();

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

    private void createGraphics() {
        long t1 = System.nanoTime();
        XYChart chart = getChart(graphicsPanel.getWidth(), graphicsPanel.getHeight());
        XChartPanel<XYChart> graphXChartPanel = new XChartPanel<>(chart);
        if (graphicsPanel.getComponents().length > 0) {
            graphicsPanel.removeAll();
        }

        graphicsPanel.add(graphXChartPanel);
        long t2 = System.nanoTime();
        System.out.println("--> " + (t2 - t1) / 1000);
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

    public XYChart getChart(int width, int height) {

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(height).yAxisTitle("Нагрузка (%)").theme(Styler.ChartTheme.Matlab).build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        chart.getStyler().setYAxisDecimalPattern("###.##");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(1);
        chart.getStyler().setLegendSeriesLineLength(1);

        // Series
        // @formatter:off

        ArrayList<Double> xAges = getGraphicXAges();

        ArrayList<Double> memoryData = this.memoryValues;

        ArrayList<Double> cpuData = this.cpuValues;

        ArrayList<Double> cpuServerData = this.cpuServerValues;

        ArrayList<Double> cpuTemperatureData = this.cpuTemperatureValue;
        // @formatter:on

        System.out.println("sizes: " + xAges.size() + ' ' + memoryData.size() + ' ' + cpuData.size());


        XYSeries memory = chart.addSeries("ОЗУ", xAges, memoryData);
        memory.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        memory.setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN);


        chart.addSeries("ЦП (" + DEGREE + "C)", xAges, cpuTemperatureData).setMarker(SeriesMarkers.NONE).setLineColor(Color.ORANGE);
        chart.addSeries("ЦП (все)", xAges, cpuData).setMarker(SeriesMarkers.NONE).setLineColor(Color.RED);
        chart.addSeries("ЦП (сервер)", xAges, cpuServerData).setMarker(SeriesMarkers.NONE).setLineColor(Color.BLUE);


        return chart;
    }

    private ArrayList<Double> getGraphicXAges() {
        ArrayList<Double> result = new ArrayList<>(GRAPHICS_LENGTH);
        for (int i = 0; i < GRAPHICS_LENGTH; i++) {
            result.add((double) i);
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


    private ArrayList<Double> updateGraphicValue(ArrayList<Double> oldValues, double newValue) {
        if (caretPosition >= oldValues.size()) caretPosition = 0;
        if (oldValues.size() != GRAPHICS_LENGTH) {
            for (int i = 0; i < GRAPHICS_LENGTH; i++) {
                oldValues.add(0d);
            }
        }
        oldValues.set(caretPosition, newValue);
        return oldValues;
    }
}
