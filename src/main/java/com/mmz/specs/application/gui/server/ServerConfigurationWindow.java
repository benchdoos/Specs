package com.mmz.specs.application.gui.server;

import com.mmz.specs.application.managers.CommonSettingsManager;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class ServerConfigurationWindow extends JDialog {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public boolean isCorrect = false;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField; // create comboBox when... sometime
    private JButton buttonBrowse;

    public ServerConfigurationWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Конфигурация файла настроек сервера");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/interfaces/usb.png")));
        initGui();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        initKeyboardActions();

        pack();
    }

    private void initKeyboardActions() {
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initGui() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        buttonBrowse.addActionListener(e -> onBrowse());

        initTextField();
    }

    private void initTextField() {
        try {
            textField.setText(CommonSettingsManager.getServerSettingsFilePath());
        } catch (IOException e) {log.warn("Could not get Server Settings filepath",e);}
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберете файл конфигурации (.xml)");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        FileFilter fileFilter = new FileNameExtensionFilter("Файл конфигурации", "xml");
        chooser.setFileFilter(fileFilter);
        int returnValue = chooser.showDialog(this, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isFile()) {
                System.out.println("You selected the file: " + chooser.getSelectedFile());
                textField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private void onOK() {
        if (checkInput()) {
            try {
                CommonSettingsManager.setServerSettingsFilePath(textField.getText());
                isCorrect = true;
                dispose();
            } catch (IOException e) {
                log.warn("Could not save location of selected file", e);
                JOptionPane.showMessageDialog(this, "Не удалось сохранить местоположение конфигурационного файла\n" + e.getLocalizedMessage());
            }
        }

    }

    private boolean checkInput() {
        String path = textField.getText();
        if (path != null) {
            if (!path.isEmpty()) {
                File file = new File(path);
                if (!file.exists()) {
                    FrameUtils.shakeFrame(this);
                    JOptionPane.showMessageDialog(this, "Файл конфигурации сервера не существует\n" + file.getAbsolutePath(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                } else {
                    JOptionPane.showMessageDialog(this, "Файл конфигурации сервера найден", "Успех", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                FrameUtils.shakeFrame(this);
                JOptionPane.showMessageDialog(this, "Файл конфигурации сервера не указан", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Файл конфигурации сервера не указан", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void onCancel() {
        // add your code here if necessary
        System.exit(0); //TODO Fix this was set to prevent server start without settings file
        dispose();
    }
}
