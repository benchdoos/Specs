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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.gui.common.utils.PlaceholderTextField;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mmz.specs.application.core.ApplicationConstants.NO_DATA_STRING;

public class SelectMultipleDetails extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final Session session;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<DetailEntity> mainList;
    private JLabel unitLabel;
    private JLabel weightLabel;
    private JLabel activeLabel;
    private JScrollPane scrollPane;
    private JTextField searchTextField;
    private JButton importButton;

    private ArrayList<DetailEntity> selectedDetailEntities;

    SelectMultipleDetails(Session session) {
        this.session = session;

        $$$setupUI$$$();
        initGui();

        initListeners();

        initKeyBindings();

        initSearchTextField();

        initList();

        fillList();

        pack();
        setMinimumSize(getSize());
    }

    private void fillList() {
        DefaultListModel<DetailEntity> model = new DefaultListModel<>();

        DetailService service = new DetailServiceImpl(session);
        ArrayList<DetailEntity> list = (ArrayList<DetailEntity>) service.listDetails();

        Collections.sort(list);

        for (DetailEntity entity : list) {
            model.addElement(entity);
        }
        mainList.setModel(model);
    }

    private void initList() {
        mainList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailEntity) {
                    DetailEntity entity = (DetailEntity) value;
                    final String info = entity.getCode() + " " + CommonUtils.substring(20, entity.getDetailTitleByDetailTitleId().getTitle());
                    return super.getListCellRendererComponent(list, info, index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        mainList.addListSelectionListener(e -> {
            JList list = (JList) e.getSource();
            int selected = list.getSelectedIndex();

            if (selected > 0 && selected < list.getModel().getSize()) {
                final DetailEntity current = mainList.getModel().getElementAt(selected);
                fillDetailInfo(current);
            }

        });
    }

    private void fillDetailInfo(DetailEntity current) {
        if (current != null) {
            unitLabel.setText(current.isUnit() ? "да" : "нет");
            weightLabel.setText(
                    (current.getWorkpieceWeight() != null ? current.getWorkpieceWeight() + "" : "0") + "; " +
                            (current.getFinishedWeight() != null ? current.getFinishedWeight() + "" : "0"));
            activeLabel.setText(current.isUnit() ? "да" : "нет");
        } else {
            unitLabel.setText(NO_DATA_STRING);
            weightLabel.setText(NO_DATA_STRING);
            activeLabel.setText(NO_DATA_STRING);
        }
    }

    private void initSearchTextField() {
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                DefaultListModel<DetailEntity> model = (DefaultListModel<DetailEntity>) mainList.getModel();
                for (int i = 0; i < model.size(); i++) {
                    final DetailEntity entity = model.getElementAt(i);
                    if (entity.getCode().toUpperCase().contains(searchTextField.getText().replace(",", ".").toUpperCase())) {
                        mainList.ensureIndexIsVisible(i);
                        break;
                    }
                }
            }
        });
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onPaste() {
        try {
            String data = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (data != null && !data.isEmpty()) {
                ArrayList<String> detailCodes = getDetailCodesFromString(data);
                ArrayList<String> broken = new ArrayList<>();
                ArrayList<Integer> indexes = new ArrayList<>();
                for (String string : detailCodes) {
                    DefaultListModel<DetailEntity> model = (DefaultListModel<DetailEntity>) mainList.getModel();
                    boolean isOk = addIndex(indexes, string, model);
                    if (!broken.contains(string) && !isOk) {
                        broken.add(string);
                    }
                }

                selectIndexes(indexes);

                if (broken.size() > 0) {
                    showBrokenCodesWarning(broken);
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            log.warn("Could not get details from clipboard", e);
        }
    }

    private void selectIndexes(ArrayList<Integer> indexes) {
        Integer[] integers = indexes.toArray(new Integer[0]);
        int[] realIndexes = Arrays.stream(integers).mapToInt(Integer::intValue).toArray();

        mainList.setSelectedIndices(realIndexes);
        if (realIndexes.length > 1) {
            mainList.ensureIndexIsVisible(realIndexes[realIndexes.length - 1]);
        }
    }

    private boolean addIndex(ArrayList<Integer> indexes, String string, DefaultListModel<DetailEntity> model) {
        boolean isOk = false;
        for (int i = 0; i < model.size(); i++) {
            final DetailEntity entity = model.getElementAt(i);

            final String code = entity.getCode();

            if (code.equalsIgnoreCase(string)) {
                indexes.add(i);
                isOk = true;
            }
        }
        return isOk;
    }

    private void showBrokenCodesWarning(ArrayList<String> broken) {
        StringBuilder builder = new StringBuilder();
        for (String s : broken) {
            builder.append(s);
            builder.append("\n");
        }
        JOptionPane.showMessageDialog(this, "Не удалось найти обозначения:\n" +
                builder.toString(), "Ошибка во время выбора нескольких деталей", JOptionPane.WARNING_MESSAGE);
    }

    private ArrayList<String> getDetailCodesFromString(String data) {
        ArrayList<String> result = new ArrayList<>();
        log.debug("Got data from clipboard: {}", data);
        if (data != null) {
            final String[] split = data.split("\n");
            for (String code : split) {
                code = code.replace(" ", "");
                result.add(code);
            }
        }
        return result;
    }

    private void initListeners() {
        importButton.addActionListener(e -> onPaste());

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Выбор нескольких деталей");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/tree/someDetails.png")));
    }

    private void onOK() {
        final List<DetailEntity> selectedValuesList = mainList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty()) {
            final ArrayList<DetailEntity> selectedValuesList1 = (ArrayList<DetailEntity>) selectedValuesList;
            if (selectedValuesList1.size() > 1) {
                selectedDetailEntities = selectedValuesList1;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Выеберете как минимум 2 детали",
                        "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            selectedDetailEntities = null;
            dispose();
        }
    }

    private void onCancel() {
        selectedDetailEntities = null;
        dispose();
    }

    ArrayList<DetailEntity> getSelectedDetailEntities() {
        return selectedDetailEntities;
    }

    private void createUIComponents() {
        searchTextField = new PlaceholderTextField();
        ((PlaceholderTextField) searchTextField).setPlaceholder("Поиск");
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
        panel3.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(230, 300), new Dimension(230, 300), null, 0, false));
        scrollPane = new JScrollPane();
        panel4.add(scrollPane, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mainList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        mainList.setModel(defaultListModel1);
        scrollPane.setViewportView(mainList);
        searchTextField.setToolTipText("Пролистывает список деталей до искомого текста");
        panel4.add(searchTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        importButton = new JButton();
        importButton.setBorderPainted(false);
        importButton.setContentAreaFilled(false);
        importButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/import16.png")));
        importButton.setMargin(new Insets(2, 2, 2, 2));
        importButton.setText("");
        importButton.setToolTipText("Импортирует номера деталей с буфера обмена");
        panel4.add(importButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Узел:");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Масса, норма:");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("нет данных");
        panel6.add(unitLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(300, -1), new Dimension(300, -1), new Dimension(300, -1), 0, false));
        weightLabel = new JLabel();
        weightLabel.setText("нет данных");
        panel6.add(weightLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        activeLabel = new JLabel();
        activeLabel.setText("нет данных");
        panel6.add(activeLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Активна:");
        panel5.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
