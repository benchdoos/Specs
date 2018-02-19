package com.mmz.specs.application.gui.common;

import com.mmz.specs.application.core.security.SecurityManager;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.model.UsersEntity;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class PasswordChangeWindow extends JDialog {
    UsersEntity user;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton generatePasswordButton;
    private JTextField generatedTextField;
    private JLabel usernameLabel;

    public PasswordChangeWindow(UsersEntity user) {
        this.user = user;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/security-shield.png")));
        setTitle("Смена пароля");


        initGui();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        initKeyboardActions();

        pack();
        setMinimumSize(getSize());


    }

    private void initKeyboardActions() {
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initGui() {
        usernameLabel.setText(user.getUsername());


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
    }

    private void onGeneratePassword() {
        String newPassword = SecurityManager.generatePassword();
        passwordField1.setText(newPassword);
        passwordField2.setText(newPassword);
        generatedTextField.setText(newPassword);
    }

    private void onOK() {
        // add your code here
        if (passwordField1.getPassword().length >= SecurityManager.MINIMUM_PASSWORD_LENGTH) {
            if (Arrays.equals(passwordField1.getPassword(), passwordField2.getPassword())) {
                if (SecurityManager.isPasswordStrong(new String(passwordField1.getPassword()))) {
                    //TODO save password for user to db
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

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
