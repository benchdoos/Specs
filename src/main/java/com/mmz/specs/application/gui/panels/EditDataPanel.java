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
import com.mmz.specs.application.gui.client.CreateMaterialWindow;
import com.mmz.specs.application.gui.client.EditTitleWindow;
import com.mmz.specs.application.gui.common.utils.Renders;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.dao.MaterialDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import static com.mmz.specs.application.core.ApplicationConstants.NO_DATA_STRING;

public class EditDataPanel extends JPanel implements AccessPolicy, Transactional {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane;
    private JPanel editTitlesPanel;
    private JList<DetailTitleEntity> titleList;
    private JButton addTitleItemButton;
    private JButton removeTitleItemButton;
    private JLabel titleUsageCountLabel;
    private JLabel titleActiveLabel;
    private JLabel titleNameLabel;
    private JButton editTitleButton;
    private JButton findTitleUsageButton;
    private JList<MaterialEntity> materialList;
    private JButton addMaterialItemButton;
    private JButton removeMaterialItemButton;
    private JButton editMaterialItemButton;
    private JLabel longMarkLabel;
    private JLabel longProfileLabel;
    private JLabel shortMarkLabel;
    private JLabel shortProfileLabel;
    private JLabel activeMarkLabel;
    private JPanel editMaterialsPanel;
    private JPanel editDetailsPanel;
    private JLabel detailCodeAndTitleLabel;
    private JLabel usedCountLabel;
    private JLabel activeAndInterchangableLabel;
    private JList detailsList;
    private ActionListener notifyUserIsActiveListener = FrameUtils.getNotifyUserIsActiveActionListener(this);

    public EditDataPanel() {
        this.session = ClientBackgroundService.getInstance().getSession();
        session.getTransaction().begin();
        initGui();

        initListeners();

    }

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        initEditTitleTab();

        initEditMaterialTab();

