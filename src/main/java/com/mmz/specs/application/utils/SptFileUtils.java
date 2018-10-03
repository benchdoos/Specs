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

package com.mmz.specs.application.utils;

import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;

public class SptFileUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static TreePath findNextSearchText(JTree tree, DefaultMutableTreeNode currentSelectedNode, String search) {
       /* if (tree == null  || search == null) {
            throw new IllegalArgumentException("Params can not be null: Model(" + tree + ") Node(" + currentSelectedNode + ") Search(" + search + ")");
        }
        int childCount =0;
        if (currentSelectedNode != null) {
             childCount = currentSelectedNode.getChildCount();
        } else {
             childCount = (DefaultMutableTreeNode)tree.getModel().getRoot().getChildCount();
        }
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) currentSelectedNode.getChildAt(i);
                final Object userObject = childAt.getUserObject();
                if (userObject instanceof TreeSPTRecord) {
                    TreeSPTRecord record = (TreeSPTRecord) userObject;
                    if (record.getDetail().getCode().toUpperCase().contains(search.toUpperCase())) {
                        return tree.getPathForRow(i);
                    } else if (record.getDetail().getDetailTitleByDetailTitleId().getTitle().toUpperCase().contains(search.toUpperCase())) {
                        return tree.getPathForRow(i);
                    } else {
                        return findNextSearchText(tree, (DefaultMutableTreeNode) tree.getLastSelectedPathComponent(), search);
                    }
                }
            }
        }*/
        return null;
    }

    public static ArrayList<TreePath> find(DefaultMutableTreeNode root, String text) {
        ArrayList<TreePath> result = new ArrayList<>();
        log.debug("Searching text: {} in root: {}", text, root);
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.getUserObject() instanceof TreeSPTRecord) {
                TreeSPTRecord record = (TreeSPTRecord) node.getUserObject();
                final DetailEntity detail = record.getDetail();
                if (detail != null) {
                    try {
                        if (detail.getCode().toUpperCase().contains(text.toUpperCase())) {
                            result.add(new TreePath(node.getPath()));
                            //return new TreePath(node.getPath());
                        }
                        final DetailTitleEntity title = detail.getDetailTitleByDetailTitleId();
                        if (title != null) {
                            if (title.getTitle().toUpperCase().contains(text.toUpperCase())) {
                                result.add(new TreePath(node.getPath()));
//                                return new TreePath(node.getPath());
                            }
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        log.debug("Total result for search text: {} is: {}", text, result.size());
        if (result.size() > 0) {
            return result;
        }
        return null;
    }
}
