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
import com.mmz.specs.application.gui.client.*;
import com.mmz.specs.application.gui.common.CommonComboBoxUtils;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.gui.common.SelectionDetailWindow;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.*;
import com.mmz.specs.model.*;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.swing.JOptionPane.*;

public class EditNoticePanel extends JPanel implements AccessPolicy, Transactional {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int MAXIMUM_STRING_LENGTH = 35;

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
    private JTextField detailQuantityTextField;
    private JButton editMaterialButton;
    private JLabel materialLabel;
    private JButton editImageButton;
    private JToolBar treeToolBar;
    private JCheckBox isInterchangeableCheckBox;
    private Session session;
    private DetailEntity detailEntity;

    EditNoticePanel(DetailEntity detailEntity) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
        this.session = ClientBackgroundService.getInstance().getSession();

        session.getTransaction().begin();

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

        fillMainTree();


        initDetailTitleComboBox();

        fillDetailTitleComboBox();

        initTechProcessComboBox();

        fillTechProcessComboBox();

        fillEmptyDetailInfoPanel();
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

        codeComboBox.addItemListener(e -> log.debug(">>> " + codeComboBox.getSelectedItem()));


        detailQuantityTextField.getDocument().addDocumentListener(new DocumentListener() {
            final int MAX_VALUE = 1000;
            private final String toolTip = detailQuantityTextField.getToolTipText();

            private void verifyInput() {
                String text = detailQuantityTextField.getText();
                try {
                    Long number = Long.parseLong(text);
                    if (number <= 0) {
                        showWarning();
                        return;
                    }
                    if (number > MAX_VALUE) {
                        showWarning();
                        return;
                    }

                    detailQuantityTextField.setBorder(new JTextField().getBorder());
                    detailQuantityTextField.setToolTipText(toolTip);
                    updateEntity();
                } catch (Throwable throwable) {
                    showWarning();
                }
            }

            private void showWarning() {
                detailQuantityTextField.setBorder(new LineBorder(Color.RED));
                detailQuantityTextField.setToolTipText("Количетво должно состоять только из положительных цельных чисел, не должно превышать 1000.");
            }

            private void updateEntity() {
                DetailEntity parent = getParent(mainTree.getSelectionPath());
                DetailEntity child = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
                List<DetailListEntity> detailListEntitiesByParentAndChild = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByParentAndChild(parent, child);
                Collections.sort(detailListEntitiesByParentAndChild);
                log.debug("Parent: {}, Child: {}.", parent, child);
                if (detailListEntitiesByParentAndChild.size() > 0) {
                    for (DetailListEntity entity : detailListEntitiesByParentAndChild) {
                        log.debug("Current entity: " + entity);
                        if (entity.isActive()) {
                            String text = detailQuantityTextField.getText();
                            try {
                                int quantity = Integer.parseInt(text);
                                if (quantity >= 1 && quantity <= 1000) {
                                    entity.setQuantity(quantity);
                                    entity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
                                    log.debug("Added quantity: {}", quantity);
                                    updateTreeDetail();
                                }
                            } catch (NumberFormatException ignored) {
                                String parentInfo = entity.getDetailByParentDetailId().getCode() + entity.getDetailByParentDetailId().getDetailTitleByDetailTitleId().getTitle();
                                String childInfo = entity.getDetailByChildDetailId().getCode() + entity.getDetailByChildDetailId().getDetailTitleByDetailTitleId().getTitle();
                                log.warn("Could not set quantity: {}, for entity: (parent: {}, child: {})", detailQuantityTextField.getText(), parentInfo, childInfo);
                            }
                        }
                    }
                } else {
                    log.debug("What to do here??? parent: {}, child: {}", parent, child);
                }
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


        unitCheckBox.addActionListener(e -> {
            //todo check if DetailListEntity does not have detailEntity as a parent (not a unit, otherwise - notify user)
            DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();

            DetailListService detailListService = new DetailListServiceImpl(new DetailListDaoImpl(session));
//            ArrayList<DetailListEntity> detailListByParent = (ArrayList<DetailListEntity>) detailListService.getDetailListByParent(detailEntity);

            detailEntity.setUnit(unitCheckBox.isSelected());
            DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
            service.updateDetail(detailEntity);
            updateTreeDetail();

           /* DetailListEntity latestEntity = null;
            if (!detailListByParent.isEmpty()) {
                latestEntity = detailListByParent.get(detailListByParent.size() - 1);
            }*/
            /*if (detailListByParent.isEmpty()) {

            } else {
                JOptionPane.showMessageDialog(this,
                        "Невозможно указать узел " + detailEntity.getCode() + " как деталь,\n" +
                                "т.к. он содержит в себе детали / узлы", "Ошибка изменения", JOptionPane.WARNING_MESSAGE);
                unitCheckBox.setSelected(true);
            }*/

        });

        finishedWeightTextField.getDocument().addDocumentListener(new WeightDocumentListener(finishedWeightTextField) {
            @Override
            public void updateEntity() {
                DetailEntity child = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
                try {
                    double weight = Double.parseDouble(finishedWeightTextField.getText());
                    child.setFinishedWeight(weight);
                    updateTreeDetail();
                } catch (NumberFormatException ignored) {
                }
            }
        });

        workpieceWeightTextField.getDocument().addDocumentListener(new WeightDocumentListener(workpieceWeightTextField) {
            @Override
            public void updateEntity() {
                DetailEntity child = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
                try {
                    double weight = Double.parseDouble(workpieceWeightTextField.getText());
                    child.setWorkpieceWeight(weight);
                    updateTreeDetail();
                } catch (NumberFormatException ignored) {
                }
            }
        });

        createTitleButton.addActionListener(e -> onCreateNewTitle());
        editMaterialButton.addActionListener(e -> onEditMaterial());
        createMaterialButton.addActionListener(e -> onCreateNewMaterial());
        createTechProcessButton.addActionListener(e -> onCreateNewTechProcess());

        isActiveCheckBox.addActionListener(e -> {
            try {
                DetailEntity selectedEntity = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
                final boolean selected = isActiveCheckBox.isSelected();
                selectedEntity.setActive(!selected);
                updateTreeDetail();
            } catch (Exception ex) {
                log.warn("Could not set detail activity", e);
            }
        });

        isInterchangeableCheckBox.addActionListener(e -> {
            DetailEntity parent = getParent(mainTree.getSelectionPath());
            DetailEntity child = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
            List<DetailListEntity> detailListByParentAndChild = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByParentAndChild(parent, child);
            Collections.sort(detailListByParentAndChild);
            for (DetailListEntity entity : detailListByParentAndChild) {
                if (entity.isActive()) {
                    entity.setInterchangeableNode(isInterchangeableCheckBox.isSelected());
                    updateTreeDetail();
                }
            }
        });

        editImageButton.addActionListener(e -> onEditDetailImage());
    }

    private void onCreateNewTitle() {
        DetailTitleEntity titleEntity = new CommonWindowUtils(session).onCreateNewTitle(this);
        if (titleEntity != null) {
            detailTitleComboBox.removeAllItems();
            fillDetailTitleComboBox();

            detailTitleComboBox.setSelectedItem(titleEntity);
        }
    }

    private void onEditDetailImage() {
        DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();

        if (detailEntity != null) {
            EditImageWindow editImageWindow = new EditImageWindow(detailEntity);
            editImageWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), editImageWindow));
            editImageWindow.setVisible(true);
        }
    }

    private void onCreateNewTechProcess() {
        String result = showInputDialog(this,
                "Введите новый тех.процесс:", "Новый технический процесс", PLAIN_MESSAGE);
        if (result != null) {
            if (!result.isEmpty()) {
                result = result.toUpperCase();

                int MAX_FIELD_LENGTH = 1000;
                if (result.length() <= MAX_FIELD_LENGTH) {
                    TechProcessService service = new TechProcessServiceImpl(new TechProcessDaoImpl(session));
                    final TechProcessEntity techProcessByValue = service.getTechProcessByValue(result);
                    if (techProcessByValue == null) {
                        TechProcessEntity techProcessEntity = new TechProcessEntity();
                        techProcessEntity.setProcess(result);
                        final int id = service.addTechProcess(techProcessEntity);

                        fillTechProcessComboBox();
                        try {
                            techProcessComboBox.setSelectedItem(service.getTechProcessById(id));
                        } catch (Throwable ignore) {/*NOP*/}
                    } else {
                        JOptionPane.showMessageDialog(this, "Тех. процесс " + result + "\n" +
                                "Уже существует.", "Ошибка добавления", JOptionPane.INFORMATION_MESSAGE);
                        techProcessComboBox.setSelectedItem(techProcessByValue);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Длинна тех.процесса не может превышать "
                                    + MAX_FIELD_LENGTH + " символов (указано " + result.length() + " символов).",
                            "Ошибка добавления", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        }
    }

    private void onCreateNewMaterial() {
        CreateMaterialWindow createMaterialWindow = new CreateMaterialWindow(null);
        createMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), createMaterialWindow));
        createMaterialWindow.setVisible(true);
    }

    private void updateTreeDetail() {
        TreePath selectionPath = mainTree.getSelectionPath();
        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        model.reload(node);
        mainTree.expandPath(selectionPath);
    }


    private void onEditMaterial() {
        final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();


        EditMaterialListWindow materialListWindow = new EditMaterialListWindow(detailEntity);
        materialListWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), materialListWindow));
        materialListWindow.setVisible(true);

        ArrayList<MaterialListEntity> newMaterials = (ArrayList<MaterialListEntity>) materialListWindow.getEditedMaterials();

        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        final ArrayList<MaterialListEntity> currentDbMaterials = (ArrayList<MaterialListEntity>) service.getMaterialListByDetail(detailEntity);

        if (newMaterials != null) {
            updateMaterialsForEntity(detailEntity, newMaterials, currentDbMaterials);
        }

        List<MaterialListEntity> usedMaterials = getUsedMaterials(detailEntity);
        materialLabel.setText(CommonUtils.substring(25, getUsedMaterialsString(usedMaterials)));
        materialLabel.setToolTipText(getUsedMaterialsString(usedMaterials));
    }

    private void updateMaterialsForEntity(DetailEntity detailEntity, ArrayList<MaterialListEntity> newMaterialsList, ArrayList<MaterialListEntity> oldMaterialsList) {
        final String newMaterialsNames = getMaterialsNames(newMaterialsList);

        MaterialListService materialListService = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        for (MaterialListEntity entity : oldMaterialsList) {
            System.out.println("old>> " + entity);
            entity.setActive(false);
            materialListService.updateMaterialList(entity);
        }

        for (MaterialListEntity entity : newMaterialsList) {
            System.out.println("new>> " + entity);
            MaterialListEntity materialListById;

            try {
                materialListById = materialListService.getMaterialListById(entity.getId());
            } catch (HibernateException e) {
                materialListById = new MaterialListEntity();
            }

            materialListById.setDetailByDetailId(detailEntity);
            materialListById.setActive(entity.isActive());
            materialListById.setMainMaterial(entity.isMainMaterial());
            materialListById.setMaterialByMaterialId(entity.getMaterialByMaterialId());
            materialListById.setActive(true);

            try {
                materialListService.updateMaterialList(materialListById);
            } catch (HibernateException e) {
                materialListService.addMaterialList(materialListById);
            }
        }

    }

    private String getMaterialsNames(ArrayList<MaterialListEntity> listEntities) { //fixme or not me... old list is incorrect and half duplicate new one
        StringBuilder result = new StringBuilder();
        for (MaterialListEntity entity : listEntities) {
            if (entity.isActive()) {
                final MaterialEntity material = entity.getMaterialByMaterialId();
                if (material.isActive()) {
                    result.append(material.getShortMark()).append(" ").append(material.getShortProfile());
                    if (entity.isMainMaterial()) {
                        result.append(" (Основной)");
                    }
                    result.append("\n\t");
                }
            }
        }
        return result.toString();
    }

    private void initDetailTitleComboBox() {
        detailTitleComboBox.setRenderer(new CommonComboBoxUtils(session).getTitleComboBoxRenderer());
    }

    private void fillDetailTitleComboBox() {
        detailTitleComboBox.setModel(new CommonComboBoxUtils(session).getTitleComboBoxModel(detailTitleComboBox));
        detailTitleComboBox.setSelectedIndex(-1);
    }


    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
        addExisting.addActionListener(e -> onAddExistingItem());


        JMenuItem addNew = new JMenuItem("Добавить новую");
        addNew.setIcon(new ImageIcon(getClass().getResource("/img/gui/addNewItem.png")));
        addNew.addActionListener(e -> onAddNewItem());

        jPopupMenu.add(addExisting);
        jPopupMenu.add(addNew);
        jPopupMenu.show(addItemButton, addItemButton.getBounds().x, addItemButton.getBounds().y
                + addItemButton.getBounds().height);

    }

    private void onAddNewItem() {
        CreateDetailWindow addDetailWindow = new CreateDetailWindow();
        addDetailWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), addDetailWindow));
        addDetailWindow.setVisible(true);
        DetailEntity entity = addDetailWindow.getDetailEntity();

        if (entity != null) {
            /*final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();*/
            final TreePath selectionPath = mainTree.getSelectionPath();
            addTreeNode(entity, selectionPath);
        }
    }

    private void addTreeNode(DetailEntity entity, TreePath selectionPath) {
        log.debug("Adding new entity {} to path: {}", entity, selectionPath);
        if (selectionPath != null) {
            if (notContainsEntityInParents(selectionPath, entity)) {
                if (notContainsEntityInChildren(selectionPath, entity)) {
                    //fixme when we select root (for example) and add existing element from root - we got 2 entitys in root
                    if (entity != null) {
                        log.debug("Session transaction is: " + session.getTransaction().getStatus());
                        addItemToTree(selectionPath, entity);
                        fillEmptyDetailInfoPanel();
                    }
                } else {
                    showMessageDialog(this, "Нельзя добавить деталь / узел "
                            + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                            "т.к. он(а) уже содержится в узле.", "Ошибка добавления", ERROR_MESSAGE);
                }
            } else {
                showMessageDialog(this, "Нельзя добавить узел "
                        + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                        "т.к. он не может содержать самого себя.", "Ошибка добавления", ERROR_MESSAGE);
            }
        } else {
            showMessageDialog(this, "Укажите сначала, куда необходимо добавить деталь", "Ошибка добавления", ERROR_MESSAGE);
        }
    }

    private void onAddExistingItem() {
        SelectionDetailWindow selectionDetailWindow = new SelectionDetailWindow();
        selectionDetailWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
        selectionDetailWindow.setVisible(true);

        DetailEntity entity = selectionDetailWindow.getSelectedEntity();
        if (entity != null) {
            final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
            final TreePath selectionPath = mainTree.getSelectionPath();

            addTreeNode(entity, selectionPath);

            DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();
        }
    }

    private void addItemToTree(TreePath path, DetailEntity entity) { //TESTME all of this code should be tested! MUST be tested!
        log.debug("Adding entity: {} to path: {}, component is: {}, parent component is: {}", entity, path, path.getLastPathComponent(), path.getParentPath().getLastPathComponent());

        DefaultMutableTreeNode detailNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode node;
        DetailEntity detail = (DetailEntity) detailNode.getUserObject();
        if (detail.isUnit()) {
            node = (DefaultMutableTreeNode) path.getLastPathComponent();
        } else {
            node = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
        }

        DetailService detailService = new DetailServiceImpl(new DetailDaoImpl(session));
        try {
            entity = detailService.getDetailById(entity.getId());
        } catch (Exception e) {
            entity.setActive(true);
            entity = detailService.getDetailById(detailService.addDetail(entity));
        }
        DetailListEntity detailListEntity = getFilledEntity(entity, node);
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(ClientBackgroundService.getInstance().getSession()));
        service.getDetailListById(service.addDetailList(detailListEntity));
        DefaultMutableTreeNode fullNode = new MainWindowUtils(session).getChildren(new DetailListServiceImpl(new DetailListDaoImpl(session)), entity);
        node.add(fullNode);


        /*try {
            log.debug(">>>> ---- Session transaction is: " + session.getTransaction().getStatus());

            DetailListEntity detailListEntity = getFilledEntity(entity, node);

            for (DetailEntity e : new DetailServiceImpl(new DetailDaoImpl(session)).listDetails()) {
                System.out.println(">>>> entity: " + e);
            }
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(ClientBackgroundService.getInstance().getSession()));

            log.debug(">>>> ---- Session transaction is: " + session.getTransaction().getStatus());
            detailListEntity = service.getDetailListById(service.addDetailList(detailListEntity));

            DefaultMutableTreeNode fullNode = new MainWindowUtils(session).getChildren(new DetailListServiceImpl(new DetailListDaoImpl(session)), entity);
            log.debug(">>>> ---- Session transaction is: " + session.getTransaction().getStatus());

            node.add(fullNode);

        } catch (Exception e) {
            log.debug("Can not find entity: {}, adding new one", entity, e);
            DetailService detailService = new DetailServiceImpl(new DetailDaoImpl(session));
            entity.setActive(true);
            entity = detailService.getDetailById(detailService.addDetail(entity));

            DetailListEntity detailListEntity = getFilledEntity(entity, node);

            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(ClientBackgroundService.getInstance().getSession()));
            DetailListEntity detailList = service.getDetailListById(service.addDetailList(detailListEntity));
            log.debug("Added DetailListEntity: {}", detailList);
            log.debug(">>>> ---- Session transaction is: " + session.getTransaction().getStatus());

            node.add(new DefaultMutableTreeNode(entity));
        }*/

        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(node);
    }

    private DetailListEntity getFilledEntity(DetailEntity entity, DefaultMutableTreeNode node) {
        DetailListEntity detailListEntity = new DetailListEntity();
        detailListEntity.setQuantity(1);
        detailListEntity.setActive(true);
        detailListEntity.setDetailByParentDetailId((DetailEntity) node.getUserObject()); //todo check this...
        detailListEntity.setDetailByChildDetailId(entity);
        detailListEntity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
        String parentInfo = detailListEntity.getDetailByParentDetailId().getCode() + " " + detailListEntity.getDetailByParentDetailId().getDetailTitleByDetailTitleId().getTitle();
        String childInfo = detailListEntity.getDetailByChildDetailId().getCode() + " " + detailListEntity.getDetailByChildDetailId().getDetailTitleByDetailTitleId().getTitle();
        log.debug("New detailListEntity parent: {} and child: {} and quantity: {}", parentInfo, childInfo, detailListEntity.getQuantity());
        return detailListEntity;
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
                    if (detailEntity != null && entity != null) {
                        if (detailEntity.getId() == entity.getId()) {
                            return false;
                        }
                    } else return false;
                }
            }
        }
        return true;
    }

    private void onRemoveItem() { //Testme need several tests
        DefaultMutableTreeNode selectedPath = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();

        DetailEntity selected = (DetailEntity) selectedPath.getUserObject();
        TreePath parentPath = mainTree.getSelectionPath().getParentPath();

        DetailEntity parent = (DetailEntity) (((DefaultMutableTreeNode) parentPath.getLastPathComponent()).getUserObject());
        log.debug("Class: {}, {} ", selected.getClass().getName(), parent.getClass().getName());
        log.debug("User want to remove item: {}, its parent: {}", selected, parent);
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
        List<DetailListEntity> detailListByParentAndChild = service.getDetailListByParentAndChild(parent, selected);
        ArrayList<DetailListEntity> lastUsed = new ArrayList<>();

        for (DetailListEntity entity : detailListByParentAndChild) {
            if (entity.isActive()) {
                lastUsed.add(entity);
            }
        }

        if (!lastUsed.isEmpty()) {
            for (DetailListEntity entity : lastUsed) {
                if (entity.isActive()) {
                    entity.setActive(false);
                    service.updateDetailList(entity);
                    log.debug("DetailList {} successfully was marked as not active", entity);
                }
            }
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        parentNode.remove(selectedPath);

        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(selectedPath);

        mainTree.expandPath(parentPath);
    }

    private void onMoveItemUp() {
        System.out.println("not supported yet");
    }

    private void onMoveItemDown() {
        System.out.println("not supported yet");
    }


    @javax.transaction.Transactional
    private void onOK() {
        int result = JOptionPane.showConfirmDialog(this, "Вы точно хотите сохранить изменения в базе данных?\n" +
                        "Введенные вами данные будут сохранены в базе и появятся у всех пользователей.\n" +
                        "Также все проведённые изменения будут закреплены за вами, \n" +
                        "и в случае вопросов, будут обращаться к вам.\n" +
                        "Провести изменения?", "Подтверждение изменений", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/upload.png"))));
        log.debug("User wanted to commit changes, user's choice is: " + result);
        if (result == 0) {
            try {
                uploadImages();
                updateNotice();

                //todo update info about notice!!!!
                log.debug("Transaction status: {}", session.getTransaction().getStatus());
                session.getTransaction().commit();
                log.info("New state of db is commited");

                closeTab();
            } catch (Exception e) {
                log.warn("Could not call commit for transaction", e);
                JOptionPane.showMessageDialog(this,
                        "Не удалось завершить транзакцию\n" + e.getLocalizedMessage(), "Ошибка сохранения",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void updateNotice() {
        NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
        if (selectedItem != null) {
            ClientMainWindow clientMainWindow = getClientMainWindow();
            if (clientMainWindow != null) {
                UsersEntity currentUser = clientMainWindow.getCurrentUser();
                if (currentUser != null) {
                    selectedItem.setUsersByProvidedByUserId(currentUser);
                }
            }
        }
    }

    private void closeTab() {
        ClientMainWindow clientMainWindow = getClientMainWindow();
        if (clientMainWindow != null) {
            clientMainWindow.closeTab(this);
        }
    }

    private ClientMainWindow getClientMainWindow() {
        Window parentWindow = FrameUtils.findWindow(this);
        if (parentWindow instanceof ClientMainWindow) {
            return (ClientMainWindow) parentWindow;
        }
        return null;
    }

    private void uploadImages() {
        DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
        List<DetailEntity> detailEntities = service.listDetails();
        for (DetailEntity entity : detailEntities) {
            if (entity.getImagePath() != null) {
                if (!entity.getImagePath().isEmpty()) {
                    File file = new File(entity.getImagePath());
                    if (file.exists()) {
                        try {
                            final FtpUtils ftpUtils = FtpUtils.getInstance();
                            ftpUtils.uploadImage(entity.getId(), file);
                        } catch (IOException e) {
                            log.warn("Could not upload image for entity: {}", entity, e);
                        }
                    } else {
                        log.warn("Could not find file for entity: {}, file: {}", entity, file);
                    }
                    entity.setImagePath(null);
                } else {
                    entity.setImagePath(null);
                    service.updateDetail(entity);
                }
            }
        }
    }

    private void onCancel() {
        int result = JOptionPane.showConfirmDialog(this, "Вы точно хотите отменить изменения?\n" +
                "В случае подтверждения все изменения не сохранятся и никак\n" +
                "не повлияют на базу данных.\n" +
                "Отменить изменения?", "Отмена изменений", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        log.debug("User wanted to rollback changes, user's choice is: " + result);
        if (result == 0) {
            session.getTransaction().rollback();
            closeTab();
        }
    }

    private void onEditNotice() {
        Object selectedItem = noticeComboBox.getSelectedItem();
        if (selectedItem != null) {
            NoticeEntity entity = (NoticeEntity) selectedItem;
            CreateNoticeWindow noticeWindow = new CreateNoticeWindow(entity);
            noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
            noticeWindow.setVisible(true);
            NoticeEntity noticeEntity = noticeWindow.getNoticeEntity();
            noticeComboBox.removeAllItems();
            fillNoticeComboBox();
            noticeComboBox.setSelectedItem(noticeEntity);
        }
    }

    private void onCreateNewNotice() {
        CreateNoticeWindow noticeWindow = new CreateNoticeWindow(null);
        noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
        noticeWindow.setVisible(true);
        NoticeEntity noticeEntity = noticeWindow.getNoticeEntity();
        noticeComboBox.removeAllItems();
        fillNoticeComboBox();
        noticeComboBox.setSelectedItem(noticeEntity);
    }


    private void fillDetailInfoPanel(final DetailListEntity mentioned, final DetailEntity detailEntity) {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {

                if (mentioned != null) {
                    detailQuantityTextField.setText(mentioned.getQuantity() + "");
                    isInterchangeableCheckBox.setSelected(mentioned.isInterchangeableNode());
                } else {
                    detailQuantityTextField.setText("");
                    isInterchangeableCheckBox.setSelected(false);
                }
                if (detailEntity != null) {
                    codeComboBox.setSelectedItem(detailEntity);

                    detailTitleComboBox.setSelectedItem(detailEntity.getDetailTitleByDetailTitleId());

                    unitCheckBox.setSelected(detailEntity.isUnit());

                    if (detailEntity.isUnit()) {
                        finishedWeightTextField.setText("");
                        workpieceWeightTextField.setText("");

                        Border border = new JTextField().getBorder();
                        detailQuantityTextField.setBorder(border);
                        finishedWeightTextField.setBorder(border);
                        workpieceWeightTextField.setBorder(border);
                    } else {
                        String fixedFinishedWeight = detailEntity.getFinishedWeight() + "";
                        fixedFinishedWeight = fixedFinishedWeight.replace("null", "0");
                        finishedWeightTextField.setText(fixedFinishedWeight);
                        String fixedWorkpieceWeight = detailEntity.getWorkpieceWeight() + "";
                        fixedWorkpieceWeight = fixedWorkpieceWeight.replace("null", "0");
                        workpieceWeightTextField.setText(fixedWorkpieceWeight);
                    }

                    if (detailEntity.getId() != 0) {
                        List<MaterialListEntity> usedMaterials = getUsedMaterials(detailEntity);
                        materialLabel.setText(CommonUtils.substring(25, getUsedMaterialsString(usedMaterials)));
                        materialLabel.setToolTipText(getUsedMaterialsString(usedMaterials));
                    }

                    final TechProcessEntity techProcessByTechProcessId = detailEntity.getTechProcessByTechProcessId();
                    if (techProcessByTechProcessId != null) {
                        techProcessComboBox.setSelectedItem(techProcessByTechProcessId);
                    } else techProcessComboBox.setSelectedIndex(-1);

                    isActiveCheckBox.setSelected(!detailEntity.isActive());

                }
                updatePermissions();
            }
        }

    }

    private void fillEmptyDetailInfoPanel() {
        codeComboBox.setSelectedItem(null);
        codeComboBox.setEnabled(false);

        detailTitleComboBox.setSelectedItem(null);
        detailTitleComboBox.setEnabled(false);

        createTitleButton.setEnabled(false);

        detailQuantityTextField.setEnabled(false);

        unitCheckBox.setSelected(false);
        unitCheckBox.setEnabled(false);

        workpieceWeightTextField.setText("");
        workpieceWeightTextField.setEnabled(false);

        finishedWeightTextField.setText("");
        finishedWeightTextField.setEnabled(false);

        materialLabel.setText("нет данных");

        editMaterialButton.setEnabled(false);

        createMaterialButton.setEnabled(false);

        techProcessComboBox.setSelectedItem(null);
        techProcessComboBox.setEnabled(false);
        createTechProcessButton.setEnabled(false);

        isActiveCheckBox.setSelected(false);
        isActiveCheckBox.setEnabled(false);

        isInterchangeableCheckBox.setSelected(false);
        isInterchangeableCheckBox.setEnabled(false);

        editImageButton.setEnabled(false);
    }

    private String getUsedMaterialsString(List<MaterialListEntity> usedMaterials) {
        if (usedMaterials != null) {
            if (usedMaterials.size() > 0) {
                Collections.sort(usedMaterials);
                StringBuilder result = new StringBuilder();
                for (MaterialListEntity entity : usedMaterials) {
                    if (entity.isActive()) {
                        MaterialEntity materialByMaterialId = entity.getMaterialByMaterialId();
                        if (materialByMaterialId.isActive()) {
                            result.append(materialByMaterialId.getShortMark()).append(" ").append(materialByMaterialId.getShortProfile()).append(", ");
                        }
                    }
                }
                if (result.toString().length() > 2) {
                    result.delete(result.toString().length() - 2, result.toString().length());
                }
                return result.toString();
            }
        }
        return "нет данных";
    }

    private List<MaterialListEntity> getUsedMaterials(DetailEntity detailEntity) {
        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        return service.getMaterialListByDetail(detailEntity);
    }

    private void updatePermissions() {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {
                final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();
                final boolean isRoot = mainTree.getModel().getRoot().equals(lastSelectedPathComponent.getParent());

                codeComboBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);
                detailTitleComboBox.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));
                createTitleButton.setEnabled(currentUser.isAdmin());

                unitCheckBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);

                detailQuantityTextField.setEnabled(!isRoot && (currentUser.isAdmin() || isConstructor(currentUser)));

                finishedWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isConstructor(currentUser)));

                workpieceWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                editMaterialButton.setEnabled(!detailEntity.isUnit());

                createMaterialButton.setEnabled(!detailEntity.isUnit() && currentUser.isAdmin());

                techProcessComboBox.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                createTechProcessButton.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                isActiveCheckBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);

                isInterchangeableCheckBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);

                editImageButton.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));

                //----------------------

                addItemButton.setEnabled((!isRoot || detailEntity.isUnit()) && (currentUser.isAdmin() || isConstructor(currentUser)));
