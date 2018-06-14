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
import com.mmz.specs.application.gui.client.CreateDetailWindow;
import com.mmz.specs.application.gui.client.MaterialListWindow;
import com.mmz.specs.application.gui.client.SelectDetailEntityWindow;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.dao.MaterialListDaoImpl;
import com.mmz.specs.model.*;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.imgscalr.Scalr;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mmz.specs.application.gui.client.SelectDetailEntityWindow.MODE.CREATE_SINGLE;

public class DetailListPanel extends JPanel implements AccessPolicy {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int MAXIMUM_STRING_LENGTH = 35;
    private JPanel detailListPanel;
    private JPanel detailInfoPanel;
    private JTextField searchTextField;
    private JLabel numberLabel;
    private JLabel titleLabel;
    private JLabel unitLabel;
    private JLabel finishedWeightLabel;
    private JLabel workpieceWeightLabel;
    private JLabel techProcessLabel;
    private JLabel isActiveLabel;
    private JLabel detailIconLabel;
    private JButton noticeInfoButton;
    private JTree mainTree;
    private JButton refreshSessionButton;
    private JButton addButton;
    private Session session;
    private JButton editButton;
    private JLabel materialMarkLabel;
    private JLabel materialProfileLabel;
    private JButton copyButton;

    public DetailListPanel() {
        $$$setupUI$$$();
        session = ClientBackgroundService.getInstance().getSession();

        initGui();
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(detailListPanel);

        initListeners();

        initMainTree();

    }

