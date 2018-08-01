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

package com.mmz.specs.application.gui.panels;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.model.DetailEntity;
import org.imgscalr.Scalr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class DetailInfoPanel extends JPanel {
    private JPanel contentPane;
    private JLabel detailIconLabel;
    private JPanel detailInfoPanel;
    private JLabel numberLabel;
    private JLabel titleLabel;
    private JLabel unitLabel;
    private JLabel finishedWeightLabel;
    private JLabel workpieceWeightLabel;
    private JLabel techProcessLabel;
    private JLabel isActiveLabel;
    private JPanel panel;
    private DetailEntity entity;

    DetailInfoPanel(DetailEntity entity) {
        this.entity = entity;

        initGui();

        updateDetailInfoPanel();
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        Timer updateIconTimer = new Timer(500, e -> updateIcon());
        updateIconTimer.setRepeats(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateIconTimer.restart();
            }
        });
    }

    private void updateDetailInfoPanel() {
        updateDetailInfoPanelWithEmptyEntity();
        if (entity != null) {
            updateDetailInfoPanelWithEntity(entity);
        }
    }

    private void updateDetailInfoPanelWithEmptyEntity() {
        numberLabel.setText("");
        unitLabel.setText("");
        workpieceWeightLabel.setText("");


        titleLabel.setText("");

        finishedWeightLabel.setText("");

        techProcessLabel.setToolTipText("");
        techProcessLabel.setText("");

        detailIconLabel.setIcon(null);
        if (detailIconLabel.getMouseListeners().length > 0) {
            for (MouseListener listener : detailIconLabel.getMouseListeners()) {
                detailIconLabel.removeMouseListener(listener);
            }
        }
        isActiveLabel.setText("");
    }

    private void updateDetailInfoPanelWithEntity(DetailEntity detailEntity) {
        numberLabel.setText(CommonUtils.substring(25, detailEntity.getCode()));

        if (detailEntity.getDetailTitleByDetailTitleId() != null) {
            String title = detailEntity.getDetailTitleByDetailTitleId().getTitle();
            titleLabel.setToolTipText(title);
            titleLabel.setText(title);
        }

        unitLabel.setText(Boolean.toString(detailEntity.isUnit())
                .replace("false", "нет").replace("true", "да"));

        if (detailEntity.getFinishedWeight() != null) {
            finishedWeightLabel.setText((Double.toString(detailEntity.getFinishedWeight())));
        }

        if (detailEntity.getWorkpieceWeight() != null) {
            workpieceWeightLabel.setText(Double.toString(detailEntity.getWorkpieceWeight()));
        }

        updateIcon();

        if (detailEntity.getTechProcessByTechProcessId() != null) {
            String process = detailEntity.getTechProcessByTechProcessId().getProcess();
            techProcessLabel.setToolTipText(process);
            techProcessLabel.setText(CommonUtils.substring(25, process));
        }

        isActiveLabel.setText(Boolean.toString(!detailEntity.isActive())
                .replace("false", "нет").replace("true", "да"));
    }

    private void updateIcon() {
        FtpUtils ftp = FtpUtils.getInstance();
        BufferedImage image = ftp.getImage(entity.getId());

        FrameUtils.removeAllComponentListeners(detailIconLabel);

        if (image != null) {
            Component c = this;
            final int DEFAULT_IMAGE_SIZE = 256;

            int targetSize = getImageSize(image);
            BufferedImage scaledImage = Scalr.resize(image, targetSize > 0 ? targetSize : DEFAULT_IMAGE_SIZE);
            detailIconLabel.setIcon(new ImageIcon(scaledImage));
            detailIconLabel.setText("");
            detailIconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        FrameUtils.onShowImage(FrameUtils.findWindow(c), false, image,
                                "Изображение " + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle());
                    }
                }
            });
        } else {
            detailIconLabel.setIcon(null);
            detailIconLabel.setText("Нет изображения");
        }
    }

    private int getImageSize(BufferedImage image) {
        final int MAGIC_CONSTANT_GAP = 56;
        final int windowWidth = FrameUtils.findWindow(this).getSize().width;
        final int detailInfoPanelWidth = detailInfoPanel.getWidth();
        try {
            final int c = image.getHeight() / image.getWidth();
            if (c >= 0.7) {
                final int size = FrameUtils.findWindow(this).getSize().height - 140;
                final int actualWidth = c * size;
                if (actualWidth + detailInfoPanelWidth <= windowWidth) {
                    return size;
                } else {
                    return windowWidth - detailInfoPanelWidth;
                }
            } else {
                return windowWidth - detailInfoPanelWidth - MAGIC_CONSTANT_GAP;
            }
        } catch (Exception e) {
            return 0;
        }
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
        panel1.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detailInfoPanel = new JPanel();
        detailInfoPanel.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(detailInfoPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Обозначение:");
        detailInfoPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        detailInfoPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Узел:");
        detailInfoPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Масса готовой детали:");
        detailInfoPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Масса заготовки:");
        detailInfoPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Аннулирована:");
        detailInfoPanel.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Технический процесс:");
        detailInfoPanel.add(label7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberLabel = new JLabel();
        numberLabel.setText("    ");
        detailInfoPanel.add(numberLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        titleLabel = new JLabel();
        titleLabel.setText("    ");
        detailInfoPanel.add(titleLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("    ");
        detailInfoPanel.add(unitLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        finishedWeightLabel = new JLabel();
        finishedWeightLabel.setText("    ");
        detailInfoPanel.add(finishedWeightLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        workpieceWeightLabel = new JLabel();
        workpieceWeightLabel.setText("    ");
        detailInfoPanel.add(workpieceWeightLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        techProcessLabel = new JLabel();
        techProcessLabel.setText(" ");
        detailInfoPanel.add(techProcessLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        isActiveLabel = new JLabel();
        isActiveLabel.setText("   ");
        detailInfoPanel.add(isActiveLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detailIconLabel = new JLabel();
        detailIconLabel.setForeground(new Color(-10395295));
        detailIconLabel.setHorizontalAlignment(0);
        detailIconLabel.setHorizontalTextPosition(0);
        detailIconLabel.setText("Нет изображения");
        panel.add(detailIconLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(256, 256), null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
