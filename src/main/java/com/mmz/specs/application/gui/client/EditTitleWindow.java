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
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.service.DetailTitleService;
import com.mmz.specs.service.DetailTitleServiceImpl;
import org.apache.commons.text.WordUtils;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class EditTitleWindow extends JDialog {
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField titleTextField;
    private JCheckBox activeCheckBox;
    private DetailTitleEntity detailTitleEntity;

    public EditTitleWindow(DetailTitleEntity detailTitleEntity) {
        this.detailTitleEntity = detailTitleEntity;
        this.session = ClientBackgroundService.getInstance().getSession();
        initGui();

        initTitleTextField();

        initActiveCheckBox();

        initListeners();

        initKeyBindings();
    }

    private void initTitleTextField() {
        if (detailTitleEntity != null) {
            titleTextField.setText(detailTitleEntity.getTitle());
        }
    }

    private void initActiveCheckBox() {
        if (detailTitleEntity == null) {
            activeCheckBox.setSelected(true);
            activeCheckBox.setEnabled(false);
        } else {
            activeCheckBox.setSelected(detailTitleEntity.isActive());
        }
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

    private void onOK() {
        addNewTitle();
    }

    private void onCancel() {
        dispose();
    }

    private void addNewTitle() {
        final int MAX_TITLE_LENGTH = 120;
        String result = titleTextField.getText();
        if (result != null) {
            if (!result.isEmpty()) {
                if (result.length() <= MAX_TITLE_LENGTH) {
                    result = result.replace("\n", " ");
                    result = fixTitle(result);

                    DetailTitleService titleService = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
                    DetailTitleEntity detailTitleByTitle = titleService.getDetailTitleByTitle(result);

                    if (detailTitleByTitle == null && detailTitleEntity == null) {
                        DetailTitleEntity titleEntity = new DetailTitleEntity();
                        titleEntity.setActive(true);
                        titleEntity.setTitle(result);

                        detailTitleEntity = createNewTitle(result, titleEntity);
                        dispose();
                    } else if (detailTitleEntity != null) {
                        if (detailTitleByTitle != null) {
                            if (detailTitleByTitle.getId() == detailTitleEntity.getId()) {
                                updateTitleEntity(result);
                            }
                        } else {
                            updateTitleEntity(result);
                        }
                    } else {
                        showMessageDialog(this,
                                "Наименование " + result +
                                        "\nУже существует.", "Ошибка добавления", WARNING_MESSAGE);
                    }
                } else {
                    showMessageDialog(this,
                            "Длина наименования не может привышать 120 символов (сейчас: "
                                    + result.length() + ")",
                            "Ошибка ввода", WARNING_MESSAGE);
                }
            } else {
                showMessageDialog(this,
                        "Наименование не может быть пустым",
                        "Ошибка ввода", WARNING_MESSAGE);
            }
        }
    }

    private String fixTitle(String title) {
        if (title == null) return null;
        if (title.length() > 0) {
            final String[] split = title.split(" ");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    split[i] = WordUtils.capitalizeFully(split[i]);
                } else {
                    split[i] = split[i].toLowerCase();
                }
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                builder.append(str);
                if (i != split.length - 1) {
                    builder.append(" ");
                }
            }

            title = builder.toString();
        }
        return title;
    }

    private void updateTitleEntity(String result) {
        DetailTitleEntity titleEntity = detailTitleEntity;
        titleEntity.setActive(activeCheckBox.isSelected());
        titleEntity.setTitle(result);

        detailTitleEntity = createNewTitle(result, titleEntity);
        dispose();
    }

    private DetailTitleEntity createNewTitle(String result, DetailTitleEntity titleEntity) {
        try {
            DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
            if (detailTitleEntity != null) {
                service.updateDetailTitle(titleEntity);
            } else {
                titleEntity = service.getDetailTitleById(service.addDetailTitle(titleEntity));
            }
            return titleEntity;
        } catch (Throwable throwable) {
            showMessageDialog(this,
                    "Не удалось добавить " + result + "\n" + throwable.getLocalizedMessage(),
                    "Ошибка добавления", WARNING_MESSAGE);
            return null;
        }
    }

    public DetailTitleEntity getDetailTitleEntity() {
        return detailTitleEntity;
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        if (detailTitleEntity == null) {
            setTitle("Новое наименование");
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/titleNew16.png")));
        } else {
            setTitle("Изменение наименования: " + detailTitleEntity.getTitle());
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/titleEdit16.png")));
        }
        pack();
        setResizable(false);
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
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Введите новое наименование:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        titleTextField = new JTextField();
        panel3.add(titleTextField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        activeCheckBox = new JCheckBox();
        activeCheckBox.setHorizontalTextPosition(2);
        activeCheckBox.setSelected(true);
        activeCheckBox.setText("Активна:");
        activeCheckBox.setMnemonic('А');
        activeCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(activeCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(titleTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
