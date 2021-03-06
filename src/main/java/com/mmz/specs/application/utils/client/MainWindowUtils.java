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
import com.mmz.specs.application.gui.common.MessageBuilder;
import com.mmz.specs.application.gui.common.utils.JTreeUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.application.utils.SessionUtils;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.dao.MaterialListDaoImpl;
import com.mmz.specs.model.*;
import com.mmz.specs.service.*;
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

import static com.mmz.specs.service.CommonServiceUtils.getRootObjects;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

@SuppressWarnings("DeprecatedIsStillUsed")
public class MainWindowUtils {
    public static final String ROOT_UNIT_CODE = ".00.000";
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final long NANO_TIME = 1000000;
    private final Session session;
    private ClientMainWindow clientMainWindow = null;


    public MainWindowUtils(Session session) {
        this.session = session;
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

    private void expandPath(TreePath selectedPath, JTree mainTree) {
        DetailEntity selectedEntity = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);
        if (selectedEntity != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
            int childCount = node.getChildCount();
            log.debug("Children for: {} count: {}", selectedEntity.toSimpleString(), childCount);
            if (childCount == 0) {
                new MainWindowUtils(session).getModuleChildren(node, selectedEntity);
                DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
                model.reload(node);
                mainTree.expandPath(selectedPath);
            }
        }
    }

    public DefaultMutableTreeNode fillMainTree(DetailEntity detailEntity) {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode();
        if (session != null) {
            if (detailEntity != null) {
                DetailListService detailListService = new DetailListServiceImpl(new DetailListDaoImpl(session));
                if (!detailEntity.isUnit()) {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                    DefaultMutableTreeNode detail = new DefaultMutableTreeNode(detailEntity, false);
                    root.add(detail);
                    return root;
                } else {
                    DefaultMutableTreeNode detailListTreeByDetailList = new MainWindowUtils(session).getModuleDetailListTreeByEntityList(detailListService.getDetailListByParent(detailEntity));
                    if (detailListTreeByDetailList.children().hasMoreElements()) {
                        return detailListTreeByDetailList;
                    } else {
                        DetailService detailService = new DetailServiceImpl(new DetailDaoImpl(session));
                        if (detailService.getDetailById(detailEntity.getId()) == null) {
                            detailEntity = detailService.getDetailById(detailService.addDetail(detailEntity));
                        } else {
                            detailEntity = detailService.getDetailById(detailEntity.getId());
                        }
                        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode();
                        newChild.setUserObject(detailEntity);
                        root.add(newChild);
                        return root;
                    }
                }
            }
        }
        return result;
    }

