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

package com.mmz.specs.application.gui.common.utils;

import com.mmz.specs.io.formats.TreeSPTRecord;
import com.mmz.specs.model.DetailEntity;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class JTreeUtils {
    public static DetailEntity getSelectedDetailEntityFromTree(JTree tree) {
        final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent != null) {
            return (DetailEntity) lastSelectedPathComponent.getUserObject();
        } else return null;
    }

    public static DetailEntity getParentForSelectionPath(JTree tree) {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getPath()[selectionPath.getPath().length - 2];
            if (node != null) {
                final Object userObject = node.getUserObject();
                if (userObject instanceof DetailEntity) {
                    return (DetailEntity) userObject;
                }
            }
        }
        return null;
    }

    public static TreeSPTRecord getSelectedTreeSptRecord(JTree tree) {
        final DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent != null) {
            return (TreeSPTRecord) lastSelectedPathComponent.getUserObject();
        } else return null;
    }
}
