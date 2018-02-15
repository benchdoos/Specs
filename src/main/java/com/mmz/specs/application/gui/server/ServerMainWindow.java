package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.gui.common.PasswordChangeWindow;
import com.mmz.specs.dao.entity.UsersEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;

public class ServerMainWindow extends JFrame {
    private static boolean isUnlocked = false;
    Thread onlineUsersThread;
    Thread threadCounterThread;
    Thread serverOnlineCounterThread;
    long onlineSeconds = 0;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel monitorPanel;
    private JList onlineUserList;
    private JLabel onlineUsersCount;
    private JLabel threadsCount;
    private JLabel serverOnlineCountLabel;
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
    private boolean serverOnlineCountLabelCounterShow = true;
    private Date serverStartDate = Calendar.getInstance().getTime();
    private JPanel onlyAdminTabsList[] = new JPanel[]{controlPanel};


    public ServerMainWindow() {
        setContentPane(contentPane);
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

        setUnlocked(false);

        initThreads();


    }

    private void initThreads() {
        threadCounterThread = new Thread(new Runnable() {
            Timer timer = new Timer(1000, e -> {
                threadsCount.setText(Thread.activeCount() + "");
            });

            @Override
            public void run() {
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        threadCounterThread.start();

        serverOnlineCounterThread = new Thread(new Runnable() {
            Timer timer = new Timer(1000, e -> {
                onlineSeconds++;
                if (serverOnlineCountLabelCounterShow) {
                    String text = getServerOnlineString();
                    System.out.println(text);
                    serverOnlineCountLabel.setText(text);
                } else {
                    serverOnlineCountLabel.setText(serverStartDate.toString());
                }
                if (serverOnlineCounterThread.isInterrupted()) {
                    ((Timer) e.getSource()).stop();
                }
            });

            @Override
            public void run() {
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        serverOnlineCounterThread.start();

        onlineUsersThread = new Thread(new Runnable() {
            Timer timer = new Timer(1000, e -> {
                onlineUsersCount.setText(onlineUserList.getModel().getSize() + "");// TODO make manager mby???? or something to update everything
                if (Thread.currentThread().isInterrupted()) {
                    ((Timer) e.getSource()).stop();
                }
            });

            @Override
            public void run() {
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        onlineUsersThread.start();
    }

    String getServerOnlineString() {
        long seconds = onlineSeconds % 60;
        long minutes = (onlineSeconds / 60) % 60;
        long hours = (onlineSeconds / 60 / 60) % 24;
        long days = (onlineSeconds / 60 / 60 / 24);
        return days + "д. " + hours + "ч. " + minutes + "м. " + seconds + "с.";
    }

    private void initKeyboardActions() {
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initGui() {
        //test
        DefaultListModel listModel = new DefaultListModel<>();

        for (int i = 0; i < 20; i++) {
            listModel.addElement("User:" + i);
        }
        onlineUserList.setModel(listModel);

        buttonForceUserDisconnect.addActionListener(e -> {
            onForceUserDisconnect(listModel);
        });

        buttonAdminLock.addActionListener(e -> {
            onButtonAdminLock();
        });

        serverOnlineCountLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onServerOnlineCountLabel();
            }
        });

        refreshPasswordButton.addActionListener(e -> {
            onRefreshPasswordButton();
        });
    }

    private void onForceUserDisconnect(DefaultListModel listModel) {
        if (onlineUserList.getSelectedIndex() >= 0 && onlineUserList.getSelectedIndex() < listModel.getSize()) {
            listModel.remove(onlineUserList.getSelectedIndex());
            if (listModel.getSize() > 0) {
                onlineUserList.setSelectedIndex(0);
            }
        }
    }

    private void onButtonAdminLock() {
        if (ServerMainWindow.isUnlocked) {
            setUnlocked(false);
        } else {
            LoginWindow loginWindow = new LoginWindow(this);
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
        passwordChangeWindow.setVisible(true);
    }

    private void onServerOnlineCountLabel() {
        serverOnlineCountLabelCounterShow = !serverOnlineCountLabelCounterShow;
        if (serverOnlineCountLabelCounterShow) {
            serverOnlineCountLabel.setText(getServerOnlineString());
        } else {
            serverOnlineCountLabel.setText(serverStartDate.toString());
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
        onlineUsersThread.interrupt();
        serverOnlineCounterThread.interrupt();
        threadCounterThread.interrupt();
        super.dispose();
    }
}
