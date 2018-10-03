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
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.client.MainWindowUtils;
import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.model.DetailEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class DetailSptJTree extends JTree {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static final Color BACKGROUND_SELECTION_COLOR = new DefaultTreeCellRenderer().getBackgroundSelectionColor();
    private static final Color BACKGROUND_NON_SELECTION_COLOR = new JPanel().getBackground();

    private static final Icon UNIT_CLOSED_ICON = new ImageIcon(DetailSptJTree.class.getResource("/img/gui/tree/unitOpened.png"));
    private static final Icon UNIT_OPENED_ICON = new ImageIcon(DetailSptJTree.class.getResource("/img/gui/tree/unitOpened.png"));
    private static final Icon DETAIL_ICON = new ImageIcon(DetailSptJTree.class.getClass().getResource("/img/gui/tree/detail.png"));
    private String searchText = null;

    public DetailSptJTree() {
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
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                restore(selected);


                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof TreeSPTRecord) {
                    TreeSPTRecord treeSPTRecord = (TreeSPTRecord) node.getUserObject();
                    if (treeSPTRecord != null) {
                        updateBackgroundColor(selected, treeSPTRecord.isInterchangeable());

                        final DetailEntity detail = treeSPTRecord.getDetail();

                        if (detail != null) {
                            updateIcon(detail);
                            updateSearch(detail);

                            if (detail.getDetailTitleByDetailTitleId() != null) {
                                final boolean isManipulator = detail.getCode().contains(MainWindowUtils.ROOT_UNIT_CODE);
                                String quantity = isManipulator ? " " : (" (" + treeSPTRecord.getQuantity() + ") ");
                                String view = detail.getCode() + quantity + detail.getDetailTitleByDetailTitleId().getTitle();
                                return super.getTreeCellRendererComponent(tree, view, selected, expanded, leaf, row, hasFocus);
                            }
                        }
                    }
                }
                return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }

            private void restore(boolean selected) {
                if (selected) {
                    this.setBackgroundSelectionColor(BACKGROUND_SELECTION_COLOR);
                } else {
                    this.setBackgroundNonSelectionColor(BACKGROUND_NON_SELECTION_COLOR);
                }
                this.setBorder(null);
            }

            private void updateBackgroundColor(boolean selected, boolean interchangeable) {
                if (interchangeable) {
                    if (!selected) {
                        this.setBackgroundNonSelectionColor(Color.GRAY.brighter());
                    } else {
                        this.setBackgroundSelectionColor(Color.GRAY);
                    }
                }
            }

            private void updateIcon(DetailEntity child) {
                setLeafIcon(child.isUnit() ? UNIT_OPENED_ICON : DETAIL_ICON);
                setOpenIcon(child.isUnit() ? UNIT_OPENED_ICON : DETAIL_ICON);
            }

            private void updateSearch(DetailEntity detailEntity) {
                if (searchText != null && detailEntity != null) {
                    if (detailEntity.getCode().contains(searchText)) {
                        this.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.ORANGE));
                    } else if (detailEntity.getDetailTitleByDetailTitleId().getTitle().toUpperCase().contains(searchText.toUpperCase())) {
                        this.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.ORANGE));
                    } else {
                        this.setBorder(null);
                    }
                } else {
                    this.setBorder(null);
                }
            }

           /* @Override
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
            }*/
        };
    }

    private void initIcons(DefaultTreeCellRenderer renderer) {
        renderer.setClosedIcon(UNIT_CLOSED_ICON);
        renderer.setOpenIcon(UNIT_OPENED_ICON);
        renderer.setLeafIcon(DETAIL_ICON);
    }

    public void setSearchText(String text) {
        this.searchText = text;
    }
}
