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
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.gui.client.DetailInfoWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.NoticeEntity;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.mmz.specs.application.core.ApplicationConstants.NO_DATA_STRING;

public class NoticeInfoPanel extends JPanel implements AccessPolicy {
    private JPanel contentPane;
    private JList<NoticeEntity> noticeList;
    private JTextArea descriptionTextArea;
    private JList<DetailEntity> detailEffectedList;
    private JLabel numberLabel;
    private JLabel dateLabel;
    private JLabel userLabel;
    private JLabel detailInfoLabel;
    private JLabel noticeCreatedByUserLabel;
    private JLabel noticeCreationDateLabel;
    private Session session;
    private List<NoticeEntity> noticeEntities;
    private DetailEntity detailEntity;
    private ActionListener notifyUserIsActiveListener = FrameUtils.getNotifyUserIsActiveActionListener(this);


    public NoticeInfoPanel(Session session, List<NoticeEntity> noticeEntities) {

        this.session = session;
        this.noticeEntities = noticeEntities;

        initGui();

    }

    public void setDetailEntity(DetailEntity detailEntity) {
        this.detailEntity = detailEntity;
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        updateNoticeList();
        initDetailEffectedList();

        initListeners();

        initUpdateUserIsActiveListeners();

    }

    private void initUpdateUserIsActiveListeners() {
        noticeList.addListSelectionListener(e -> notifyUserIsActiveListener.actionPerformed(null));
        detailEffectedList.addListSelectionListener(e -> notifyUserIsActiveListener.actionPerformed(null));
    }

    private void initDetailInfoLabel() {
        if (detailEntity != null) {
            detailInfoLabel.setText("Извещения, которые затрагивают " + detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle());
        } else detailInfoLabel.setVisible(false);
    }

    private void updateNoticeList() {
        DefaultListModel<NoticeEntity> model = new DefaultListModel<>();
        for (NoticeEntity entity : noticeEntities) {
            model.addElement(entity);
        }
        noticeList.setModel(model);

        noticeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof NoticeEntity) {
                    NoticeEntity entity = (NoticeEntity) value;
                    return super.getListCellRendererComponent(list, entity.getNumber(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });


        noticeList.addListSelectionListener(e -> {
            final int selectedIndex = noticeList.getSelectedIndex();
            if (selectedIndex >= 0) {
                updateNoticeInfo(noticeList.getModel().getElementAt(selectedIndex));
            } else {
                updateNoticeInfo(null);
            }
        });

        if (noticeList.getModel().getSize() >= 0) {
            noticeList.setSelectedIndex(0);
        }

    }

    private void initDetailEffectedList() {
        detailEffectedList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailEntity) {
                    DetailEntity entity = (DetailEntity) value;
                    return super.getListCellRendererComponent(list, entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }

    private void initListeners() {
        Component c = this;
        detailEffectedList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (detailEffectedList.getSelectedValue() != null) {
                        DetailInfoWindow detailInfoWindow = new DetailInfoWindow(detailEffectedList.getSelectedValue(), session);
                        detailInfoWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(c), detailInfoWindow));
                        detailInfoWindow.setVisible(true);
                    }
                }
            }
        });
    }

    private void updateNoticeInfo(NoticeEntity selectedValue) {
        if (selectedValue != null) {
            numberLabel.setText(selectedValue.getNumber());

            dateLabel.setText(selectedValue.getDate() != null ?
                    ApplicationConstants.DEFAULT_DATE_FORMAT.format(selectedValue.getDate()) : NO_DATA_STRING);

            userLabel.setText(selectedValue.getUsersByProvidedByUserId().getName()
                    + " " + selectedValue.getUsersByProvidedByUserId().getSurname());

            descriptionTextArea.setText(selectedValue.getDescription());
            descriptionTextArea.setCaretPosition(0);

            noticeCreatedByUserLabel.setText(
                    selectedValue.getAuthorByUserId() == null ? NO_DATA_STRING :
                            selectedValue.getAuthorByUserId().getName() + " " + selectedValue.getAuthorByUserId().getSurname());

            noticeCreationDateLabel.setText(selectedValue.getCreationDate() != null ?
                    ApplicationConstants.DEFAULT_DATE_FORMAT.format(selectedValue.getCreationDate()) : NO_DATA_STRING);


            ListModel<DetailEntity> effectList = new CommonWindowUtils(session).getEffectList(selectedValue.getId());

            if (effectList != null) {
                detailEffectedList.setModel(effectList);
            } else {
                detailEffectedList.setModel(new DefaultListModel<>());
            }
        } else {
            numberLabel.setText(NO_DATA_STRING);
            dateLabel.setText(NO_DATA_STRING);
            userLabel.setText(NO_DATA_STRING);
            descriptionTextArea.setText(NO_DATA_STRING);
            noticeCreatedByUserLabel.setText(NO_DATA_STRING);
            noticeCreationDateLabel.setText(NO_DATA_STRING);

            detailEffectedList.setModel(new DefaultListModel<>());
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        initDetailInfoLabel();
        super.setVisible(aFlag);
    }

    @Override
    public AccessPolicyManager getPolicyManager() {
        return new AccessPolicyManager(false, false);
    }

    @Override
    public void setUIEnabled(boolean enable) {
        /*NOP*/
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(158);
        splitPane1.setLastDividerLocation(128);
        contentPane.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder("Список извещений:"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(64, -1), null, null, 0, false));
        noticeList = new JList();
        scrollPane1.setViewportView(noticeList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel2);
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setOrientation(0);
        panel2.add(splitPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setLeftComponent(panel3);
        panel3.setBorder(BorderFactory.createTitledBorder("Информация о измещении:"));
        final JLabel label1 = new JLabel();
        label1.setText("Номер:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Дата изменения:");
        label2.setToolTipText("Дата последнего изменения");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Описание:");
        panel3.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Изменил:");
        panel3.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 70), null, null, 0, false));
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setBackground(new Color(-855310));
        descriptionTextArea.setEditable(false);
        Font descriptionTextAreaFont = UIManager.getFont("Label.font");
        if (descriptionTextAreaFont != null) descriptionTextArea.setFont(descriptionTextAreaFont);
        scrollPane2.setViewportView(descriptionTextArea);
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        userLabel = new JLabel();
        userLabel.setText("нет данных");
        panel4.add(userLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(150, -1), 0, false));
        numberLabel = new JLabel();
        numberLabel.setText("нет данных");
        panel4.add(numberLabel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateLabel = new JLabel();
        dateLabel.setText("нет данных");
        panel4.add(dateLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(150, -1), 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Дата выпуска:");
        panel4.add(label5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeCreationDateLabel = new JLabel();
        noticeCreationDateLabel.setText("нет данных");
        panel4.add(noticeCreationDateLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(150, -1), 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Выпустил:");
        panel4.add(label6, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeCreatedByUserLabel = new JLabel();
        noticeCreatedByUserLabel.setText("нет данных");
        panel4.add(noticeCreatedByUserLabel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(150, -1), 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setRightComponent(panel5);
        panel5.setBorder(BorderFactory.createTitledBorder("Детали и узлы, которые упоминает извещение:"));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel5.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        detailEffectedList = new JList();
        scrollPane3.setViewportView(detailEffectedList);
        detailInfoLabel = new JLabel();
        detailInfoLabel.setText("Извещения, которые затрагивают");
        contentPane.add(detailInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label3.setLabelFor(descriptionTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
