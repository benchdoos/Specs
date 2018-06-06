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
import com.mmz.specs.application.gui.common.SmartJTextField;
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
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateDetailWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int MAXIMUM_STRING_LENGTH = 35;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField codeTextField;
    private JComboBox<DetailTitleEntity> titleComboBox;
    private JCheckBox unitCheckBox;
    private JButton createTitleButton;

    private DetailEntity detailEntity;
    private Session session;

    public CreateDetailWindow(DetailEntity detailEntity) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
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
        if (detailEntity == null) {
            setTitle("Добавление новой детали / узла");
        } else {
            setTitle("Изменение " + detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle());
        }

        initComboBox();

        fillComboBox();

        initUnitCheckBox();

        initCreateNewTitleButton();

        fillDetailInfo();

        pack();
        setMinimumSize(getSize());
    }

    private void initCreateNewTitleButton() {
        final boolean b = session.getTransaction().getStatus() == TransactionStatus.NOT_ACTIVE;
        createTitleButton.setEnabled(!b);
        if (b) {
            createTitleButton.setToolTipText("Создание нового наименования доступно в режиме \"Изменение извещения\"");
        }
    }

    private void fillDetailInfo() {
        log.debug("Filling detail info: {}", detailEntity);
        if (detailEntity != null) {
            codeTextField.setText(detailEntity.getCode());
            if (detailEntity.getDetailTitleByDetailTitleId() != null) {
                titleComboBox.setSelectedItem(detailEntity.getDetailTitleByDetailTitleId());
            }
        }
    }

    private void initUnitCheckBox() {
        if (detailEntity != null) {
            unitCheckBox.setEnabled(false);
            unitCheckBox.setSelected(detailEntity.isUnit());
        }
    }

    private void fillComboBox() {
        titleComboBox.setModel(new CommonComboBoxUtils(session).getTitleComboBoxModel(titleComboBox));
    }

    private void initComboBox() {
        titleComboBox.setRenderer(new CommonComboBoxUtils(session).getTitleComboBoxRenderer());
    }

    private void initListeners() {
        createTitleButton.addActionListener(e -> onCreateTitle());
        ((SmartJTextField) codeTextField).setChildComboBox(titleComboBox);

        codeTextField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatus() {
                ((SmartJTextField) codeTextField).setStatus(SmartJTextField.STATUS.NORMAL);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }
        });

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
        DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
        DetailEntity dbDetail;
        try {
            dbDetail = service.getDetailByIndex(fixedCode);
        } catch (Exception e) {
            dbDetail = null;
        }

        if (detailEntity == null) {
            onNewDetailSave(fixedCode, dbDetail);
        } else {
            onEditDetailSave(fixedCode, dbDetail);
        }
    }

    private void onEditDetailSave(String fixedCode, DetailEntity dbDetail) {
        DetailEntity maybeNewDetail = new DetailEntity();
        maybeNewDetail.setCode(fixedCode);
        maybeNewDetail.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());

        if (dbDetail != null) {
            if (detailEntity.getId() == dbDetail.getId()) {
                if (verifyInput(maybeNewDetail)) {
                    updateDetail(fixedCode);
                }
            } else {
                FrameUtils.shakeFrame(this);
                JOptionPane.showMessageDialog(this,
                        "Вы указали существующий индекс другой детали: \n"
                                + dbDetail.getCode() + " "
                                + dbDetail.getDetailTitleByDetailTitleId().getTitle());
                log.warn("User tried to change code for entity: {}, but he chose code: {}, that has another entity: {}",
                        detailEntity, fixedCode, dbDetail);
            }
        } else {
            if (verifyInput(maybeNewDetail)) {
                updateDetail(fixedCode);
            }
        }
    }

    private void updateDetail(String fixedCode) {
        log.debug("Updating detail: ");
        final DetailTitleEntity selectedTitle = (DetailTitleEntity) titleComboBox.getSelectedItem();
        if (selectedTitle != null) {
            final int i = JOptionPane.showConfirmDialog(this, "Вы точно хотите изменить данные по детали:\n"
                    + detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle() + "\n" +
                    "на: " + fixedCode + " " + selectedTitle.getTitle());
            if (i == 0) { // confirm
                detailEntity.setCode(fixedCode);
                detailEntity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                dispose();
            }

        } else {
            log.warn("User tried to save detail with null title for entity: {}", detailEntity);
            JOptionPane.showMessageDialog(this,
                    "Необходимо указать наименование для детали", "Ошибка изменения", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onNewDetailSave(String fixedCode, DetailEntity dbDetail) {
        if (dbDetail != null) {
            log.debug("User tried to add existing detail: {}, existing: {}", fixedCode, dbDetail);
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this,
                    "Деталь с таким индексом уже существует: " + dbDetail.getCode() + " "
                            + dbDetail.getDetailTitleByDetailTitleId().getTitle(),
                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
        } else {
            if (!fixedCode.isEmpty()) {
                DetailEntity detailEntity = new DetailEntity();
                detailEntity.setCode(fixedCode);
                detailEntity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                detailEntity.setUnit(unitCheckBox.isSelected());
                if (verifyInput(detailEntity)) {
                    log.debug("Added new detail: " + detailEntity);
                    this.detailEntity = detailEntity;
                    dispose();
                }
            } else {
                log.debug("User tried to add detail with empty index.");
                FrameUtils.shakeFrame(this);
                ((SmartJTextField) codeTextField).setStatus(SmartJTextField.STATUS.WARNING);
            }
        }
    }

    private boolean verifyInput(DetailEntity entity) {
        final String ERROR_TITLE = "Ошибка верификации";
        log.debug("Verifying entity: {}", entity);
        if (entity == null) {
            log.warn("Can not verify null entity");
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Не могу проверить корректность ввода",
                    ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            if (entity.getCode().isEmpty() || entity.getCode().length() > 30) {
                throw new IllegalArgumentException("Code length can not be 0 or >30, code is: "
                        + entity.getCode() + " " + entity.getCode().length());
            }
        } catch (Exception e) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this,
                    "Длинна индекса детали должна быть в диапазоне: [0;30]", ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
            log.warn("Could not verify code for detail: {}", entity, e);
            return false;
        }

        if (titleComboBox.getSelectedItem() == null) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Необходимо указать наименование детали",
                    ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
            return false;
        }

        log.debug("Verification for entity {} successfully finished", entity);
        return true;
    }

    private void onCancel() {
        detailEntity = null;
        dispose();
    }

    public DetailEntity getDetailEntity() {
        return detailEntity;
    }

    private void createUIComponents() {
        codeTextField = new SmartJTextField();
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
