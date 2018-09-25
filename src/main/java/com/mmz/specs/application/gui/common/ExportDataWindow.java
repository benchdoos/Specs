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
import com.mmz.specs.io.IOManager;
import com.mmz.specs.io.SPTreeIOManager;
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
    public ProgressManager progressManager = new ProgressManager();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel exportDateLabel;
    private JCheckBox allDataExportCheckBox;
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
    private String datePattern = "dd.MM.yyyy";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    private IOManager manager = null;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public ExportDataWindow() {
        initGui();

        initPanel();

        initTimers();

        initListeners();

        initKeyBindings();
        pack();
        setMinimumSize(getSize());
    }

    private void initGui() {
        setTitle("Экспорт данных Базы Данных");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/menu/export.png")));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }

    private void initPanel() {
        exportDateLabel.setText(dateFormatter.format(Calendar.getInstance().getTime()));
        filePathTextField.setText(ApplicationConstants.APPLICATION_EXPORT_FOLDER_LOCATION);
    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void initTimers() {
        progressTimer = new Timer(250, e -> {
            totalProgressBar.setValue(progressManager.getTotalProgress());
            totalProgressBar.setIndeterminate(progressManager.isTotalIndeterminate());
            totalProgressBar.setMaximum(progressManager.getTotalMaxValue());

            currentProgressBar.setValue(progressManager.getCurrentProgress());
            currentProgressBar.setIndeterminate(progressManager.isCurrentIndeterminate());
        });
        progressTimer.setRepeats(true);
        progressTimer.restart();
    }

    private void initListeners() {
        browseButton.addActionListener(e -> onBrowseFolder());

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void onBrowseFolder() {
        JFileChooser chooser = new JFileChooser();
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

    private void onOK() {
        buttonOK.setEnabled(false);
        selectedMode = getSelectedMode();
        log.info("Starting exporting data. Export mode is: " + selectedMode);
        startExport();

    }

    private void startExport() {
        switch (selectedMode) {
            case SPT:
                exportTree(allDataExportCheckBox.isSelected());
                break;
            default:
                exportTree(allDataExportCheckBox.isSelected());
                break;
        }
    }

    private void exportTree(boolean allDataExport) {
        manager = new SPTreeIOManager(progressManager);
        Runnable runnable = () -> {
            final String filePath = filePathTextField.getText() + exportDateLabel.getText() + EXPORT_TREE_EXTENSION;
            try {
                manager.exportData(new File(filePath));
            } catch (IOException e) {
                log.warn("Could not export tree to file: {}", filePath, e);
                JOptionPane.showMessageDialog(this, "Во время экспорта произошла ошибка:\n"
                        + e.getLocalizedMessage(), "Ошибка при экспорте данных", JOptionPane.WARNING_MESSAGE);
                resetProgressManager();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
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

    private void resetProgressManager() {
        progressManager.setTotalProgress(0);
        progressManager.setTotalIndeterminate(false);
        progressManager.setCurrentProgress(0);
        progressManager.setCurrentIndeterminate(false);
    }

    private void onCancel() {
        int result = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите остановить экспорт данных?",
                "Подтверждение действий", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == 0) {
            if (manager != null) {
                manager.interrupt();
            }
            dispose();
        }
    }

    @Override
    public void dispose() {
        progressTimer.stop();
        super.dispose();
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
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setMnemonic('Т');
        buttonCancel.setDisplayedMnemonicIndex(1);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportDateLabel = new JLabel();
        exportDateLabel.setText("нет данных");
        panel3.add(exportDateLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Дата создания:");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        allDataExportCheckBox = new JCheckBox();
        allDataExportCheckBox.setSelected(true);
        allDataExportCheckBox.setText("Экспорт всех данных");
        panel3.add(allDataExportCheckBox, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentProgressBar = new JProgressBar();
        currentProgressBar.setStringPainted(true);
        panel3.add(currentProgressBar, new GridConstraints(7, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Текущая операция:");
        panel3.add(label2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Режим экспорта:");
        panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportSPTRadioButton = new JRadioButton();
        exportSPTRadioButton.setSelected(true);
        exportSPTRadioButton.setText("Экспорт дерева для локального просмотра (*.SPT)");
        exportSPTRadioButton.setMnemonic('S');
        exportSPTRadioButton.setDisplayedMnemonicIndex(43);
        exportSPTRadioButton.setToolTipText("Экспортируемые данные можно переносить на другие компьютеры и открывать их локально");
        panel4.add(exportSPTRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportGDBRadioButton = new JRadioButton();
        exportGDBRadioButton.setEnabled(false);
        exportGDBRadioButton.setText("Экспорт данных БД (*.GDB)");
        exportGDBRadioButton.setMnemonic('G');
        exportGDBRadioButton.setDisplayedMnemonicIndex(21);
        exportGDBRadioButton.setToolTipText("Экспортируется файл БД");
        panel4.add(exportGDBRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportSQLRadioButton = new JRadioButton();
        exportSQLRadioButton.setEnabled(false);
        exportSQLRadioButton.setText("Экспорт данных БД (*.SQL)");
        exportSQLRadioButton.setMnemonic('Q');
        exportSQLRadioButton.setDisplayedMnemonicIndex(22);
        exportSQLRadioButton.setToolTipText("Экспортируется SQL файл для создания БД (dump)");
        panel4.add(exportSQLRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportAllDataRadioButton = new JRadioButton();
        exportAllDataRadioButton.setEnabled(false);
        exportAllDataRadioButton.setText("Экспорт всех данных для переноса (*.SPB)");
        exportAllDataRadioButton.setMnemonic('B');
        exportAllDataRadioButton.setDisplayedMnemonicIndex(38);
        exportAllDataRadioButton.setToolTipText("Экспортируются все данные: файл базы данных, данные FTP");
        panel4.add(exportAllDataRadioButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Прогресс:");
        panel3.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalProgressBar = new JProgressBar();
        totalProgressBar.setStringPainted(true);
        panel3.add(totalProgressBar, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        operationInfoLabel = new JLabel();
        operationInfoLabel.setText("    ");
        panel3.add(operationInfoLabel, new GridConstraints(6, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Местоположение:");
        panel3.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filePathTextField = new JTextField();
        panel3.add(filePathTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        browseButton = new JButton();
        browseButton.setText("Обзор");
        panel3.add(browseButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

    private enum MODE {SPT, GDB, SQL, SPB}

}
