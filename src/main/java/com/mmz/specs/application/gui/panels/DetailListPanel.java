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

import com.google.common.collect.ComparisonChain;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.client.MaterialListWindow;
import com.mmz.specs.application.gui.client.SelectDetailEntityWindow;
import com.mmz.specs.application.gui.common.DetailJTree;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.gui.common.utils.PlaceholderTextField;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.imgscalr.Scalr;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mmz.specs.application.core.client.ClientConstants.IMAGE_REMOVE_KEY;
import static com.mmz.specs.application.gui.client.SelectDetailEntityWindow.MODE.COPY;
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
    private JLabel materialLabel;
    private JPanel controlsBar;
    private ActionListener notifyUserIsActiveListener = FrameUtils.getNotifyUserIsActiveActionListener(this);
    private Thread searchThread = null;

    public DetailListPanel() {
        $$$setupUI$$$();
        session = ClientBackgroundService.getInstance().getSession();

        initGui();
        fillMainTreeFully();

    }

    DetailListPanel(DetailEntity rootEntity) {
        $$$setupUI$$$();
        session = ClientBackgroundService.getInstance().getSession();

        initGui();
        fillMainTree(rootEntity);
        hideControls();

    }

    DetailListPanel(Session session, DetailEntity rootEntity) {
        $$$setupUI$$$();
        this.session = session;

        initGui();
        fillMainTree(rootEntity);
        hideControls();

    }

    private void fillMainTree(DetailEntity rootEntity) {
        mainTree.setModel(new DefaultTreeModel(new MainWindowUtils(session).fillMainTree(rootEntity)));
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(detailListPanel);

        initListeners();

        initUpdateUserIsActive();

        initMainTree();
    }

    private void initUpdateUserIsActive() {
        noticeInfoButton.addActionListener(notifyUserIsActiveListener);

        refreshSessionButton.addActionListener(notifyUserIsActiveListener);

        addButton.addActionListener(notifyUserIsActiveListener);

        copyButton.addActionListener(notifyUserIsActiveListener);

        editButton.addActionListener(notifyUserIsActiveListener);
    }

    private void hideControls() {
        controlsBar.setVisible(false);
        searchTextField.setVisible(false);
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


        addButton.addActionListener(e -> onAddNewItem());

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

    private void initKeyBindings() {
        registerKeyboardAction(e -> onAddNewItem(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(e -> onCopyButton(),
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(e -> onEditDetail(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(e -> onNoticeInfo(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onAddNewItem() {
        Session newSession = ClientBackgroundService.getInstance().getSession();
        newSession.beginTransaction();

        SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(newSession, null, CREATE_SINGLE);
        selectionDetailWindow.setLocation(FrameUtils
                .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
        selectionDetailWindow.setVisible(true);
        ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();
        if (list != null) {
            if (list.size() == 1) {
                DetailEntity entity = list.get(0);
                if (entity != null) {
                    DetailService service = new DetailServiceImpl(newSession);
                    final DetailEntity detailByIndex = service.getDetailByCode(entity.getCode());
                    if (detailByIndex == null) {
                        entity.setActive(true);
                        entity.setCode(entity.getCode().toUpperCase());

                        ClientMainWindow mainWindow = new MainWindowUtils(newSession).getClientMainWindow(this);
                        if (mainWindow != null) {
                            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                            try {
                                mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(newSession, entity, true), true);
                            } catch (IllegalStateException e) {
                                closeSession(newSession);
                                log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                                JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                                        "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            closeSession(newSession);
                        }
                    } else {
                        closeSession(newSession);
                    }
                } else {
                    closeSession(newSession);
                }
            } else {
                closeSession(newSession);
            }
        } else {
            closeSession(newSession);
        }
    }

    private void closeSession(Session newSession) {
        try {
            log.debug("Closing session");
            newSession.close();
            log.info("Session successfully closed");
        } catch (HibernateException e) {
            log.warn("Could not close session", e);
        }
    }

    private void onCopyButton() {
        Session newSession = ClientBackgroundService.getInstance().getSession();
        newSession.beginTransaction();

        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedEntity != null) {
            if (selectedEntity.isUnit()) {
                SelectDetailEntityWindow selectionDetailWindow = new SelectDetailEntityWindow(newSession, null, COPY);
                selectionDetailWindow.setLocation(FrameUtils
                        .getFrameOnCenter(FrameUtils.findWindow(this), selectionDetailWindow));
                selectionDetailWindow.setVisible(true);
                ArrayList<DetailEntity> list = selectionDetailWindow.getEntities();

                if (list != null) {
                    if (list.size() == 1) {
                        DetailEntity entity = list.get(0);
                        if (entity != null) {
                            entity.setUnit(true);
                            entity.setActive(true);

                            ClientMainWindow mainWindow = new MainWindowUtils(newSession).getClientMainWindow(this);

                            if (mainWindow != null) {
                                ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                                try {
                                    mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(newSession, entity, selectedEntity), true);
                                } catch (IllegalStateException e) {
                                    closeSession(newSession);
                                    log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                                    JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                                            "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                                }
                            } else {
                                closeSession(newSession);
                            }
                        } else {
                            closeSession(newSession);
                        }
                    } else {
                        closeSession(newSession);
                    }
                } else {
                    closeSession(newSession);
                }
            } else {
                closeSession(newSession);
            }
        } else {
            closeSession(newSession);
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


                    if (searchThread == null) {
                        searchThread = new Thread(() -> fillMainTreeBySearch(searchText));
                        searchThread.start();
                    } else {
                        if (searchThread.isAlive()) {
                            log.debug("Interrupting search thread");
                            searchThread.interrupt();
                        }
                        searchThread = new Thread(() -> fillMainTreeBySearch(searchText));
                        searchThread.start();
                    }
                } else {
                    if (searchThread == null) {
                        searchThread = new Thread(() -> fillMainTreeFully());
                        searchThread.start();
                    } else {
                        if (searchThread.isAlive()) {
                            log.debug("Interrupting search thread");
                            searchThread.interrupt();
                        }
                        searchThread = new Thread(() -> fillMainTreeFully());
                        searchThread.start();
                    }
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
        Thread thread = Thread.currentThread();
        if (session != null && !thread.isInterrupted()) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            final List<DetailListEntity> askedListRoot = service.listDetailLists();
            if (!thread.isInterrupted()) {
                askedListRoot.sort((o1, o2) -> ComparisonChain.start()
                        .compare(o1.getDetailByParentDetailId().getCode(), o2.getDetailByParentDetailId().getCode())
                        .compareTrueFirst(o1.isActive(), o2.isActive())
                        .result());
            }

            if (!thread.isInterrupted()) {
                final DefaultMutableTreeNode detailListFullTree = new MainWindowUtils(session).getModuleDetailListFullTree(askedListRoot);
                mainTree.setModel(new DefaultTreeModel(detailListFullTree));
            }
        }

        if (thread.isInterrupted()) {
            mainTree.setModel(new DefaultTreeModel(null));
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
        final MainWindowUtils mainWindowUtils = new MainWindowUtils(session);
        mainWindowUtils.setClientMainWindow(this);
        if (ClientBackgroundService.getInstance().isConnected()) {
            new Thread(() -> {
                refreshSessionButton.setEnabled(false);
                searchTextField.setEnabled(false);

                mainWindowUtils.updateMessage("/img/gui/animated/sync.gif", "Обновляем список деталей...");

                updateDetailInfoPanelWithEmptyEntity();

                final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
                mainTree.setModel(model);

                ClientBackgroundService.getInstance().refreshSession(session, DetailListEntity.class);
                fillMainTreeFully();

                mainWindowUtils.updateMessage(null, null);
                searchTextField.setEnabled(true);
                refreshSessionButton.setEnabled(true);
                searchTextField.setText("");

            }).start();
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

    private void fillMainTreeBySearch(final String searchText) {
        final Thread thread = Thread.currentThread();
        if (session != null && !thread.isInterrupted()) {
            DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
            final List<DetailListEntity> detailListBySearch = service.getDetailListBySearch(searchText);

            if (!thread.isInterrupted()) {
                if (detailListBySearch != null && detailListBySearch.size() > 0) {
                    final DefaultMutableTreeNode moduleDetailListTreeByEntityList = new MainWindowUtils(session).getModuleDetailListTreeByEntityList(detailListBySearch);
                    if (!thread.isInterrupted()) {
                        mainTree.setModel(new DefaultTreeModel(moduleDetailListTreeByEntityList));
                    }
                } else {
                    DetailService detailService = new DetailServiceImpl(session);
                    final List<DetailEntity> detailsBySearch = detailService.getDetailsBySearch(searchText);

                    if (!thread.isInterrupted()) {
                        if (detailsBySearch != null && detailsBySearch.size() > 0) {
                            final TreeNode detailsTreeByDetails = new MainWindowUtils(session).getDetailsTreeByDetails(detailsBySearch);
                            if (!thread.isInterrupted()) {
                                mainTree.setModel(new DefaultTreeModel(detailsTreeByDetails));
                            }
                        } else {
                            DetailTitleService detailTitleService = new DetailTitleServiceImpl(session);
                            final List<DetailTitleEntity> detailTitlesBySearch = detailTitleService.getDetailTitlesBySearch(searchText);
                            if (!thread.isInterrupted()) {
                                if (detailTitlesBySearch != null) {
                                    List<DetailEntity> resultDetails = new ArrayList<>();
                                    if (!thread.isInterrupted()) {
                                        for (DetailTitleEntity e : detailTitlesBySearch) {
                                            if (e != null && !thread.isInterrupted()) {
                                                final List<DetailEntity> detailsByTitle = detailService.getDetailsByTitle(e);

                                                resultDetails.addAll(detailsByTitle);
                                            }
                                        }
                                        final TreeNode detailsTreeByDetails = new MainWindowUtils(session).getDetailsTreeByDetails(resultDetails);
                                        if (!thread.isInterrupted()) {
                                            mainTree.setModel(new DefaultTreeModel(detailsTreeByDetails));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (thread.isInterrupted()) {
            mainTree.setModel(new DefaultTreeModel(null));
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

            if (selectedComponent != null) {
                if (selectedComponent.getImagePath() == null) {
                    loadImageFromFtp(selectedComponent);
                } else {
                    if (selectedComponent.getImagePath().equalsIgnoreCase(IMAGE_REMOVE_KEY)) {
                        setEmptyImageIcon();
                    } else {
                        loadImageFromLocalStorage(selectedComponent);
                    }
                }
            } else {
                setEmptyImageIcon();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void loadImageFromLocalStorage(DetailEntity selectedComponent) {
        final String imagePath = selectedComponent.getImagePath();
        File file = new File(imagePath);
        if (file.exists()) {
            final boolean jpg = file.getAbsolutePath().toLowerCase().endsWith("jpg");
            if (jpg) {
                final BufferedImage bufferedImage = CommonUtils.getBufferedImage(file);
                if (bufferedImage != null) {
                    updateDetailIconByImage(selectedComponent, bufferedImage);
                } else {
                    setEmptyImageIcon();
                }
            }
        }
    }

    private void loadImageFromFtp(DetailEntity selectedComponent) {
        FtpUtils ftp = FtpUtils.getInstance();
        BufferedImage image = ftp.getImage(selectedComponent.getId());

        if (image != null) {
            updateDetailIconByImage(selectedComponent, image);
        } else {
            setEmptyImageIcon();
        }
    }

    private void updateDetailIconByImage(DetailEntity selectedComponent, BufferedImage image) {
        BufferedImage scaledImage = Scalr.resize(image, 128);

        DetailEntity current = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedComponent.equals(current)) {// prevents setting image for not current selected DetailEntity (fixes time delay)
            detailIconLabel.setIcon(new ImageIcon(scaledImage));
            detailIconLabel.setText("");
            detailIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            detailIconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    FrameUtils.onShowImage(FrameUtils.findWindow(DetailListPanel.super.getRootPane()), false,
                            image, "Изображение " + selectedComponent.getCode() + " "
                                    + selectedComponent.getDetailTitleByDetailTitleId().getTitle());
                }
            });
        }
    }

    private void setEmptyImageIcon() {
        detailIconLabel.setIcon(null);
        detailIconLabel.setText("Нет изображения");
        detailIconLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

                    String longProfile = entity.getMaterialByMaterialId().getLongProfile();
                    String longMark = entity.getMaterialByMaterialId().getLongMark();
                    String delimiter = CommonWindowUtils.createDelimiter(longProfile, longMark);
                    String tooltipText = "<html> <p style=\"line-height: 0.2em;\">" + longProfile + "<br>" + delimiter + "<br>" + longMark + "</p></html>";
                    materialMarkLabel.setToolTipText(tooltipText);
                    materialProfileLabel.setToolTipText(tooltipText);
                }
            }
            initMaterialLabelListener(materialList);
        }
    }

    private void clearDetailMaterialInfo() {
        materialLabel.setText("Материал:");
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

            int counter = 0;
            for (MaterialListEntity e : materialList) {
                if (e.isActive()) {
                    counter++;
                }
            }
            if (counter > 1) {
                materialLabel.setText("Материал (" + counter + "):");
            } else materialLabel.setText("Материал:");
            materialMarkLabel.addMouseListener(adapter);
            materialProfileLabel.addMouseListener(adapter);
        }
    }

    private void initMainTree() {
        DetailJTree jTree = (DetailJTree) mainTree;
        jTree.setSession(session);

        mainTree.addTreeSelectionListener(e -> {
            DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
            if (selectedEntity != null) {
                DetailServiceImpl detailService = new DetailServiceImpl(new DetailDaoImpl(session));
                DetailEntity loadedEntity = detailService.getDetailById(selectedEntity.getId());

                updateDetailInfoPanel(loadedEntity);
            }
        });

        MouseListener ml = new MainWindowUtils(session).getMouseListener(mainTree);
        mainTree.addMouseListener(ml);
        KeyListener k1 = new MainWindowUtils(session).getArrowKeyListener(mainTree);
        mainTree.addKeyListener(k1);

    }

    private void onEditDetail(boolean select) {
        Session newSession = ClientBackgroundService.getInstance().getSession();
        newSession.beginTransaction();

        final DetailEntity entityFromTree = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (entityFromTree != null) {
            ClientMainWindow mainWindow = new MainWindowUtils(newSession).getClientMainWindow(this);
            if (mainWindow != null) {
                final UsersEntity currentUser = mainWindow.getCurrentUser();
                if (currentUser != null) {
                    if ((currentUser.isEditor() || currentUser.isAdmin()) && currentUser.isActive()) {
                        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/noticeEdit16.png")));
                        try {
                            mainWindow.addTab("Редактирование извещения", icon, new EditNoticePanel(newSession, entityFromTree), select);
                        } catch (IllegalStateException e) {
                            closeSession(newSession);
                            log.warn("User tried to add transactional tab ({}), but transaction is already active", EditNoticePanel.class.getName(), e);
                            JOptionPane.showMessageDialog(this, "Нельзя открыть тракзационную вкладку\n" +
                                    "т.к. нельзя редактировать 2 извещения одновременно.", "Ошибка добавления вкладки", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        closeSession(newSession);
                    }
                } else {
                    closeSession(newSession);
                }
            } else {
                closeSession(newSession);
            }
        } else {
            closeSession(newSession);
        }
    }

    private void createUIComponents() {
        mainTree = new DetailJTree();
        searchTextField = new PlaceholderTextField();
        ((PlaceholderTextField) searchTextField).setPlaceholder("Поиск");
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


    private void notifyUserIsActive() {
        final Window window = FrameUtils.findWindow(this);
        if (window instanceof ClientMainWindow) {
            ClientMainWindow clientMainWindow = (ClientMainWindow) window;
            clientMainWindow.resetUnlockedSeconds();
        }
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
        label1.setText("Обозначение:");
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
        noticeInfoButton.setToolTipText("Информация о извещениях (CTRL+I)");
        detailInfoPanel.add(noticeInfoButton, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        detailInfoPanel.add(spacer1, new GridConstraints(2, 2, 11, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        materialLabel = new JLabel();
        materialLabel.setText("Материал:");
        detailInfoPanel.add(materialLabel, new GridConstraints(7, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        detailInfoPanel.add(panel2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.add(searchTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        detailInfoPanel.add(spacer2, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        materialMarkLabel = new JLabel();
        materialMarkLabel.setText(" ");
        detailInfoPanel.add(materialMarkLabel, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialProfileLabel = new JLabel();
        materialProfileLabel.setText(" ");
        detailInfoPanel.add(materialProfileLabel, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        detailListPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Узлы"));
        mainTree.setFocusable(true);
        mainTree.setRootVisible(false);
        scrollPane1.setViewportView(mainTree);
        controlsBar = new JPanel();
        controlsBar.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        detailListPanel.add(controlsBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(0);
        controlsBar.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        refreshSessionButton = new JButton();
        refreshSessionButton.setDisabledIcon(new ImageIcon(getClass().getResource("/img/application/loading2.gif")));
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
        addButton.setToolTipText("Добавить узел (INSERT)");
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
    }
}
