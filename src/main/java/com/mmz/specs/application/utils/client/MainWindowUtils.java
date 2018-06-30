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

package com.mmz.specs.application.utils.client;

import com.google.common.collect.ComparisonChain;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.dao.MaterialListDaoImpl;
import com.mmz.specs.model.*;
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;
import com.mmz.specs.service.MaterialListService;
import com.mmz.specs.service.MaterialListServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MainWindowUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final long NANO_TIME = 1000000;
    private Session session;


    public MainWindowUtils(Session session) {
        this.session = session;
    }


    public DefaultMutableTreeNode getDetailListFullTree(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();
        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getRootObjects(listEntities);

        System.out.println("roots: ");
        for (DetailEntity e : roots) {
            System.out.println("root: " + e.getCode() + " " + e.getDetailTitleByDetailTitleId().getTitle());
        }

        for (DetailEntity entity : roots) {
            DefaultMutableTreeNode node = getChildren(entity);
            result.add(node);
        }
        System.err.println("TOTAL: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }


    public DefaultMutableTreeNode getModuleDetailListFullTree(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();
        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getRootObjects(listEntities);

        System.out.println("roots: ");
        for (DetailEntity e : roots) {
            result.add(new DefaultMutableTreeNode(e));
            System.out.println("root: " + e.getCode() + " " + e.getDetailTitleByDetailTitleId().getTitle());
        }
        System.err.println("TOTAL: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }

    private ArrayList<DetailEntity> getRootObjects(List<DetailListEntity> listEntities) {
        DetailListService service = new DetailListServiceImpl(session);

        ArrayList<DetailEntity> roots = new ArrayList<>();
        ArrayList<DetailEntity> children = new ArrayList<>();

        for (DetailListEntity current : listEntities) {
            final DetailEntity parentDetailEntity = current.getDetailByParentDetailId();

            if (!roots.contains(parentDetailEntity)) {
                if (!children.contains(parentDetailEntity)) {
                    List<DetailEntity> parents = service.listParents(parentDetailEntity);
                    if (parents.size() == 0) {
                        roots.add(parentDetailEntity);
                    } else {
                        children.add(parentDetailEntity);
                    }
                }
            }
        }
        return roots;
    }


    public DefaultMutableTreeNode getDetailListTreeByEntityList(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();

        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getParentsObjects(listEntities);
        System.out.println(">>> roots: ");
        System.out.println(roots);
        for (DetailEntity entity : roots) {
            DefaultMutableTreeNode node = getChildren(entity);
            result.add(node);
        }
        System.err.println("TOTAL: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }

    public DefaultMutableTreeNode getModuleDetailListTreeByEntityList(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();

        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getParentsObjects(listEntities);
        System.out.println(">>> roots: ");
        System.out.println(roots);
        for (DetailEntity entity : roots) {
            result.add(new DefaultMutableTreeNode(entity));
        }
        System.err.println("TOTAL: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }

    private ArrayList<DetailEntity> getParentsObjects(List<DetailListEntity> listEntities) {
        ArrayList<DetailEntity> result = new ArrayList<>();
        for (DetailListEntity e : listEntities) {
            final DetailEntity detailByParentDetailId = e.getDetailByParentDetailId();
            if (!result.contains(detailByParentDetailId)) {
                result.add(detailByParentDetailId);
            }
        }
        return result;
    }

    public DefaultMutableTreeNode getChildren(DetailEntity parent) {
        log.warn("STARTING getting children for parent: {}; {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), parent);
        final long total1 = System.nanoTime();

        DefaultMutableTreeNode result = null;
        if (parent.isActive()) {
            DetailListService service = new DetailListServiceImpl(session);
            final long t1 = System.nanoTime();

            ArrayList<DetailEntity> children = (ArrayList<DetailEntity>) service.listChildren(parent);
            result = new DefaultMutableTreeNode();
            if (children.size() > 0) {
                log.trace("Parent {} has children count: {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), children.size());

                if (children.size() > 1) {
                    children.sort((o1, o2) -> ComparisonChain.start()
                            .compareTrueFirst(o1.isUnit(), o2.isUnit())
                            .compare(o1.getCode(), o2.getCode())
                            .compareTrueFirst(o1.isActive(), o2.isActive())
                            .result());
                }

                for (DetailEntity child : children) {
                    if (child != null) {
                        if (child.isActive()) {
                            DetailListEntity lastDetailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, child);
                            if (lastDetailListEntity != null) {
                                if (lastDetailListEntity.isActive()) {
                                    if (child.isUnit()) {
                                        final long time1 = System.nanoTime();
                                        result.add(getChildren(child));
                                        System.err.println("adding children to result COST: " + ((System.nanoTime() - time1) / NANO_TIME) + " for: " + lastDetailListEntity);

                                    } else {
                                        result.add(new DefaultMutableTreeNode(child));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            result.setAllowsChildren(parent.isUnit());
            result.setUserObject(parent);
        }

        final long total2 = System.nanoTime();

        log.info("Getting children for parent: {} COST in time: {}",
                parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(),
                ((total2 - total1) / NANO_TIME));
        return result;
    }

    private void getModuleChildren(DefaultMutableTreeNode result, DetailEntity parent) {
        log.debug("STARTING getting children for parent: {}; {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), parent);
        final long total1 = System.nanoTime();
        if (parent.isActive()) {
            DetailListService service = new DetailListServiceImpl(session);
            final long t1 = System.nanoTime();

            ArrayList<DetailEntity> children = (ArrayList<DetailEntity>) service.listChildren(parent);

            if (children.size() > 0) {
                log.trace("Parent {} has children count: {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), children.size());

                if (children.size() > 1) {
                    children.sort((o1, o2) -> ComparisonChain.start()
                            .compareTrueFirst(o1.isUnit(), o2.isUnit())
                            .compare(o1.getCode(), o2.getCode())
                            .compareTrueFirst(o1.isActive(), o2.isActive())
                            .result());
                }

                for (DetailEntity child : children) {
                    if (child != null) {
                        if (child.isActive()) {
                            DetailListEntity lastDetailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, child);
                            if (lastDetailListEntity != null) {
                                if (lastDetailListEntity.isActive()) {
                                    final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                                    childNode.setAllowsChildren(parent.isUnit());
                                    result.add(childNode);
                                }
                            }
                        }
                    }
                }
            }

        }

        final long total2 = System.nanoTime();

        log.info("Getting children for parent: {} COST in time: {}",
                parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(),
                ((total2 - total1) / NANO_TIME));
    }

    public DetailListEntity getLatestDetailListEntity(List<DetailListEntity> result) {
        DetailListEntity latest = null;
        log.trace("Getting latest result from :" + result);
        for (DetailListEntity entity : result) {
            if (latest != null) {
                if (latest.getNoticeByNoticeId() != null) {
                    Date date = latest.getNoticeByNoticeId().getDate();
                    if (date != null) {
                        NoticeEntity noticeEntity = entity.getNoticeByNoticeId();
                        if (noticeEntity != null) {
                            Date noticeDate = noticeEntity.getDate();
                            if (noticeDate != null) {
                                boolean before = date.before(noticeDate);
                                boolean equals = date.equals(noticeDate);
                                log.trace("Comparing latest " + latest + " and current" + entity);
                                log.trace("Latest is before current: " + before + " latest date equals current: " + equals);
                                if (before || equals) {
                                    log.trace("Changing latest from " + latest + " to " + entity);
                                    latest = entity;
                                }
                            }
                        }
                    }
                }
            } else {
                latest = entity;
            }
        }
        log.trace("Latest entity: " + latest + " from " + result);
        return latest;
    }


    public ClientMainWindow getClientMainWindow(Component component) {
        Window parentWindow = FrameUtils.findWindow(component);
        if (parentWindow instanceof ClientMainWindow) {
            return (ClientMainWindow) parentWindow;
        }
        return null;
    }

    public boolean containsMaterialEntityInMaterialListEntity(MaterialEntity materialEntity) {
        MaterialListService service = new MaterialListServiceImpl(new MaterialListDaoImpl(session));
        ArrayList<MaterialListEntity> list = (ArrayList<MaterialListEntity>) service.listMaterialLists();
        for (MaterialListEntity entity : list) {
            if (entity.getMaterialByMaterialId().equals(materialEntity)) {
                return true;
            }
        }
        return false;
    }

    public TreeNode getDetailsTreeByDetails(List<DetailEntity> detailsBySearch) {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        for (DetailEntity e : detailsBySearch) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(e);
            result.add(node);
        }
        return result;
    }

    public MouseListener getMouseListener(JTree mainTree) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = mainTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = mainTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                        System.out.println(e.getButton());
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            addPopup(selPath);
                        }
                    } else if (e.getClickCount() == 2) {
                        if (!mainTree.isExpanded(selPath)) {
                            expandPath(selPath, mainTree);
                        }
                    }
                }
            }

            private void addPopup(TreePath selectedPath) {
                final JPopupMenu popup = new JPopupMenu();
                JMenuItem reload = new JMenuItem("Обновить",
                        new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/refresh-left-arrow.png"))));
                reload.addActionListener(e -> {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                    node.removeAllChildren();
                    expandPath(selectedPath, mainTree);
                });
                popup.add(reload);
                mainTree.setComponentPopupMenu(popup);
            }
        };
    }

    public KeyListener getKeyListener(JTree mainTree) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                final int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                    if (node != null) {
                        final DetailEntity detail = (DetailEntity) node.getUserObject();
                        if (detail != null) {
                            if (node.getChildCount() == 0 && detail.isUnit()) {
                                TreePath selPath = mainTree.getSelectionPath();
                                expandPath(selPath, mainTree);
                            }
                        }
                    }
                }
            }
        };
    }


    private void expandPath(TreePath selectedPath, JTree mainTree) {
        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedEntity != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
            int childCount = node.getChildCount();
            System.out.println("children count: " + selectedEntity.toSimpleString() + " " + childCount);
            if (childCount == 0) {
                new MainWindowUtils(session).getModuleChildren(node, selectedEntity);
                DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
                model.reload(node);
                mainTree.expandPath(selectedPath);
            }
        }
    }
}