        initUpdateUserIsActiveListeners();

    }

    private void initUpdateUserIsActiveListeners() {
        tabbedPane.addChangeListener(e -> notifyUserIsActiveListener.actionPerformed(null));

        titleList.addListSelectionListener(e -> notifyUserIsActiveListener.actionPerformed(null));
        materialList.addListSelectionListener(e -> notifyUserIsActiveListener.actionPerformed(null));

        addTitleItemButton.addActionListener(notifyUserIsActiveListener);
        removeTitleItemButton.addActionListener(notifyUserIsActiveListener);

        editTitleButton.addActionListener(notifyUserIsActiveListener);
        findTitleUsageButton.addActionListener(notifyUserIsActiveListener);

        addMaterialItemButton.addActionListener(notifyUserIsActiveListener);
        removeMaterialItemButton.addActionListener(notifyUserIsActiveListener);
        editMaterialItemButton.addActionListener(notifyUserIsActiveListener);
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initTitleList() {
        final DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
            //fixme color does not represent
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                setOpaque(true);
                DetailTitleEntity entity = (DetailTitleEntity) value;
                if (!entity.isActive()) {
                    if (isSelected) {
                        setBackground(Color.GREEN.brighter().brighter());
                    } else {
                        setBackground(Color.GREEN.darker().darker());
                    }
                }
                return super.getListCellRendererComponent(list, entity.getTitle(), index, isSelected, cellHasFocus);
            }
        };

        titleList.setCellRenderer(cellRenderer);
        titleList.addListSelectionListener(e -> updateTitleInfo());
    }

    private void updateTitleInfo() {
        final DetailTitleEntity selectedValue = titleList.getSelectedValue();
        if (selectedValue != null) {
            titleNameLabel.setText(selectedValue.getTitle());
            titleActiveLabel.setText(selectedValue.isActive() ? "да" : "нет");
            DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
            final ArrayList<DetailEntity> detailsByTitle = (ArrayList<DetailEntity>) service.getDetailsByTitle(selectedValue);
            if (detailsByTitle != null) {
                titleUsageCountLabel.setText(detailsByTitle.size() + "");
            } else {
                titleUsageCountLabel.setText("0");
            }
        } else {
            titleNameLabel.setText(NO_DATA_STRING);
            titleActiveLabel.setText(NO_DATA_STRING);
            titleUsageCountLabel.setText(NO_DATA_STRING);
        }
    }

    private void fillTitleList() {
        DefaultListModel<DetailTitleEntity> model = new DefaultListModel<>();

        DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
        final ArrayList<DetailTitleEntity> detailTitleEntities = (ArrayList<DetailTitleEntity>) service.listDetailTitles();
        detailTitleEntities.sort((o1, o2) -> {
            if (o2 != null) {
                return ComparisonChain.start()
                        .compareTrueFirst(o1.isActive(), o2.isActive())
                        .compare(o1.getTitle(), o2.getTitle())
                        .result();
            } else {
                return -1;
            }
        });

        for (DetailTitleEntity entity : detailTitleEntities) {
            model.addElement(entity);
        }
        titleList.setModel(model);
    }

    private void initEditTitleTab() {
        initTitleList();
        fillTitleList();
        initEditTitleTabButtons();
    }

    private void initEditTitleTabButtons() {
        addTitleItemButton.addActionListener(e -> {
            DetailTitleEntity titleEntity = new CommonWindowUtils(session).onCreateNewTitle(this);
            if (titleEntity != null) {
                final DefaultListModel<DetailTitleEntity> model = (DefaultListModel<DetailTitleEntity>) titleList.getModel();
                model.addElement(titleEntity);

                fillTitleList();

                titleList.setSelectedValue(titleEntity, true);
            }
        });

        removeTitleItemButton.addActionListener(e -> {
            final DetailTitleEntity selectedValue = titleList.getSelectedValue();
            final int selectedIndex = titleList.getSelectedIndex();
            if (selectedValue != null) {
                DetailService service = new DetailServiceImpl(new DetailDaoImpl(session));
                final ArrayList<DetailEntity> detailsByTitle = (ArrayList<DetailEntity>) service.getDetailsByTitle(selectedValue);
                if (detailsByTitle.isEmpty()) {
                    new DetailTitleServiceImpl(new DetailTitleDaoImpl(session)).removeDetailTitle(selectedValue.getId());

                    final DefaultListModel<DetailTitleEntity> model = (DefaultListModel<DetailTitleEntity>) titleList.getModel();
                    model.removeElement(selectedValue);
                    if (selectedIndex > 0 && selectedIndex < model.getSize() - 1) {
                        titleList.setSelectedIndex(titleList.getModel().getSize() - 1);
                    } else if (selectedIndex == 0 && model.getSize() > 0) {
                        titleList.setSelectedIndex(0);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Нельзя удалить элемент, который используется в базе", "Ошибка удаления",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        editTitleButton.addActionListener(e -> {
            final DetailTitleEntity selectedValue = titleList.getSelectedValue();
            final int selectedIndex = titleList.getSelectedIndex();
            if (selectedValue != null) {
                EditTitleWindow editTitleWindow = new EditTitleWindow(session, selectedValue);
                editTitleWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), editTitleWindow));
                editTitleWindow.setVisible(true);
                DetailTitleEntity titleEntity = editTitleWindow.getDetailTitleEntity();

                final DefaultListModel<DetailTitleEntity> model = (DefaultListModel<DetailTitleEntity>) titleList.getModel();
                model.setElementAt(titleEntity, selectedIndex);

                fillTitleList();

                titleList.setSelectedValue(titleEntity, true);
            }
        });
    }

    private void initEditMaterialTab() {
        initMaterialList();
        fillMaterialList();

        initEditMaterialTabButtons();
    }

    private void fillMaterialList() {
        DefaultListModel<MaterialEntity> model = new DefaultListModel<>();

        MaterialService service = new MaterialServiceImpl(new MaterialDaoImpl(session));
        final ArrayList<MaterialEntity> materialListEntities = (ArrayList<MaterialEntity>) service.listMaterials();

        Collections.sort(materialListEntities);

        for (MaterialEntity e : materialListEntities) {
            model.addElement(e);
        }
        materialList.setModel(model);
    }

    private void fillMaterialInfoLabel() {
        final MaterialEntity selectedValue = materialList.getSelectedValue();
        if (selectedValue != null) {
            longMarkLabel.setText(selectedValue.getLongMark());
            longProfileLabel.setText(selectedValue.getLongProfile());
            shortMarkLabel.setText(selectedValue.getShortMark());
            shortProfileLabel.setText(selectedValue.getShortProfile());
            activeMarkLabel.setText(selectedValue.isActive() ? "да" : "нет");
        } else {
            longMarkLabel.setText(NO_DATA_STRING);
            longProfileLabel.setText(NO_DATA_STRING);
            shortMarkLabel.setText(NO_DATA_STRING);
            shortProfileLabel.setText(NO_DATA_STRING);
            activeMarkLabel.setText(NO_DATA_STRING);
        }
    }

    private void initEditMaterialTabButtons() {
        addMaterialItemButton.addActionListener(e -> {
            CreateMaterialWindow createMaterialWindow = new CreateMaterialWindow(null, session);
            createMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), createMaterialWindow));
            createMaterialWindow.setVisible(true);
            final MaterialEntity materialEntity = createMaterialWindow.getMaterialEntity();

            DefaultListModel<MaterialEntity> model = (DefaultListModel<MaterialEntity>) materialList.getModel();
            model.addElement(materialEntity);
            materialList.setSelectedValue(materialEntity, true);

        });

        removeMaterialItemButton.addActionListener(e -> {
            MaterialEntity materialEntity = materialList.getSelectedValue();
            if (materialEntity != null) {
                if (!new MainWindowUtils(session).containsMaterialEntityInMaterialListEntity(materialEntity)) {
                    MaterialService service = new MaterialServiceImpl(new MaterialDaoImpl(session));
                    service.removeMaterial(materialEntity.getId());
                    DefaultListModel<MaterialEntity> model = (DefaultListModel<MaterialEntity>) materialList.getModel();
                    model.removeElement(materialEntity);
                }
            }

        });

        editMaterialItemButton.addActionListener(e -> {
            MaterialEntity materialEntity = materialList.getSelectedValue();
            if (materialEntity != null) {
                CreateMaterialWindow createMaterialWindow = new CreateMaterialWindow(materialEntity, session);
                createMaterialWindow.setLocation(FrameUtils.getFrameOnCenter(FrameUtils.findWindow(this), createMaterialWindow));
                createMaterialWindow.setVisible(true);
                final MaterialEntity entity = createMaterialWindow.getMaterialEntity();

                DefaultListModel<MaterialEntity> model = (DefaultListModel<MaterialEntity>) materialList.getModel();

                materialList.setSelectedValue(entity, true);
            }
        });
    }

    @Override
    public void setUIEnabled(boolean enable) {
        /*NOP*/
    }

    @Override
    public void rollbackTransaction() {
        onCancel();
    }

    private void onCancel() {
        int result = JOptionPane.showConfirmDialog(FrameUtils.findWindow(this), "Вы точно хотите отменить изменения?\n" +
                "В случае подтверждения все изменения не сохранятся и никак\n" +
                "не повлияют на базу данных.\n" +
                "Отменить изменения?", "Отмена изменений", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        log.debug("User wanted to rollback changes, user's choice is: " + result);
        if (result == 0) {
            session.getTransaction().rollback();
            session.close();
            closeTab();
        }
    }

    private void closeTab() {
        ClientMainWindow clientMainWindow = new MainWindowUtils(session).getClientMainWindow(this);
        if (clientMainWindow != null) {
            clientMainWindow.closeTab(this);
        }
    }

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
                session.getTransaction().commit();
                session.close();
                closeTab();
            } catch (Exception e) {
                log.warn("Could not call commit for transaction", e);
                JOptionPane.showMessageDialog(this,
                        "Не удалось завершить транзакцию\n" + e.getLocalizedMessage(), "Ошибка сохранения",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void initMaterialList() {
        materialList.setCellRenderer(Renders.DEFAULT_LIST_MATERIAL_CELL_RENDERER);
        materialList.addListSelectionListener(e -> fillMaterialInfoLabel());
    }

    @Override
    public AccessPolicyManager getPolicyManager() {
        AccessPolicyManager accessPolicyManager = new AccessPolicyManager(true, false);
        accessPolicyManager.setAvailableOnlyForAdmin(true);
        return accessPolicyManager;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
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
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tabbedPane = new JTabbedPane();
        panel3.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        editTitlesPanel = new JPanel();
        editTitlesPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Наименования", editTitlesPanel);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        editTitlesPanel.add(panel4, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Список наименований:"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        titleList = new JList();
        scrollPane1.setViewportView(titleList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel4.add(toolBar1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addTitleItemButton = new JButton();
        addTitleItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addTitleItemButton.setText("");
        toolBar1.add(addTitleItemButton);
        removeTitleItemButton = new JButton();
        removeTitleItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeTitleItemButton.setText("");
        toolBar1.add(removeTitleItemButton);
        editTitleButton = new JButton();
        editTitleButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editTitleButton.setText("");
        toolBar1.add(editTitleButton);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        editTitlesPanel.add(panel5, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Информация о наименовании:"));
        final JLabel label1 = new JLabel();
        label1.setText("Наименование:");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Активно:");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Использований:");
        panel5.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Ограничение:");
        panel5.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleNameLabel = new JLabel();
        titleNameLabel.setText("нет данных");
        panel5.add(titleNameLabel, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleActiveLabel = new JLabel();
        titleActiveLabel.setText("нет данных");
        panel5.add(titleActiveLabel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleUsageCountLabel = new JLabel();
        titleUsageCountLabel.setText("нет данных");
        panel5.add(titleUsageCountLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findTitleUsageButton = new JButton();
        findTitleUsageButton.setBorderPainted(false);
        findTitleUsageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/search16.png")));
        findTitleUsageButton.setMargin(new Insets(2, 2, 2, 2));
        findTitleUsageButton.setText("");
        findTitleUsageButton.setToolTipText("Поиск использований");
        panel5.add(findTitleUsageButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JTextArea textArea1 = new JTextArea();
        textArea1.setEditable(false);
        Font textArea1Font = UIManager.getFont("Label.font");
        if (textArea1Font != null) textArea1.setFont(textArea1Font);
        textArea1.setText("Длина не более 120 символов");
        scrollPane2.setViewportView(textArea1);
        final Spacer spacer4 = new Spacer();
        editTitlesPanel.add(spacer4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        editMaterialsPanel = new JPanel();
        editMaterialsPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Материалы", editMaterialsPanel);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        editMaterialsPanel.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder("Информация о материале:"));
        final JLabel label5 = new JLabel();
        label5.setText("Марка (полная):");
        panel6.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Марка (сокращенная):");
        panel6.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Активный:");
        panel6.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel6.add(spacer5, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        longMarkLabel = new JLabel();
        longMarkLabel.setText("нет данных");
        panel6.add(longMarkLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shortMarkLabel = new JLabel();
        shortMarkLabel.setText("нет данных");
        panel6.add(shortMarkLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        activeMarkLabel = new JLabel();
        activeMarkLabel.setText("нет данных");
        panel6.add(activeMarkLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Ограничения:");
        panel6.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Профиль (полный):");
        panel6.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        longProfileLabel = new JLabel();
        longProfileLabel.setText("нет данных");
        panel6.add(longProfileLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Профиль (сокращенный):");
        panel6.add(label10, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shortProfileLabel = new JLabel();
        shortProfileLabel.setText("нет данных");
        panel6.add(shortProfileLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel6.add(scrollPane3, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JTextArea textArea2 = new JTextArea();
        textArea2.setEditable(false);
        Font textArea2Font = UIManager.getFont("Label.font");
        if (textArea2Font != null) textArea2.setFont(textArea2Font);
        textArea2.setText("Поля полных данных: 120 символов, коротких:  40,50 символов");
        scrollPane3.setViewportView(textArea2);
        final Spacer spacer6 = new Spacer();
        editMaterialsPanel.add(spacer6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        editMaterialsPanel.add(panel7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder("Список материалов:"));
        final JScrollPane scrollPane4 = new JScrollPane();
        panel7.add(scrollPane4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        materialList = new JList();
        scrollPane4.setViewportView(materialList);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel7.add(toolBar2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addMaterialItemButton = new JButton();
        addMaterialItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/add.png")));
        addMaterialItemButton.setText("");
        toolBar2.add(addMaterialItemButton);
        removeMaterialItemButton = new JButton();
        removeMaterialItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        removeMaterialItemButton.setText("");
        toolBar2.add(removeMaterialItemButton);
        editMaterialItemButton = new JButton();
        editMaterialItemButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/edit.png")));
        editMaterialItemButton.setText("");
        toolBar2.add(editMaterialItemButton);
        editDetailsPanel = new JPanel();
        editDetailsPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Детали", editDetailsPanel);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        editDetailsPanel.add(panel8, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder("Список деталей:"));
        final JScrollPane scrollPane5 = new JScrollPane();
        panel8.add(scrollPane5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        detailsList = new JList();
        scrollPane5.setViewportView(detailsList);
        final JToolBar toolBar3 = new JToolBar();
        toolBar3.setFloatable(false);
        panel8.add(toolBar3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JButton button1 = new JButton();
        button1.setIcon(new ImageIcon(getClass().getResource("/img/gui/edit/remove.png")));
        button1.setText("");
        toolBar3.add(button1);
        final Spacer spacer7 = new Spacer();
        editDetailsPanel.add(spacer7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        editDetailsPanel.add(panel9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel9.setBorder(BorderFactory.createTitledBorder("Информация о детали:"));
        final JLabel label11 = new JLabel();
        label11.setText("Обозначение, наименование:");
        panel9.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Использовано:");
        panel9.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Аннулирована / Взаимозаменяемая:");
        panel9.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel9.add(spacer8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        detailCodeAndTitleLabel = new JLabel();
        detailCodeAndTitleLabel.setText("нет данных");
        panel9.add(detailCodeAndTitleLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usedCountLabel = new JLabel();
        usedCountLabel.setText("нет данных");
        panel9.add(usedCountLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        activeAndInterchangableLabel = new JLabel();
        activeAndInterchangableLabel.setText("нет данных / нет данных");
        panel9.add(activeAndInterchangableLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Ограничения:");
        panel9.add(label14, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane6 = new JScrollPane();
        panel9.add(scrollPane6, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JTextArea textArea3 = new JTextArea();
        textArea3.setEditable(false);
        Font textArea3Font = UIManager.getFont("Label.font");
        if (textArea3Font != null) textArea3.setFont(textArea3Font);
        textArea3.setText("поля полных данных: 120 символов, коротких:  40,50 символов");
        scrollPane6.setViewportView(textArea3);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Извещения", panel10);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Тех. процессы", panel11);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
