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

package com.mmz.specs.application.gui.common;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.security.SecurityManager;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.dao.UsersDaoImpl;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.UsersService;
import com.mmz.specs.service.UsersServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class PasswordChangeWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private UsersEntity user;
    private Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton generatePasswordButton;
    private JTextField generatedTextField;
    private JLabel usernameLabel;

    public PasswordChangeWindow(UsersEntity user, Session session) {
        this.user = user;
        this.session = session;

        initGui();

        initListeners();

        initKeyBindings();

        pack();
        setMinimumSize(getSize());
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initGui() {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/securityShield16.png")));
        setTitle("Смена пароля");

        usernameLabel.setText(user.getUsername());


    }

    private FocusListener getPasswordFieldListener(JPasswordField passwordField) {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                int position = passwordField.getPassword().length - 1;
                if (position < 0) position = 0;
                passwordField.setCaretPosition(position);
            }
        };
    }

    private void onGeneratePassword() {
        String newPassword = SecurityManager.generatePassword();
        passwordField1.setText(newPassword);
        passwordField2.setText(newPassword);
        generatedTextField.setText(newPassword);
    }

    public UsersEntity getUserWithNewPassword() {
        return user;
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        generatePasswordButton.addActionListener(e -> onGeneratePassword());

        passwordField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                generatedTextField.setText("");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                generatedTextField.setText("");

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                generatedTextField.setText("");
            }
        });

        passwordField1.addFocusListener(getPasswordFieldListener(passwordField1));

        passwordField2.addFocusListener(getPasswordFieldListener(passwordField2));
    }

    private void onOK() {
        if (passwordField1.getPassword().length >= SecurityManager.MINIMUM_PASSWORD_LENGTH) {
            if (Arrays.equals(passwordField1.getPassword(), passwordField2.getPassword())) {
                if (SecurityManager.isPasswordStrong(new String(passwordField1.getPassword()))) {
                    user.setPassword(SecurityManager.encryptPassword(Arrays.toString(passwordField1.getPassword())));

                    updateUser(user);

                    dispose();
                } else {
                    showPasswordInstallationErrorMessage("Слишком простой пароль!\n" +
                            "Пароль должен быть больше " + SecurityManager.MINIMUM_PASSWORD_LENGTH + " символов,\n" +
                            "должен содержать буквы разного регистра,\n" +
                            "спец-символы или цифры.");
                }
            } else {
                showPasswordInstallationErrorMessage("Пароли должны совпадать");
            }
        } else {
            showPasswordInstallationErrorMessage("Слишком простой пароль!\n" +
                    "Пароль должен быть больше " + SecurityManager.MINIMUM_PASSWORD_LENGTH + " символов,\n" +
                    "должен содержать буквы разного регистра,\n" +
                    "спец-символы или цифры.");
        }
    }

    private void showPasswordInstallationErrorMessage(String s) {
        FrameUtils.shakeFrame(this);
        JOptionPane.showMessageDialog(this, s, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void updateUser(UsersEntity user) {
        if (session.getTransaction().getStatus() == TransactionStatus.NOT_ACTIVE) {
            try {
                session.getTransaction().begin();
                UsersService service = new UsersServiceImpl(new UsersDaoImpl(session));
                service.updateUser(user);
                session.getTransaction().commit();
                log.info("User password successfully updated ({})", user.getUsername());
            } catch (RuntimeException e) {
                log.warn("Could not update user {}", user, e);
                JOptionPane.showMessageDialog(this, "Не удалось обновить пароль",
                        "Ошибка обновления", JOptionPane.ERROR_MESSAGE);
                try {
                    session.getTransaction().rollback();
                } catch (RuntimeException ex) {
                    log.warn("Could not rollback transaction", ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Нельзя сменить пароль, пока проходит другая транзакция", "Ошибка обновления",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onCancel() {
        user = null;
        dispose();
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
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setMnemonic('Т');
        buttonCancel.setDisplayedMnemonicIndex(1);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        passwordField2 = new JPasswordField();
        panel3.add(passwordField2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Пользователь:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setText("username");
        panel3.add(usernameLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generatePasswordButton = new JButton();
        generatePasswordButton.setText("Генерировать");
        generatePasswordButton.setMnemonic('Г');
        generatePasswordButton.setDisplayedMnemonicIndex(0);
        panel3.add(generatePasswordButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generatedTextField = new JTextField();
        generatedTextField.setEditable(false);
        panel3.add(generatedTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordField1 = new JPasswordField();
        panel3.add(passwordField1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Новый пароль:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Повторите пароль:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label3.setLabelFor(passwordField2);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
