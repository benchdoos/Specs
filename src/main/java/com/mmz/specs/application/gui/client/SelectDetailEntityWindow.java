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

import com.google.common.collect.ComparisonChain;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import com.mmz.specs.service.DetailTitleService;
import com.mmz.specs.service.DetailTitleServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

public class SelectDetailEntityWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final static String ERROR_TITLE = "Ошибка верификации";
    private final static String LOG_VERIFICATION_ERROR_MESSAGE = "Could not verify selected entities: ";
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> codeComboBox;
    private JButton severalSelectionButton;
    private JComboBox<DetailTitleEntity> titleComboBox;
    private JCheckBox unitCheckBox;
    private ArrayList<DetailEntity> entities;
    private DetailEntity incomingDetailEntity;
    private boolean singleSelectionOnly;
    private boolean unitEnabled;

    public SelectDetailEntityWindow(DetailEntity incomingDetailEntity, boolean singleSelectionOnly, boolean unitEnabled) {
        this.incomingDetailEntity = incomingDetailEntity;
        this.singleSelectionOnly = singleSelectionOnly;
        this.unitEnabled = unitEnabled;
        this.session = ClientBackgroundService.getInstance().getSession();
        initWindow();
    }

    private void initWindow() {
        initGui();

        initListeners();
        initKeyBindings();

        initCodeComboBox();

        fillCodeComboBox();


        initTitleComboBox();

        fillTitleComboBox();

        initSeveralSelectionButton();

        pack();
        setResizable(false);
    }

    private void initSeveralSelectionButton() {
        severalSelectionButton.addActionListener(e -> {
            SelectMultipleDetails selectMultipleDetails = new SelectMultipleDetails();
            selectMultipleDetails.setLocation(FrameUtils.getFrameOnCenter(this, selectMultipleDetails));
            selectMultipleDetails.setVisible(true);
            final ArrayList<DetailEntity> selectedDetailEntities = selectMultipleDetails.getSelectedDetailEntities();

            if (selectedDetailEntities != null) {
                this.entities = selectedDetailEntities;
                fillDetailInfo(entities);
            } else {
                fillCodeComboBox();
                fillTitleComboBox();
                fillDetail(new DetailServiceImpl(session));
            }
        });
    }

    private void initCodeComboBox() {
        DetailService service = new DetailServiceImpl(session);

        codeComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (evt.getKeyChar() == ',' || evt.getKeyChar() == ' ') {
                    fixLastIndex();
                }
            }

            private void fixLastIndex() {
                final JTextComponent editor = (JTextComponent) codeComboBox.getEditor().getEditorComponent();
                final Document document = editor.getDocument();

                int start = editor.getSelectionStart() - 1;
                start = (start < 0) ? 0 : start;
                int end = editor.getSelectionStart();

                String fixedString = fixCode((String) codeComboBox.getSelectedItem());

                try {
                    if (document instanceof AbstractDocument) {
                        if (fixedString != null) {
                            ((AbstractDocument) document).replace(start, end - start, ".",
                                    null);
                        }
                    } else {
                        final String text = document.getText(start, end - start);

                        if (text.length() == 1) {
                            final char c = text.toCharArray()[0];
                            if (c == ',' || c == ' ') {
                                if (fixedString != null) {
                                    document.remove(start, end - start);
                                    document.insertString(start, ".", null);
                                }
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        codeComboBox.addItemListener(e -> {
            fillDetail(service);
        });

    }

    private void fillDetail(DetailService service) {
        final String selectedItem = (String) codeComboBox.getSelectedItem();
        final DetailEntity detailEntity = service.getDetailByIndex(selectedItem);
        if (detailEntity != null) {
            entities = new ArrayList<>();
            entities.add(detailEntity);
            fillDetailInfo(entities);
        } else {
            entities = null;
            fillDetailInfo(null);
        }
    }

    private void initTitleComboBox() {
        titleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailTitleEntity) {
                    DetailTitleEntity entity = (DetailTitleEntity) value;
                    return super.getListCellRendererComponent(list, entity.getTitle(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        titleComboBox.addKeyListener(new KeyAdapter() {
            ArrayList<DetailTitleEntity> list;

            @Override
            public void keyPressed(KeyEvent e) {
                list = getTitleEntities();

                final char keyChar = e.getKeyChar();
                if (isLetter(keyChar)) {
                    select(list, keyChar);
                }

            }

            private void select(ArrayList<DetailTitleEntity> list, char keyChar) {
                for (DetailTitleEntity entity : list) {
                    String title = entity.getTitle();
                    if (title != null) {
                        if (!title.isEmpty()) {

                            title = title.toUpperCase();
                            keyChar = Character.toUpperCase(keyChar); // toUpperCase
                            if (title.toCharArray()[0] == keyChar) {
                                titleComboBox.setSelectedItem(entity);
                                break;
                            }
                        }
                    }
                }
            }

            private ArrayList<DetailTitleEntity> getTitleEntities() {
                ArrayList<DetailTitleEntity> result = new ArrayList<>();
                DefaultComboBoxModel<DetailTitleEntity> model = (DefaultComboBoxModel<DetailTitleEntity>) titleComboBox.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    DetailTitleEntity entity = model.getElementAt(i);
                    result.add(entity);
                }
                return result;
            }
        });
    }

    private void fillCodeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        DetailService service = new DetailServiceImpl(session);
        final ArrayList<DetailEntity> detailEntities = (ArrayList<DetailEntity>) service.listDetails();

        detailEntities.sort((o1, o2) -> {
            if (o2 != null) {
                return ComparisonChain.start()
                        .compare(o1.getCode(), o2.getCode())
                        .compareTrueFirst(o1.isActive(), o2.isActive())
                        .compareTrueFirst(o1.isUnit(), o2.isUnit())
                        .result();
            } else {
                return -1;
            }
        });

        for (DetailEntity entity : detailEntities) {
            model.addElement(entity.getCode());
        }

        codeComboBox.setModel(model);
        AutoCompleteDecorator.decorate(this.codeComboBox);
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/tree/someDetails.png")));
        if (incomingDetailEntity == null) {
            setTitle("Выбор детали");
        } else {
            final String detailInfo = incomingDetailEntity.getCode() + " " + incomingDetailEntity.getDetailTitleByDetailTitleId().getTitle();
            setTitle("Редактирование детали: " + detailInfo);
            severalSelectionButton.setEnabled(false);
            unitCheckBox.setEnabled(false);

            final ArrayList<DetailEntity> detailEntities = new ArrayList<>();
            detailEntities.add(incomingDetailEntity);
            fillDetailInfo(detailEntities);
        }

        severalSelectionButton.setEnabled(!singleSelectionOnly);
    }

    private void fillTitleComboBox() {
        DefaultComboBoxModel<DetailTitleEntity> model = new DefaultComboBoxModel<>();

        DetailTitleService service = new DetailTitleServiceImpl(session);
        ArrayList<DetailTitleEntity> list = (ArrayList<DetailTitleEntity>) service.listDetailTitles();

        Collections.sort(list);

        for (DetailTitleEntity entity : list) {
            if (entity.isActive()) {
                model.addElement(entity);
            }
        }
        titleComboBox.setModel(model);
    }

    private void onOK() {
        if (verify()) {
            dispose();
        }
    }

    private boolean verify() {
        if (entities != null) {
            if (entities.size() > 0) {
                if (entities.size() > 1) {
                    return !hasErrorsEntityList();
                } else {
                    return !checkSingleEntity();
                }
            } else {
                FrameUtils.shakeFrame(this);
                JOptionPane.showMessageDialog(this, "Необходимо выбрать хотя-бы одну деталь",
                        ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
                log.warn(LOG_VERIFICATION_ERROR_MESSAGE + "size is: {}, entities: {}", entities.size(), entities);
                return false;
            }
        } else {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Необходимо выбрать хотя-бы одну деталь",
                    ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
            log.warn(LOG_VERIFICATION_ERROR_MESSAGE + "{}", entities);
            return false;
        }
    }

    private boolean checkSingleEntity() {
        DetailEntity entity = entities.get(0);
        if (entity == null) {
            FrameUtils.shakeFrame(this);
            return true;
        }

        try {
            if (entity.getCode().isEmpty() || entity.getCode().length() > 30) {
                throw new IllegalArgumentException("Code length can not be 0 or >30, code is: "
                        + entity.getCode() + " " + entity.getCode().length());
            }
        } catch (Exception e) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this,
                    "Длинна индекса детали должна быть в диапазоне: [0;30]", ERROR_TITLE,
                    JOptionPane.WARNING_MESSAGE);
            log.warn(LOG_VERIFICATION_ERROR_MESSAGE + ": {}", entity, e);
            return true;
        }

        if (titleComboBox.getSelectedItem() == null) {
            FrameUtils.shakeFrame(this);
            JOptionPane.showMessageDialog(this, "Необходимо указать наименование детали",
                    ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
            return true;
        }

        return false;
    }

    private boolean hasErrorsEntityList() {
        for (DetailEntity entity : entities) {
            if (entity == null) {
                FrameUtils.shakeFrame(this);
                JOptionPane.showMessageDialog(this,
                        "Одна из деталей некорректна (обратитесь к разрабочику)",
                        ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
                log.warn(LOG_VERIFICATION_ERROR_MESSAGE + " one of entities is null, entities: {}", entities);
                return true;
            }
        }
        return false;
    }

    private void onCancel() {
        entities = null;
        dispose();
    }

    private String fixCode(String code) {
        if (code != null) {
            return code.toUpperCase()
                    .replace(",", ".")
                    .replace("/", ".")
                    .replace(" ", "");
        } else return null;
    }

    public ArrayList<DetailEntity> getEntities() {
        return entities;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private void fillDetailInfo(ArrayList<DetailEntity> detailEntities) {
        codeComboBox.setEnabled(true);
        unitCheckBox.setEnabled(unitEnabled);

        if (detailEntities == null) {
            codeComboBox.setEnabled(true);

            titleComboBox.setEnabled(true);

            unitCheckBox.setEnabled(unitEnabled);
            unitCheckBox.setSelected(false);

        } else {
            if (detailEntities.size() == 0) {
                titleComboBox.setSelectedItem(null);
                unitCheckBox.setSelected(false);
            } else {
                if (detailEntities.size() == 1) {
                    final DetailEntity detailEntity = detailEntities.get(0);
                    titleComboBox.setSelectedItem(detailEntity.getDetailTitleByDetailTitleId());
                    unitCheckBox.setSelected(detailEntity.isUnit());
                    unitCheckBox.setEnabled(false);
                } else {
                    DefaultComboBoxModel<String> codeModel = new DefaultComboBoxModel<>();
                    final String SOME_DETAILS_SELECTED_STRING = "выбрано несколько деталей";
                    codeModel.addElement(SOME_DETAILS_SELECTED_STRING);
                    codeComboBox.setModel(codeModel);
                    codeComboBox.setEnabled(false);

                    DefaultComboBoxModel<DetailTitleEntity> titleModel = new DefaultComboBoxModel<>();
                    final DetailTitleEntity title = new DetailTitleEntity();
                    title.setTitle(SOME_DETAILS_SELECTED_STRING);
                    titleModel.addElement(title);
                    titleComboBox.setModel(titleModel);
                    titleComboBox.setEnabled(false);

                    unitCheckBox.setSelected(false);
                    unitCheckBox.setEnabled(false);
                }
            }
        }
    }

    private boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'а' && ch <= 'я')
                || (ch >= 'А' && ch <= 'Я');
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
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(600, 400), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Введите индекс:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        codeComboBox = new JComboBox();
        codeComboBox.setEditable(true);
        panel3.add(codeComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(400, -1), 0, false));
        severalSelectionButton = new JButton();
        severalSelectionButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/tree/someDetails.png")));
        panel3.add(severalSelectionButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleComboBox = new JComboBox();
        panel3.add(titleComboBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), new Dimension(400, -1), 0, false));
        unitCheckBox = new JCheckBox();
        unitCheckBox.setText("Узел");
        unitCheckBox.setMnemonic('У');
        unitCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(unitCheckBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(codeComboBox);
        label2.setLabelFor(titleComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
