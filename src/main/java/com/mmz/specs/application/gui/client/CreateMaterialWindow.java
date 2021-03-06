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
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.MaterialDaoImpl;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.MaterialService;
import com.mmz.specs.service.MaterialServiceImpl;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateMaterialWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField shortMarkTextField;
    private JTextField longProfileTextField;
    private JTextField longMarkTextField;
    private JTextField shortProfileTextField;
    private JCheckBox activeCheckBox;
    private Session session;

    private MaterialEntity materialEntity;

    public CreateMaterialWindow(Session session, MaterialEntity materialEntity) {
        this.materialEntity = materialEntity;
        this.session = session;

        initGui();

        initListeners();

        initKeyBindings();
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
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setMnemonic('Т');
        buttonCancel.setDisplayedMnemonicIndex(1);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Профиль (полный)");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Марка (сокращенная):");
        panel3.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shortMarkTextField = new JTextField();
        panel3.add(shortMarkTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        longProfileTextField = new JTextField();
        panel3.add(longProfileTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        activeCheckBox = new JCheckBox();
        activeCheckBox.setEnabled(false);
        activeCheckBox.setHorizontalTextPosition(2);
        activeCheckBox.setSelected(true);
        activeCheckBox.setText("Активна:");
        activeCheckBox.setMnemonic('К');
        activeCheckBox.setDisplayedMnemonicIndex(1);
        panel3.add(activeCheckBox, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Марка (полная):");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        longMarkTextField = new JTextField();
        panel3.add(longMarkTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Профиль (сокращенный):");
        panel3.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shortProfileTextField = new JTextField();
        panel3.add(shortProfileTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label1.setLabelFor(longProfileTextField);
        label2.setLabelFor(shortMarkTextField);
        label3.setLabelFor(longMarkTextField);
        label4.setLabelFor(shortProfileTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private MaterialEntity createMaterial() {
        MaterialEntity materialEntity = new MaterialEntity();
        materialEntity.setLongMark(longMarkTextField.getText());
        materialEntity.setLongProfile(longProfileTextField.getText());
        materialEntity.setShortMark(shortMarkTextField.getText());
        materialEntity.setShortProfile(shortProfileTextField.getText());
        materialEntity.setActive(activeCheckBox.isSelected());
        return materialEntity;
    }

    private boolean exist(MaterialEntity materialEntity) {
        MaterialService service = new MaterialServiceImpl(new MaterialDaoImpl(session));
        MaterialEntity materialByShortMarkAndProfile = null;
        try {
            materialByShortMarkAndProfile = service.getMaterialByShortMarkAndProfile(materialEntity.getShortMark(), materialEntity.getShortProfile());
        } catch (NonUniqueResultException e) {
            JOptionPane.showMessageDialog(this, "Существует дубликат данной записи, обратитесь к администратору.",
                    "Ошибка изменения", JOptionPane.WARNING_MESSAGE);
        }
        return materialByShortMarkAndProfile != null;
    }

    private void fillFields(MaterialEntity materialEntity) {
        if (materialEntity != null) {
            longMarkTextField.setText(materialEntity.getLongMark());
            longProfileTextField.setText(materialEntity.getLongProfile());
            shortMarkTextField.setText(materialEntity.getShortMark());
            shortProfileTextField.setText(materialEntity.getShortProfile());
        }
    }

    public MaterialEntity getMaterialEntity() {
        return materialEntity;
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        if (materialEntity == null) {
            setTitle("Добавить новый материал");
        } else {
            setTitle("Редактировать материал");
            activeCheckBox.setEnabled(true);
        }

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/materialNew16.png")));

        getRootPane().setDefaultButton(buttonOK);

        fillFields(materialEntity);

        pack();
        setMinimumSize(getSize());
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

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void loginAndUpdate(MaterialEntity materialEntity) {
        LoginWindow loginWindow = new LoginWindow(session);
        loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
        loginWindow.setVisible(true);
        UsersEntity user = loginWindow.getAuthorizedUser();
        if (user != null) {
            if (user.isActive()) {
                if (user.isAdmin() || user.isEditor()) {
                    saveMaterial(materialEntity);
                    dispose();
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Необходимо быть действующим редактором " +
                "\nили администратором, чтобы продожить.", "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        if (verify()) {
            MaterialEntity materialEntity = createMaterial();

            if (exist(materialEntity)) {
                if (this.materialEntity == null) {
                    FrameUtils.shakeFrame(this);
                    JOptionPane.showMessageDialog(this, "Данный материал существует",
                            "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
                } else {
                    loginAndUpdate(materialEntity);
                }
            } else {
                loginAndUpdate(materialEntity);
            }
        }
    }

    private void saveMaterial(MaterialEntity materialEntity) {

        MaterialService service = new MaterialServiceImpl(new MaterialDaoImpl(session));
        if (this.materialEntity != null) {
            materialEntity.setId(this.materialEntity.getId());
            service.updateMaterial(materialEntity);
            this.materialEntity = materialEntity;
        } else {
            this.materialEntity = service.getMaterialById(service.addMaterial(materialEntity));
        }
    }

    private boolean verify() {
        if (longMarkTextField.getText().isEmpty() || longMarkTextField.getText().length() > 200) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Поле полной марки не может быть пустым или длинна поля быть более 200");
            return false;
        }
        if (longProfileTextField.getText().isEmpty() || longProfileTextField.getText().length() > 200) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Поле полного профиля не может быть пустым или длинна поля быть более 200");
            return false;
        }
        if (shortMarkTextField.getText().isEmpty() || shortMarkTextField.getText().length() > 40) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Поле короткой марки не может быть пустым или длинна поля быть более 40");
            return false;
        }
        if (shortProfileTextField.getText().isEmpty() || shortProfileTextField.getText().length() > 50) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Поле короткого профиля не может быть пустым или длинна поля быть более 50");
            return false;
        }
        return true;
    }
}
