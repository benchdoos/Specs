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
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.common.utils.SwitchingComboBox;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.MaterialDaoImpl;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.service.MaterialService;
import com.mmz.specs.service.MaterialServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;

public class SelectionMaterialWindow extends JDialog {
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private SwitchingComboBox<MaterialEntity> materialComboBox;
    private JLabel longMarkLabel;
    private JLabel longProfileLabel;
    private MaterialEntity selectedMaterialEntity;
    private ActionListener notifyUserIsActiveListener = FrameUtils.getNotifyUserIsActiveActionListener(this);

    public SelectionMaterialWindow() {
        $$$setupUI$$$();
        this.session = ClientBackgroundService.getInstance().getSession();

        initGui();

        initListeners();

        initKeyBindings();

        initMaterialComboBox();

        fillMaterialComboBox();

        initUpdateUserIsActiveListeners();


        pack();
        setMinimumSize(getSize());
    }

    private void initUpdateUserIsActiveListeners() {
        buttonOK.addActionListener(notifyUserIsActiveListener);
        buttonCancel.addActionListener(notifyUserIsActiveListener);
        materialComboBox.addActionListener(notifyUserIsActiveListener);

    }

    private void initMaterialComboBox() {
        materialComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof MaterialEntity) {
                    final MaterialEntity materialEntity = (MaterialEntity) value;
                    String additional = materialEntity.isActive() ? "" : " (не исп.)";
                    return super.getListCellRendererComponent(list, materialEntity.getShortMark() + " "
                            + materialEntity.getShortProfile() + additional, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        materialComboBox.addActionListener(e -> {
            MaterialEntity selectedItem = (MaterialEntity) materialComboBox.getSelectedItem();
            if (selectedItem != null) {
                longMarkLabel.setText(selectedItem.getLongMark());
                longProfileLabel.setText(selectedItem.getLongProfile());
            } else {
                longMarkLabel.setText("нет данных");
                longProfileLabel.setText("нет данных");
            }
        });
    }

    private void fillMaterialComboBox() {
        MaterialService service = new MaterialServiceImpl(new MaterialDaoImpl(session));
        final List<MaterialEntity> materialEntities = service.listMaterials();
        Collections.sort(materialEntities);

        DefaultComboBoxModel<MaterialEntity> model = new DefaultComboBoxModel<>();

        for (MaterialEntity entity : materialEntities) {
            model.addElement(entity);
        }

        materialComboBox.setModel(model);
        materialComboBox.setSelectedIndex(-1);
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Выбор материала");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/material16.png")));
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        selectedMaterialEntity = (MaterialEntity) materialComboBox.getSelectedItem();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public MaterialEntity getSelectedValue() {
        return selectedMaterialEntity;
    }

    private void createUIComponents() {
        materialComboBox = new SwitchingComboBox<MaterialEntity>() {
            @Override
            public void selectTypedItem() {
                final String typedItem = getTypedItem();

                DefaultComboBoxModel<MaterialEntity> model = (DefaultComboBoxModel<MaterialEntity>) materialComboBox.getModel();
                if (!typedItem.isEmpty()) {
                    for (int i = 0; i < model.getSize(); i++) {
                        final MaterialEntity elementAt = model.getElementAt(i);
                        if (elementAt.getShortMark().toLowerCase().startsWith(typedItem)) {
                            materialComboBox.setSelectedItem(elementAt);
                            break;
                        }
                    }
                }
            }
        };
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
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
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Материал:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel3.add(materialComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(250, -1), new Dimension(250, -1), 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Материал:");
        panel3.add(label2, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        longMarkLabel = new JLabel();
        longMarkLabel.setText("нет данных");
        panel3.add(longMarkLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        longProfileLabel = new JLabel();
        longProfileLabel.setText("нет данных");
        panel3.add(longProfileLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(materialComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
