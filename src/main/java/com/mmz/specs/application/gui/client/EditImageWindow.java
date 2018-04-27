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
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.DetailEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class EditImageWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel detailIconLabel;
    private JButton uploadButton;
    private JButton deleteButton;
    private DetailEntity detailEntity;

    public EditImageWindow(DetailEntity detailEntity) {
        this.detailEntity = detailEntity;

        if (detailEntity == null) {
            return;
        }
        initGui();

        initListeners();

        updateIcon();

        initKeyBindings();

        pack();
        setResizable(false);
    }

    private void initGui() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/pictureEdit64.png")));
        setTitle("Редактирование изображения");
    }

    private void updateIcon() {
        FtpUtils ftp = FtpUtils.getInstance();
        BufferedImage image = ftp.getImage(detailEntity.getId());

        if (image != null) {
            BufferedImage scaledImage = Scalr.resize(image, 128);
            detailIconLabel.setIcon(new ImageIcon(scaledImage));
            detailIconLabel.setText("");

            FrameUtils.removeAllComponentListeners(detailIconLabel);

            detailIconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    FrameUtils.onShowImage(FrameUtils.findWindow(EditImageWindow.super.getRootPane()), image, "Изображение " + detailEntity.getCode());
                }
            });
        } else {
            detailIconLabel.setIcon(null);
            detailIconLabel.setText("Нет изображения");
        }
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());
        uploadButton.addActionListener(e -> onUpload());
        deleteButton.addActionListener(e -> onDelete());
    }

    private void onDelete() {
        FtpUtils ftp = FtpUtils.getInstance();
        try {
            ftp.deleteImage(detailEntity.getId());
        } catch (IOException e) {
            log.warn("Could not delete image for detail: " + detailEntity, e);
        }
        updateIcon();
    }

    private void onUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберете изображение (jpg)");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        FileFilter fileFilter = new FileNameExtensionFilter("Изображение png", "jpg");
        chooser.setFileFilter(fileFilter);
        int returnValue = chooser.showDialog(this, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isFile()) {
                System.out.println("You selected the file: " + chooser.getSelectedFile());
                try {
                    FtpUtils ftp = FtpUtils.getInstance();
                    final BufferedImage image = ftp.getImage(detailEntity.getId());
                    if (image != null) {
                        ftp.deleteImage(detailEntity.getId());
                    }
                    ftp.uploadImage(detailEntity.getId(), chooser.getSelectedFile().getAbsolutePath());
                } catch (IOException e) {
                    log.warn("Could not upload image for detail: " + detailEntity, e);

                    JOptionPane.showMessageDialog(this, "Не удалось загрузить изображение:\n" +
                            chooser.getSelectedFile().getAbsolutePath() +
                            "\n" + e.getLocalizedMessage(), "Ошибка загрузки", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        updateIcon();
    }

    private void onOK() {

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
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detailIconLabel = new JLabel();
        detailIconLabel.setForeground(new Color(-10395295));
        detailIconLabel.setHorizontalAlignment(0);
        detailIconLabel.setHorizontalTextPosition(0);
        detailIconLabel.setText("Нет изображения");
        panel3.add(detailIconLabel, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        uploadButton = new JButton();
        uploadButton.setText("Загрузить");
        panel3.add(uploadButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Удалить");
        panel3.add(deleteButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}