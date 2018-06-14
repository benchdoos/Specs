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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JButton multipleSelectionButton;
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

        updateCodeComboBox();

        pack();
        setResizable(false);
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


   /* private void onNewDetailSave(String fixedCode, DetailEntity dbDetail) {
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
    }*/

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
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

    private void updateCodeComboBox() {
        if (incomingDetailEntity != null) {
            codeComboBox.setSelectedItem(incomingDetailEntity.getCode());
        } else {
            if (codeComboBox.getItemCount() >= 1) {
                codeComboBox.setSelectedItem(0);
            }
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

        titleComboBox.addActionListener(e -> {
            if (entities != null) {
                if (entities.size() == 1) {
                    final DetailEntity entity = entities.get(0);
                    if (!entity.getDetailTitleByDetailTitleId().equals(titleComboBox.getSelectedItem())) {
                        if (incomingDetailEntity != null) {
                            DetailService service = new DetailServiceImpl(session);
                            final DetailEntity detailByIndex = service.getDetailByIndex(entity.getCode());
                            if (detailByIndex == null || detailByIndex.getCode().equalsIgnoreCase(incomingDetailEntity.getCode())) {
                                entity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                            }
                        }
                    }
                }
            }
        });
    }

            /*
        try {
                            DetailEntity dbDetail = service.getDetailByIndex(selectedDetail.getCode());

                        } catch (Exception e) {
                            if (incomingDetailEntity == null) {
                                selectedDetail.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                                entities.set(0, service.getDetailById(service.addDetail(selectedDetail)));
                                dispose();
                            } else {
                                incomingDetailEntity.setCode((String) codeComboBox.getSelectedItem());
                                incomingDetailEntity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());

                                service.updateDetail(incomingDetailEntity);

                                entities.set(0, service.getDetailById(service.addDetail(incomingDetailEntity)));
                                dispose();
                            }
                        }
         */

        /*
                            final DetailTitleEntity selectedTitle = (DetailTitleEntity) titleComboBox.getSelectedItem();
                            final String selectedCode = (String) codeComboBox.getSelectedItem();
                            if (incomingDetailEntity != null) {
                                if (selectedTitle != null) {
                                    if (!incomingDetailEntity.getDetailTitleByDetailTitleId().equals(selectedTitle)
                                            || !incomingDetailEntity.getCode().equalsIgnoreCase(selectedCode)) {
                                        final int i = JOptionPane.showConfirmDialog(this, "Вы точно хотите изменить данные по детали:\n"
                                                + selectedDetail.getCode() + " " + selectedDetail.getDetailTitleByDetailTitleId().getTitle() + "\n" +
                                                "на: " + dbDetail.getCode() + " " + selectedTitle.getTitle());
                                        if (i == 0) { // confirm
                                            if (dbDetail.getId() == incomingDetailEntity.getId()) {

                                                selectedDetail.setDetailTitleByDetailTitleId(selectedTitle);

                                                service.updateDetail(selectedDetail);

                                                entities.set(0, service.getDetailById(incomingDetailEntity.getId()));
                                                dispose();
                                            } else {
                                                FrameUtils.shakeFrame(this);
                                                JOptionPane.showMessageDialog(this,
                                                        "Вы указали существующий индекс другой детали: \n"
                                                                + dbDetail.getCode() + " "
                                                                + dbDetail.getDetailTitleByDetailTitleId().getTitle());
                                                log.warn("User tried to change code for entity: {}, that has another entity: {}",
                                                        selectedDetail, dbDetail);
                                            }
                                        }
                                    } else {
                                        System.out.println("!!!!!!!!!!!!! + everything is alright! ");
                                        dispose();
                                    }
                                } else {

                                }
                            }
*/

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
            multipleSelectionButton.setEnabled(false);
            unitCheckBox.setEnabled(false);

            final ArrayList<DetailEntity> detailEntities = new ArrayList<>();
            detailEntities.add(incomingDetailEntity);
            fillDetailInfo(detailEntities);
        }

        multipleSelectionButton.setEnabled(!singleSelectionOnly);
    }

    private void initSeveralSelectionButton() {
        multipleSelectionButton.addActionListener(e -> onMultipleSelectionButton());
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
//            return true;
        }
    }

    private boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'а' && ch <= 'я')
                || (ch >= 'А' && ch <= 'Я');
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
                titleComboBox.setEnabled(true);

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

    private void onMultipleSelectionButton() {
        SelectMultipleDetails selectMultipleDetails = new SelectMultipleDetails();
        selectMultipleDetails.setLocation(FrameUtils.getFrameOnCenter(this, selectMultipleDetails));
        selectMultipleDetails.setVisible(true);
        final ArrayList<DetailEntity> selectedDetailEntities = selectMultipleDetails.getSelectedDetailEntities();

        if (selectedDetailEntities != null) {
            this.entities = selectedDetailEntities;
            if (selectedDetailEntities.size() > 1) {
                fillCodeComboBox();
                fillTitleComboBox();
                fillDetailInfo(entities);
            }
        } else {
            this.entities = null;
            fillCodeComboBox();
            fillTitleComboBox();
            fillDetail(new DetailServiceImpl(session));
        }
    }

    private void fillDetail(DetailService service) {
        final String selectedItem = (String) codeComboBox.getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem.length() < 30) {
                final DetailEntity detailEntity = service.getDetailByIndex(selectedItem);
                if (detailEntity != null) {
                    entities = new ArrayList<>();
                    entities.add(detailEntity);
                    fillDetailInfo(entities);
                } else {
                    if (entities != null) {
                        entities = null;
                        fillDetailInfo(null);
                    } else {
                        entities = new ArrayList<>();

                        DetailEntity entity = new DetailEntity();
                        entity.setCode((String) codeComboBox.getSelectedItem());
                        entity.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                        entity.setActive(true);

                        entities.add(entity);
                        fillDetailInfo(entities);
                    }
                }
            }
        }
    }

    private void editExistingDetail(DetailEntity entity) {
        DetailService service = new DetailServiceImpl(session);

        if (entity.getCode().equals(incomingDetailEntity.getCode())) {
            if (entity.getDetailTitleByDetailTitleId().equals(incomingDetailEntity.getDetailTitleByDetailTitleId())) {
                dispose();
            } else {
                final int i = JOptionPane.showConfirmDialog(this, "Вы точно хотите изменить данные по детали:\n"
                        + incomingDetailEntity.getCode() + " " + incomingDetailEntity.getDetailTitleByDetailTitleId().getTitle() + "\n" +
                        "на: " + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle());
                if (i == 0) { // confirm
                    service.updateDetail(entity); //add something here? what to do here???
                    dispose();
                }
            }
        } else {
            try {
                DetailEntity dbDetail = service.getDetailByIndex(entity.getCode());
                if (dbDetail != null) {
                    FrameUtils.shakeFrame(this);
                    JOptionPane.showMessageDialog(this,
                            "Вы указали существующий индекс другой детали: \n"
                                    + dbDetail.getCode() + " "
                                    + dbDetail.getDetailTitleByDetailTitleId().getTitle());
                    log.warn("User tried to change code for entity: {}, that has another entity: {}",
                            entity, dbDetail);
                } else {
                    incomingDetailEntity.setCode(entity.getCode());
                    incomingDetailEntity.setDetailTitleByDetailTitleId(entity.getDetailTitleByDetailTitleId());
                    service.updateDetail(incomingDetailEntity);
                    dispose();
                }
            } catch (HeadlessException e) {
                incomingDetailEntity.setCode(entity.getCode());
                incomingDetailEntity.setDetailTitleByDetailTitleId(entity.getDetailTitleByDetailTitleId());
                service.updateDetail(incomingDetailEntity);
                dispose();
            }
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
                } else if (text.length() > 1) {
                    final char[] oldChars = text.toCharArray();
                    if (containsChars(oldChars)) {
                        if (fixedString != null) {
                            document.remove(start, end - start);
                            document.insertString(start, fixedString, null);
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            log.warn("Could not find location to fix codeComboBox", e);
        }
    }

    private boolean containsChars(char[] oldChars) {
        for (char c : oldChars) {
            if (c == ',' || c == ' ') {
                return true;
            }
        }
        return false;
    }

    private void onOK() {
        log.debug("Got entities to save: " + entities);
        if (verify()) {
            if (entities != null) {
                if (entities.size() == 1) {
                    DetailEntity selectedDetail = entities.get(0);
                    if (selectedDetail != null) {
                        // fixme this breaks another entity, that is not changing.... wtf??? links???
//                        selectedDetail.setDetailTitleByDetailTitleId((DetailTitleEntity) titleComboBox.getSelectedItem());
                        if (incomingDetailEntity == null) {
                            createNewDetail(selectedDetail);
                        } else {
                            editExistingDetail(selectedDetail);
                        }

                    } else {
                        dispose();
                    }
                } else {
                    dispose();
                }
            } else {
                dispose();
            }
        }
    }

    private void createNewDetail(DetailEntity entity) {
        log.debug("Creating / adding new entity: ");
        DetailService service = new DetailServiceImpl(session);
        try {
            DetailEntity dbDetail = service.getDetailByIndex(entity.getCode());
            if (!dbDetail.getDetailTitleByDetailTitleId().equals(entity.getDetailTitleByDetailTitleId())) {
                final int i = JOptionPane.showConfirmDialog(this, "Вы точно хотите изменить данные по детали:\n"
                        + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + "\n" +
                        "на: " + dbDetail.getCode() + " " + dbDetail.getDetailTitleByDetailTitleId().getTitle());
                if (i == 0) { // confirm
                    service.updateDetail(entity); //add something here? what to do here???
                    dispose();
                }
            } else {
                dispose();
            }
        } catch (Exception e) {
            DetailTitleEntity title = (DetailTitleEntity) titleComboBox.getSelectedItem();
            if (title != null) {
                entity.setDetailTitleByDetailTitleId(title);
                entities.set(0, service.getDetailById(service.addDetail(entity)));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Укажите наименование детали", "Ошибка сохранения", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void initCodeComboBox() {
        DetailService service = new DetailServiceImpl(session);

        ((JTextField) codeComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                check(e);
            }

            private void check(DocumentEvent e) {
                try {
                    final String text = e.getDocument().getText(e.getDocument().getStartPosition().getOffset(), e.getDocument().getEndPosition().getOffset());
                    if (text.contains(",") || text.contains(" ")) {
                        Runnable runnable = () -> fixLastIndex();
                        SwingUtilities.invokeLater(runnable);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check(e);
            }
        });

        codeComboBox.addItemListener(e -> {
            if (entities != null) {
                if (entities.size() <= 1) {
                    fillDetail(service);
                }
            } else {
                fillDetail(service);
            }
        });
        codeComboBox.setSelectedIndex(-1);
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
        buttonCancel.setMnemonic('О');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(600, 400), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Введите индекс:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        codeComboBox = new JComboBox();
        codeComboBox.setEditable(true);
        codeComboBox.setMaximumRowCount(15);
        panel3.add(codeComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(400, -1), 0, false));
        multipleSelectionButton = new JButton();
        multipleSelectionButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/tree/someDetails.png")));
        panel3.add(multipleSelectionButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleComboBox = new JComboBox();
        titleComboBox.setMaximumRowCount(15);
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
