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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

public class SptFileUtils {
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

    public static TreePath find(DefaultMutableTreeNode root, String s) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().toUpperCase().contains(s.toUpperCase())) {
                return new TreePath(node.getPath());
            }
        }
        return null;
    }
}
