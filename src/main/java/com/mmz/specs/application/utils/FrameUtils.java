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

import hu.kazocsaba.imageviewer.ImageViewer;
import hu.kazocsaba.imageviewer.ResizeStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class FrameUtils {
    private static final Timer timer = new Timer(60, null);
    public static final Dimension DEFAULT_DIMENSION = new Dimension(640, 480);

    /**
     * Finds window on component given.
     *
     * @param component Component where window is located.
     * @return Window that is searched.
     **/
    public static Window findWindow(Component component) {
        if (component == null) {
            return JOptionPane.getRootFrame();
        } else if (component instanceof Window) {
            return (Window) component;
        } else {
            return findWindow(component.getParent());
        }
    }

    /**
     * Shakes window like in MacOS.
     *
     * @param component Component to shake
     */
    public static void shakeFrame(final Component component) {
        final Window window = findWindow(component);
        ActionListener listener = new ActionListener() {
            final static int maxCounter = 6;
            Point location = window.getLocation();
            int counter = 0;
            int step = 14;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (counter <= 2) {
                    step = 14;
                } else if (counter <= 4) {
                    step = 7;
                } else if (counter <= 6) {
                    step = 3;
                }

                if (counter >= 0) {
                    if (counter <= maxCounter) {
                        counter++;

                        if (location.x < 0 || location.y < 0) {
                            window.setLocation(getFrameOnCenterLocationPoint(window));
                            location = window.getLocation();
                        }
                        if (counter % 2 != 0) {
                            Point newLocation = new Point(location.x + step, location.y);
                            window.setLocation(newLocation);
                        } else {
                            Point newLocation = new Point(location.x - step, location.y);
                            window.setLocation(newLocation);
                        }
                    } else {
                        Point newLocation = new Point(location.x, location.y);
                        window.setLocation(newLocation);

                        counter = 0;
                        timer.removeActionListener(timer.getActionListeners()[0]);
                        timer.stop();
                    }
                }
            }
        };

        if (!timer.isRunning()) {
            timer.addActionListener(listener);
            timer.start();
        }
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Returns the location of point of window, when it should be on center of the screen.
     *
     * @return Point of <code>Window</code> that is moved to center of the screen.
     * @see java.awt.Component#getLocation
     */
    private static Point getFrameOnCenterLocationPoint(Window window) {
        Dimension size = window.getSize();
        int width = (int) ((Toolkit.getDefaultToolkit().getScreenSize().width / (double) 2) - (size.getWidth() / (double) 2));
        int height = (int) ((Toolkit.getDefaultToolkit().getScreenSize().height / (double) 2) - (size.getHeight() / (double) 2));
        return new Point(width, height);
    }

    /**
     * Returns the location of point of child, when it should be on center of the parent window.
     *
     * @return Point of <code>Window</code> that is moved to center of the parent.
     * @see java.awt.Component#getLocation
     */
    private static Point getFrameOnParentCenterLocationPoint(Window parent, Window child) {

        int parentCenterX = (int) (parent.getLocation().getX() + (parent.getWidth() / (double) 2));
        int parentCenterY = (int) (parent.getLocation().getY() + (parent.getHeight() / (double) 2));

        int childCenterX = (int) (child.getSize().getWidth() / (double) 2);
        int childCenterY = (int) (child.getSize().getHeight() / (double) 2);

        int width = parentCenterX - childCenterX;
        int height = parentCenterY - childCenterY;

        return new Point(width, height);
    }

    /**
     * Gets the point-position to set <code>Window</code> on center of the parent or center of the screen
     *
     * @param parent of the new-child window
     * @param child  to set on center
     * @see java.awt.Component#getLocation
     */
    public static Point getFrameOnCenter(Window parent, Window child) {
        if (parent != null) {
            return FrameUtils.getFrameOnParentCenterLocationPoint(parent, child);
        } else {
            return FrameUtils.getFrameOnCenterLocationPoint(child);
        }
    }

    public static void addActionListenerToAll(Component parent, ActionListener listener) {
        if (parent instanceof AbstractButton) {
            ((AbstractButton) parent).addActionListener(listener);
        } else if (parent instanceof JComboBox) {
            ((JComboBox<?>) parent).addActionListener(listener);
        }

        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).addChangeListener(e -> listener.actionPerformed(null));
        }

        if (parent instanceof JTextField) {
            ((JTextField) parent).addActionListener(e -> listener.actionPerformed(null));
        }

        if (parent instanceof JTable) {
            ((JTable) parent).getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    listener.actionPerformed(null);
                }
            });
        }

        if (parent instanceof Container) {
            // recursively map child components
            Component[] comps = ((Container) parent).getComponents();
            for (Component c : comps) {
                addActionListenerToAll(c, listener);
            }
        }
    }


    public static void onShowImage(Window parent, BufferedImage image, String title) {
        final ImageViewer imageViewer = new ImageViewer(image);
        imageViewer.setPixelatedZoom(true);

        final JFrame imageFrame = new JFrame();
        imageFrame.setTitle(title);
        imageFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(FrameUtils.class.getResource("/img/gui/picture64.png")));
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        imageFrame.add(imageViewer.getComponent());

        imageFrame.setSize(FrameUtils.DEFAULT_DIMENSION);
        imageFrame.setMinimumSize(new Dimension(256, 256));
        imageFrame.setLocation(FrameUtils.getFrameOnCenter(parent, imageFrame));

        imageFrame.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {//FIXME zooming not working correctly
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) {
                        imageViewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
                        imageViewer.setZoomFactor(imageViewer.getZoomFactor() + 0.1);
                    } else {
                        imageViewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
                        imageViewer.setZoomFactor(imageViewer.getZoomFactor() - 0.1);
                    }
                }
            }
        });

        imageFrame.setVisible(true);
    }
}
