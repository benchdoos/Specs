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
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.client.ClientConstants;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.client.*;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.gui.panels.service.InitializationPanel;
import com.mmz.specs.application.utils.*;
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
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.mmz.specs.application.core.ApplicationConstants.NO_DATA_STRING;
import static com.mmz.specs.application.gui.client.SelectDetailEntityWindow.MODE.COPY;
import static com.mmz.specs.application.gui.client.SelectDetailEntityWindow.MODE.EDIT;
import static com.mmz.specs.application.gui.panels.EditNoticePanel.Status.DEFAULT;
import static com.mmz.specs.application.gui.panels.EditNoticePanel.Status.SAVING;
import static javax.swing.JOptionPane.*;

public class EditNoticePanel extends JPanel implements AccessPolicy, Transactional {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int MAXIMUM_STRING_LENGTH = 25;

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
    private JCheckBox unitCheckBox;
    private JTextField finishedWeightTextField;
    private JTextField workpieceWeightTextField;
    private JComboBox<TechProcessEntity> techProcessComboBox;
    private JButton createTechProcessButton;
    private JCheckBox isActiveCheckBox;
    private JPanel changePanel;
    private JPanel noticePanel;
    private JPanel savePanel;
    private JSpinner detailQuantitySpinner;
    private JButton editMaterialButton;
    private JLabel materialLabel;
    private JButton editImageButton;
    private JToolBar treeToolBar;
    private JCheckBox isInterchangeableCheckBox;
    private JLabel noticeCreatedByUserLabel;
    private JLabel noticeCreationDateLabel;
    private JLabel detailCodeLabel;
    private JLabel detailTitleLabel;
    private JButton editDetailInfoButton;
    private JButton copyButton;
    private JPanel savePreviewPanel;
    private JScrollPane scrollPane;
    private Session session;
    private DetailEntity detailEntity;
    private DetailEntity rootEntity;
    private ActionListener notifyUserIsActiveListener = FrameUtils.getNotifyUserIsActiveActionListener(this);
    private Status status = DEFAULT;

    EditNoticePanel(Session session, DetailEntity detailEntity) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
        this.rootEntity = detailEntity;
        this.session = session;

        initGui();

