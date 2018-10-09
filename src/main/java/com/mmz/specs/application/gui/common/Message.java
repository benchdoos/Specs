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

import javax.swing.*;
import java.awt.*;

public class Message {
    private ImageIcon icon;
    private String text;

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(String path) {
        if (path != null) {
            try {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(path))
                        .getScaledInstance(16, 16, 1));
            } catch (Exception e) {
                icon = null;
            }
        } else {
            icon = null;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void setIcon(Image image) {
        this.icon = new ImageIcon(image.getScaledInstance(16, 16, 1));
    }
}