//                addItemButton.setEnabled(!isRoot && (currentUser.isAdmin() || isConstructor(currentUser)));
                removeItemButton.setEnabled((!isRoot || detailEntity.isUnit()) && (currentUser.isAdmin() || isConstructor(currentUser)));
//                removeItemButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));
                moveItemUpButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));
                moveItemDownButton.setEnabled(!isRoot && currentUser.isAdmin() || isConstructor(currentUser));

            } else {
                codeComboBox.setEnabled(false);
                detailTitleComboBox.setEnabled(false);
                createTitleButton.setEnabled(false);
                unitCheckBox.setEnabled(false);
                detailQuantityTextField.setEnabled(false);
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
                    String code = entity.getCode();
                    return super.getListCellRendererComponent(list, CommonUtils.substring(MAXIMUM_STRING_LENGTH, code), index, isSelected, cellHasFocus);
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
        mainTree.addTreeSelectionListener(e -> {
            Object lastSelectedPathComponent = mainTree.getLastSelectedPathComponent();
            if (lastSelectedPathComponent instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastSelectedPathComponent;
                DetailEntity selected = (DetailEntity) node.getUserObject();

                DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
                Object parent = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                DetailListEntity lastUsed = null;
                try {
                    List<DetailListEntity> detailListByParentAndChild = service.getDetailListByParentAndChild((DetailEntity) parent, selected);

                    lastUsed = null;

                    for (DetailListEntity entity : detailListByParentAndChild) {
                        if (entity.isActive()) {
                            lastUsed = entity;
                        }
                    }
                } catch (Exception ignore) {
                    fillEmptyDetailInfoPanel();
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
            if (detailEntity != null) {
                DetailListService detailListService = new DetailListServiceImpl(new DetailListDaoImpl(session));
                if (!detailEntity.isUnit()) {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                    DefaultMutableTreeNode detail = new DefaultMutableTreeNode(detailEntity, false);
                    root.add(detail);
                    mainTree.setModel(new DefaultTreeModel(root));
                } else {
                    DefaultMutableTreeNode detailListTreeByDetailList = new MainWindowUtils(session).getDetailListTreeByDetailList(detailListService.getDetailListByParent(detailEntity.getCode()));
                    if (detailListTreeByDetailList.children().hasMoreElements()) {
                        mainTree.setModel(new DefaultTreeModel(detailListTreeByDetailList));
                    } else {
                        if (detailEntity != null) {
                            DetailService detailService = new DetailServiceImpl(new DetailDaoImpl(session));
                            detailEntity = detailService.getDetailById(detailService.addDetail(detailEntity));
                            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                            DefaultMutableTreeNode newChild = new DefaultMutableTreeNode();
                            newChild.setUserObject(detailEntity);
                            root.add(newChild);
                            mainTree.setModel(new DefaultTreeModel(root));
                        }
                    }
                }
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
                    String description = CommonUtils.substring(noticeComboBox.getSize().width / 10, entity.getDescription().replaceAll("\n", " "));

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
        noticeComboBox.setSelectedIndex(-1);
    }


    private void initTechProcessComboBox() {
        techProcessComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof TechProcessEntity) {
                    TechProcessEntity techProcessEntity = (TechProcessEntity) value;
                    String process = techProcessEntity.getProcess();
                    return super.getListCellRendererComponent(list, CommonUtils.substring(MAXIMUM_STRING_LENGTH, process), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        techProcessComboBox.addActionListener(e -> {
            try {
                DetailEntity selectedEntity = (DetailEntity) ((DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent()).getUserObject();
                selectedEntity.setTechProcessByTechProcessId((TechProcessEntity) techProcessComboBox.getSelectedItem());
            } catch (Exception ignore) {/*NOP*/
            }
        });

        techProcessComboBox.setSelectedIndex(-1);
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

    @Override
    public AccessPolicyManager getPolicyManager() {
        return new AccessPolicyManager(true, true);
    }

    @Override
    public void setUIEnabled(boolean ignore) {
        /*NOP*/
    }

    @Override
    public void rollbackTransaction() {
        onCancel();
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
        contentPane.add(mainTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
        panel1.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel1.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editNoticeButton = new JButton();
        editNoticeButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editNoticeButton.setText("");
        editNoticeButton.setToolTipText("Редактировать извещение");
        panel1.add(editNoticeButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        changePanel = new JPanel();
        changePanel.setLayout(new GridLayoutManager(14, 6, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Изменения", changePanel);
        final Spacer spacer2 = new Spacer();
        changePanel.add(spacer2, new GridConstraints(10, 5, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        changePanel.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(13, 1, new Insets(0, 0, 0, 0), -1, -1));
        changePanel.add(panel2, new GridConstraints(1, 0, 13, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(0, 0, 12, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mainTree.setRootVisible(false);
        mainTree.setScrollsOnExpand(true);
        scrollPane2.setViewportView(mainTree);
        treeToolBar = new JToolBar();
        treeToolBar.setFloatable(false);
        panel2.add(treeToolBar, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
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
        moveItemUpButton.setVisible(false);
        treeToolBar.add(moveItemUpButton);
        moveItemDownButton = new JButton();
        moveItemDownButton.setEnabled(false);
        moveItemDownButton.setFocusable(false);
        moveItemDownButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/downArrow16.png")));
        moveItemDownButton.setText("");
        moveItemDownButton.setToolTipText("Опустить вниз (CTRL+ВНИЗ)");
        moveItemDownButton.setVisible(false);
        treeToolBar.add(moveItemDownButton);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(9, 4, new Insets(0, 0, 0, 0), -1, -1));
        changePanel.add(panel3, new GridConstraints(1, 1, 9, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        codeComboBox = new JComboBox();
        codeComboBox.setMaximumRowCount(30);
        panel3.add(codeComboBox, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Индекс:");
        panel3.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Наименование:");
        panel3.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitCheckBox = new JCheckBox();
        unitCheckBox.setHorizontalTextPosition(2);
        unitCheckBox.setText("Узел:");
        unitCheckBox.setMnemonic('У');
        unitCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(unitCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Масса готовой детали:");
        panel3.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightTextField = new JTextField();
        panel3.add(finishedWeightTextField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Норма расхода:");
        panel3.add(label9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightTextField = new JTextField();
        panel3.add(workpieceWeightTextField, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Материал:");
        panel3.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createMaterialButton = new JButton();
        createMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/materialNew16.png")));
        createMaterialButton.setText("");
        createMaterialButton.setToolTipText("Создать новый материал");
        panel3.add(createMaterialButton, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTitleButton = new JButton();
        createTitleButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/titleNew16.png")));
        createTitleButton.setText("");
        createTitleButton.setToolTipText("Создать новое наименование");
        panel3.add(createTitleButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Технический процесс:");
        panel3.add(label11, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessComboBox = new JComboBox();
        panel3.add(techProcessComboBox, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTechProcessButton = new JButton();
        createTechProcessButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/techprocessNew16.png")));
        createTechProcessButton.setText("");
        createTechProcessButton.setToolTipText("Создать новый тех.процесс");
        panel3.add(createTechProcessButton, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setHorizontalTextPosition(2);
        isActiveCheckBox.setSelected(false);
        isActiveCheckBox.setText("Аннулирована:");
        isActiveCheckBox.setMnemonic('А');
        isActiveCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(isActiveCheckBox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialLabel = new JLabel();
        materialLabel.setText("нет данных");
        panel3.add(materialLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailTitleComboBox = new JComboBox();
        detailTitleComboBox.setMaximumRowCount(30);
        detailTitleComboBox.setToolTipText("Тип пользователя");
        panel3.add(detailTitleComboBox, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editImageButton = new JButton();
        editImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/pictureEdit16.png")));
        editImageButton.setText("");
        editImageButton.setToolTipText("Редактировать изображение детали");
        panel3.add(editImageButton, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isInterchangeableCheckBox = new JCheckBox();
        isInterchangeableCheckBox.setHorizontalTextPosition(2);
        isInterchangeableCheckBox.setText("Замена (прим. в замен):");
        panel3.add(isInterchangeableCheckBox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editMaterialButton = new JButton();
        editMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editMaterialButton.setText("");
        editMaterialButton.setToolTipText("Добавить материал");
        panel3.add(editMaterialButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Количество:");
        panel3.add(label12, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailQuantityTextField = new JTextField();
        panel3.add(detailQuantityTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, -1), new Dimension(40, -1), 0, false));
        savePanel = new JPanel();
        savePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Сохранение", savePanel);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        savePanel.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel4.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel4.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("Подтвердить");
        panel5.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        panel5.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        savePanel.add(spacer5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label6.setLabelFor(codeComboBox);
        label8.setLabelFor(finishedWeightTextField);
        label9.setLabelFor(workpieceWeightTextField);
        label11.setLabelFor(techProcessComboBox);
        label12.setLabelFor(detailQuantityTextField);
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

    private abstract class WeightDocumentListener implements DocumentListener {
        private final String defaultTooltipText;
        private JTextField textField;

        WeightDocumentListener(JTextField textField) {
            this.textField = textField;
            this.defaultTooltipText = textField.getToolTipText();
        }

        private void verifyInput(JTextField textField) {
            try {
                String text = textField.getText();
                log.info("Hello, " + text);
                text = text.replaceAll(",", ".");
                if (text.contains("d")) {
                    throw new IllegalArgumentException("This textfield can not have d (double thing): " + text);
                }

                double value = Double.parseDouble(text);

                if (value < 0) {
                    throw new IllegalArgumentException("Value is < 0, value is: " + value);
                }
                textField.setBorder(new JTextField().getBorder());
                textField.setToolTipText(defaultTooltipText);
                updateEntity();
            } catch (Throwable throwable) {
                if (!(throwable instanceof NumberFormatException && throwable.getMessage().equals("empty String"))) {
                    log.warn("Value is incorrect", throwable);
                }
                Object lastSelectedPathComponent = mainTree.getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    DetailEntity detailEntity = (DetailEntity) ((DefaultMutableTreeNode) lastSelectedPathComponent).getUserObject();
                    if (!detailEntity.isUnit()) {
                        textField.setBorder(new LineBorder(Color.RED));
                        textField.setToolTipText("Масса должна состоять только из положительных чисел.");
                    } else {
                        textField.setBorder(new LineBorder(Color.RED));
                        textField.setToolTipText(defaultTooltipText);
                    }
                }
            }
        }

        public abstract void updateEntity();

        @Override
        public void insertUpdate(DocumentEvent e) {
            verifyInput(textField);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            verifyInput(textField);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            verifyInput(textField);
        }
    }

}
