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
import javax.swing.text.Document;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SmartJTextField extends JTextField {
    private JComboBox comboBox;

    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                if (comboBox != null) {
                    comboBox.processKeyEvent(e);
                }
            }
        }
    };

    public SmartJTextField() {
        addKeyListener(keyAdapter);
    }

    public SmartJTextField(String text) {
        super(text);
        addKeyListener(keyAdapter);
    }

    public SmartJTextField(int columns) {
        super(columns);
        addKeyListener(keyAdapter);
    }

    public SmartJTextField(String text, int columns) {
        super(text, columns);
        addKeyListener(keyAdapter);
    }

    public SmartJTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        addKeyListener(keyAdapter);
    }

    public void setChildComboBox(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

}
