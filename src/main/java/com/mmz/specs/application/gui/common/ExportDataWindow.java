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
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SystemUtils;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.io.IOManager;
import com.mmz.specs.io.SPTreeIOManager;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.mmz.specs.application.utils.SupportedExtensionsConstants.EXPORT_TREE_EXTENSION;

public class ExportDataWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private ProgressManager progressManager = new ProgressManager();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel exportDateLabel;
    private JProgressBar currentProgressBar;
    private JLabel operationInfoLabel;
    private JRadioButton exportSPTRadioButton;
    private JRadioButton exportGDBRadioButton;
    private JRadioButton exportSQLRadioButton;
    private JRadioButton exportAllDataRadioButton;
    private JProgressBar totalProgressBar;
    private JTextField filePathTextField;
    private JButton browseButton;
    private MODE selectedMode = MODE.SPT;
    private Timer progressTimer;
    private String datePattern = "dd.MM.yyyy HH.mm";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
    private Thread managerThread;

    private IOManager manager = null;

    public ExportDataWindow() {
        initGui();

        initPanel();

        initTimers();

        initListeners();

        initKeyBindings();
        pack();
        setMinimumSize(getSize());
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
        contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setMnemonic('Т');
        buttonCancel.setDisplayedMnemonicIndex(1);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        currentProgressBar = new JProgressBar();
        currentProgressBar.setStringPainted(true);
        panel3.add(currentProgressBar, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Текущая операция:");
        panel3.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Прогресс:");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalProgressBar = new JProgressBar();
        totalProgressBar.setStringPainted(true);
        panel3.add(totalProgressBar, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        operationInfoLabel = new JLabel();
        operationInfoLabel.setText("    ");
        panel3.add(operationInfoLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Режим экспорта:");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportSPTRadioButton = new JRadioButton();
        exportSPTRadioButton.setSelected(true);
        exportSPTRadioButton.setText("Экспорт дерева для локального просмотра (*.SPT)");
        exportSPTRadioButton.setMnemonic('S');
        exportSPTRadioButton.setDisplayedMnemonicIndex(43);
        exportSPTRadioButton.setToolTipText("Экспортируемые данные можно переносить на другие компьютеры и открывать их локально");
        panel5.add(exportSPTRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportGDBRadioButton = new JRadioButton();
        exportGDBRadioButton.setEnabled(false);
        exportGDBRadioButton.setText("Экспорт данных БД (*.GDB)");
        exportGDBRadioButton.setMnemonic('G');
        exportGDBRadioButton.setDisplayedMnemonicIndex(21);
        exportGDBRadioButton.setToolTipText("Экспортируется файл БД");
        panel5.add(exportGDBRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportSQLRadioButton = new JRadioButton();
        exportSQLRadioButton.setEnabled(false);
        exportSQLRadioButton.setText("Экспорт данных БД (*.SQL)");
        exportSQLRadioButton.setMnemonic('Q');
        exportSQLRadioButton.setDisplayedMnemonicIndex(22);
        exportSQLRadioButton.setToolTipText("Экспортируется SQL файл для создания БД (dump)");
        panel5.add(exportSQLRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportAllDataRadioButton = new JRadioButton();
        exportAllDataRadioButton.setEnabled(false);
        exportAllDataRadioButton.setText("Экспорт всех данных для переноса (*.SPB)");
        exportAllDataRadioButton.setMnemonic('B');
        exportAllDataRadioButton.setDisplayedMnemonicIndex(38);
        exportAllDataRadioButton.setToolTipText("Экспортируются все данные: файл базы данных, данные FTP");
        panel5.add(exportAllDataRadioButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Дата создания:");
        panel4.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportDateLabel = new JLabel();
        exportDateLabel.setText("нет данных");
        panel4.add(exportDateLabel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Местоположение:");
        panel4.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filePathTextField = new JTextField();
        panel4.add(filePathTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        browseButton = new JButton();
        browseButton.setText("Обзор");
        panel4.add(browseButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        contentPane.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(exportAllDataRadioButton);
        buttonGroup.add(exportAllDataRadioButton);
        buttonGroup.add(exportGDBRadioButton);
        buttonGroup.add(exportSPTRadioButton);
        buttonGroup.add(exportSQLRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    @Override
    public void dispose() {
        progressTimer.stop();
        super.dispose();
    }

    private void enableControls(boolean enable) {
        filePathTextField.setEnabled(enable);
        browseButton.setEnabled(enable);
        buttonOK.setEnabled(enable);
    }

    private void exportTree() {
        manager = new SPTreeIOManager(ServerDBConnectionPool.getInstance().getSession(), progressManager);
        Runnable runnable = () -> {
            final String filePath = filePathTextField.getText() + exportDateLabel.getText() + EXPORT_TREE_EXTENSION;
            try {
                manager.exportData(new File(filePath));
                if (!Thread.currentThread().isInterrupted()) {
                    dispose();

                    if (SystemUtils.isWindows()) {
                        Runtime.getRuntime().exec("explorer.exe /select," + filePath);
                    } else if (SystemUtils.isUnix()) {
                        final File folder = new File(filePath).getParentFile();
                        try {
                            Desktop.getDesktop().open(folder);
                        } catch (IOException e) {
                            log.warn("Could not open folder: {}, showing message to user", e);
                            JOptionPane.showMessageDialog(this, "Файл успешно экспортирован:\n" +
                                    folder, "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } catch (IOException | ZipException e) {
                log.warn("Could not export tree to file: {}", filePath, e);
                JOptionPane.showMessageDialog(this, "Во время экспорта произошла ошибка:\n"
                        + e.getLocalizedMessage(), "Ошибка при экспорте данных", JOptionPane.WARNING_MESSAGE);
                progressManager.reset();
                enableControls(true);
            }
        };
        managerThread = new Thread(runnable);
        managerThread.start();
    }

    private MODE getSelectedMode() {
        if (exportSPTRadioButton.isSelected()) {
            return MODE.SPT;
        } else if (exportGDBRadioButton.isSelected()) {
            return MODE.GDB;
        } else if (exportSQLRadioButton.isSelected()) {
            return MODE.SQL;
        } else if (exportAllDataRadioButton.isSelected()) {
            return MODE.SPB;
        } else {
            //default
            return MODE.SPT;
        }
    }

    private void initGui() {
        setTitle("Экспорт данных Базы Данных");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/export.png")));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void initListeners() {
        browseButton.addActionListener(e -> onBrowseFolder());

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initPanel() {
        exportDateLabel.setText(dateFormatter.format(Calendar.getInstance().getTime()));
        filePathTextField.setText(ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION);
    }

    private void initTimers() {
        progressTimer = new Timer(250, e -> {
            totalProgressBar.setValue(progressManager.getTotalProgress());
            totalProgressBar.setIndeterminate(progressManager.isTotalIndeterminate());
            totalProgressBar.setMaximum(progressManager.getTotalMaxValue());

            currentProgressBar.setValue(progressManager.getCurrentProgress());
            final boolean currentIndeterminate = progressManager.isCurrentIndeterminate();
            currentProgressBar.setIndeterminate(currentIndeterminate);
            currentProgressBar.setStringPainted(!currentIndeterminate);

            operationInfoLabel.setText(progressManager.getText());
        });
        progressTimer.setRepeats(true);
        progressTimer.restart();
    }

    private void onBrowseFolder() {
        JFileChooser chooser = new JFileChooser(new File(ApplicationConstants.USER_HOME_LOCATION));
        if (!new File(filePathTextField.getText()).exists() && new File(filePathTextField.getText()).isFile()) {
            chooser.setCurrentDirectory(new File(ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION));
        } else {
            chooser.setCurrentDirectory(new File(filePathTextField.getText()));
        }
        chooser.setDialogTitle("Выбор папки назначения");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final String absolutePath = chooser.getSelectedFile().getAbsolutePath();
            log.debug("User changed destination folder to: {}", absolutePath);
            filePathTextField.setText(absolutePath + File.separator);
        }
    }

    private void onCancel() {
        int result = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите остановить экспорт данных?",
                "Подтверждение действий", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == 0) {
            log.warn("User canceled export!");
            if (managerThread != null) {
                if (managerThread.isAlive()) {
                    managerThread.interrupt();
                    enableControls(true);
                } else {
                    dispose();
                }
            } else {
                dispose();
            }
        }
    }

    private void onOK() {
        enableControls(false);
        selectedMode = getSelectedMode();
        log.info("Starting exporting data. Export mode is: " + selectedMode);
        startExport();

    }

    private void startExport() {
        switch (selectedMode) {
            case SPT:
                exportTree();
                break;
            default:
                exportTree();
                break;
        }
    }

    private enum MODE {SPT, GDB, SQL, SPB}

}