    private void initListeners() {
        noticeInfoButton.addActionListener(e -> onNoticeInfo(true));

        noticeInfoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    onNoticeInfo(false);
                }
            }
        });

        initSearchTextFieldDocumentListeners();

        refreshSessionButton.addActionListener(e -> onRefreshSession());

        initSearchTextFieldKeysBindings();

        initKeyBindings();


        /*addButton.addActionListener(e -> onAddNewItem());*/
        addButton.addActionListener(e -> onAddNewItemNew());

        copyButton.addActionListener(e -> onCopyButton());

        editButton.addActionListener(e -> onEditDetail(true));
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    onEditDetail(false);
                }
            }
        });
    }

    private void onAddNewItemNew() {
        SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(null, CREATE_SINGLE);
        selectionDetailWindow.setLocation(FrameUtils
                .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
        selectionDetailWindow.setVisible(true);
        ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();
    }

    private void initKeyBindings() {
        registerKeyboardAction(e -> onAddNewItem(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(e -> onCopyButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(e -> onEditDetail(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onAddNewItem() {
        CreateDetailWindow addDetailWindow = new CreateDetailWindow(null);
        addDetailWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), addDetailWindow));
        addDetailWindow.setVisible(true);
        DetailEntity entity = addDetailWindow.getDetailEntity();
        if (entity != null) {
            entity.setActive(true);
            Window window = FrameUtils.findWindow(this);
            if (window instanceof ClientMainWindow) {
                ClientMainWindow mainWindow = (ClientMainWindow) window;
                ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                try {
                    mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(entity), true);
                } catch (IllegalStateException e) {
                    log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                    JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                            "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void onCopyButton() {
        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedEntity != null) {
            if (selectedEntity.isUnit()) {
                CreateDetailWindow addDetailWindow = new CreateDetailWindow(null, selectedEntity.isUnit());
                addDetailWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), addDetailWindow));
                addDetailWindow.setVisible(true);

                DetailEntity entity = addDetailWindow.getDetailEntity();

                if (entity != null) {
                    entity.setUnit(true);
                    entity.setActive(true);

                    Window window = FrameUtils.findWindow(this);
                    if (window instanceof ClientMainWindow) {
                        ClientMainWindow mainWindow = (ClientMainWindow) window;
                        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                        try {
                            mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(entity, selectedEntity), true);
                        } catch (IllegalStateException e) {
                            log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                            JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                                    "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private void initSearchTextFieldKeysBindings() {
        registerKeyboardAction(e -> searchTextField.requestFocus(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        searchTextField.registerKeyboardAction(e -> searchTextField.setText(""),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void updateDetailInfoPanel(DetailEntity selectedComponent) {
        updateDetailInfoPanelWithEmptyEntity();
        if (selectedComponent != null) {
            updateDetailInfoPanelWithEntity(selectedComponent);
        }
    }

    private void updateDetailInfoPanelWithEmptyEntity() {
        numberLabel.setText("");
        titleLabel.setText("");

        detailIconLabel.setIcon(null);
        if (detailIconLabel.getMouseListeners().length > 0) {
            for (MouseListener listener : detailIconLabel.getMouseListeners()) {
                detailIconLabel.removeMouseListener(listener);
            }
        }

        unitLabel.setText("");
        finishedWeightLabel.setText("");
        workpieceWeightLabel.setText("");

        techProcessLabel.setToolTipText("");
        techProcessLabel.setText("");

        isActiveLabel.setText("");
    }

    private void initSearchTextFieldDocumentListeners() {
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            String searchText = "";
            final Timer searchTimer = new Timer(1000, e -> {
                if (!searchText.isEmpty()) {
                    searchText = searchText.replace(",", ".");
                    searchText = searchText.toUpperCase();
                    log.debug("User is searching for: " + searchText);
                    fillMainTreeBySearch(searchText);
                } else {
                    fillMainTreeFully();
                }
            });

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchText = searchTextField.getText();
                searchTimer.setRepeats(false);
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else searchTimer.start();
            }
        });
    }

    private void fillMainTreeFully() {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).getDetailListFullTree(service.listDetailLists())));
        }
    }

    private void onNoticeInfo(boolean select) {
        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedEntity != null) {
            DetailListService detailListService = new DetailListServiceImpl(new DetailListDaoImpl(session));
            List<DetailListEntity> list;

            if (detailListService.getDetailListByParent(selectedEntity).size() > 0) {
                list = detailListService.getDetailListByParent(selectedEntity);
            } else {
                list = detailListService.getDetailListByChild(selectedEntity);

            }
            if (list != null) {
                if (list.size() > 0) {
                    List<NoticeEntity> noticeEntities = getUniqueNoticeList(list);
                    NoticeInfoPanel noticeInfoPanel = new NoticeInfoPanel(session, noticeEntities);
                    noticeInfoPanel.setDetailEntity(selectedEntity);

                    ClientMainWindow clientMainWindow = (ClientMainWindow) FrameUtils.findWindow(this);
                    ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/notice16.png")));
                    clientMainWindow.addTab("Информация о извещениях", icon, noticeInfoPanel, select);
                }
            }
        }
    }

    private void onRefreshSession() {
        if (ClientBackgroundService.getInstance().isConnected()) {
            ClientBackgroundService.getInstance().refreshSession();
            fillMainTreeFully();
            searchTextField.setText("");
        }
    }

    private List<NoticeEntity> getUniqueNoticeList(List<DetailListEntity> list) {
        List<NoticeEntity> result = new ArrayList<>();
        List<NoticeEntity> noticeList = new ArrayList<>();
        for (DetailListEntity entity : list) {
            NoticeEntity noticeEntity = entity.getNoticeByNoticeId();

            if (noticeEntity != null) {
                if (!noticeList.contains(noticeEntity)) {
                    noticeList.add(noticeEntity);
                    result.add(noticeEntity);
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private void fillMainTreeBySearch(String searchText) {
        if (session != null) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).getDetailListTreeByDetailList(service.getDetailListBySearch(searchText))));
        }
    }

    private void updateDetailInfoPanelWithEntity(DetailEntity selectedComponent) {
        numberLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, selectedComponent.getCode()));

        if (selectedComponent.getDetailTitleByDetailTitleId() != null) {
            String title = selectedComponent.getDetailTitleByDetailTitleId().getTitle();
            titleLabel.setToolTipText(title);
            titleLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, title));
        }

        unitLabel.setText(Boolean.toString(selectedComponent.isUnit())
                .replace("false", "нет").replace("true", "да"));

        if (selectedComponent.getFinishedWeight() != null) {
            finishedWeightLabel.setText((Double.toString(selectedComponent.getFinishedWeight())));
        }

        if (selectedComponent.getWorkpieceWeight() != null) {
            workpieceWeightLabel.setText(Double.toString(selectedComponent.getWorkpieceWeight()));
        }

        fillDetailMaterialInfo(selectedComponent);


        updateDetailImage(selectedComponent);

        if (selectedComponent.getTechProcessByTechProcessId() != null) {
            String process = selectedComponent.getTechProcessByTechProcessId().getProcess();
            techProcessLabel.setToolTipText(process);
            techProcessLabel.setText(CommonUtils.substring(25, process));
        }

        isActiveLabel.setText(Boolean.toString(!selectedComponent.isActive())
                .replace("false", "нет").replace("true", "да"));
    }

    private void updateDetailImage(final DetailEntity selectedComponent) {
        Runnable runnable = () -> {
            detailIconLabel.setText("Загрузка...");
            FrameUtils.removeAllComponentListeners(detailIconLabel);

            FtpUtils ftp = FtpUtils.getInstance();
            BufferedImage image = ftp.getImage(selectedComponent.getId());

            if (image != null) {
                BufferedImage scaledImage = Scalr.resize(image, 128);

                DetailEntity current = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
                if (selectedComponent.equals(current)) {// prevents setting image for not current selected DetailEntity (fixes time delay)
                    detailIconLabel.setIcon(new ImageIcon(scaledImage));
                    detailIconLabel.setText("");
                    detailIconLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            FrameUtils.onShowImage(FrameUtils.findWindow(DetailListPanel.super.getRootPane()), false,
                                    image, "Изображение " + selectedComponent.getCode() + " "
                                            + selectedComponent.getDetailTitleByDetailTitleId().getTitle());
                        }
                    });
                }
            } else {
                detailIconLabel.setIcon(null);
                detailIconLabel.setText("Нет изображения");
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void fillDetailMaterialInfo(DetailEntity selectedComponent) {
        clearDetailMaterialInfo();

        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        List<MaterialListEntity> materialList = service.getMaterialListByDetail(selectedComponent);
        if (!materialList.isEmpty()) {
            for (MaterialListEntity entity : materialList) {
                if (entity.isMainMaterial() && entity.isActive()) {
                    String substringLongMark = CommonUtils.substring(MAXIMUM_STRING_LENGTH, entity.getMaterialByMaterialId().getLongMark());
                    String substringLongProfile = CommonUtils.substring(MAXIMUM_STRING_LENGTH, entity.getMaterialByMaterialId().getLongProfile());

                    materialMarkLabel.setText(substringLongMark);
                    materialProfileLabel.setText(substringLongProfile);

                    String longMark = entity.getMaterialByMaterialId().getLongMark();
                    String longProfile = entity.getMaterialByMaterialId().getLongProfile();
                    String delimiter = CommonWindowUtils.createDelimiter(longMark, longProfile);
                    String tooltipText = "<html> <p style=\"line-height: 0.2em;\">" + longMark + "<br>" + delimiter + "<br>" + longProfile + "</p></html>";
                    materialMarkLabel.setToolTipText(tooltipText);
                    materialProfileLabel.setToolTipText(tooltipText);
                }
            }
            initMaterialLabelListener(materialList);
        }
    }

    private void clearDetailMaterialInfo() {
        materialMarkLabel.setText(" ");
        materialProfileLabel.setText(" ");
        initMaterialLabelListener(null);
    }

    private void initMaterialLabelListener(List<MaterialListEntity> materialList) {
        for (MouseListener listener : materialMarkLabel.getMouseListeners()) {
            materialMarkLabel.removeMouseListener(listener);
            materialProfileLabel.removeMouseListener(listener);
        }

        if (materialList != null) {
            final MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    MaterialListWindow materialListWindow = new MaterialListWindow(materialList);
                    materialListWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(DetailListPanel.super.getRootPane()), materialListWindow));
                    materialListWindow.setVisible(true);
                }
            };
            materialMarkLabel.addMouseListener(adapter);
            materialProfileLabel.addMouseListener(adapter);
        }
    }

    private void initMainTree() {
        mainTree.addTreeSelectionListener(e -> {
            DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
            if (selectedEntity != null) {
                DetailServiceImpl detailService = new DetailServiceImpl(new DetailDaoImpl(session));
                DetailEntity loadedEntity = detailService.getDetailById(selectedEntity.getId());

                updateDetailInfoPanel(loadedEntity);
            }
        });

        fillMainTreeFully();
    }

    private void onEditDetail(boolean select) {
        final DetailEntity entityFromTree = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (entityFromTree != null) {
            Window window = FrameUtils.findWindow(this);
            if (window instanceof ClientMainWindow) {
                ClientMainWindow mainWindow = (ClientMainWindow) window;
                final UsersEntity currentUser = mainWindow.getCurrentUser();
                if (currentUser != null) {
                    if ((currentUser.isEditor() || currentUser.isAdmin()) && currentUser.isActive()) {
                        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                        try {
                            mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(entityFromTree), select);
                        } catch (IllegalStateException e) {
                            log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                            JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                                    "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private void createUIComponents() {
        mainTree = new DetailJTree();
    }

    @Override
    public AccessPolicyManager getPolicyManager() {
        return new AccessPolicyManager(false, false);
    }

    @Override
    public void setUIEnabled(boolean enable) {
        addButton.setEnabled(enable);
        editButton.setEnabled(enable);
        final DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        copyButton.setEnabled(selectedEntity != null ? enable && selectedEntity.isUnit() : enable);
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel = new JPanel();
        detailListPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(detailListPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detailInfoPanel = new JPanel();
        detailInfoPanel.setLayout(new GridLayoutManager(13, 3, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel.add(detailInfoPanel, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Индекс:");
        detailInfoPanel.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        detailInfoPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Узел:");
        detailInfoPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Масса готовой детали:");
        detailInfoPanel.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Норма расхода:");
        detailInfoPanel.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Аннулирована:");
        detailInfoPanel.add(label6, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Технический процесс:");
        detailInfoPanel.add(label7, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberLabel = new JLabel();
        numberLabel.setText("нет данных");
        detailInfoPanel.add(numberLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(210, -1), null, 0, false));
        titleLabel = new JLabel();
        titleLabel.setText("нет данных");
        detailInfoPanel.add(titleLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(210, -1), null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("нет данных");
        detailInfoPanel.add(unitLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightLabel = new JLabel();
        finishedWeightLabel.setText("нет данных");
        detailInfoPanel.add(finishedWeightLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightLabel = new JLabel();
        workpieceWeightLabel.setText("нет данных");
        detailInfoPanel.add(workpieceWeightLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        techProcessLabel = new JLabel();
        techProcessLabel.setText("нет данных");
        detailInfoPanel.add(techProcessLabel, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveLabel = new JLabel();
        isActiveLabel.setText("нет данных");
        detailInfoPanel.add(isActiveLabel, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        detailIconLabel = new JLabel();
        detailIconLabel.setForeground(new Color(-10395295));
        detailIconLabel.setHorizontalAlignment(0);
        detailIconLabel.setHorizontalTextPosition(0);
        detailIconLabel.setText("Нет изображения");
        detailInfoPanel.add(detailIconLabel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Последнее извещение:");
        detailInfoPanel.add(label8, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noticeInfoButton = new JButton();
        noticeInfoButton.setBorderPainted(false);
        noticeInfoButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/info.png")));
        noticeInfoButton.setIconTextGap(0);
        noticeInfoButton.setMargin(new Insets(2, 2, 2, 2));
        noticeInfoButton.setOpaque(false);
        noticeInfoButton.setText("");
        detailInfoPanel.add(noticeInfoButton, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        detailInfoPanel.add(spacer1, new GridConstraints(2, 2, 11, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Материал:");
        detailInfoPanel.add(label9, new GridConstraints(7, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialMarkLabel = new JLabel();
        materialMarkLabel.setText("    ");
        detailInfoPanel.add(materialMarkLabel, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialProfileLabel = new JLabel();
        materialProfileLabel.setText("     ");
        detailInfoPanel.add(materialProfileLabel, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        detailInfoPanel.add(panel2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchTextField = new JTextField();
        panel2.add(searchTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setIcon(new ImageIcon(getClass().getResource("/img/gui/search16.png")));
        label10.setText("");
        panel2.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        detailInfoPanel.add(spacer2, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        detailListPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Узлы"));
        mainTree.setFocusable(true);
        mainTree.setRootVisible(false);
        scrollPane1.setViewportView(mainTree);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(0);
        panel3.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        refreshSessionButton = new JButton();
        refreshSessionButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh-left-arrow.png")));
        refreshSessionButton.setMargin(new Insets(2, 2, 2, 2));
        refreshSessionButton.setText("");
        refreshSessionButton.setToolTipText("Обновить данные с базы данных");
        toolBar1.add(refreshSessionButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        addButton = new JButton();
        addButton.setEnabled(false);
        addButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addButton.setText("");
        addButton.setToolTipText("Добавить узел (CTRL+INSERT)");
        toolBar1.add(addButton);
        copyButton = new JButton();
        copyButton.setEnabled(false);
        copyButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/copy.png")));
        copyButton.setText("");
        copyButton.setToolTipText("Дублировать узел (CTRL+D)");
        toolBar1.add(copyButton);
        editButton = new JButton();
        editButton.setEnabled(false);
        editButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editButton.setText("");
        editButton.setToolTipText("Редактировать (CTRL+E)");
        toolBar1.add(editButton);
        label10.setLabelFor(searchTextField);
    }
}
