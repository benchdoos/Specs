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

import com.google.gson.JsonObject;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.gui.common.DetailSptJTree;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.gui.common.utils.PlaceholderTextField;
import com.mmz.specs.application.gui.panels.service.MaterialPanel;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.io.IOConstants;
import com.mmz.specs.io.SPTreeIOManager;
import com.mmz.specs.io.formats.SPTFileFormat;
import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.io.utils.ImportSPTUtils;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialEntity;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class SptFileViewPanel extends JPanel implements Cleanable {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static final int MAXIMUM_STRING_LENGTH = 35;

    private JPanel contentPane;
    private JTree tree;
    private JTextField searchTextField;
    private JLabel imageLabel;
    private JLabel titleLabel;
    private JLabel unitLabel;
    private JLabel finishedWeightLabel;
    private JLabel workpieceWeightLabel;
    private JLabel materialTextLabel;
    private MaterialPanel materialPanel;
    private JLabel codeLabel;
    private File folder;
    private File jsonFile;
    private JsonObject rootJsonObject;

    public SptFileViewPanel(File folder) {
        $$$setupUI$$$();
        this.folder = folder;

        try {
            initBusinessLogic();
            printTreeInformation();
            initGui();

        } catch (Exception e) {
            log.warn("Could not open SPT folder: {}", folder, e);
            JOptionPane.showMessageDialog(this, "Не удалось открыть файл\n" +
                    e.getLocalizedMessage(), "Ошибка открытия", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initGui() {
        setLayout(new GridLayout());
        add(contentPane);

        initTree();
        initTreeListeners();
    }

    private void initTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        final DefaultMutableTreeNode node = ImportSPTUtils.getDefaultTreeModelFromJsonObject(root, rootJsonObject.get(IOConstants.TREE).getAsJsonArray());
        DefaultTreeModel model = new DefaultTreeModel(node);
        tree.setModel(model);
        tree.setRootVisible(false);
    }

    private void initTreeListeners() {
        tree.addTreeSelectionListener(e -> {
            TreeSPTRecord treeSPTRecord = JTreeUtils.getSelectedTreeSptRecord(tree);
            updateInfoAboutSelectedRecord(treeSPTRecord);
        });
    }

    private void updateInfoAboutSelectedRecord(TreeSPTRecord treeSPTRecord) {
        if (treeSPTRecord == null) {
            clearInfo();
        } else {
            fillInfo(treeSPTRecord);
        }
    }

    private void fillInfo(TreeSPTRecord treeSPTRecord) {
        final String NO_DATA_STRING = "нет данных";

        DetailEntity detailEntity = treeSPTRecord.getDetail();

        codeLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, detailEntity.getCode()));

        if (detailEntity.getDetailTitleByDetailTitleId() != null) {
            String title = detailEntity.getDetailTitleByDetailTitleId().getTitle();
            titleLabel.setToolTipText(title);
            titleLabel.setText(CommonUtils.substring(MAXIMUM_STRING_LENGTH, title));
        }

        unitLabel.setText(Boolean.toString(detailEntity.isUnit())
                .replace("false", "нет").replace("true", "да"));
        if (detailEntity.getWorkpieceWeight() != null && !detailEntity.isUnit()) {
            workpieceWeightLabel.setText((Double.toString(detailEntity.getWorkpieceWeight())));
        } else {
            workpieceWeightLabel.setText(NO_DATA_STRING);
        }

        if (detailEntity.getFinishedWeight() != null && !detailEntity.isUnit()) {
            finishedWeightLabel.setText((Double.toString(detailEntity.getFinishedWeight())));
        } else {
            finishedWeightLabel.setText(NO_DATA_STRING);
        }
        fillMaterialInfoLabel(treeSPTRecord.getMaterials());
    }

    private void fillMaterialInfoLabel(ArrayList<MaterialEntity> materials) {
        clearDetailMaterialInfo();
        materialPanel.setMaxStringSize(MAXIMUM_STRING_LENGTH);
        if (materials != null) {
            if (materials.size() > 0) {
                materialPanel.setMaterialEntity(materials.get(0));
            } else {
                materialTextLabel.setText("Материал:");
                materialPanel.setMaterialEntity(null);
            }
        }
    }

    private void clearInfo() {
        final String NO_DATA_STRING = "нет данных";
        codeLabel.setText(NO_DATA_STRING);
        titleLabel.setText(NO_DATA_STRING);
        unitLabel.setText(NO_DATA_STRING);
        workpieceWeightLabel.setText(NO_DATA_STRING);
        finishedWeightLabel.setText(NO_DATA_STRING);
        clearDetailMaterialInfo();
    }

    private void clearDetailMaterialInfo() {
        materialTextLabel.setText("Материал:");
        materialPanel.setMaterialEntity(null);
        initMaterialLabelListener(null);
    }

    private void initMaterialLabelListener(ArrayList<MaterialEntity> entities) {

    }

    private void initBusinessLogic() throws IOException {
        jsonFile = new File(folder.getAbsolutePath() + File.separator + SPTreeIOManager.JSON_FILE_NAME);
        log.info("Loading json tree from: {}", jsonFile);
        JsonObject object = SPTreeIOManager.loadJsonFromFile(jsonFile);
        final String string;
        try {
            string = object.get(IOConstants.TYPE).getAsString();
        } catch (JSONException e) {
            throw new IOException("JSON file " + jsonFile + " is not an STP file!");
        }

        if (string.equalsIgnoreCase(SPTFileFormat.DEFAULT_TREE_TYPE)) {
            rootJsonObject = object;
        } else {
            throw new IOException("JSON file " + jsonFile + " is not an STP file!");
        }
        log.info("Successfully loaded json tree from: {}, size is: {}", jsonFile, rootJsonObject.size());
    }

    private void printTreeInformation() {
        final String type = rootJsonObject.get(IOConstants.TYPE).getAsString();
        Date date = new Date(rootJsonObject.get(IOConstants.TIMESTAMP).getAsLong());
        final String author = rootJsonObject.get(IOConstants.AUTHOR).getAsString();
        log.info("File {} information:", jsonFile);
        log.info("Type: {}, Author: {}, Date: {}", type, author, date);
    }

    @Override
    public void clean() {
        try {
            log.info("Deleting directory: {}", folder);
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            log.warn("Could not delete tmp folder: {}", folder, e);
        }
    }

    private void createUIComponents() {
        searchTextField = new PlaceholderTextField();
        ((PlaceholderTextField) searchTextField).setPlaceholder("Поиск");
        tree = new DetailSptJTree();
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.add(searchTextField, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Обозначение:");
        panel2.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        codeLabel = new JLabel();
        codeLabel.setText("нет данных");
        panel2.add(codeLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        imageLabel = new JLabel();
        imageLabel.setForeground(new Color(-10395295));
        imageLabel.setHorizontalAlignment(0);
        imageLabel.setText("нет изображения");
        panel2.add(imageLabel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Наименование:");
        panel2.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Узел:");
        panel2.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Масса готовой детали:");
        panel2.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Норма расхода:");
        panel2.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleLabel = new JLabel();
        titleLabel.setText("нет данных");
        panel2.add(titleLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("нет данных");
        panel2.add(unitLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finishedWeightLabel = new JLabel();
        finishedWeightLabel.setText("нет данных");
        panel2.add(finishedWeightLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        workpieceWeightLabel = new JLabel();
        workpieceWeightLabel.setText("нет данных");
        panel2.add(workpieceWeightLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialTextLabel = new JLabel();
        materialTextLabel.setText("Материал:");
        panel2.add(materialTextLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        materialPanel = new MaterialPanel();
        panel2.add(materialPanel.$$$getRootComponent$$$(), new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 40), null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Узлы"));
        scrollPane1.setViewportView(tree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
