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
import com.mmz.specs.application.gui.client.EditMaterialListWindow;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.gui.common.SelectionDetailWindow;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.*;
import com.mmz.specs.model.*;
import com.mmz.specs.service.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
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
    private JComboBox<DetailEntity> codeComboBox;
    private JComboBox<DetailTitleEntity> detailTitleComboBox;
    private JCheckBox unitCheckBox;
    private JTextField finishedWeightTextField;
    private JTextField workpieceWeightTextField;
    private JButton createMaterialButton;
    private JButton createTitleButton;
    private JComboBox<TechProcessEntity> techProcessComboBox;
    private JButton createTechProcessButton;
    private JCheckBox isActiveCheckBox;
    private JPanel changePanel;
    private JPanel noticePanel;
    private JPanel savePanel;
    private JTextField detailCountTextField;
    private JButton editMaterialButton;
    private JLabel materialLabel;
    private JButton editImageButton;
    private JToolBar treeToolBar;
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

        initCodeComboBox();

        fillCodeComboBox();

        initNoticeComboBox();

        fillNoticeComboBox();


        initMainTree();

        initDetailTitleComboBox();

        fillDetailTitleComboBox();

        initTechProcessComboBox();

        fillTechProcessComboBox();
    }

    private void initListeners() {
        createNoticeButton.addActionListener(e -> onCreateNewNotice());
        editNoticeButton.addActionListener(e -> onEditNotice());

        initEditPanelListeners();

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initEditPanelListeners() {
        addItemButton.addActionListener(e -> onAddNewItemButton());
        removeItemButton.addActionListener(e -> onRemoveItem());
        moveItemUpButton.addActionListener(e -> onMoveItemUp());
        moveItemDownButton.addActionListener(e -> onMoveItemDown());

        detailCountTextField.getDocument().addDocumentListener(new DocumentListener() {
            final int MAX_VALUE = 1000;
            private final String toolTip = detailCountTextField.getToolTipText();

            private void verifyInput() {
                String text = detailCountTextField.getText();
                try {
                    Long number = Long.parseLong(text);
                    if (number < 0) {
                        showWarning();
                        return;
                    }

                    if (number > MAX_VALUE) {
                        showWarning();

                        return;
                    }

                    detailCountTextField.setBorder(new JTextField().getBorder());
                    detailCountTextField.setToolTipText(toolTip);
                } catch (Throwable throwable) {
                    showWarning();
                }
            }

            private void showWarning() {
                detailCountTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
                detailCountTextField.setToolTipText("Количетво должно состоять только из положительных цельных чисел, не должно превышать 1000.");
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                verifyInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verifyInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                verifyInput();
            }
        });

        finishedWeightTextField.getDocument().addDocumentListener(new WeightDocumentListener(finishedWeightTextField));

        workpieceWeightTextField.getDocument().addDocumentListener(new WeightDocumentListener(workpieceWeightTextField));

        createTitleButton.addActionListener(e -> onCreateNewTitle());
        editMaterialButton.addActionListener(e -> onEditMaterial());

    }

    private void verifyInput(JTextField textField, String toolTip) {
        try {
            String text = textField.getText();
            text = text.replace(",", ".");
            if (text.contains("d")) {
                throw new IllegalArgumentException();
            }

            double value = Double.parseDouble(text);

            if (value < 0) {
                throw new IllegalArgumentException();
            }
            textField.setBorder(new JTextField().getBorder());
            textField.setToolTipText(toolTip);
        } catch (Throwable throwable) {
            DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
            if (!detailEntity.isUnit()) {
                textField.setBorder(BorderFactory.createLineBorder(Color.RED));
                textField.setToolTipText("Масса должна состоять только из положительных чисел.");
            } else {
                textField.setBorder(new JTextField().getBorder());
                textField.setToolTipText(toolTip);
            }
        }
    }

    private void onEditMaterial() {
        EditMaterialListWindow materialListWindow = new EditMaterialListWindow();
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

            detailTitleComboBox.removeAllItems();
            fillDetailTitleComboBox();

            detailTitleComboBox.setSelectedItem(titleEntity);

        } catch (Throwable throwable) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось добавить " + result + "\n" + throwable.getLocalizedMessage(),
                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initDetailTitleComboBox() {
        detailTitleComboBox.setRenderer(new DefaultListCellRenderer() {
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
    }

    private void fillDetailTitleComboBox() {
        DefaultComboBoxModel<DetailTitleEntity> model = (DefaultComboBoxModel<DetailTitleEntity>) detailTitleComboBox.getModel();
        DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
        List<DetailTitleEntity> detailTitleEntities = service.listDetailTitles();
        Collections.sort(detailTitleEntities);

        model.removeAllElements();

        for (DetailTitleEntity entity : detailTitleEntities) {
            if (entity != null) {
                if (entity.isActive()) {
                    //DetailTitleEntity current = new DetailTitleEntity(entity.getId(), entity.getTitle(), entity.isActive());
                    model.addElement(entity);
                }
            }
        }
        detailTitleComboBox.setSelectedIndex(-1);
    }


    private void initKeyBindings() {
        /*contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);*/

        //fixme this doesn't work for some reason
        contentPane.registerKeyboardAction(e -> onAddNewItemButton(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onRemoveItem(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onMoveItemUp(), KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onMoveItemDown(), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onAddNewItemButton() {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem addExisting = new JMenuItem("Добавить существующую");
        addExisting.setIcon(new ImageIcon(getClass().getResource("/img/gui/addExistingItem.png")));
        addExisting.addActionListener(e -> onAddNewItem());


        JMenuItem addNew = new JMenuItem("Добавить новую");
        addNew.setIcon(new ImageIcon(getClass().getResource("/img/gui/addNewItem.png")));

        jPopupMenu.add(addExisting);
        jPopupMenu.add(addNew);
        jPopupMenu.show(addItemButton, addItemButton.getBounds().x, addItemButton.getBounds().y
                + addItemButton.getBounds().height);

    }

    private void onAddNewItem() {
        SelectionDetailWindow selectionDetailWindow = new SelectionDetailWindow();
        selectionDetailWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
        selectionDetailWindow.setVisible(true);

        DetailEntity entity = selectionDetailWindow.getSelectedEntity();
        if (entity != null) {
            final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
            final TreePath selectionPath = mainTree.getSelectionPath();

            if (notContainsEntityInParents(selectionPath, entity)) {
                if (notContainsEntityInChildren(selectionPath, entity)) {
                    addItemToTree(selectionPath, entity);
                } else {
                    JOptionPane.showMessageDialog(this, "Нельзя добавить деталь / узел "
                            + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                            "т.к. он(а) уже содержится в узле.", "Ошибка добавления", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Нельзя добавить узел "
                        + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                        "т.к. он не может содержать самого себя.", "Ошибка добавления", JOptionPane.ERROR_MESSAGE);
            }

            DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();

            /*System.out.println("> " + detailEntity + " " + entity);*/
        }
    }

    private void addItemToTree(TreePath path, DetailEntity entity) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
        node.add(new MainWindowUtils(session).getChildren(new DetailListServiceImpl(new DetailListDaoImpl(session)), entity));
        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(node);
        System.out.println(">L>>> " + node);
    }

    private DetailEntity getParent(TreePath selectionPath) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getPath()[selectionPath.getPath().length - 2];
        return (DetailEntity) node.getUserObject();
    }

    private boolean notContainsEntityInParents(TreePath selectionPath, DetailEntity entity) {
        for (Object o : selectionPath.getPath()) {
            if (o != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                if (node.getUserObject() instanceof DetailEntity) {
                    DetailEntity current = (DetailEntity) node.getUserObject();
                    if (current.equals(entity)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean notContainsEntityInChildren(TreePath path, DetailEntity entity) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
        for (int i = 0; i < node.getChildCount(); i++) {
            final Object child = mainTree.getModel().getChild(node, i);
            if (child != null) {
                if (child instanceof DefaultMutableTreeNode) {
                    DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) child).getUserObject();
                    if (detailEntity.getId() == entity.getId()) {
                        return false;
                    }
                }
            }
        }
        return true;
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
        System.out.println("onOK not supported yet");
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
        fillNoticeComboBox();
    }


    private void fillDetailInfoPanel(final DetailListEntity mentioned, final DetailEntity detailEntity) {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {

                if (mentioned != null) {
                    detailCountTextField.setText(mentioned.getQuantity() + "");
                } else {
                    detailCountTextField.setText("");
                }
                if (detailEntity != null) {
                    codeComboBox.setSelectedItem(detailEntity);

                    detailTitleComboBox.setSelectedItem(detailEntity.getDetailTitleByDetailTitleId());

                    unitCheckBox.setSelected(detailEntity.isUnit());

                    if (detailEntity.isUnit()) {
                        finishedWeightTextField.setText("");
                        workpieceWeightTextField.setText("");

                        Border border = new JTextField().getBorder();
                        detailCountTextField.setBorder(border);
                        finishedWeightTextField.setBorder(border);
                        workpieceWeightTextField.setBorder(border);
                    } else {
                        finishedWeightTextField.setText(detailEntity.getFinishedWeight() + "");
                        workpieceWeightTextField.setText(detailEntity.getWorkpieceWeight() + "");
                    }

                    List<MaterialListEntity> usedMaterials = getUsedMaterials(detailEntity);
                    materialLabel.setText(CommonUtils.substring(25, getUsedMaterialsString(usedMaterials)));
                    materialLabel.setToolTipText(getUsedMaterialsString(usedMaterials));

                    techProcessComboBox.setSelectedItem(detailEntity.getTechProcessByTechProcessId());

                    isActiveCheckBox.setSelected(!detailEntity.isActive());

                }
                updatePermissions();
            }
        }

    }

    private String getUsedMaterialsString(List<MaterialListEntity> usedMaterials) {
        if (usedMaterials != null) {
            if (usedMaterials.size() > 0) {
                StringBuilder result = new StringBuilder();
                for (MaterialListEntity entity : usedMaterials) {
                    if (entity.isActive()) {
                        MaterialEntity materialByMaterialId = entity.getMaterialByMaterialId();
                        if (materialByMaterialId.getActive()) {
                            result.append(materialByMaterialId.getShortMark()).append(" ").append(materialByMaterialId.getShortProfile()).append(", ");
                        }
                    }
                }
                result.delete(result.toString().length() - 2, result.toString().length());
                return result.toString();
            }
        }
        return "";
    }

    private List<MaterialListEntity> getUsedMaterials(DetailEntity detailEntity) {
        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        return service.getMaterialListByDetail(detailEntity);
    }

    public void updatePermissions() {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {
                final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();

                codeComboBox.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));
                detailTitleComboBox.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));
                createTitleButton.setEnabled(currentUser.isAdmin());

                unitCheckBox.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));

                final boolean isRoot = mainTree.getModel().getRoot().equals(lastSelectedPathComponent.getParent());
                detailCountTextField.setEnabled(!isRoot && (currentUser.isAdmin() || isConstructor(currentUser)));

                finishedWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isConstructor(currentUser)));

                workpieceWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                editMaterialButton.setEnabled(!detailEntity.isUnit());

                createMaterialButton.setEnabled(!detailEntity.isUnit() && currentUser.isAdmin());

                techProcessComboBox.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                createTechProcessButton.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                createTechProcessButton.setEnabled(currentUser.isAdmin());

                isActiveCheckBox.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));

                editImageButton.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));

                //----------------------

                addItemButton.setEnabled(!isRoot && (currentUser.isAdmin() || isConstructor(currentUser)));
                removeItemButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));
                moveItemUpButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));
                moveItemDownButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));

            } else {
                codeComboBox.setEnabled(false);
                detailTitleComboBox.setEnabled(false);
                createTitleButton.setEnabled(false);
                unitCheckBox.setEnabled(false);
                detailCountTextField.setEnabled(false);
                finishedWeightTextField.setEnabled(false);
                workpieceWeightTextField.setEnabled(false);
                createMaterialButton.setEnabled(false);
                techProcessComboBox.setEnabled(false);
                createTechProcessButton.setEnabled(false);
                isActiveCheckBox.setEnabled(false);
            }
        }
    }


    private boolean isConstructor(UsersEntity entity) {
        return entity.getUserType().getName().equalsIgnoreCase("Конструктор");
    }

    private boolean isTechnologist(UsersEntity entity) {
        return entity.getUserType().getName().equalsIgnoreCase("Технолог");
    }


    private void initCodeComboBox() {
        codeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailEntity) {
                    DetailEntity entity = (DetailEntity) value;
                    return super.getListCellRendererComponent(list, entity.getCode(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
    }

    private void fillCodeComboBox() {
        DefaultComboBoxModel<DetailEntity> model = new DefaultComboBoxModel<>();
        DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
        final List<DetailEntity> detailEntities = service.listDetails();
        Collections.sort(detailEntities);
        for (DetailEntity entity : detailEntities) {
            model.addElement(entity);
        }
        codeComboBox.setModel(model);
        codeComboBox.setSelectedItem(-1);
    }


    private void initMainTree() {
        if (detailEntity != null) {
            fillMainTree();
        }
        mainTree.addTreeSelectionListener(e -> {
            Object lastSelectedPathComponent = mainTree.getLastSelectedPathComponent();
            if (lastSelectedPathComponent instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastSelectedPathComponent;
                DetailEntity selected = (DetailEntity) node.getUserObject();

                DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
                Object parent = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                List<DetailListEntity> detailListByParentAndChild = service.getDetailListByParentAndChild((DetailEntity) parent, selected);

                DetailListEntity lastUsed = null;

                for (DetailListEntity entity : detailListByParentAndChild) {
                    if (entity.isActive()) {
                        lastUsed = entity;
                    }
                }

                final boolean isRoot = mainTree.getModel().getRoot().equals(node.getParent());
                addItemButton.setEnabled(!isRoot);
                removeItemButton.setEnabled(!isRoot);
                moveItemUpButton.setEnabled(!isRoot);
                moveItemDownButton.setEnabled(!isRoot);

                fillDetailInfoPanel(lastUsed, selected);
            }
        });
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
            model.addElement(entity);
        }

        noticeComboBox.setModel(model);
        noticeComboBox.setSelectedItem(-1);
    }


    private void initTechProcessComboBox() {
        techProcessComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof TechProcessEntity) {
                    TechProcessEntity techProcessEntity = (TechProcessEntity) value;
                    return super.getListCellRendererComponent(list, techProcessEntity.getProcess(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        techProcessComboBox.addActionListener(e -> System.out.println(">>>" + techProcessComboBox.getSelectedItem()));
    }

    private void fillTechProcessComboBox() {
        TechProcessService service = new TechProcessServiceImpl(new TechProcessDaoImpl(session));
        DefaultComboBoxModel<TechProcessEntity> model = new DefaultComboBoxModel<>();

        List<TechProcessEntity> techProcessEntities = service.listTechProcesses();

        techProcessComboBox.removeAllItems();

        for (TechProcessEntity entity : techProcessEntities) {
            TechProcessEntity current = new TechProcessEntity(entity.getId(), entity.getProcess());
            model.addElement(current);
        }

        techProcessComboBox.setModel(model);
        techProcessComboBox.setSelectedIndex(-1);
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
        noticeComboBox.setMaximumRowCount(30);
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
        treeToolBar = new JToolBar();
        treeToolBar.setFloatable(false);
        changePanel.add(treeToolBar, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addItemButton = new JButton();
        addItemButton.setEnabled(false);
        addItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addItemButton.setText("");
        addItemButton.setToolTipText("Добавить деталь / узел (CTRL++)");
        treeToolBar.add(addItemButton);
        removeItemButton = new JButton();
        removeItemButton.setEnabled(false);
        removeItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeItemButton.setText("");
        removeItemButton.setToolTipText("Удалить деталь / узел (CTRL+-)");
        treeToolBar.add(removeItemButton);
        moveItemUpButton = new JButton();
        moveItemUpButton.setEnabled(false);
        moveItemUpButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/upArrow16.png")));
        moveItemUpButton.setText("");
        moveItemUpButton.setToolTipText("Поднять вверх (CTRL+ВВЕРХ)");
        treeToolBar.add(moveItemUpButton);
        moveItemDownButton = new JButton();
        moveItemDownButton.setEnabled(false);
        moveItemDownButton.setFocusable(false);
        moveItemDownButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/downArrow16.png")));
        moveItemDownButton.setText("");
        moveItemDownButton.setToolTipText("Опустить вниз (CTRL+ВНИЗ)");
        treeToolBar.add(moveItemDownButton);
        codeComboBox = new JComboBox();
        codeComboBox.setMaximumRowCount(30);
        changePanel.add(codeComboBox, new GridConstraints(0, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        changePanel.add(spacer3, new GridConstraints(9, 6, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        createMaterialButton.setToolTipText("Создать новый материал");
        changePanel.add(createMaterialButton, new GridConstraints(6, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTitleButton = new JButton();
        createTitleButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/titleNew16.png")));
        createTitleButton.setText("");
        createTitleButton.setToolTipText("Создать новое наименование");
        changePanel.add(createTitleButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Технический процесс:");
        changePanel.add(label11, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessComboBox = new JComboBox();
        changePanel.add(techProcessComboBox, new GridConstraints(7, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTechProcessButton = new JButton();
        createTechProcessButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/techprocessNew16.png")));
        createTechProcessButton.setText("");
        createTechProcessButton.setToolTipText("Создать новый тех.процесс");
        changePanel.add(createTechProcessButton, new GridConstraints(7, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setHorizontalTextPosition(2);
        isActiveCheckBox.setSelected(false);
        isActiveCheckBox.setText("Аннулирована:");
        isActiveCheckBox.setMnemonic('А');
        isActiveCheckBox.setDisplayedMnemonicIndex(0);
        changePanel.add(isActiveCheckBox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Количество:");
        changePanel.add(label12, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailCountTextField = new JTextField();
        changePanel.add(detailCountTextField, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, -1), new Dimension(40, -1), 0, false));
        editMaterialButton = new JButton();
        editMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editMaterialButton.setText("");
        editMaterialButton.setToolTipText("Добавить материал");
        changePanel.add(editMaterialButton, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialLabel = new JLabel();
        materialLabel.setText("нет данных");
        changePanel.add(materialLabel, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailTitleComboBox = new JComboBox();
        detailTitleComboBox.setMaximumRowCount(30);
        detailTitleComboBox.setToolTipText("Тип пользователя");
        changePanel.add(detailTitleComboBox, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editImageButton = new JButton();
        editImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/picture16.png")));
        editImageButton.setText("");
        editImageButton.setToolTipText("Редактировать изображение детали");
        changePanel.add(editImageButton, new GridConstraints(8, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        label6.setLabelFor(codeComboBox);
        label8.setLabelFor(finishedWeightTextField);
        label9.setLabelFor(workpieceWeightTextField);
        label11.setLabelFor(techProcessComboBox);
        label12.setLabelFor(detailCountTextField);
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

    private class WeightDocumentListener implements DocumentListener {
        private JTextField textField;

        WeightDocumentListener(JTextField textField) {
            this.textField = textField;
        }


        @Override
        public void insertUpdate(DocumentEvent e) {
            verifyInput(textField, textField.getToolTipText());
        }


        @Override
        public void removeUpdate(DocumentEvent e) {
            verifyInput(textField, textField.getToolTipText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            verifyInput(textField, textField.getToolTipText());
        }
    }

}
