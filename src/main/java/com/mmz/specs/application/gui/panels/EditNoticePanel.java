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

package com.mmz.specs.application.gui.panels;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.client.CreateNoticeWindow;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public class EditNoticePanel extends JPanel {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton createNoticeButton;
    private JTabbedPane mainTabbedPane;
    private JTree mainTree;
    private JComboBox<NoticeEntity> noticeComboBox;
    private JTextArea noticeDescriptionTextArea;
    private JLabel noticeNumberLabel;
    private JLabel noticeDateLabel;
    private JLabel noticeUserLabel;
    private JButton editNoticeButton;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton moveItemUpButton;
    private JButton moveItemDownButton;
    private JComboBox numberComboBox;
    private JComboBox<DetailTitleEntity> titleComboBox;
    private JCheckBox unitCheckBox;
    private JTextField finishedWeightTextField;
    private JTextField workpieceWeightTextField;
    private JButton createMaterialButton;
    private JButton createTitleButton;
    private JComboBox techProcessComboBox;
    private JButton createTechProcessButton;
    private JCheckBox isActive;
    private JPanel changePanel;
    private JPanel noticePanel;
    private JPanel savePanel;
    private JTextField detailCount;
    private JButton setMaterial;
    private JLabel materialLabel;
    private Session session;
    private DetailEntity detailEntity;

    EditNoticePanel(DetailEntity detailEntity) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
        this.session = ClientBackgroundService.getInstance().getSession();
        initGui();
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        initListeners();

        initKeyBindings();

        initNoticeComboBox();

        fillNoticeComboBox();


        initMainTree();

        initDetailTitleComboBox();

        fillDetailTitleComboBox();

    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        createNoticeButton.addActionListener(e -> onCreateNewNotice());

        editNoticeButton.addActionListener(e -> onEditNotice());

        addItemButton.addActionListener(e -> onAddNewItem());
        removeItemButton.addActionListener(e -> onRemoveItem());
        moveItemUpButton.addActionListener(e -> onMoveItemUp());
        moveItemDownButton.addActionListener(e -> onMoveItemDown());

        createTitleButton.addActionListener(e -> onCreateNewTitle());
    }

    private void onCreateNewTitle() {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.isActive()) {
                    if (currentUser.isAdmin()) {
                        String result = JOptionPane.showInputDialog(this,
                                "Введите новое наименование:", "Новое наименование", JOptionPane.PLAIN_MESSAGE);
                        if (result != null) {
                            if (!result.isEmpty()) {
                                if (result.length() <= 120) {
                                    if (currentUser.isAdmin()) {
                                        result = result.replace("\n", " ");
                                        DetailTitleEntity titleEntity = new DetailTitleEntity();
                                        titleEntity.setActive(true);
                                        titleEntity.setTitle(result);
                                        DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));

                                        createNewTitle(result, titleEntity, service);
                                    } else {
                                        JOptionPane.showMessageDialog(this,
                                                "Вам необходимо быть администратором,\n" +
                                                        "чтобы проводить изменения.", "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                            "Длина наименования не может привышать 120 символов (сейчас: " + result.length() + ")", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        }
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "Вам необходимо быть администратором,\n" +
                                "чтобы проводить изменения.", "Ошибка доступа", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Необходимо выполнить вход, чтобы продолжить", "Ошибка входа", JOptionPane.WARNING_MESSAGE);
            }
        }

    }

    private void createNewTitle(String result, DetailTitleEntity titleEntity, DetailTitleService service) {
        try {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            service.addDetailTitle(titleEntity);
            transaction.commit();

            ClientBackgroundService.getInstance().refreshSession();

            titleComboBox.removeAllItems();
            fillDetailTitleComboBox();

            titleComboBox.setSelectedItem(titleEntity);

        } catch (Throwable throwable) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось добавить " + result + "\n" + throwable.getLocalizedMessage(),
                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initDetailTitleComboBox() {
        titleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailTitleEntity) {
                    DetailTitleEntity detailTitleEntity = (DetailTitleEntity) value;
                    return super.getListCellRendererComponent(list, detailTitleEntity.getTitle(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        titleComboBox.addActionListener(e -> System.out.println(">> " + titleComboBox.getSelectedItem()));
    }

    private void fillDetailTitleComboBox() {
        DefaultComboBoxModel<DetailTitleEntity> model = (DefaultComboBoxModel<DetailTitleEntity>) titleComboBox.getModel();
        DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
        List<DetailTitleEntity> detailTitleEntities = service.listDetailTitles();
        Collections.sort(detailTitleEntities);

        model.removeAllElements();

        for (DetailTitleEntity entity : detailTitleEntities) {
            if (entity != null) {
                if (entity.isActive()) {
                    DetailTitleEntity current = new DetailTitleEntity(entity.getId(), entity.getTitle(), entity.isActive());
                    model.addElement(current);
                }
            }
        }
    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //fixme this doesn't work for some reason
        contentPane.registerKeyboardAction(e -> onAddNewItem(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onRemoveItem(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onMoveItemUp(), KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onMoveItemDown(), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onAddNewItem() {
        System.out.println("not supported yet");
    }

    private void onRemoveItem() {
        System.out.println("not supported yet");

    }

    private void onMoveItemUp() {
        System.out.println("not supported yet");

    }

    private void onMoveItemDown() {
        System.out.println("not supported yet");
    }


    private void onOK() {
    }

    private void onCancel() {
        System.out.println("onCancel not supported yet");
    }

    private void onEditNotice() {
        Object selectedItem = noticeComboBox.getSelectedItem();
        if (selectedItem != null) {
            NoticeEntity entity = (NoticeEntity) selectedItem;
            CreateNoticeWindow noticeWindow = new CreateNoticeWindow(entity);
            noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
            noticeWindow.setVisible(true);
            ClientBackgroundService.getInstance().refreshSession();
            Object selectedObject = noticeComboBox.getSelectedItem();
            noticeComboBox.removeAllItems();
            fillNoticeComboBox();
            if (selectedObject != null) {
                noticeComboBox.setSelectedItem(selectedObject);
            }
        }
    }

    private void onCreateNewNotice() {
        CreateNoticeWindow noticeWindow = new CreateNoticeWindow(null);
        noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
        noticeWindow.setVisible(true);
        ClientBackgroundService.getInstance().refreshSession();
        noticeComboBox.removeAllItems();
        initNoticeComboBox();
    }

    private void initMainTree() {
        if (detailEntity != null) {
            fillMainTree();
        }
    }

    private void fillMainTree() {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            if (!detailEntity.isUnit()) {
                DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                DefaultMutableTreeNode detail = new DefaultMutableTreeNode(detailEntity, false);
                root.add(detail);
                mainTree.setModel(new DefaultTreeModel(root));
            } else {
                mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).getDetailListTreeByDetailList(service.getDetailListByParent(detailEntity.getCode()))));
            }
        }
    }

    private void initNoticeComboBox() {
        noticeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof NoticeEntity) {
                    NoticeEntity entity = (NoticeEntity) value;
                    String date = new SimpleDateFormat("dd.MM.yyyy").format(entity.getDate());
                    String description = CommonUtils.substring(90, entity.getDescription().replace("\n", " "));

                    String text = entity.getNumber() + " (посл. изм. " + date + ") " + description;
                    return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        noticeComboBox.addActionListener(e -> {
            NoticeEntity entity = (NoticeEntity) noticeComboBox.getSelectedItem();
            if (entity != null) {
                noticeNumberLabel.setText(entity.getNumber());
                noticeDateLabel.setText(new SimpleDateFormat("dd.MM.yyyy").format(entity.getDate()));
                UsersEntity user = entity.getUsersByProvidedByUserId();
                noticeUserLabel.setText(user.getUsername() + " " + user.getName() + " " + user.getSurname());
                noticeDescriptionTextArea.setText(entity.getDescription());
            } else {
                noticeNumberLabel.setText("нет данных");
                noticeDateLabel.setText("нет данных");
                noticeUserLabel.setText("нет данных");
                noticeDescriptionTextArea.setText("");
            }
        });

        if (noticeComboBox.getModel().getSize() > 0) {
            noticeComboBox.setSelectedIndex(0);
        }

    }

    private void fillNoticeComboBox() {
        NoticeService service = new NoticeServiceImpl(new NoticeDaoImpl(session));
        DefaultComboBoxModel<NoticeEntity> model = new DefaultComboBoxModel<>();

        List<NoticeEntity> list = service.listNotices();
        Collections.sort(list);

        for (NoticeEntity entity : list) {
            NoticeEntity noticeEntity = new NoticeEntity(entity.getId(), entity.getNumber(),
                    entity.getDate(), entity.getDescription(), entity.getUsersByProvidedByUserId());
            model.addElement(noticeEntity);
        }

        noticeComboBox.setModel(model);
    }

    private void createUIComponents() {
        mainTree = new DetailJTree();
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane = new JTabbedPane();
        contentPane.add(mainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        noticePanel = new JPanel();
        noticePanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainTabbedPane.addTab("Извещение", noticePanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        noticePanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Извещение:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createNoticeButton = new JButton();
        createNoticeButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/noticeNew16.png")));
        createNoticeButton.setText("");
        createNoticeButton.setToolTipText("Создать новое извещение");
        panel1.add(createNoticeButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeComboBox = new JComboBox();
        panel1.add(noticeComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(-1, 150), 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Номер:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Провел:");
        panel1.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Описание:");
        panel1.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 100), null, null, 0, false));
        noticeDescriptionTextArea = new JTextArea();
        noticeDescriptionTextArea.setBackground(new Color(-855310));
        noticeDescriptionTextArea.setEditable(false);
        Font noticeDescriptionTextAreaFont = this.$$$getFont$$$("Consolas", -1, 14, noticeDescriptionTextArea.getFont());
        if (noticeDescriptionTextAreaFont != null) noticeDescriptionTextArea.setFont(noticeDescriptionTextAreaFont);
        scrollPane1.setViewportView(noticeDescriptionTextArea);
        final JLabel label5 = new JLabel();
        label5.setText("Дата:");
        panel1.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeNumberLabel = new JLabel();
        noticeNumberLabel.setText("нет данных");
        panel1.add(noticeNumberLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeDateLabel = new JLabel();
        noticeDateLabel.setText("нет данных");
        panel1.add(noticeDateLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeUserLabel = new JLabel();
        noticeUserLabel.setText("нет данных");
        panel1.add(noticeUserLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editNoticeButton = new JButton();
        editNoticeButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editNoticeButton.setText("");
        editNoticeButton.setToolTipText("Редактировать извещение");
        panel1.add(editNoticeButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        changePanel = new JPanel();
        changePanel.setLayout(new GridLayoutManager(11, 7, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Изменения", changePanel);
        final JScrollPane scrollPane2 = new JScrollPane();
        changePanel.add(scrollPane2, new GridConstraints(0, 0, 10, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(150, -1), null, null, 0, false));
        mainTree.setBackground(new Color(-855310));
        mainTree.setRootVisible(false);
        mainTree.setScrollsOnExpand(true);
        mainTree.setShowsRootHandles(true);
        mainTree.setVerifyInputWhenFocusTarget(false);
        mainTree.setVisibleRowCount(2);
        scrollPane2.setViewportView(mainTree);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        changePanel.add(toolBar1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addItemButton = new JButton();
        addItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addItemButton.setText("");
        addItemButton.setToolTipText("Добавить деталь / узел (CTRL++)");
        toolBar1.add(addItemButton);
        removeItemButton = new JButton();
        removeItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeItemButton.setText("");
        removeItemButton.setToolTipText("Удалить деталь / узел (CTRL+-)");
        toolBar1.add(removeItemButton);
        moveItemUpButton = new JButton();
        moveItemUpButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/upArrow16.png")));
        moveItemUpButton.setText("");
        moveItemUpButton.setToolTipText("Поднять вверх (CTRL+ВВЕРХ)");
        toolBar1.add(moveItemUpButton);
        moveItemDownButton = new JButton();
        moveItemDownButton.setFocusable(false);
        moveItemDownButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/downArrow16.png")));
        moveItemDownButton.setText("");
        moveItemDownButton.setToolTipText("Опустить вниз (CTRL+ВНИЗ)");
        toolBar1.add(moveItemDownButton);
        numberComboBox = new JComboBox();
        numberComboBox.setEditable(true);
        changePanel.add(numberComboBox, new GridConstraints(0, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        changePanel.add(spacer3, new GridConstraints(9, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Индекс:");
        changePanel.add(label6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Наименование:");
        changePanel.add(label7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitCheckBox = new JCheckBox();
        unitCheckBox.setHorizontalTextPosition(2);
        unitCheckBox.setText("Узел:");
        unitCheckBox.setMnemonic('У');
        unitCheckBox.setDisplayedMnemonicIndex(0);
        changePanel.add(unitCheckBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Масса готовой детали:");
        changePanel.add(label8, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightTextField = new JTextField();
        changePanel.add(finishedWeightTextField, new GridConstraints(4, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Норма расхода:");
        changePanel.add(label9, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightTextField = new JTextField();
        changePanel.add(workpieceWeightTextField, new GridConstraints(5, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Материал:");
        changePanel.add(label10, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createMaterialButton = new JButton();
        createMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/materialNew16.png")));
        createMaterialButton.setText("");
        changePanel.add(createMaterialButton, new GridConstraints(6, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTitleButton = new JButton();
        createTitleButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/titleNew16.png")));
        createTitleButton.setText("");
        changePanel.add(createTitleButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Технический процесс:");
        changePanel.add(label11, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessComboBox = new JComboBox();
        changePanel.add(techProcessComboBox, new GridConstraints(7, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTechProcessButton = new JButton();
        createTechProcessButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/techprocessNew16.png")));
        createTechProcessButton.setText("");
        changePanel.add(createTechProcessButton, new GridConstraints(7, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActive = new JCheckBox();
        isActive.setHorizontalTextPosition(2);
        isActive.setSelected(false);
        isActive.setText("Аннулирована:");
        isActive.setMnemonic('А');
        isActive.setDisplayedMnemonicIndex(0);
        changePanel.add(isActive, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Количество:");
        changePanel.add(label12, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailCount = new JTextField();
        changePanel.add(detailCount, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, -1), new Dimension(40, -1), 0, false));
        setMaterial = new JButton();
        setMaterial.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        setMaterial.setText("");
        setMaterial.setToolTipText("Добавить материал");
        changePanel.add(setMaterial, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialLabel = new JLabel();
        materialLabel.setText("нет данных");
        changePanel.add(materialLabel, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleComboBox = new JComboBox();
        titleComboBox.setMaximumRowCount(30);
        titleComboBox.setToolTipText("Тип пользователя");
        changePanel.add(titleComboBox, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        savePanel = new JPanel();
        savePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Сохранение", savePanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        savePanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("Подтвердить");
        panel3.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        panel3.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        savePanel.add(spacer5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label6.setLabelFor(numberComboBox);
        label8.setLabelFor(finishedWeightTextField);
        label9.setLabelFor(workpieceWeightTextField);
        label11.setLabelFor(techProcessComboBox);
        label12.setLabelFor(detailCount);
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
