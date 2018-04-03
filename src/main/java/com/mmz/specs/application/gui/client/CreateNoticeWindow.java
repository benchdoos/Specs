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
import com.mmz.specs.application.gui.common.LoginWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.NoticeService;
import com.mmz.specs.service.NoticeServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateNoticeWindow extends JDialog {
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea noticeDescriptionTextArea;
    private JTextField numberTextField;
    private JLabel noticeDateLabel;
    private NoticeEntity noticeEntity;

    public CreateNoticeWindow(NoticeEntity noticeEntity) {
        this.noticeEntity = noticeEntity;
        this.session = ClientBackgroundService.getInstance().getSession();

        initGui();
    }

    private void initGui() {
        if (noticeEntity != null) {
            setTitle("Редактирование извещения " + noticeEntity.getNumber());
        } else {
            setTitle("Новое извещение");
        }
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeNew64.png")));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        initListeners();

        initKeyBindings();

        fillFieldsIfNoticeEntityNotEmpty(noticeEntity);

        pack();
        setMinimumSize(getSize());
    }

    private void fillFieldsIfNoticeEntityNotEmpty(NoticeEntity notice) {
        if (notice != null) {
            numberTextField.setText(notice.getNumber());
            noticeDateLabel.setText(new SimpleDateFormat("dd.MM.yyyy").format(notice.getDate()));
            noticeDescriptionTextArea.setText(notice.getDescription());
        } else {
            numberTextField.setText("");
            noticeDateLabel.setText(new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime()));
            noticeDescriptionTextArea.setText("");
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

    private void onOK() {
        LoginWindow loginWindow = new LoginWindow(session);
        loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
        loginWindow.setVisible(true);
        UsersEntity user = loginWindow.getAuthorizedUser();
        if (user != null) {
            if (user.isActive()) {
                if (user.isAdmin() || (user.isEditor() &&
                        (user.getUserType().getId() == 1
                                || user.getUserType().getId() == 3
                                || user.getUserType().getId() == 4))) {
                    NoticeService service = new NoticeServiceImpl(new NoticeDaoImpl(session));

                    NoticeEntity entity;
                    if (noticeEntity != null) {
                        entity = noticeEntity;
                    } else {
                        entity = new NoticeEntity();
                    }
                    entity.setNumber(numberTextField.getText());
                    entity.setDate(new Date(Calendar.getInstance().getTime().getTime()));
                    entity.setDescription(noticeDescriptionTextArea.getText());
                    entity.setUsersByProvidedByUserId(user);

                    session.beginTransaction();
                    if (noticeEntity != null) {
                        service.updateNotice(entity);
                    } else {
                        service.addNotice(entity);
                    }
                    session.getTransaction().commit();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Пользователь должен быть администратором или редактором (конструктором).",
                            "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Пользователь должен быть действующим!",
                        "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Чтобы продолжить, необходимо выполнить вход.",
                    "Ошибка входа", JOptionPane.WARNING_MESSAGE);
        }
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

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());


        numberTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNumberTextField();
            }

            private void updateNumberTextField() {
                int length = numberTextField.getText().length();
                if (length > 8) {
                    numberTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
                    numberTextField.setToolTipText("Номер извещения не должен превышать 8 символов (сейчас: " + length + ")");
                    buttonOK.setEnabled(false);
                } else {
                    numberTextField.setBorder(new JTextField().getBorder());
                    numberTextField.setToolTipText("");
                    if (noticeDescriptionTextArea.getText().length() <= 2000) {
                        buttonOK.setEnabled(true);
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNumberTextField();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateNumberTextField();
            }
        });

        noticeDescriptionTextArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateDescriptionTextArea() {
                int length = noticeDescriptionTextArea.getText().length();
                if (length > 2000) {
                    noticeDescriptionTextArea.setBorder(BorderFactory.createLineBorder(Color.RED));
                    noticeDescriptionTextArea.setToolTipText("Описание извещения не должно привышать 2000 символов (сейчас: " + length + ")");
                    buttonOK.setEnabled(false);
                } else {
                    noticeDescriptionTextArea.setBorder(new JTextArea().getBorder());
                    noticeDescriptionTextArea.setToolTipText("");
                    if (numberTextField.getText().length() <= 8) {
                        buttonOK.setEnabled(true);
                    }
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDescriptionTextArea();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDescriptionTextArea();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDescriptionTextArea();
            }


        });
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
        buttonCancel.setText("Отмена");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Номер:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Дата:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Описание:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeDateLabel = new JLabel();
        noticeDateLabel.setText("нет данных");
        panel3.add(noticeDateLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(300, 150), null, null, 0, false));
        noticeDescriptionTextArea = new JTextArea();
        Font noticeDescriptionTextAreaFont = this.$$$getFont$$$("Consolas", -1, 14, noticeDescriptionTextArea.getFont());
        if (noticeDescriptionTextAreaFont != null) noticeDescriptionTextArea.setFont(noticeDescriptionTextAreaFont);
        scrollPane1.setViewportView(noticeDescriptionTextArea);
        numberTextField = new JTextField();
        panel3.add(numberTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