        fillMainTree();
    }


    EditNoticePanel(Session session, DetailEntity detailEntity, boolean create) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
        this.rootEntity = detailEntity;
        this.session = session;

        if (create) {
            DetailService service = new DetailServiceImpl(session);
            this.detailEntity = service.getDetailById(service.addDetail(detailEntity));
        }

        initGui();

        fillMainTree();
    }

    /**
     * Creates duplicate of brother for detailEntity
     */
    EditNoticePanel(Session session, DetailEntity detailEntity, DetailEntity brotherEntity) {
        $$$setupUI$$$();
        this.detailEntity = detailEntity;
        this.rootEntity = detailEntity;
        this.session = session;

        initGui();

        copyBrotherEntityToDetailEntity(this.detailEntity, brotherEntity);

        fillMainTree();
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
        panel1.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        noticePanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Извещение:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createNoticeButton = new JButton();
        createNoticeButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/noticeNew16.png")));
        createNoticeButton.setMargin(new Insets(2, 5, 2, 5));
        createNoticeButton.setText("");
        createNoticeButton.setToolTipText("Создать новое извещение");
        panel1.add(createNoticeButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeComboBox = new JComboBox();
        noticeComboBox.setMaximumRowCount(30);
        panel1.add(noticeComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(-1, 150), 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Изменил:");
        label2.setToolTipText("Последнее изменение по извещению внес пользователь");
        panel1.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Описание:");
        panel1.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 100), null, null, 0, false));
        noticeDescriptionTextArea = new JTextArea();
        noticeDescriptionTextArea.setBackground(new Color(-855310));
        noticeDescriptionTextArea.setEditable(false);
        Font noticeDescriptionTextAreaFont = UIManager.getFont("Label.font");
        if (noticeDescriptionTextAreaFont != null) noticeDescriptionTextArea.setFont(noticeDescriptionTextAreaFont);
        scrollPane1.setViewportView(noticeDescriptionTextArea);
        final JLabel label4 = new JLabel();
        label4.setText("Дата изменения:");
        panel1.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeDateLabel = new JLabel();
        noticeDateLabel.setText("нет данных");
        noticeDateLabel.setToolTipText("Дата последнего изменения");
        panel1.add(noticeDateLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeUserLabel = new JLabel();
        noticeUserLabel.setText("нет данных");
        panel1.add(noticeUserLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Выпустил:");
        panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeCreatedByUserLabel = new JLabel();
        noticeCreatedByUserLabel.setText("нет данных");
        panel1.add(noticeCreatedByUserLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Дата выпуска:");
        panel1.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeCreationDateLabel = new JLabel();
        noticeCreationDateLabel.setText("нет данных");
        panel1.add(noticeCreationDateLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Номер:");
        panel1.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeNumberLabel = new JLabel();
        noticeNumberLabel.setText("нет данных");
        panel1.add(noticeNumberLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editNoticeButton = new JButton();
        editNoticeButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editNoticeButton.setMargin(new Insets(2, 5, 2, 5));
        editNoticeButton.setText("");
        editNoticeButton.setToolTipText("Редактировать извещение");
        panel1.add(editNoticeButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        changePanel = new JPanel();
        changePanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainTabbedPane.addTab("Изменения", changePanel);
        final Spacer spacer2 = new Spacer();
        changePanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        changePanel.add(panel2, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPane = new JScrollPane();
        panel2.add(scrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mainTree.setFocusable(true);
        mainTree.setRootVisible(false);
        scrollPane.setViewportView(mainTree);
        treeToolBar = new JToolBar();
        treeToolBar.setFloatable(false);
        panel2.add(treeToolBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addItemButton = new JButton();
        addItemButton.setEnabled(false);
        addItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addItemButton.setText("");
        addItemButton.setToolTipText("Добавить деталь / узел (INSERT)");
        treeToolBar.add(addItemButton);
        copyButton = new JButton();
        copyButton.setEnabled(false);
        copyButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/copy.png")));
        copyButton.setText("");
        copyButton.setToolTipText("Дублировать узел (CTRL+D)");
        treeToolBar.add(copyButton);
        removeItemButton = new JButton();
        removeItemButton.setEnabled(false);
        removeItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeItemButton.setText("");
        removeItemButton.setToolTipText("Удалить деталь / узел (DELETE)");
        treeToolBar.add(removeItemButton);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        changePanel.add(panel3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Обозначение:");
        panel3.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Наименование:");
        panel3.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Масса готовой детали:");
        panel3.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightTextField = new JTextField();
        panel3.add(finishedWeightTextField, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Норма расхода:");
        panel3.add(label11, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightTextField = new JTextField();
        panel3.add(workpieceWeightTextField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Материал:");
        panel3.add(label12, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Технический процесс:");
        panel3.add(label13, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessComboBox = new JComboBox();
        panel3.add(techProcessComboBox, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        createTechProcessButton = new JButton();
        createTechProcessButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/techprocessNew16.png")));
        createTechProcessButton.setText("");
        createTechProcessButton.setToolTipText("Создать новый тех.процесс");
        panel3.add(createTechProcessButton, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setHorizontalTextPosition(2);
        isActiveCheckBox.setSelected(false);
        isActiveCheckBox.setText("Аннулирована:");
        isActiveCheckBox.setMnemonic('А');
        isActiveCheckBox.setDisplayedMnemonicIndex(0);
        isActiveCheckBox.setToolTipText("Аннулирован/а");
        panel3.add(isActiveCheckBox, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialLabel = new JLabel();
        materialLabel.setText("нет данных");
        panel3.add(materialLabel, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editImageButton = new JButton();
        editImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/pictureEdit16.png")));
        editImageButton.setText("");
        editImageButton.setToolTipText("Редактировать изображение");
        panel3.add(editImageButton, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isInterchangeableCheckBox = new JCheckBox();
        isInterchangeableCheckBox.setHorizontalTextPosition(2);
        isInterchangeableCheckBox.setText("Замена (прим. в замен):");
        isInterchangeableCheckBox.setMnemonic('З');
        isInterchangeableCheckBox.setDisplayedMnemonicIndex(0);
        isInterchangeableCheckBox.setToolTipText("Применяется в замен другого узла / детали");
        panel3.add(isInterchangeableCheckBox, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Количество:");
        panel3.add(label14, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailQuantitySpinner = new JSpinner();
        panel3.add(detailQuantitySpinner, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, -1), new Dimension(60, -1), new Dimension(60, -1), 0, false));
        detailCodeLabel = new JLabel();
        detailCodeLabel.setText("нет данных");
        panel3.add(detailCodeLabel, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        detailTitleLabel = new JLabel();
        detailTitleLabel.setText("нет данных");
        panel3.add(detailTitleLabel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        editDetailInfoButton = new JButton();
        editDetailInfoButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editDetailInfoButton.setText("");
        editDetailInfoButton.setToolTipText("Редактировать обозначение и наименование");
        panel3.add(editDetailInfoButton, new GridConstraints(0, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editMaterialButton = new JButton();
        editMaterialButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editMaterialButton.setText("");
        editMaterialButton.setToolTipText("Редактировать материал");
        panel3.add(editMaterialButton, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitCheckBox = new JCheckBox();
        unitCheckBox.setHorizontalTextPosition(2);
        unitCheckBox.setText("Узел:");
        unitCheckBox.setMnemonic('У');
        unitCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(unitCheckBox, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        changePanel.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        buttonOK.setMnemonic('П');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel5.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setMnemonic('О');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel5.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        savePreviewPanel = new JPanel();
        savePreviewPanel.setLayout(new CardLayout(0, 0));
        savePanel.add(savePreviewPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label1.setLabelFor(noticeComboBox);
        label10.setLabelFor(finishedWeightTextField);
        label11.setLabelFor(workpieceWeightTextField);
        label13.setLabelFor(techProcessComboBox);
        label14.setLabelFor(detailQuantitySpinner);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void addDetailToTree(DetailEntity entity) {
        final TreePath selectionPath = mainTree.getSelectionPath();
        if (new MainWindowUtils(session).pathNotContainsEntity(this, selectionPath, entity)) {
            addTreeNode(entity, selectionPath);
            mainTree.setSelectionPath(selectionPath);
            mainTree.requestFocus();
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

        DetailEntity parent = (DetailEntity) node.getUserObject();

        try {
            DetailListEntity detailListEntity = generateDetailListFromEntity(parent, entity);
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));

            service.getDetailListById(service.addDetailList(detailListEntity));

            final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(entity);
            new MainWindowUtils(session).getModuleChildren(newChild, entity);

            node.add(newChild);
        } catch (Exception e) {
            log.warn("Could not add new {} for parent: {} and child: {}", DetailListEntity.class, parent, entity);
        }

        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(node);
    }

    private void addNode(TreePath selectionPath, DetailEntity entity) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getPath()[selectionPath.getPath().length - 2];
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(entity);

        boolean contains = false;
        for (int i = 0; i < node.getChildCount(); i++) {
            final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childAt.getUserObject().equals(entity)) {
                contains = true;
            }
        }

        if (!contains) {
            node.add(newChild);
        }
        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(node);
    }

    private void addTreeNode(DetailEntity entity, TreePath selectionPath) {
        log.debug("Adding new entity {} to path: {}", entity, selectionPath);
        if (selectionPath != null) {
            if (new MainWindowUtils(session).pathNotContainsEntity(this, selectionPath, entity)) {
                if (entity != null) {
                    log.debug("Session transaction status is: " + session.getTransaction().getStatus());
                    addItemToTree(selectionPath, entity);
                    fillEmptyDetailInfoPanel();
                }
            }
        } else {
            showMessageDialog(this, "Укажите сначала, куда необходимо добавить деталь", "Ошибка добавления", ERROR_MESSAGE);
        }
    }

    private void closeTab() {
        ClientMainWindow clientMainWindow = new MainWindowUtils(session).getClientMainWindow(this);
        if (clientMainWindow != null) {
            clientMainWindow.closeTab(this);
        }
    }

    private DetailEntity copyBrotherEntityToDetailEntity(DetailEntity detailEntity, DetailEntity brotherEntity) {
        if (brotherEntity != null) {
            DetailListService service = new DetailListServiceImpl(session);
            final ArrayList<DetailListEntity> detailListByParent = (ArrayList<DetailListEntity>) service.getDetailListByParent(brotherEntity);
            if (!detailListByParent.isEmpty()) {
                ArrayList<DetailListEntity> finalList = new ArrayList<>();
                for (DetailListEntity entity : detailListByParent) {
                    if (entity.isActive()) {
                        List<DetailListEntity> result = service.getDetailListByParentAndChild(brotherEntity, entity.getDetailByChildDetailId());
                        DetailListEntity finalEntity = new MainWindowUtils(session).getLatestDetailListEntity(result);
                        finalList.add(finalEntity);
                    }
                }

                detailEntity = createDetailEntityIfNotExist(detailEntity);

                for (DetailListEntity entity : finalList) {
                    if (entity != null) {
                        DetailListEntity newEntity = new DetailListEntity();
                        newEntity.setDetailByParentDetailId(detailEntity);
                        newEntity.setDetailByChildDetailId(entity.getDetailByChildDetailId());
                        newEntity.setQuantity(entity.getQuantity());
                        newEntity.setInterchangeableNode(entity.isInterchangeableNode());
                        newEntity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
                        newEntity.setActive(true);

                        service.addDetailList(newEntity);
                    }
                }

                showAllEntities(detailEntity);
            }
        }
        return detailEntity;
    }

    private DetailEntity createDetailEntityIfNotExist(DetailEntity detailEntity) {
        if (detailEntity != null) {
            DetailService service = new DetailServiceImpl(session);
            try {
                final DetailEntity detailById = service.getDetailById(detailEntity.getId());
                if (detailById == null) {
                    detailEntity = service.getDetailById(service.addDetail(detailEntity));
                } else {
                    detailEntity = detailById;
                }
            } catch (Exception e) {
                detailEntity = service.getDetailById(service.addDetail(detailEntity));
            }
        }
        return detailEntity;
    }

    private void createUIComponents() {
        mainTree = new DetailJTree();
    }

    private void fillDetailInfoPanel(final DetailListEntity mentioned, final DetailEntity detailEntity) {
        Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            UsersEntity currentUser = clientMainWindow.getCurrentUser();
            if (currentUser != null) {

                if (mentioned != null) {
                    detailQuantitySpinner.setValue(mentioned.getQuantity());
                    isInterchangeableCheckBox.setSelected(mentioned.isInterchangeableNode());
                } else {
                    isInterchangeableCheckBox.setSelected(false);
                }
                if (detailEntity != null) {
                    detailCodeLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, detailEntity.getCode()));

                    detailTitleLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, detailEntity.getDetailTitleByDetailTitleId().getTitle()));

                    unitCheckBox.setSelected(detailEntity.isUnit());

                    if (detailEntity.isUnit()) {
                        finishedWeightTextField.setText("");
                        workpieceWeightTextField.setText("");

                        Border border = new JTextField().getBorder();
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
        detailQuantitySpinner.setBorder(new JSpinner().getBorder());
    }

    private void fillEmptyDetailInfoPanel() {
        detailCodeLabel.setText(NO_DATA_STRING);

        detailTitleLabel.setText(NO_DATA_STRING);

        editDetailInfoButton.setEnabled(false);

        detailQuantitySpinner.setEnabled(false);

        unitCheckBox.setSelected(false);
        unitCheckBox.setEnabled(false);

        workpieceWeightTextField.setText("");
        workpieceWeightTextField.setEnabled(false);

        finishedWeightTextField.setText("");
        finishedWeightTextField.setEnabled(false);

        materialLabel.setText(NO_DATA_STRING);

        editMaterialButton.setEnabled(false);

        techProcessComboBox.setSelectedItem(null);
        techProcessComboBox.setEnabled(false);
        createTechProcessButton.setEnabled(false);

        isActiveCheckBox.setSelected(false);
        isActiveCheckBox.setEnabled(false);

        isInterchangeableCheckBox.setSelected(false);
        isInterchangeableCheckBox.setEnabled(false);

        editImageButton.setEnabled(false);
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
                    DefaultMutableTreeNode detailListTreeByDetailList = new MainWindowUtils(session).getModuleDetailListTreeByEntityList(detailListService.getDetailListByParent(detailEntity));
                    if (detailListTreeByDetailList.children().hasMoreElements()) {
                        mainTree.setModel(new DefaultTreeModel(detailListTreeByDetailList));
                    } else {
                        if (detailEntity != null) {
                            DetailService detailService = new DetailServiceImpl(session);
                            if (detailService.getDetailById(detailEntity.getId()) == null) {
                                detailEntity = detailService.getDetailById(detailService.addDetail(detailEntity));
                            } else {
                                detailEntity = detailService.getDetailById(detailEntity.getId());
                            }
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

    private DetailListEntity generateDetailListFromEntity(DetailEntity parent, DetailEntity child) {
        log.debug("Generating new {} for parent: {} and child: {}", DetailListEntity.class.getName(), parent, child);
        if (parent == null || child == null) {
            throw new IllegalArgumentException("Parent or entity can not be null; parent: " + parent + ", child: {}" + child);
        }
        DetailListEntity detailListEntity = new DetailListEntity();
        detailListEntity.setQuantity(1);
        detailListEntity.setActive(true);

        detailListEntity.setDetailByParentDetailId(parent);
        detailListEntity.setDetailByChildDetailId(child);

        final NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();

        detailListEntity.setNoticeByNoticeId(selectedItem);

        log.debug("New detailListEntity parent: {} and child: {} and quantity: ({})", parent.toSimpleString(), child.toSimpleString(), detailListEntity.getQuantity());
        return detailListEntity;
    }

    private DetailListEntity getNewDetailListEntity(DetailEntity parent, DetailEntity entity) {
        DetailListEntity detailListEntity = new DetailListEntity();
        detailListEntity.setDetailByParentDetailId(parent);
        detailListEntity.setDetailByChildDetailId(entity);
        detailListEntity.setQuantity(1);
        detailListEntity.setActive(true);
        detailListEntity.setInterchangeableNode(false);
        detailListEntity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
        return detailListEntity;
    }

    @Override
    public AccessPolicyManager getPolicyManager() {
        return new AccessPolicyManager(true, true);
    }

    private List<MaterialListEntity> getUsedMaterials(DetailEntity detailEntity) {
        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        return service.getMaterialListByDetail(detailEntity);
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
                            String shortProfile = materialByMaterialId.getShortProfile();
                            shortProfile = CommonWindowUtils.getCanonicalProfile(shortProfile);
                            result.append(materialByMaterialId.getShortMark()).append(" ").append(shortProfile).append(", ");
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

    private void initDetailQuantitySpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 1000, 1);
        detailQuantitySpinner.setModel(model);

        JComponent comp = detailQuantitySpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(field::selectAll);
            }
        });
    }

    private void initEditImageButton() {
        Component component = this;
        final DropTarget dropTarget = new DropTarget() {
            Timer timer = null;

            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                onDrop(evt);
            }

            private void onDrop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);

                    final Object transferData = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    List<?> list = (List<?>) transferData;
                    if (list.size() == 1) {
                        if (list.get(0) instanceof File) {
                            File file = (File) list.get(0);
                            final boolean extension = FtpUtils.getInstance().isImage(file);
                            if (file.exists() && extension) {
                                final DetailEntity selectedDetailEntityFromTree = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                                if (selectedDetailEntityFromTree != null) {
                                    if (file.length() <= FtpUtils.MAX_IMAGE_FILE_SIZE) {
                                        onSuccess(file, selectedDetailEntityFromTree);
                                    } else {
                                        SwingUtilities.invokeLater(() ->
                                                JOptionPane.showMessageDialog(component,
                                                        "Размер файла изображения больше 5 мегабайт!",
                                                        "Ошибка", JOptionPane.WARNING_MESSAGE));
                                        onFail();
                                    }
                                } else {
                                    onFail();
                                }

                            } else {
                                onFail();
                            }
                        } else {
                            onFail();
                        }
                    } else {
                        onFail();
                    }
                    if (timer == null) {
                        timer = new Timer(3000, e -> editImageButton.setIcon(new ImageIcon(
                                getClass().getResource("/img/gui/pictureEdit16.png"))));
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        timer.restart();
                    }
                } catch (Exception ex) {
                    log.warn("Could not update image by drag&drop", ex);
                }
            }

            private void onFail() {
                editImageButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit()
                        .getImage(getClass().getResource("/img/gui/fail.png"))));
            }

            private void onSuccess(File file, DetailEntity selectedDetailEntityFromTree) {
                selectedDetailEntityFromTree.setImagePath(file.getAbsolutePath());
                editImageButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit()
                        .getImage(getClass().getResource("/img/gui/success.png"))));
            }
        };
        editImageButton.setDropTarget(dropTarget);

        try {
            dropTarget.addDropTargetListener(new DropTargetAdapter() {

                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                    editImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/import16.png")));
                    super.dragEnter(dtde);
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                    setDefaultImage();
                    super.dragExit(dte);
                }

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    setDefaultImage();
                }

                private void setDefaultImage() {
                    editImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/pictureEdit16.png")));
                }
            });
        } catch (TooManyListenersException e) {
            log.warn("Can not init drag and drop dropTarget", e);
        }

    }

    private void initEditPanelListeners() {
        addItemButton.addActionListener(e -> onAddNewItemButton());
        copyButton.addActionListener(e -> onCopyButton());
        removeItemButton.addActionListener(e -> onRemoveItem());

        detailQuantitySpinner.addChangeListener(new ChangeListener() {
            final int MAX_VALUE = 1000;
            private final String toolTip = detailQuantitySpinner.getToolTipText();

            private void showWarning() {
                detailQuantitySpinner.setBorder(new LineBorder(Color.RED));
                detailQuantitySpinner.setToolTipText("Количетво должно состоять только из положительных цельных чисел, не должно превышать 1000.");
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                verifyInput();
            }

            private void updateEntity() {
                DetailEntity parent = JTreeUtils.getParentForSelectionPath(mainTree);
                DetailEntity child = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                List<DetailListEntity> detailListEntitiesByParentAndChild = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByParentAndChild(parent, child);
                Collections.sort(detailListEntitiesByParentAndChild);
                log.debug("Parent: {}, Child: {}.", parent, child);
                if (detailListEntitiesByParentAndChild.size() > 0) {
                    for (DetailListEntity entity : detailListEntitiesByParentAndChild) {
                        log.debug("Current entity: " + entity);
                        if (entity.isActive()) {
                            int quantity = (Integer) detailQuantitySpinner.getValue();
                            if (quantity >= 1 && quantity <= 1000) {
                                entity.setQuantity(quantity);
                                entity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
                                log.debug("Added quantity: {}", quantity);
                                updateTreeDetail();
                            }
                        }
                    }
                } else {
                    log.debug("What to do here??? parent: {}, child: {}", parent, child);
                }
            }

            private void verifyInput() {
                try {
                    Integer number = (Integer) detailQuantitySpinner.getValue();
                    if (number <= 0) {
                        showWarning();
                        return;
                    }
                    if (number > MAX_VALUE) {
                        showWarning();
                        return;
                    }

                    detailQuantitySpinner.setBorder(new JSpinner().getBorder());
                    detailQuantitySpinner.setToolTipText(toolTip);
                } catch (Throwable throwable) {
                    showWarning();
                }
                updateEntity();
            }
        });

        final JFormattedTextField component = ((JFormattedTextField) detailQuantitySpinner.getEditor().getComponent(0));
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("?");
            }
        });

        unitCheckBox.addActionListener(e -> {
            DetailEntity detailEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
            if (detailEntity != null) {
                DetailListService detailListService = new DetailListServiceImpl(session);
                final List<DetailListEntity> detailListByParent = detailListService.getDetailListByParent(detailEntity);

                if (detailListByParent.size() == 0) {
                    detailEntity.setUnit(unitCheckBox.isSelected());
                    DetailService service = new DetailServiceImpl(session);
                    if (!unitCheckBox.isSelected()) {
                        detailEntity.setFinishedWeight(null);
                        detailEntity.setWorkpieceWeight(null);

                    }
                    service.updateDetail(detailEntity);

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                    node.setAllowsChildren(unitCheckBox.isSelected());

                    updateTreeDetail();
                }
            }

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
                DetailEntity parent = JTreeUtils.getParentForSelectionPath(mainTree);
                DetailEntity child = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                try {
                    if (child != null) {
                        String text = finishedWeightTextField.getText();
                        text = text.replaceAll(",", ".");
                        double weight = Double.parseDouble(text);
                        child.setFinishedWeight(weight);

                        updateDetail(child);

                        updateLatestDetailListEntity(parent, child);

                        updateTreeDetail();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        finishedWeightTextField.addFocusListener(weightFocusAdapter(finishedWeightTextField));

        workpieceWeightTextField.getDocument().addDocumentListener(new WeightDocumentListener(workpieceWeightTextField) {
            @Override
            public void updateEntity() {
                DetailEntity parent = JTreeUtils.getParentForSelectionPath(mainTree);
                DetailEntity child = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                try {
                    if (child != null) {
                        String text = workpieceWeightTextField.getText();
                        text = text.replaceAll(",", ".");
                        double weight = Double.parseDouble(text);
                        child.setWorkpieceWeight(weight);
                        updateDetail(child);
                        updateLatestDetailListEntity(parent, child);

                        updateTreeDetail();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        workpieceWeightTextField.addFocusListener(weightFocusAdapter(workpieceWeightTextField));

        editDetailInfoButton.addActionListener(e -> onEditDetailInfo());
        editMaterialButton.addActionListener(e -> onEditMaterial());
        createTechProcessButton.addActionListener(e -> onCreateNewTechProcess());

        isActiveCheckBox.addActionListener(e -> {
            try {
                DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                if (selectedEntity != null) {
                    final boolean selected = isActiveCheckBox.isSelected();
                    selectedEntity.setActive(!selected);
                    DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
                    service.updateDetail(selectedEntity);
                    updateTreeDetail();
                }
            } catch (Exception ex) {
                log.warn("Could not set detail activity", e);
            }
        });

        isInterchangeableCheckBox.addActionListener(e -> {
            DetailEntity parent = JTreeUtils.getParentForSelectionPath(mainTree);
            DetailEntity child = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
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

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        initListeners();

        initUpdateUserIsActiveListeners();

        initKeyBindings();

        initNoticeComboBox();

        fillNoticeComboBox();

        initMainTree();

        initDetailQuantitySpinner();

        initTechProcessComboBox();

        fillTechProcessComboBox();

        fillEmptyDetailInfoPanel();

        initEditImageButton();
    }

    private void initKeyBindings() {
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> onAddNewItemButton(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onCopyButton(), KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onRemoveItem(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initListeners() {
        createNoticeButton.addActionListener(e -> onCreateNewNotice());
        editNoticeButton.addActionListener(e -> onEditNotice());

        initEditPanelListeners();

        Component c = this;
        mainTabbedPane.addChangeListener(new ChangeListener() {
            int previousTabIndex = -1;

            @Override
            public void stateChanged(ChangeEvent e) {
                NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
                final int selectedIndex = mainTabbedPane.getSelectedIndex();
                if (selectedIndex != 0) {
                    if (selectedItem == null) {
                        mainTabbedPane.setSelectedIndex(0);
                        JOptionPane.showMessageDialog(FrameUtils.findWindow(c),
                                "Укажите извещение!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    } else {
                        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) mainTree.getModel().getRoot();
                        updateNotices();
                        if (selectedIndex == 1) {
                            if (root.getChildCount() == 0) {
                                fillMainTree();
                                fillEmptyDetailInfoPanel();
                            }
                        }

                        if (selectedIndex == 2) {
                            savePreviewPanel.removeAll();
                            InitializationPanel panel = new InitializationPanel();
                            savePreviewPanel.add(panel);
                            SwingUtilities.invokeLater(() -> {
                                final DetailListViewPanel comp = new DetailListViewPanel(session, rootEntity);
                                savePreviewPanel.add(comp);
                                savePreviewPanel.remove(panel);
                            });
                        }
                    }
                }
                previousTabIndex = selectedIndex;
            }
        });

        mainTabbedPane.addChangeListener(e ->
                FrameUtils.getNotifyUserIsActiveActionListener(this).actionPerformed(null));

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initMainTree() {
        DetailJTree jTree = (DetailJTree) mainTree;
        jTree.setSession(session);

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

                try {
                    fillDetailInfoPanel(lastUsed, selected);
                } catch (Exception e1) {
                    log.warn("Could not fill detail info for: {}", selected.toSimpleString(), e1);
                }
            }
        });

        MouseListener ml = new MainWindowUtils(session).getMouseListener(mainTree);
        mainTree.addMouseListener(ml);
        KeyListener initArrowKeyListener = new MainWindowUtils(session).getArrowKeyListener(mainTree);
        mainTree.addKeyListener(initArrowKeyListener);
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        mainTree.registerKeyboardAction(e -> onPasteFromClipboard(), ctrlV, JComponent.WHEN_FOCUSED);
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
            final String NO_DATA_STRING = "нет данных";
            if (entity != null) {
                noticeNumberLabel.setText(entity.getNumber());
                noticeDateLabel.setText(entity.getDate() != null ?
                        ApplicationConstants.DEFAULT_DATE_FORMAT.format(entity.getDate()) : NO_DATA_STRING);

                UsersEntity user = entity.getUsersByProvidedByUserId();
                noticeUserLabel.setText(entity.getAuthorByUserId() == null ? NO_DATA_STRING :
                        user.getUsername() + " " + user.getName() + " " + user.getSurname());
                noticeDescriptionTextArea.setText(entity.getDescription());
                noticeCreatedByUserLabel.setText(
                        entity.getAuthorByUserId() == null ? NO_DATA_STRING :
                                entity.getAuthorByUserId().getUsername() + " " + entity.getAuthorByUserId().getName() + " " + entity.getAuthorByUserId().getSurname());

                noticeCreationDateLabel.setText(entity.getCreationDate() != null ?
                        ApplicationConstants.DEFAULT_DATE_FORMAT.format(entity.getCreationDate()) : NO_DATA_STRING);
            } else {
                noticeNumberLabel.setText(NO_DATA_STRING);
                noticeDateLabel.setText(NO_DATA_STRING);
                noticeUserLabel.setText(NO_DATA_STRING);
                noticeDescriptionTextArea.setText("");
                noticeCreatedByUserLabel.setText(NO_DATA_STRING);
                noticeCreationDateLabel.setText(NO_DATA_STRING);
            }
        });
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
                DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                if (selectedEntity != null) {
                    selectedEntity.setTechProcessByTechProcessId((TechProcessEntity) techProcessComboBox.getSelectedItem());
                }
            } catch (Exception ignore) {/*NOP*/
            }
        });

        techProcessComboBox.setSelectedIndex(-1);
    }

    private void initUpdateUserIsActiveListeners() {
        createNoticeButton.addActionListener(notifyUserIsActiveListener);
        editNoticeButton.addActionListener(notifyUserIsActiveListener);
        buttonOK.addActionListener(notifyUserIsActiveListener);
        buttonCancel.addActionListener(notifyUserIsActiveListener);
        addItemButton.addActionListener(notifyUserIsActiveListener);
        copyButton.addActionListener(notifyUserIsActiveListener);
        removeItemButton.addActionListener(notifyUserIsActiveListener);
        editDetailInfoButton.addActionListener(notifyUserIsActiveListener);
        editImageButton.addActionListener(notifyUserIsActiveListener);
    }

    private boolean isConstructor(UsersEntity entity) {
        return entity.getUserType().getName().equalsIgnoreCase("Конструктор");
    }

    private boolean isTechnologist(UsersEntity entity) {
        return entity.getUserType().getName().equalsIgnoreCase("Технолог");
    }

    private void onAddNewItemButton() {
        final Object lastSelectedPathComponent = mainTree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent != null) {
            SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(session, null, SelectDetailEntityWindow.MODE.DEFAULT);
            selectionDetailWindow.setLocation(FrameUtils
                    .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
            selectionDetailWindow.setVisible(true);
            ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();

            if (list != null) {
                for (DetailEntity entity : list) {
                    if (entity != null) {
                        entity = createDetailEntityIfNotExist(entity);

                        addDetailToTree(entity);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Укажите, куда добавить деталь",
                    "Ошибка добавления", JOptionPane.WARNING_MESSAGE);
        }

    }

    private void onCancel() {
        if (status != SAVING) {
            int result = JOptionPane.showConfirmDialog(FrameUtils.findWindow(this), "Вы точно хотите отменить изменения?\n" +
                            "В случае подтверждения все изменения не сохранятся и никак\n" +
                            "не повлияют на базу данных.\n" +
                            "Отменить изменения?", "Отмена изменений", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                    new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/animated/uploading_cancel.gif"))));
            log.debug("User wanted to rollback changes, user's choice is: " + result);
            if (result == 0) {
                CommonUtils.rollbackAndCloseSession(session);
                closeTab();
            }
        }
    }

    private void onCopyButton() {
        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent != null) {
            final boolean isRoot = mainTree.getModel().getRoot().equals(lastSelectedPathComponent.getParent());
            if (!isRoot) {
                final TreePath selectionPath = mainTree.getSelectionPath();

                DetailEntity parent = JTreeUtils.getParentForSelectionPath(mainTree);
                if (selectedEntity != null && parent != null) {
                    SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(session, null, COPY);
                    selectionDetailWindow.setLocation(FrameUtils
                            .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
                    selectionDetailWindow.setVisible(true);
                    ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();

                    if (list != null) {
                        if (list.size() == 1) {
                            DetailEntity entity = list.get(0);
                            if (entity != null) {
                                entity.setUnit(selectedEntity.isUnit());
                                entity.setActive(true);

                                entity = copyBrotherEntityToDetailEntity(entity, selectedEntity);

                                DetailListService service = new DetailListServiceImpl(session);

                                ArrayList<DetailListEntity> listEntities;
                                try {
                                    listEntities = (ArrayList<DetailListEntity>) service.getDetailListByParentAndChild(parent, entity);
                                    if (listEntities.isEmpty()) {
                                        DetailListEntity detailListEntity = getNewDetailListEntity(parent, entity);
                                        service.addDetailList(detailListEntity);
                                    }
                                } catch (Exception e) {
                                    DetailListEntity detailListEntity = getNewDetailListEntity(parent, entity);
                                    service.addDetailList(detailListEntity);
                                }

                                addNode(selectionPath, entity);
                                mainTree.requestFocus();
                            }
                            showAllEntities(entity);
                        }
                    }
                }
            }
        }
    }

    private void onCreateNewNotice() {
        CreateNoticeWindow noticeWindow = new CreateNoticeWindow(session, null);
        noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
        noticeWindow.setVisible(true);
        NoticeEntity noticeEntity = noticeWindow.getNoticeEntity();
        noticeComboBox.removeAllItems();
        fillNoticeComboBox();
        noticeComboBox.setSelectedItem(noticeEntity);
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

    private void onEditDetailImage() {
        DetailEntity detailEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);

        if (detailEntity != null) {
            EditImageWindow editImageWindow = new EditImageWindow(detailEntity);
            editImageWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), editImageWindow));
            editImageWindow.setVisible(true);
        }
    }

    private void onEditDetailInfo() {
        final DetailEntity selectedDetailEntityFromTree = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);

        if (selectedDetailEntityFromTree != null) {
            SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(session, selectedDetailEntityFromTree, EDIT);
            selectionDetailWindow.setLocation(FrameUtils
                    .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
            selectionDetailWindow.setVisible(true);

            ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();

            if (list != null) {
                if (list.size() > 0) {
                    final DetailEntity changed = list.get(0);
                    if (changed != null) {
                        updateTreeDetail();
                        detailCodeLabel.setText(changed.getCode());
                        detailTitleLabel.setText(changed.getDetailTitleByDetailTitleId().getTitle());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Нельзя сделать выбор из нескольких деталей",
                            "Ошибка редактирования", JOptionPane.WARNING_MESSAGE);
                }
            }
        }


    }

    private void onEditMaterial() {
        final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        DetailEntity detailEntity = (DetailEntity) lastSelectedPathComponent.getUserObject();


        EditMaterialListWindow materialListWindow = new EditMaterialListWindow(session, detailEntity);
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

    private void onEditNotice() {
        Object selectedItem = noticeComboBox.getSelectedItem();
        if (selectedItem != null) {
            NoticeEntity entity = (NoticeEntity) selectedItem;
            CreateNoticeWindow noticeWindow = new CreateNoticeWindow(session, entity);
            noticeWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), noticeWindow));
            noticeWindow.setVisible(true);
            NoticeEntity noticeEntity = noticeWindow.getNoticeEntity();
            noticeComboBox.removeAllItems();
            fillNoticeComboBox();
            noticeComboBox.setSelectedItem(noticeEntity);
        }
    }

    @javax.transaction.Transactional
    private void onOK() {
        int result = JOptionPane.showConfirmDialog(this, "Вы точно хотите сохранить изменения в базе данных?\n" +
                        "Введенные вами данные будут сохранены в базе и появятся у всех пользователей.\n" +
                        "Также все проведённые изменения будут закреплены за вами, \n" +
                        "и в случае вопросов, будут обращаться к вам.\n" +
                        "Провести изменения?", "Подтверждение изменений", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/animated/sync.gif"))));
        log.debug("User wanted to commit changes, user's choice is: " + result);

        if (result == 0) {
            new Thread(this::saveChanges).start();
        }
    }

    private void onPasteFromClipboard() {
        System.out.println("hello");
        if (mainTree.hasFocus()) {
            try {
                String string = (String) Toolkit.getDefaultToolkit()
                        .getSystemClipboard().getData(DataFlavor.stringFlavor);
                System.out.println("Got string: " + string);
                DetailEntity detailEntity = new StringUtils(session).getDetailEntityFromString(string);
                System.out.println("Detail from clipboard: " + detailEntity);
                if (detailEntity != null) {
                    addDetailToTree(detailEntity);
                }
            } catch (UnsupportedFlavorException | IOException ignore) {
            }
        }
    }

    private void onRemoveItem() {
        DefaultMutableTreeNode selectedPath = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();

        DetailEntity selected = (DetailEntity) selectedPath.getUserObject();
        TreePath parentPath = mainTree.getSelectionPath().getParentPath();

        DetailEntity parent = (DetailEntity) (((DefaultMutableTreeNode) parentPath.getLastPathComponent()).getUserObject());
        log.debug("Class: {}, {} ", selected.getClass().getName(), parent.getClass().getName());
        log.debug("User want to remove item: {}, its parent: {}", selected, parent);
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
        /*List<DetailListEntity> detailListByParentAndChild = service.getDetailListByParentAndChild(parent, selected);
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
        }*/
        //testme
        DetailListEntity detailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, selected);
        if (detailListEntity != null) {
            if (detailListEntity.getNoticeByNoticeId().equals(noticeComboBox.getSelectedItem())) {
                if (detailListEntity.isActive()) {
                    detailListEntity.setActive(false);
                    service.updateDetailList(detailListEntity);
                }
            } else {
                final DetailListEntity detailListByParentAndChildAndNotice = service.getDetailListByParentAndChildAndNotice(parent, selected, (NoticeEntity) noticeComboBox.getSelectedItem());
                if (detailListByParentAndChildAndNotice != null) {
                    if (detailListByParentAndChildAndNotice.isActive()) {
                        detailListByParentAndChildAndNotice.setActive(false);
                        service.updateDetailList(detailListByParentAndChildAndNotice);
                    }
                } else {
                    detailListEntity.setActive(false);
                    service.addDetailList(detailListEntity);
                }
            }
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        parentNode.remove(selectedPath);

        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        model.reload(selectedPath);

        mainTree.expandPath(parentPath);
    }

    private void removeUnusedDetailLists() {
        log.debug("Starting removing unused detail lists");
        DetailListService service = new DetailListServiceImpl(session);
        final List<DetailListEntity> detailListEntities = service.listDetailLists();
        for (DetailListEntity entity : detailListEntities) {
            if (!entity.isActive() && entity.getNoticeByNoticeId().equals(noticeComboBox.getSelectedItem())) {
                List<DetailListEntity> result = service.getDetailListByParentAndChild(entity.getDetailByParentDetailId(), entity.getDetailByChildDetailId());//todo fix this... hm... why?

                if (result.size() > 1) {
                    DetailListEntity detailListEntity = new MainWindowUtils(session).getLatestDetailListEntity(result);
                    if (!detailListEntity.equals(entity)) {
                        log.debug("Removing detailList: {}", entity);
                        service.removeDetailList(entity.getId());
                    }

                } else if (result.size() == 1) {
                    log.debug("Removing detailList: {}", entity);
                    service.removeDetailList(entity.getId());
                }
            }
        }
        log.info("Removing unused detailLists finished");
    }

    private void removeUnusedMaterialLists() {
        log.debug("Starting removing unused MaterialLists");
        MaterialListService service = new MaterialListServiceImpl(session);
        ArrayList<MaterialListEntity> list = (ArrayList<MaterialListEntity>) service.getUnusedMaterialLists();
        for (MaterialListEntity e : list) {
            service.removeMaterialList(e.getId());
        }
        log.info("Removing unused  MaterialLists finished");
    }

    @Override
    public void rollbackTransaction() {
        if (status != SAVING) {
            onCancel();
        }
    }

    private void saveChanges() {
        final MainWindowUtils mainWindowUtils = new MainWindowUtils(session);
        mainWindowUtils.setClientMainWindow(this);

        status = SAVING;

        try {
            FrameUtils.enableAllComponents(this, false);
            mainWindowUtils.updateMessage("/img/gui/animated/uploading.gif", "Обновляем изображения...");
            uploadImages();

            NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
            if (selectedItem != null) {
                mainWindowUtils.updateMessage("/img/gui/animated/sync.gif", "Обновляем текущее извещение...");
                updateNotice();

                mainWindowUtils.updateMessage("/img/gui/animated/sync.gif", "Обновляем последние извещения для измененных деталей...");
                updateAllNotices();

                mainWindowUtils.updateMessage("/img/gui/animated/sync.gif", "Избавляемся от мусора...");
                removeUnusedDetailLists();

                removeUnusedMaterialLists();

                //todo update info about notice!!!!
                log.debug("Transaction status: {}", session.getTransaction().getStatus());

                mainWindowUtils.updateMessage("/img/gui/animated/uploading.gif", "Сохраняем изменения...");
                session.getTransaction().commit();

                mainWindowUtils.updateMessage("/img/gui/animated/uploading_completed.gif", "Изменения успешно сохранены");
                log.info("New state of db is committed");

                status = DEFAULT;
                ClientBackgroundService.getInstance().unbindTransaction();
                closeTab();
            } else {
                JOptionPane.showMessageDialog(this, "Укажите извещение!",
                        "Ошибка сохранения", JOptionPane.WARNING_MESSAGE,
                        new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/animated/uploading_error.gif"))));
            }
        } catch (Exception e) {
            FrameUtils.enableAllComponents(this, true);

            status = DEFAULT;

            log.warn("Could not call commit for transaction", e);
            mainWindowUtils.updateMessage("/img/gui/animated/uploading_error.gif", "Ошибка во время сохранения изменений");
            JOptionPane.showMessageDialog(this,
                    "Не удалось завершить транзакцию\n" + e.getLocalizedMessage(), "Ошибка сохранения",
                    JOptionPane.WARNING_MESSAGE,
                    new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/animated/uploading_error.gif"))));
        } finally {
            SessionUtils.closeSessionSilently(session);
        }
    }

    @Override
    public void setUIEnabled(boolean ignore) {
        /*NOP*/
    }

    private void showAllEntities(DetailEntity detailEntity) {
        DetailListService service = new DetailListServiceImpl(session);
        ArrayList<DetailListEntity> list = (ArrayList<DetailListEntity>) service.getDetailListByParent(detailEntity);
        String detailCode = detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle();
        log.debug("Showing all DetailListEntities for: {}", detailCode);
        for (DetailListEntity entity : list) {
            if (entity.getDetailByParentDetailId().equals(detailEntity)) {
                log.debug("Added entity {} for {}", entity, detailCode);
            }
        }
    }

    private void updateAllNotices() {
        log.debug("Starting updating all notices");
        DetailListService service = new DetailListServiceImpl(session);
        ArrayList<DetailListEntity> list = (ArrayList<DetailListEntity>) service.listDetailLists();
        for (DetailListEntity entity : list) {
            if (entity.getNoticeByNoticeId() == null) {
                NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
                entity.setNoticeByNoticeId(selectedItem);
            }
        }
        log.debug("Updating all notices finished successful");
    }

    private void updateDetail(DetailEntity detail) {
        DetailService service = new DetailServiceImpl(session);
        service.updateDetail(detail);
    }

    private void updateLatestDetailListEntity(DetailEntity parent, DetailEntity child) {
        if (parent != null && child != null) {
            final DetailListServiceImpl service = new DetailListServiceImpl(session);
            final DetailListEntity detailList = service.getLatestDetailListEntityByParentAndChild(parent, child);
            final NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
            if (selectedItem != null && detailList != null) {
                detailList.setNoticeByNoticeId(selectedItem);
                service.updateDetailList(detailList);
            }
        } else {
            log.warn("Could not update notice for pair - something is null; parent: {} and child: {}", parent, child);
        }
    }

    private void updateMaterialsForEntity(DetailEntity detailEntity, ArrayList<MaterialListEntity> newMaterialsList, ArrayList<MaterialListEntity> oldMaterialsList) {
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

    private void updateNotice() {
        NoticeEntity selectedItem = (NoticeEntity) noticeComboBox.getSelectedItem();
        log.debug("Updating notice info: {}", selectedItem);
        if (selectedItem != null) {
            ClientMainWindow clientMainWindow = new MainWindowUtils(session).getClientMainWindow(this);
            if (clientMainWindow != null) {
                UsersEntity currentUser = clientMainWindow.getCurrentUser();
                if (currentUser != null) {
                    selectedItem.setUsersByProvidedByUserId(currentUser);
                    selectedItem.setDate(new Date(Calendar.getInstance().getTime().getTime()));
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Укажите извещение!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateNotices() {
        DetailListService service = new DetailListServiceImpl(session);
        List<DetailListEntity> result = service.getDetailListByParent(detailEntity);
        for (DetailListEntity entity : result) {
            if (entity != null) {
                if (entity.getNoticeByNoticeId() == null) {
                    entity.setNoticeByNoticeId((NoticeEntity) noticeComboBox.getSelectedItem());
                    service.updateDetailList(entity);
                }
            }
        }
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

                editDetailInfoButton.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)));

                unitCheckBox.setEnabled(((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot)
                        && (new DetailListServiceImpl(session).getDetailListByParent(detailEntity).size() == 0));//testme

                detailQuantitySpinner.setEnabled(!isRoot && (currentUser.isAdmin() || isConstructor(currentUser)));

                finishedWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isConstructor(currentUser)));

                workpieceWeightTextField.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                editMaterialButton.setEnabled(!detailEntity.isUnit());

                techProcessComboBox.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                createTechProcessButton.setEnabled(!detailEntity.isUnit() && (currentUser.isAdmin() || isTechnologist(currentUser)));

                isActiveCheckBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);

                isInterchangeableCheckBox.setEnabled((currentUser.isAdmin() || isConstructor(currentUser)) && !isRoot);

                editImageButton.setEnabled(currentUser.isAdmin() || isConstructor(currentUser));

                //----------------------

                addItemButton.setEnabled((!isRoot || detailEntity.isUnit()) && (currentUser.isAdmin() || isConstructor(currentUser)));
                copyButton.setEnabled(((!isRoot || detailEntity.isUnit()) && (currentUser.isAdmin() || isConstructor(currentUser))) && detailEntity.isUnit());
                removeItemButton.setEnabled((!isRoot || detailEntity.isUnit()) && (currentUser.isAdmin() || isConstructor(currentUser)));

            } else {
                editDetailInfoButton.setEnabled(false);
                unitCheckBox.setEnabled(false);
                detailQuantitySpinner.setEnabled(false);
                finishedWeightTextField.setEnabled(false);
                workpieceWeightTextField.setEnabled(false);
                techProcessComboBox.setEnabled(false);
                createTechProcessButton.setEnabled(false);
                isActiveCheckBox.setEnabled(false);
            }
        }
    }

    private void updateTreeDetail() {
        DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
        model.reload(node);
    }

    private void uploadImages() {
        log.debug("Starting updating images");
        DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
        List<DetailEntity> detailEntities = service.listDetailsByEditedImage();
        if (detailEntities != null) {
            log.debug("Total details count: {}", detailEntities.size());
            for (DetailEntity entity : detailEntities) {
                log.trace("Checking detail: {}", entity.toSimpleString());
                try {
                    if (entity.getImagePath() != null) {
                        if (!entity.getImagePath().isEmpty()) {
                            if (!entity.getImagePath().equalsIgnoreCase(ClientConstants.IMAGE_REMOVE_KEY)) {
                                File file = new File(entity.getImagePath());
                                if (file.exists()) {
                                    if (file.length() <= FtpUtils.MAX_IMAGE_FILE_SIZE) {
                                        try {
                                            final FtpUtils ftpUtils = FtpUtils.getInstance();
                                            ftpUtils.uploadImage(entity.getId(), file);
                                        } catch (Exception e) {
                                            log.warn("Could not upload image for entity: {}", entity, e);
                                        }
                                    } else {
                                        log.warn("Image file for entity: {} ({}) size is: {} and this is more then 5MB ({})", entity, file, file.length(), FtpUtils.MAX_IMAGE_FILE_SIZE);
                                    }
                                } else {
                                    log.warn("Could not find file for entity: {}, file: {}", entity, file);
                                }
                            } else {
                                try {
                                    FtpUtils.getInstance().deleteImage(entity.getId());
                                } catch (IOException e) {
                                    log.warn("Could not delete image for {}", entity, e);
                                }
                            }
                            entity.setImagePath(null);
                        } else {
                            entity.setImagePath(null);
                            service.updateDetail(entity);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Can not upload image for detail: {}", entity, e);
                }
            }
        } else {
            log.info("Details with edited images not found!");
        }
    }

    private FocusAdapter weightFocusAdapter(JTextField textField) {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                try {
                    String text = textField.getText();
                    text = text.replaceAll(",", ".");
                    double weight = Double.parseDouble(text);
                    if (weight == 0) {
                        textField.setSelectionStart(0);
                        textField.setSelectionEnd(textField.getText().length());
                    }
                } catch (NumberFormatException ignored) {
                }

            }
        };
    }


    enum Status {DEFAULT, SAVING}

    private abstract class WeightDocumentListener implements DocumentListener {
        private final String defaultTooltipText;
        private JTextField textField;

        WeightDocumentListener(JTextField textField) {
            this.textField = textField;
            this.defaultTooltipText = textField.getToolTipText();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            verifyInput(textField);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            verifyInput(textField);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            verifyInput(textField);
        }

        public abstract void updateEntity();

        private void verifyInput(JTextField textField) {
            try {
                String text = textField.getText();
                text = text.replaceAll(",", ".");
                if (text.contains("d")) {
                    throw new IllegalArgumentException("This textfield can not have d (double thing): " + text);
                }

                System.out.println("> " + text);
                double value = Double.parseDouble(text);
                if (value < 0) {
                    throw new IllegalArgumentException("Value is < 0, value is: " + value);
                }

                textField.setBorder(new JTextField().getBorder());
                textField.setToolTipText(defaultTooltipText);
                updateEntity();
            } catch (Throwable throwable) {
                if (!(throwable instanceof NumberFormatException)) {
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
    }

}
