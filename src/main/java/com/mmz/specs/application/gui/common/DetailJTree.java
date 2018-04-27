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
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class DetailJTree extends JTree {
    public DetailJTree() {
        final Color backgroundSelectionColor = new Color(0, 120, 215);
        //final Color backgroundNonSelectionColor = new Color(242, 242, 242);
        final Color backgroundNonSelectionColor = new JPanel().getBackground();

        setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) { //todo optimize this!!!
                if (selected) {
                    setBackground(backgroundSelectionColor);
                } else {
                    setBackground(backgroundNonSelectionColor);
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

                                    List<DetailListEntity> result = mainWindowUtils.getDetailListEntitiesByParentAndChild(parent, child);

                                    setOpaque(true);

                                    if (result.size() > 0) {
                                        DetailListEntity detailListEntity = mainWindowUtils.getLatestDetailListEntity(result);
                                        if (detailListEntity.isInterchangeableNode()) {
                                            if (!selected) {
                                                setBackground(Color.GRAY.brighter());
                                            } else {
                                                setBackground(backgroundSelectionColor);
                                            }
                                            setToolTipText("Взаимозаменяемая деталь");
                                        }

                                        if (!detailListEntity.getDetailByChildDetailId().isActive()) {
                                            if (!selected) {
                                                setBackground(Color.RED);
                                            } else {
                                                setBackground(Color.RED.darker());
                                            }
                                        }

                                        String value1 = child.getCode() + " (" + detailListEntity.getQuantity() + ") " + child.getDetailTitleByDetailTitleId().getTitle();
                                        return super.getTreeCellRendererComponent(tree, value1, selected, true, leaf, row, hasFocus);
                                    }
                                }
                            }
                        }
                        return super.getTreeCellRendererComponent(tree,
                                child.getCode() + " " + child.getDetailTitleByDetailTitleId().getTitle(), selected, expanded, leaf, row, hasFocus); //spaces fixes
                        // issue when (detailListEntity.getQuantity()) does not work... fix it some how????
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
        Icon closedIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon openIcon = new ImageIcon(getClass().getResource("/img/gui/tree/unitOpened.png"));
        Icon leafIcon = new ImageIcon(getClass().getResource("/img/gui/tree/detail.png"));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);

        renderer.setBackgroundSelectionColor(backgroundSelectionColor);
        renderer.setBackgroundNonSelectionColor(backgroundNonSelectionColor);

        setCellRenderer(renderer);

    }
}
