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
import com.mmz.specs.application.gui.common.CommonComboBoxUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddDetailWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField codeTextField;
    private JComboBox<DetailTitleEntity> titleComboBox;
    private JCheckBox unitCheckBox;
    private JButton createTitleButton;

    private DetailEntity detailEntity;
    private Session session;

    public AddDetailWindow() {
        session = ClientBackgroundService.getInstance().getSession();

        initGui();

        initListeners();

        initKeyBindings();
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/addNewItem.png")));
        setTitle("Добавление новой детали / узла");

        initComboBox();

        fillComboBox();

        pack();
        setMinimumSize(getSize());
    }

    private void fillComboBox() {
        titleComboBox.setModel(new CommonComboBoxUtils(session).getTitleComboBoxModel(titleComboBox));
    }

    private void initComboBox() {
        titleComboBox.setRenderer(new CommonComboBoxUtils(session).getTitleComboBoxRenderer());
    }

    private void initListeners() {
        createTitleButton.addActionListener(e -> onCreateTitle());

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void onCreateTitle() {
        DetailTitleEntity titleEntity = new CommonWindowUtils(session).onCreateNewTitle(this);
        if (titleEntity != null) {
            titleComboBox.removeAllItems();
            fillComboBox();

            titleComboBox.setSelectedItem(titleEntity);
        }
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String fixedCode = codeTextField.getText().toUpperCase()
                .replace(",", ".")
                .replace("/", ".")
                .replace(" ", "");
        //todo test this
       /*String latinChars = "qwertyuiop[]asdfghjkl;'zxcvbnm";
        String cyrillicChars = "йцукенгшщзхъфывапролджэячсмить";
        fixedCode = StringUtils.replaceChars(fixedCode, latinChars, cyrillicChars);
        fixedCode = StringUtils.replaceChars(fixedCode, latinChars.toUpperCase(), cyrillicChars.toUpperCase());*/

        DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
        DetailEntity dbDetail = service.getDetailByIndex(fixedCode);
        if (dbDetail != null) {
            log.debug("User tried to add existing detail: {}, existing: {}", fixedCode, dbDetail);
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this,
                    "Деталь с таким индексом уже существует: " + dbDetail.getCode() + " "
                            + dbDetail.getDetailTitleByDetailTitleId().getTitle(),
                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
        } else {
            DetailEntity detailEntity = new DetailEntity();
            detailEntity.setCode(fixedCode);
            detailEntity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
            detailEntity.setUnit(unitCheckBox.isSelected());
            log.debug("Added new detail: " + detailEntity);
            this.detailEntity = detailEntity;
            dispose();
        }
    }

    private void onCancel() {
        detailEntity = null;
        dispose();
    }

    public DetailEntity getDetailEntity() {
        return detailEntity;
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
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Индекс детали:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        codeTextField = new JTextField();
        panel3.add(codeTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        titleComboBox = new JComboBox();
        titleComboBox.setMaximumRowCount(30);
        panel3.add(titleComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitCheckBox = new JCheckBox();
        unitCheckBox.setHorizontalTextPosition(2);
        unitCheckBox.setText("Узел:");
        unitCheckBox.setMnemonic('У');
        unitCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(unitCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTitleButton = new JButton();
        createTitleButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/titleNew16.png")));
        createTitleButton.setText("");
        createTitleButton.setToolTipText("Создать новое наименование");
        panel3.add(createTitleButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel3.add(spacer4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        label1.setLabelFor(codeTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
