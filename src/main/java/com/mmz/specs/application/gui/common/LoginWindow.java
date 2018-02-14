package com.mmz.specs.application.gui.common;

import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.entity.UsersEntity;

import javax.imageio.IIOImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField loginTextField;
    private JTextField passwordTextField;

    public LoginWindow(Window parent) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/security-on.png")));
        setTitle("Вход");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setLocation(FrameUtils.getFrameOnParentCenterLocationPoint(parent, this));
    }

    public UsersEntity getAuthorizedUser() {
        UsersEntity result = new UsersEntity();
        result.setUsername(loginTextField.getText());
        result.setPassword(passwordTextField.getText());
        //TODO check with db... load... get full user... and only then return back
        result.setAdmin(true);
        return result;
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
