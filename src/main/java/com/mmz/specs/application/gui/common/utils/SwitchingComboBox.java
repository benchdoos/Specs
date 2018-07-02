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

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

public abstract class SwitchingComboBox<E> extends JComboBox<E> {
    private String typedItem = "";
    private Timer timeoutTimer = new Timer(1000, e -> typedItem = "");

    public SwitchingComboBox(E[] items) {
        super(items);
        initSwitchingComboBox();
    }

    public SwitchingComboBox(Vector<E> items) {
        super(items);
        initSwitchingComboBox();
    }

    public SwitchingComboBox() {
        super();
        initSwitchingComboBox();
    }

    public SwitchingComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
        initSwitchingComboBox();
    }


    private void initSwitchingComboBox() {
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                    typedItem = typedItem + e.getKeyChar();
                    selectTypedItem();
                    timeoutTimer.restart();
                } else {
                    typedItem = "";
                    selectTypedItem();
                    timeoutTimer.restart();
                }
            }
        };
        addKeyListener(keyAdapter);

        timeoutTimer.setRepeats(false);
    }

    protected String getTypedItem() {
        return typedItem;
    }

    public abstract void selectTypedItem();
}