    public KeyListener getArrowKeyListener(JTree mainTree) {
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

    public DefaultMutableTreeNode getBoostedModuleDetailListFullTree() {
        final long time = System.nanoTime();
        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");

        DetailService service = new DetailServiceImpl(session);

        if (!Thread.currentThread().isInterrupted()) {
            ArrayList<DetailEntity> roots = (ArrayList<DetailEntity>) service.getDetailsBySearch(ROOT_UNIT_CODE);

            if (!Thread.currentThread().isInterrupted()) {
                roots.sort((o1, o2) -> ComparisonChain.start()
                        .compareTrueFirst(o1.isUnit(), o2.isUnit())
                        .compare(o1.getCode(), o2.getCode())
                        .compareTrueFirst(o1.isActive(), o2.isActive())
                        .result());

                for (DetailEntity e : roots) {
                    if (e.getCode().contains(ROOT_UNIT_CODE)) {
                        result.add(new DefaultMutableTreeNode(e));
                        System.out.println("root: " + e.getCode() + " " + e.getDetailTitleByDetailTitleId().getTitle());
                    }
                }
                log.info("Counting full tree cost in total: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
                return result;
            }
        }
        return null;
    }

    /**
     * Gives children for {@link DetailEntity} parent.
     *
     * @see #getModuleChildren(DefaultMutableTreeNode, DetailEntity)
     * @deprecated Use <code>getModuleChildren</code> instead.
     * This method loads all children -> that's why it is too slow!
     */
    @Deprecated
    private DefaultMutableTreeNode getChildren(DetailEntity parent) {
        log.warn("STARTING getting children for parent: {}; {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), parent);
        final long total1 = System.nanoTime();

        DefaultMutableTreeNode result = null;
        if (parent.isActive()) {
            DetailListService service = new DetailListServiceImpl(session);

            ArrayList<DetailEntity> children = (ArrayList<DetailEntity>) service.listChildren(parent);
            result = new DefaultMutableTreeNode();
            if (children.size() > 0) {
                log.trace("Parent {} has children count: {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), children.size());

               /* if (children.size() > 1) {
                    children.sort((o1, o2) -> ComparisonChain.start()
                            .compareTrueFirst(o1.isUnit(), o2.isUnit())
                            .compare(o1.getCode(), o2.getCode())
                            .compareTrueFirst(o1.isActive(), o2.isActive())
                            .result());
                }*/
                CommonServiceUtils.sortDetailEntityArray(children);

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

    public ClientMainWindow getClientMainWindow(Component component) {
        Window parentWindow = FrameUtils.findWindow(component);
        if (parentWindow instanceof ClientMainWindow) {
            this.clientMainWindow = (ClientMainWindow) parentWindow;
            return (ClientMainWindow) parentWindow;
        }
        return null;
    }

    /**
     * Gives full tree for listEntities, ignoring search (if there is so)
     *
     * @deprecated Use {@link #getModuleDetailListFullTree()} instead
     */
    @Deprecated
    public DefaultMutableTreeNode getDetailListTreeByEntityList(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();

        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getParentsObjects(listEntities);
        System.out.println(roots);
        for (DetailEntity entity : roots) {
            DefaultMutableTreeNode node = getChildren(entity);
            result.add(node);
        }
        log.info("Counting tree by entity cost in total: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }

    public TreeNode getDetailsTreeByDetails(List<DetailEntity> detailsBySearch) {
        detailsBySearch.sort((o1, o2) -> ComparisonChain.start()
                .compareTrueFirst(o1.isUnit(), o2.isUnit())
                .compare(o1.getCode(), o2.getCode())
                .compare(o1.getDetailTitleByDetailTitleId().getTitle(), o2.getDetailTitleByDetailTitleId().getTitle())
                .compareTrueFirst(o1.isActive(), o2.isActive())
                .result());

        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        for (DetailEntity e : detailsBySearch) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(e);
            result.add(node);
        }
        return result;
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

    public void getModuleChildren(DefaultMutableTreeNode result, DetailEntity parent) {
        log.debug("STARTING getting children for parent: {}; {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), parent);
        final long total1 = System.nanoTime();
        if (parent.isActive()) {
            DetailListService service = new DetailListServiceImpl(session);

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

    public DefaultMutableTreeNode getModuleDetailListFullTree() {
        final long time = System.nanoTime();

        if (!Thread.currentThread().isInterrupted()) {
            DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
            ArrayList<DetailEntity> roots = getRootObjects(session);


            for (DetailEntity e : roots) {
                if (e.getCode().contains(ROOT_UNIT_CODE)) {
                    if (e.isActive()) {
                        result.add(new DefaultMutableTreeNode(e));
                    }
                    System.out.println("root: " + e.getCode() + " " + e.getDetailTitleByDetailTitleId().getTitle());
                }
            }
            log.info("Counting full tree cost in total: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
            return result;
        }
        return null;
    }

    public DefaultMutableTreeNode getModuleDetailListTreeByEntityList(List<DetailListEntity> listEntities) {
        final long time = System.nanoTime();

        listEntities.sort((o1, o2) -> ComparisonChain.start()
                .compare(o1.getDetailByParentDetailId().getCode(), o2.getDetailByParentDetailId().getCode())
                .compareTrueFirst(o1.isActive(), o2.isActive())
                .result());


        DefaultMutableTreeNode result = new DefaultMutableTreeNode("root");
        ArrayList<DetailEntity> roots = getParentsObjects(listEntities);
        for (DetailEntity entity : roots) {
            result.add(new DefaultMutableTreeNode(entity));
        }
        log.info("Counting tree by entity cost in total: " + ((double) ((System.nanoTime() - time) / NANO_TIME) / 1000) + " sec");
        return result;
    }

    public MouseListener getMouseListener(JTree mainTree) {
        return new MouseAdapter() {
            private void addPopup(TreePath selectedPath) {
                final JPopupMenu popup = new JPopupMenu();
                JMenuItem reload = new JMenuItem("Обновить",
                        new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/refresh.png"))));
                reload.addActionListener(e -> reloadPath(selectedPath));
                popup.add(reload);
                mainTree.setComponentPopupMenu(popup);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = mainTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = mainTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            final TreePath closestPathForLocation = mainTree.getClosestPathForLocation(e.getX(), e.getY());
                            mainTree.setSelectionPath(closestPathForLocation);
                            addPopup(closestPathForLocation);
                        }
                    } else if (e.getClickCount() == 2) {
                        if (!mainTree.isExpanded(selPath)) {
                            expandPath(selPath, mainTree);
                        }
                    }
                }
            }

            private void reloadPath(TreePath selectedPath) {
                new MainWindowUtils(session).getClientMainWindow(mainTree).updateMessage(
                        new MessageBuilder()
                                .setIcon("/img/gui/animated/sync.gif").setText("Обновляем данные")
                                .getMessage());

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainTree.getLastSelectedPathComponent();
                node.removeAllChildren();

                final DetailEntity parentForSelectionPath = JTreeUtils.getParentForSelectionPath(mainTree);
                final DetailEntity selectedDetailEntityFromTree = JTreeUtils.getSelectedDetailEntityFromTree(mainTree);


                //todo fix it
                DetailListService service = new DetailListServiceImpl(session);
                if (parentForSelectionPath != null && selectedDetailEntityFromTree != null) {
                    try {
                        DetailListEntity detailListEntity = service.getLatestDetailListEntityByParentAndChild(parentForSelectionPath, selectedDetailEntityFromTree);
                        SessionUtils.refreshSession(session, detailListEntity);
                        SessionUtils.refreshSession(session, parentForSelectionPath);
                        SessionUtils.refreshSession(session, selectedDetailEntityFromTree);
                    } catch (Exception e1) {
                        SessionUtils.refreshSession(session, DetailEntity.class);
                        SessionUtils.refreshSession(session, DetailListEntity.class);

                    }
                } else {
                    final List<DetailEntity> detailEntities = service.listChildren(selectedDetailEntityFromTree);
                    for (DetailEntity entity : detailEntities) {
                        SessionUtils.refreshSession(session, entity);
                        final DetailListEntity latestDetailListEntityByParentAndChild = service.getLatestDetailListEntityByParentAndChild(selectedDetailEntityFromTree, entity);
                        SessionUtils.refreshSession(session, latestDetailListEntityByParentAndChild);
                    }
                }


                expandPath(selectedPath, mainTree);

                new MainWindowUtils(session).getClientMainWindow(mainTree).updateMessage(null);
            }
        };
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

    private boolean notContainsEntityInChildren(DetailEntity parent, DetailEntity entity) {
        log.debug("Checking if entity: {} contains in children of parent. For now parent is: {}", entity.toSimpleString(), parent.toSimpleString());
        if (parent.getId() != entity.getId()) {
            DetailListService service = new DetailListServiceImpl(session);
            final ArrayList<DetailEntity> detailEntities = (ArrayList<DetailEntity>) service.listChildren(parent);
            ArrayList<Boolean> results = new ArrayList<>(detailEntities.size());
            for (DetailEntity parent_ : detailEntities) {
                results.add(parent_.getId() == entity.getId());
            }
            if (results.size() > 0) {
                return results.contains(Boolean.FALSE);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean notContainsEntityInParentsHierarchically(DetailEntity parent, DetailEntity entity) {
        log.debug("Checking if entity: {} is a parent of itself. For now parent is: {}", entity.toSimpleString(), parent.toSimpleString());
        if (parent.getId() != entity.getId()) {
            DetailListService service = new DetailListServiceImpl(session);
            final ArrayList<DetailEntity> detailEntities = (ArrayList<DetailEntity>) service.listParents(parent);
            ArrayList<Boolean> results = new ArrayList<>(detailEntities.size());
            for (DetailEntity parent_ : detailEntities) {
                results.add(notContainsEntityInParentsHierarchically(parent_, entity));
            }
            if (results.size() > 0) {
                return !results.contains(Boolean.FALSE);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean notContainsEntityOnSameLevel(DefaultMutableTreeNode node, DetailEntity parent, DetailEntity entity) {
        log.debug("Checking if entity: {} contains on the same level of parent entity: {}", entity.toSimpleString(), parent.toSimpleString());
        DetailListService service = new DetailListServiceImpl(session);
        if (!parent.isUnit()) {
            DetailEntity ultraParent = ((DetailEntity) ((DefaultMutableTreeNode) node.getParent()).getUserObject());
            ArrayList<DetailEntity> children = (ArrayList<DetailEntity>) service.listChildren(ultraParent);
            for (DetailEntity child : children) {
                if (child.getId() == entity.getId()) {
                    return false;
                }
            }
            return true;
        } else {
            ArrayList<DetailEntity> children = (ArrayList<DetailEntity>) service.listChildren(parent);
            for (DetailEntity child : children) {
                if (child.getId() == entity.getId()) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean pathNotContainsEntity(Component component, TreePath selectionPath, DetailEntity entity) {
        final Object o = selectionPath.getLastPathComponent();
        if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
            final Object userObject = node.getUserObject();
            if (userObject instanceof DetailEntity) {
                DetailEntity parent = (DetailEntity) userObject;

                final boolean notContainsEntityInParentsHierarchically = notContainsEntityInParentsHierarchically(parent, entity);
                System.out.println("notContainsEntityInParentsHierarchically result: " + notContainsEntityInParentsHierarchically);

                final boolean notContainsEntityOnSameLevel = notContainsEntityOnSameLevel(node, parent, entity);
                System.out.println("notContainsEntityOnSameLevel result: " + notContainsEntityOnSameLevel);

                final boolean notContainsEntityInChildren = notContainsEntityInChildren(parent, entity);
                System.out.println("notContainsEntityInChildren result: " + notContainsEntityInChildren);


                if (notContainsEntityInParentsHierarchically) {
                    if (notContainsEntityOnSameLevel) {
                        if (!notContainsEntityInChildren) {
                            final String message = "Нельзя добавить " + (entity.isUnit() ? "узел" : "деталь") + " "
                                    + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                                    "т.к. " + (entity.isUnit() ? "он" : "она") + " уже содержится в узле.";
                            showMessageDialog(component, message, "Ошибка добавления", ERROR_MESSAGE);
                        }
                        return notContainsEntityInChildren;
                    } else {
                        final String message = "Нельзя добавить " + (entity.isUnit() ? "узел" : "деталь") + " "
                                + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                                "т.к. " + (entity.isUnit() ? "он" : "она") + " уже содержится на этом же уровне.";
                        showMessageDialog(component, message, "Ошибка добавления", ERROR_MESSAGE);
                    }

                } else {
                    showMessageDialog(component, "Нельзя добавить " + (entity.isUnit() ? "узел" : "деталь") + " "
                            + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle() + ",\n" +
                            "т.к. " + (entity.isUnit() ? "он" : "она") + " уже содержится в узле.", "Ошибка добавления", ERROR_MESSAGE);
                }

            }
        }
        return false;
    }

    public void setClientMainWindow(Component component) {
        this.clientMainWindow = getClientMainWindow(component);
    }

    public void updateMessage(String pathToImage, String text) {
        if (clientMainWindow != null) {
            final MessageBuilder messageBuilder = new MessageBuilder().setIcon(pathToImage).setText(text);
            clientMainWindow.updateMessage(messageBuilder.getMessage());
        }
    }
}