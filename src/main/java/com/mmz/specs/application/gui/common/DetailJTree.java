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

package com.mmz.specs.application.gui.common;

import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;

public class DetailJTree extends JTree {
    private static final Color BACKGROUND_SELECTION_COLOR = new Color(0, 120, 215);
    private static final Color BACKGROUND_NON_SELECTION_COLOR = new JPanel().getBackground();

    public DetailJTree() {
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));

        setBackground(BACKGROUND_NON_SELECTION_COLOR);

        DefaultTreeCellRenderer renderer = getRenderer();

        initIcons(renderer);

        renderer.setBackgroundSelectionColor(BACKGROUND_SELECTION_COLOR);
        renderer.setBackgroundNonSelectionColor(BACKGROUND_NON_SELECTION_COLOR);

        setCellRenderer(renderer);

        //setToggleClickCount(1);

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    private DefaultTreeCellRenderer getRenderer() {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) { //todo optimize this!!!
                if (selected) {
                    this.setBackgroundSelectionColor(BACKGROUND_SELECTION_COLOR);
                } else {
                    this.setBackgroundNonSelectionColor(BACKGROUND_NON_SELECTION_COLOR);
                }

                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.getUserObject() instanceof DetailEntity) {
                        DetailEntity child = (DetailEntity) node.getUserObject();
                        if (row >= 0) {
                            if (tree.getPathForRow(row) != null) {
                                Object[] pathForRow = tree.getPathForRow(row).getParentPath().getPath();
                                if (pathForRow.length > 1) {
                                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) pathForRow[pathForRow.length - 1];
                                    DetailEntity parent = (DetailEntity) mutableTreeNode.getUserObject();

                                    MainWindowUtils mainWindowUtils = new MainWindowUtils(ClientBackgroundService.getInstance().getSession());

//                                    List<DetailListEntity> result = mainWindowUtils.getDetailListEntitiesByParentAndChild(parent, child);
                                    DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(ClientBackgroundService.getInstance().getSession()));
                                    List<DetailListEntity> result = service.getDetailListByParentAndChild(parent, child);

                                    if (result.size() > 0) {
                                        DetailListEntity detailListEntity = mainWindowUtils.getLatestDetailListEntity(result);

                                        String data = child.getCode() + " (" + detailListEntity.getQuantity() + ") " + child.getDetailTitleByDetailTitleId().getTitle();
                                        Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, data, selected, true, leaf, row, hasFocus);

                                        if (detailListEntity.isInterchangeableNode()) {
                                            if (!selected) {
                                                this.setBackgroundNonSelectionColor(Color.GRAY.brighter());
                                            } else {
                                                this.setBackgroundSelectionColor(Color.GRAY);
                                            }
                                        }

                                        if (!detailListEntity.getDetailByChildDetailId().isActive()) {
                                            if (!selected) {
                                                this.setBackgroundNonSelectionColor(Color.RED);
                                            } else {
                                                this.setBackgroundSelectionColor(Color.RED.darker());
                                            }
                                        }
                                        return treeCellRendererComponent;
                                    }
                                }
                            }
                        }
                        return super.getTreeCellRendererComponent(tree,
                                child.getCode() + " " + child.getDetailTitleByDetailTitleId().getTitle(), selected, expanded, leaf, row, hasFocus);
                    }
                }
                return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                FontMetrics fm = getFontMetrics(getFont());
                char[] chars = getText().toCharArray();

                int w = getIconTextGap() + 32;
                for (char ch : chars) {
                    w += fm.charWidth(ch);
                }
                w += getText().length();
                dim.width = w;
                return dim;
            }
        };
    }

    private void initIcons(DefaultTreeCellRenderer renderer) {
        Icon closedIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon openIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon leafIcon = new ImageIcon(getClass().getResource("/img/gui/tree/detail.png"));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);
    }
}
