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
import com.mmz.specs.application.core.updater.ApplicationVersion;
import com.mmz.specs.application.core.updater.Updater;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UpdaterWindow extends JFrame {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Timer timer;
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JLabel updateVersionInfoLabel;
    private JLabel updateTitleInfoLabel;
    private JButton versionInfoButton;
    private JLabel currentSpeedLabel;
    private JLabel downloadingFileSizeLabel;
    private JLabel serverFileSizeLabel;

    public UpdaterWindow(ApplicationVersion serverVersion) {
        setContentPane(contentPane);
        setTitle("Обновление");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/update.png")));


        if (serverVersion != null) {
            updateVersionInfoLabel.setText(serverVersion.getVersion());
            updateTitleInfoLabel.setText(serverVersion.getUpdateTitle());
        }

        versionInfoButton.setEnabled(serverVersion != null);
        versionInfoButton.addActionListener(e -> {
            if (serverVersion != null) {
                JOptionPane.showMessageDialog(this,
                        serverVersion.getUpdateInfo(), "Информация о версии: " + serverVersion.getVersion(), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        timer = new Timer(1000, new ActionListener() {
            final int kilobyte = 1024;
            final int megabyte = kilobyte * kilobyte;

            long prevDownloadedSize = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverVersion != null) {
                        progressBar.setIndeterminate(false);
                        final File downloadingFile = Updater.INSTALLER_FILE;
                        final long serverSize = serverVersion.getSize();

                        final long downloadingFileSize = downloadingFile.length();
                        final int v = (int) (((double) downloadingFileSize / serverSize) * 100);
                        progressBar.setValue(v);

                        final long diff = (downloadingFileSize - prevDownloadedSize) / kilobyte;
                        currentSpeedLabel.setText(diff + "");
                        prevDownloadedSize = downloadingFileSize;

                        final double v1 = (double) downloadingFileSize / megabyte;
                        downloadingFileSizeLabel.setText(CommonUtils.round(v1, 2) + "");

                        final double v2 = (double) serverSize / megabyte;
                        serverFileSizeLabel.setText(CommonUtils.round(v2, 2) + "");
                    }
                } catch (Exception ex) {
                    log.warn("Could not update info about download", e);
                    progressBar.setIndeterminate(true);
                }
            }
        });
        timer.setRepeats(true);
        timer.start();

        setResizable(false);
        pack();
        setMinimumSize(new Dimension(getSize().width + 150, getSize().height));
    }

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Выполняется загрузка обновления:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateVersionInfoLabel = new JLabel();
        updateVersionInfoLabel.setText("нет данных");
        panel1.add(updateVersionInfoLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateTitleInfoLabel = new JLabel();
        updateTitleInfoLabel.setText("нет данных");
        panel1.add(updateTitleInfoLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Новая версия:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Название:");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel2.add(progressBar, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        versionInfoButton = new JButton();
        versionInfoButton.setBorderPainted(false);
        versionInfoButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/info.png")));
        versionInfoButton.setIconTextGap(0);
        versionInfoButton.setMargin(new Insets(2, 2, 2, 2));
        versionInfoButton.setOpaque(false);
        versionInfoButton.setText("");
        versionInfoButton.setToolTipText("Информация о извещениях (CTRL+I)");
        panel2.add(versionInfoButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Скорость:");
        panel1.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        currentSpeedLabel = new JLabel();
        currentSpeedLabel.setText("0");
        panel3.add(currentSpeedLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Кб/сек");
        panel3.add(label5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        downloadingFileSizeLabel = new JLabel();
        downloadingFileSizeLabel.setText("0");
        panel5.add(downloadingFileSizeLabel);
        final JLabel label6 = new JLabel();
        label6.setText("/");
        panel5.add(label6);
        serverFileSizeLabel = new JLabel();
        serverFileSizeLabel.setText("0");
        panel5.add(serverFileSizeLabel);
        final JLabel label7 = new JLabel();
        label7.setText("МБ");
        panel4.add(label7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Загружено:");
        panel1.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
