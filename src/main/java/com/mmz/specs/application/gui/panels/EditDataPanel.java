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
import com.mmz.specs.application.gui.client.EditTitleWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import com.mmz.specs.service.DetailTitleService;
import com.mmz.specs.service.DetailTitleServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

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

    public EditDataPanel() {
        this.session = ClientBackgroundService.getInstance().getSession();
        session.getTransaction().begin();
        initGui();

        initListeners();

        initEditTitleTab();

    }

    private void initEditTitleTab() {
        initTitleList();
        fillTitleList();
        initEditTitleTabButtons();
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

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
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
                closeTab();
            } catch (Exception e) {
                log.warn("Could not call commit for transaction", e);
                JOptionPane.showMessageDialog(this,
                        "Не удалось завершить транзакцию\n" + e.getLocalizedMessage(), "Ошибка сохранения",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
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
                }
            }
        });

        editTitleButton.addActionListener(e -> {
            final DetailTitleEntity selectedValue = titleList.getSelectedValue();
            final int selectedIndex = titleList.getSelectedIndex();
            if (selectedValue != null) {
                EditTitleWindow editTitleWindow = new EditTitleWindow(selectedValue);
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
        editTitlesPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Наименования", editTitlesPanel);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        editTitlesPanel.add(panel4, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Список наименований"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        titleList = new JList();
        scrollPane1.setViewportView(titleList);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        editTitlesPanel.add(panel5, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Информация о наименовании"));
        final JLabel label1 = new JLabel();
        label1.setText("Наименование:");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Активно:");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Использований:");
        panel5.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Ограничение:");
        panel5.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 1, 4, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        titleNameLabel = new JLabel();
        titleNameLabel.setText("нет данных");
        panel6.add(titleNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(400, -1), new Dimension(400, -1), 0, false));
        titleActiveLabel = new JLabel();
        titleActiveLabel.setText("нет данных");
        panel6.add(titleActiveLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleUsageCountLabel = new JLabel();
        titleUsageCountLabel.setText("нет данных");
        panel6.add(titleUsageCountLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("120 символов");
        panel6.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findTitleUsageButton = new JButton();
        findTitleUsageButton.setBorderPainted(false);
        findTitleUsageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/search16.png")));
        findTitleUsageButton.setMargin(new Insets(2, 2, 2, 2));
        findTitleUsageButton.setText("");
        findTitleUsageButton.setToolTipText("Поиск использований");
        panel6.add(findTitleUsageButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        editTitlesPanel.add(toolBar1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
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
        final Spacer spacer3 = new Spacer();
        editTitlesPanel.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Материалы", panel7);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Тех. процессы", panel8);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Извещения", panel9);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
