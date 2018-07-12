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
import com.mmz.specs.application.gui.common.SmartJTextField;
import com.mmz.specs.application.utils.DateLabelFormatter;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.dao.UsersDaoImpl;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.NoticeService;
import com.mmz.specs.service.NoticeServiceImpl;
import com.mmz.specs.service.UsersService;
import com.mmz.specs.service.UsersServiceImpl;
import org.hibernate.Session;
import org.jdatepicker.DateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import static com.mmz.specs.application.core.ApplicationConstants.DEFAULT_DATE_FORMAT;

public class CreateNoticeWindow extends JDialog {
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea noticeDescriptionTextArea;
    private JTextField numberTextField;
    private JLabel noticeDateLabel;
    private JComboBox<UsersEntity> authorComboBox;
    private JDatePickerImpl createdDatePicker;
    private NoticeEntity noticeEntity;

    public CreateNoticeWindow(Session session, NoticeEntity noticeEntity) {
        $$$setupUI$$$();
        this.noticeEntity = noticeEntity;
        this.session = session;

        initGui();

        initNumberTextField();

        fillFieldsIfNoticeEntityNotEmpty(noticeEntity);

        initAuthorComboBox();

        fillAuthorComboBox();

        initCreatedDatePicker();
    }

    private void initNumberTextField() {
        ((SmartJTextField) numberTextField).setChildComboBox(authorComboBox);
    }

    private void initAuthorComboBox() {
        authorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof UsersEntity) {
                    UsersEntity usersEntity = (UsersEntity) value;
                    String username = usersEntity.getName() + " " + usersEntity.getSurname();
                    return super.getListCellRendererComponent(list, username, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
    }

    private void initCreatedDatePicker() {
        if (noticeEntity == null) {
            updateDatePickerModel(new Date(Calendar.getInstance().getTimeInMillis()));
            createdDatePicker.setEnabled(true);
        } else {
            createdDatePicker.setEnabled(false);
            Date creationDate = noticeEntity.getCreationDate();
            if (creationDate != null) {
                updateDatePickerModel(creationDate);
            }
        }
    }

    private void updateDatePickerModel(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        DateModel<?> model = createdDatePicker.getModel();
        model.setYear(instance.get(Calendar.YEAR));
        model.setMonth(instance.get(Calendar.MONTH));
        model.setDay(instance.get(Calendar.DAY_OF_MONTH));
        model.setSelected(true);
    }

    private void fillAuthorComboBox() {
        UsersService service = new UsersServiceImpl(new UsersDaoImpl(session));
        ArrayList<UsersEntity> usersEntities = (ArrayList<UsersEntity>) service.listUsers();
        DefaultComboBoxModel<UsersEntity> model = new DefaultComboBoxModel<>();
        for (UsersEntity entity : usersEntities) {
            if (entity.isActive()) {
                model.addElement(entity);
            }
        }
        authorComboBox.setModel(model);

        if (noticeEntity == null) {
            authorComboBox.setEnabled(true);
            authorComboBox.setSelectedItem(null);
        } else {
            authorComboBox.setEnabled(false);
            authorComboBox.setSelectedItem(noticeEntity.getAuthorByUserId());
        }
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

        pack();
        setMinimumSize(getSize());
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

    private void fillFieldsIfNoticeEntityNotEmpty(NoticeEntity notice) {
        if (notice != null) {
            numberTextField.setText(notice.getNumber());
            noticeDateLabel.setText(DEFAULT_DATE_FORMAT.format(notice.getDate()));
            noticeDescriptionTextArea.setText(notice.getDescription());
        } else {
            numberTextField.setText("");
            noticeDateLabel.setText(DEFAULT_DATE_FORMAT.format(Calendar.getInstance().getTime()));
            noticeDescriptionTextArea.setText("");
        }
    }

    private void onCancel() {
        dispose();
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> {
            if (verifyNotice()) {
                onOK();
            }
        });

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

    private boolean verifyNotice() {
        if (numberTextField.getText().isEmpty()) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Номер извещения не указан",
                    "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (authorComboBox.getSelectedItem() == null && noticeEntity == null) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Не указан автор извещения",
                    "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (getDateFromPicker() == null && noticeEntity == null) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Не указана дата выпуска извещения",
                    "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void onOK() {
        LoginWindow loginWindow = new LoginWindow(session);
        loginWindow.setLocation(FrameUtils.getFrameOnCenter(this, loginWindow));
        loginWindow.setVisible(true);
        UsersEntity user = loginWindow.getAuthorizedUser();
        if (user != null) {
            if (user.isActive()) {
                if (user.isAdmin() || user.isEditor()) {
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
                    entity.setAuthorByUserId((UsersEntity) authorComboBox.getSelectedItem());
                    entity.setCreationDate(getDateFromPicker());

                    if (noticeEntity != null) {
                        service.updateNotice(entity);
                        noticeEntity = entity;
                        dispose();
                    } else {
                        try {
                            NoticeEntity noticeByNumber = service.getNoticeByNumber(numberTextField.getText());
                            UsersEntity usersByProvidedByUserId = noticeByNumber.getUsersByProvidedByUserId();
                            JOptionPane.showMessageDialog(this, "Извещение с таким номером уже существует!\n" +
                                            "Номер: " + noticeByNumber.getNumber() + "\n" +
                                            "Провел: " + usersByProvidedByUserId.getUsername()
                                            + " (" + usersByProvidedByUserId.getName() + " " + usersByProvidedByUserId.getSurname() + ")\n" +
                                            "Извещение последний раз изменено: " + DEFAULT_DATE_FORMAT.format(noticeByNumber.getDate()),
                                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
                        } catch (Exception e) {
                            int i = service.addNotice(entity);
                            noticeEntity = service.getNoticeById(i);
                            dispose();
                        }

                    }
                } else {
                    FrameUtils.shakeFrame(this);
                    JOptionPane.showMessageDialog(this, "Пользователь должен быть администратором или редактором (конструктором).",
                            "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                FrameUtils.shakeFrame(this);
                JOptionPane.showMessageDialog(this, "Пользователь должен быть действующим!",
                        "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Чтобы продолжить, необходимо выполнить вход.",
                    "Ошибка входа", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Date getDateFromPicker() {
        java.util.Date selectedDate = (java.util.Date) createdDatePicker.getModel().getValue();
        System.out.println(">>>> " + selectedDate);
        if (selectedDate != null) {
            return new Date(selectedDate.getTime());
        } else return null;
    }

    private JDatePickerImpl getDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Сегодня");
        properties.put("text.month", "Месяц");
        properties.put("text.year", "Год");
        JDatePanelImpl jDatePanel = new JDatePanelImpl(model, properties);
        return new JDatePickerImpl(jDatePanel, new DateLabelFormatter()) {
            @Override
            public void setEnabled(boolean enabled) {
                getComponent(1).setEnabled(enabled);
            }
        };
    }

    public NoticeEntity getNoticeEntity() {
        return noticeEntity;
    }

    private void createUIComponents() {
        numberTextField = new SmartJTextField();
        createdDatePicker = getDatePicker();
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
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Номер:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        panel3.add(numberTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Автор извещения:");
        panel3.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        authorComboBox = new JComboBox();
        panel3.add(authorComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Дата выпуска:");
        panel3.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createdDatePicker.setBackground(new Color(-986419));
        panel3.add(createdDatePicker, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label1.setLabelFor(numberTextField);
        label3.setLabelFor(noticeDescriptionTextArea);
        label4.setLabelFor(authorComboBox);
        label5.setLabelFor(createdDatePicker);
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