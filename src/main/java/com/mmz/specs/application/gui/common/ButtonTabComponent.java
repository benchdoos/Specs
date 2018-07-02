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


import com.mmz.specs.application.gui.panels.Transactional;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private final static Image grayCircle = Toolkit.getDefaultToolkit().getImage(ButtonTabComponent.class.getResource("/img/gui/circleGray12.png"));
    private final static Image redDarkCircle = Toolkit.getDefaultToolkit().getImage(ButtonTabComponent.class.getResource("/img/gui/circleRedDarker12.png"));
    private final static Image redCircle = Toolkit.getDefaultToolkit().getImage(ButtonTabComponent.class.getResource("/img/gui/circleRed12.png"));
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }

            @Override
            public Icon getIcon() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getIconAt(i);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private void manageTransactions(int i) {
        Transactional transactional = (Transactional) pane.getComponentAt(i);
        transactional.rollbackTransaction();
    }

    private class TabButton extends JButton implements ActionListener {
        TabButton() {
            int size = 10;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Закрыть вкладку (CTRL+W)");
            //Make the button looks the same for all tabs
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                if (pane.isEnabledAt(i)) {
                    try {
                        manageTransactions(i);
                    } catch (ClassCastException ex) {
                        pane.remove(i);
                    } catch (Exception ex) {
                        log.warn("Could not rollback transaction for tab: {}", i, ex);
                        pane.remove(i);
                    }
                }
            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons

            if (getModel().isRollover()) {
                if (getModel().isPressed()) {
                    g2.drawImage(redDarkCircle, 0, 0, null);

                } else {
                    g2.drawImage(redCircle, 0, 0, null);
                }
            } else {
                g2.drawImage(grayCircle, 0, 0, null);
            }
            g2.dispose();
        }
    }
}


