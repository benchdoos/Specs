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
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.managers.ClientSettingsManager;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ClientConfigurationWindow extends JDialog {
    private final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private JPanel contentPane;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField serverAddressTextField;
    private JTextField serverPortTextField;
    private JButton testConnectionButton;

    public ClientConfigurationWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(okButton);
        setTitle("Конфигурация подключения к серверу");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/networkConnections128.png")));

        initTextFields();

        initListeners();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setResizable(false);
    }

    private void initListeners() {
        okButton.addActionListener(e -> onOK());

        cancelButton.addActionListener(e -> onCancel());

        testConnectionButton.addActionListener(e -> onTestConnection());
    }

    private void onCancel() {
        dispose();
    }

    private void initTextFields() {
        serverAddressTextField.setText(ClientSettingsManager.getInstance().getServerAddress());
        serverPortTextField.setText(ClientSettingsManager.getInstance().getServerPort() + "");
    }

    private void onOK() {
        if (areSettingsValid()) {
            try {
                ClientSettingsManager.getInstance().setServerAddress(serverAddressTextField.getText());
                dispose();
            } catch (IOException e) {
                log.warn("Couldn't set settings", e);
                showIncorrectSettingsOptionPaneMessage("Не удалось сохранить настройки: " + e.getLocalizedMessage(), "Ошибка сохранения");
            }
        } else {
            FrameUtils.shakeFrame(this);
            showIncorrectSettingsOptionPaneMessage("Настройки установлены некорректно:\n" +
                    "Адрес сервера не может быть пустым, \n" +
                    "порт должен быть в числовом формате", "Ошибка");
        }
    }

    private void showIncorrectSettingsOptionPaneMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void onTestConnection() {
        if (areSettingsValid()) {
            log.debug("Testing connection to " + serverAddressTextField.getText(), Integer.parseInt(serverPortTextField.getText()));
            if (ClientBackgroundService.getInstance().isConnected()) {
                /*ClientBackgroundService.getInstance().createConnection();*/
                if (ClientBackgroundService.getInstance().testConnection(serverAddressTextField.getText(), Integer.parseInt(serverPortTextField.getText()))) {
                    JOptionPane.showMessageDialog(this, "Сервер " + serverAddressTextField.getText() + ":"
                                    + serverPortTextField.getText() + " успешно найден.",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showIncorrectSettingsOptionPaneMessage("Сервер не найден: "
                            + serverAddressTextField.getText() + ":"
                            + serverPortTextField.getText(), "Ошибка");
                }
            }
        } else {
            log.warn("Incorrect server settings: " + serverAddressTextField.getText(), serverPortTextField.getText());
            showIncorrectSettingsOptionPaneMessage("Настройки установлены некорректно:\n" +
                    "Адрес сервера не может быть пустым, \n" +
                    "порт должен быть в числовом формате", "Ошибка");
        }
    }

    private boolean areSettingsValid() {
        if (serverAddressTextField.getText().isEmpty()) {
            log.warn("Server address should not be empty: [" + serverAddressTextField.getText() + "]");
            return false;
        }
        log.info("Server params are valid: " + serverAddressTextField.getText() + ":" + serverPortTextField.getText());
        return true;
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        okButton = new JButton();
        okButton.setText("OK");
        okButton.setMnemonic('O');
        okButton.setDisplayedMnemonicIndex(0);
        panel2.add(okButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Отмена");
        cancelButton.setMnemonic('О');
        cancelButton.setDisplayedMnemonicIndex(0);
        panel2.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testConnectionButton = new JButton();
        testConnectionButton.setText("Проверить соединение");
        testConnectionButton.setMnemonic('П');
        testConnectionButton.setDisplayedMnemonicIndex(0);
        panel1.add(testConnectionButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Адрес сервера:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Порт сервера:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverAddressTextField = new JTextField();
        panel3.add(serverAddressTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        serverPortTextField = new JTextField();
        serverPortTextField.setEditable(false);
        panel3.add(serverPortTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
