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
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.common.SelectionMaterialWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.MaterialListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.model.MaterialListEntity;
import com.mmz.specs.service.MaterialListService;
import com.mmz.specs.service.MaterialListServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class EditMaterialListWindow extends JDialog {
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<MaterialListEntity> materialList;
    private JLabel longMarkLabel;
    private JLabel shortMarkLabel;
    private JLabel longProfileLabel;
    private JLabel shortProfileLabel;
    private JButton addMaterialButton;
    private JButton removeMaterial;
    private JButton createNewMaterialButton;
    private JButton editMaterialButton;
    private JCheckBox activeCheckBox;
    private JCheckBox mainCheckBox;
    private DetailEntity detailEntity;

    public EditMaterialListWindow(DetailEntity detailEntity) {
        if (detailEntity == null) {
            throw new IllegalArgumentException("Can not edit material for detail : null");
        }

        this.detailEntity = detailEntity;
        this.session = ClientBackgroundService.getInstance().getSession();

        initGui();

        initListeners();

        initKeyBindings();

        initMaterialList();

        fillMaterialList();

        pack();
        setMinimumSize(new Dimension(540, getSize().height));
        setSize(getMinimumSize());
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setTitle("Редактирование материала для " + detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle());
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/edit/edit.png")));
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

    private void initMaterialList() {

        materialList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof MaterialListEntity) {
                    MaterialListEntity entity = (MaterialListEntity) value;
                    final String shortProfile = entity.getMaterialByMaterialId().getShortProfile();
                    final String shortMark = entity.getMaterialByMaterialId().getShortMark();
                    setOpaque(true);

                    return super.getListCellRendererComponent(list, shortMark + " " + shortProfile + (entity.isMainMaterial() ? " (Основной)" : ""), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        materialList.addListSelectionListener(e -> {
            if (materialList.getSelectedValue() != null) {
                final MaterialListEntity selectedValue = materialList.getSelectedValue();
                final MaterialEntity materialByMaterialId = selectedValue.getMaterialByMaterialId();

                longMarkLabel.setText(materialByMaterialId.getLongMark());
                longProfileLabel.setText(materialByMaterialId.getLongProfile());
                shortMarkLabel.setText(materialByMaterialId.getShortMark());
                shortProfileLabel.setText(materialByMaterialId.getShortProfile());


                activeCheckBox.setSelected(materialByMaterialId.isActive());
                activeCheckBox.setEnabled(true);

                mainCheckBox.setSelected(selectedValue.isMainMaterial());
                mainCheckBox.setEnabled(true);

            } else {
                longMarkLabel.setText("Нет данных");
                longProfileLabel.setText("Нет данных");
                shortMarkLabel.setText("Нет данных");
                shortProfileLabel.setText("Нет данных");

                activeCheckBox.setSelected(false);
                activeCheckBox.setEnabled(false);

                mainCheckBox.setSelected(false);
                mainCheckBox.setEnabled(false);

            }
        });

    }

    private void onOK() {
        dispose();
    }

    private void fillMaterialList() {
        if (detailEntity != null) {
            MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
            final List<MaterialListEntity> materialListByDetail = service.getMaterialListByDetail(detailEntity);

            DefaultListModel<MaterialListEntity> model = new DefaultListModel<>();

            for (MaterialListEntity entity : materialListByDetail) {
                if (entity.isActive()) {
                    model.addElement(entity);
                }
            }

            materialList.setModel(model);
        }

    }

    private void onAddMaterial() {
        SelectionMaterialWindow selectionMaterialWindow = new SelectionMaterialWindow();
        selectionMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(this, selectionMaterialWindow));
        selectionMaterialWindow.setVisible(true);
        final MaterialEntity selectedValue = selectionMaterialWindow.getSelectedValue();

        if (selectedValue != null) {
            DefaultListModel<MaterialListEntity> model = (DefaultListModel<MaterialListEntity>) materialList.getModel();
            MaterialListEntity entity = new MaterialListEntity();
            entity.setDetailByDetailId(detailEntity);
            entity.setMaterialByMaterialId(selectedValue);
            entity.setMainMaterial(false);
            entity.setActive(true);
            if (!modelContainsMaterial(model, entity)) {
                model.addElement(entity);
            }
        }
    }

    private boolean modelContainsMaterial(DefaultListModel<MaterialListEntity> model, MaterialListEntity entity) {
        for (int i = 0; i < model.getSize(); i++) {
            MaterialListEntity modelListEntity = model.getElementAt(i);
            final MaterialEntity materialByMaterialId = modelListEntity.getMaterialByMaterialId();
            final MaterialEntity entityMaterialByMaterialId = entity.getMaterialByMaterialId();
            if (materialByMaterialId.getLongMark().equals(entityMaterialByMaterialId.getLongMark())
                    && materialByMaterialId.getLongProfile().equals(entityMaterialByMaterialId.getLongProfile())) {
                return true;
            }
        }
        return false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private void onCancel() {
        dispose();
    }

    private void onRemoveMaterial() {
        final MaterialListEntity selectedValue = materialList.getSelectedValue();
        if (selectedValue != null) {
            selectedValue.setActive(false);
            DefaultListModel<MaterialListEntity> model = (DefaultListModel<MaterialListEntity>) materialList.getModel();
            for (int i = 0; i < model.size(); i++) {
                final MaterialEntity materialByMaterialId = model.getElementAt(i).getMaterialByMaterialId();
                final MaterialEntity selectedMaterialEntity = selectedValue.getMaterialByMaterialId();
                if (materialByMaterialId.getLongMark().equals(selectedMaterialEntity.getLongMark())
                        && materialByMaterialId.getLongProfile().equals(selectedMaterialEntity.getLongProfile())) {
                    model.removeElementAt(i);
                    return;
                }
            }
        }
    }

    private void onEditMaterial() {
        final MaterialEntity materialByMaterialId = materialList.getSelectedValue().getMaterialByMaterialId();
        if (materialByMaterialId != null) {
            CreateMaterialWindow createMaterialWindow = new CreateMaterialWindow(materialByMaterialId);
            createMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), createMaterialWindow));
            createMaterialWindow.setVisible(true);
        }
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        addMaterialButton.addActionListener(e -> onAddMaterial());
        removeMaterial.addActionListener(e -> onRemoveMaterial());
        editMaterialButton.addActionListener(e -> onEditMaterial());
        createNewMaterialButton.addActionListener(e -> onCreateNewMaterial());
    }

    private void onCreateNewMaterial() {
        CreateMaterialWindow createMaterialWindow = new CreateMaterialWindow(null);
        createMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), createMaterialWindow));
        createMaterialWindow.setVisible(true);
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
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(227);
        panel3.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel4);
        final JLabel label1 = new JLabel();
        label1.setText("Марка (полная):");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Профиль (полный):");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Марка (сокращенная):");
        panel4.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Профиль (сокращенный):");
        panel4.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        longMarkLabel = new JLabel();
        longMarkLabel.setText("нет данных");
        panel4.add(longMarkLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        longProfileLabel = new JLabel();
        longProfileLabel.setText("нет данных");
        panel4.add(longProfileLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        shortMarkLabel = new JLabel();
        shortMarkLabel.setText("нет данных");
        panel4.add(shortMarkLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        shortProfileLabel = new JLabel();
        shortProfileLabel.setText("нет данных");
        panel4.add(shortProfileLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        activeCheckBox = new JCheckBox();
        activeCheckBox.setHorizontalTextPosition(2);
        activeCheckBox.setText("Активный:");
        panel4.add(activeCheckBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainCheckBox = new JCheckBox();
        mainCheckBox.setHorizontalTextPosition(2);
        mainCheckBox.setText("Основной:");
        panel4.add(mainCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel5);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        materialList = new JList();
        materialList.setSelectionMode(0);
        scrollPane1.setViewportView(materialList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel5.add(toolBar1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addMaterialButton = new JButton();
        addMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addMaterialButton.setText("");
        toolBar1.add(addMaterialButton);
        removeMaterial = new JButton();
        removeMaterial.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeMaterial.setText("");
        toolBar1.add(removeMaterial);
        editMaterialButton = new JButton();
        editMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editMaterialButton.setText("");
        toolBar1.add(editMaterialButton);
        createNewMaterialButton = new JButton();
        createNewMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/materialNew16.png")));
        createNewMaterialButton.setText("");
        toolBar1.add(createNewMaterialButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
