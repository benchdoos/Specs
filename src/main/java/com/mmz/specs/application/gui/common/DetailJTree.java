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

import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class DetailJTree extends JTree {
    private static final Color BACKGROUND_SELECTION_COLOR = new DefaultTreeCellRenderer().getBackgroundSelectionColor();
    private static final Color BACKGROUND_NON_SELECTION_COLOR = new JPanel().getBackground();
    private static final Color FOREGROUND_SELECTED_COLOR = Color.WHITE;
    private static final Color FOREGROUND_NON_SELECTED_COLOR = Color.BLACK;

    private static final Icon UNIT_CLOSED_ICON = new ImageIcon(DetailJTree.class.getResource("/img/gui/tree/unitOpened.png"));
    private static final Icon UNIT_OPENED_ICON = new ImageIcon(DetailJTree.class.getResource("/img/gui/tree/unitOpened.png"));
    private static final Icon DETAIL_ICON = new ImageIcon(DetailJTree.class.getClass().getResource("/img/gui/tree/detail.png"));
    private Session session;

    public DetailJTree() {
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));

        setBackground(BACKGROUND_NON_SELECTION_COLOR);


        DefaultTreeCellRenderer renderer = getRenderer();

        initIcons(renderer);

        renderer.setBackgroundSelectionColor(BACKGROUND_SELECTION_COLOR);
        renderer.setBackgroundNonSelectionColor(BACKGROUND_NON_SELECTION_COLOR);

        setCellRenderer(renderer);

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(e -> FrameUtils.getNotifyUserIsActiveActionListener(this).actionPerformed(null));
    }

    private DefaultTreeCellRenderer getRenderer() {
        return new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) { //todo optimize this!!!
                if (session != null) {
                    DetailListService service = new DetailListServiceImpl(session);

                    if (selected) {
                        this.setBackgroundSelectionColor(BACKGROUND_SELECTION_COLOR);
                    } else {
                        this.setBackgroundNonSelectionColor(BACKGROUND_NON_SELECTION_COLOR);
                    }

                    if (value instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                        if (node.getUserObject() instanceof DetailEntity) {
                            DetailEntity detailEntity = (DetailEntity) node.getUserObject();

                            if (row >= 0) {
                                if (tree.getPathForRow(row) != null) {
                                    Object[] pathForRow = tree.getPathForRow(row).getParentPath().getPath();
                                    if (pathForRow.length > 1) {
                                        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) pathForRow[pathForRow.length - 1];
                                        DetailEntity parent = (DetailEntity) mutableTreeNode.getUserObject();

                                        //                                    List<DetailListEntity> result = service.getDetailListByParentAndChild(parent, detailEntity);

                                        /*if (result.size() > 0) {*/
                                        DetailListEntity detailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, detailEntity);
                                        updateBackgroundColor(selected, detailListEntity);
                                        updateIcon(detailEntity);

                                        if (detailListEntity != null) {
                                            String data = detailEntity.getCode() + " (" + detailListEntity.getQuantity() + ") " + detailEntity.getDetailTitleByDetailTitleId().getTitle();
                                            return super.getTreeCellRendererComponent(tree, data, selected, expanded, leaf, row, hasFocus);
                                        } else {
                                            String data = detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle();
                                            return super.getTreeCellRendererComponent(tree, data, selected, expanded, leaf, row, hasFocus);
                                        }
                                        /*}*/
                                    }
                                }
                            }
                            updateIcon(detailEntity);
                            final String data = detailEntity.getCode() + " " + detailEntity.getDetailTitleByDetailTitleId().getTitle();
                            return super.getTreeCellRendererComponent(tree, data, selected, expanded, leaf, row, hasFocus);
                        }
                    }
                }
                return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }

            private void updateBackgroundColor(boolean selected, DetailListEntity detailListEntity) {
                if (detailListEntity != null) {
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
                }
            }

            private void updateIcon(DetailEntity child) {
                setLeafIcon(child.isUnit() ? UNIT_OPENED_ICON : DETAIL_ICON);
                setOpenIcon(child.isUnit() ? UNIT_OPENED_ICON : DETAIL_ICON);
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
        renderer.setClosedIcon(UNIT_CLOSED_ICON);
        renderer.setOpenIcon(UNIT_OPENED_ICON);
        renderer.setLeafIcon(DETAIL_ICON);
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
