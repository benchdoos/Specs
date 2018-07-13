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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class CommonUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String substring(final int length, final String text) {
        String result;

        if (text == null) {
            result = "";
            return result;
        }

        if (text.length() > length) {
            if (length >= 3) {
                return text.substring(0, length - 3) + "...";
            } else {
                return text.substring(0, 0) + "...";
            }
        } else return text;

    }

    public static Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

    public static Image iconToImage(Icon icon) {
        return ((ImageIcon) icon).getImage();
    }

    public static BufferedImage getBufferedImage(File img) {
        try {
            BufferedImage in = ImageIO.read(img);

            BufferedImage newImage = new BufferedImage(
                    in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();
            return newImage;
        } catch (IOException e) {
            log.warn("Could not get BufferedImage for: {}", img, e);
        }
        return null;
    }

    public static File getCurrentFile() {
        try {
            return new File(CommonUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ignore) {
        }
        return null;
    }

    public static void openClientGuide() {
        SwingUtilities.invokeLater(() -> {
            try {
                final File currentFile = getCurrentFile();
                if (currentFile != null) {
                    final String path = currentFile.getParentFile().getPath();
                    String pathToUserGuide = path + File.separator + "Client User Guide.pdf";
                    Desktop.getDesktop().open(new File(pathToUserGuide));
                }
            } catch (Exception ignore) {
            }
        });
    }

    public static void openServerGuide() {
        SwingUtilities.invokeLater(() -> {
            try {
                final File currentFile = getCurrentFile();
                if (currentFile != null) {
                    final String path = currentFile.getParentFile().getPath();
                    String pathToUserGuide = path + File.separator + "Server User Guide.pdf";
                    Desktop.getDesktop().open(new File(pathToUserGuide));
                }
            } catch (Exception ignore) {
            }
        });
    }


    public static String getCurrentInetAddress() {
        String result = "Unknown";
        try {
            InetAddress address = InetAddress.getLocalHost();
            result = address.getHostName();
        } catch (UnknownHostException ex) {
            log.warn("Could not find out inet address of the machine");
        }
        return result;
    }

    public static void enableAllComponents(Component component, boolean enable) {
        if (component instanceof Container) {
            Container container = (Container) component;
            final Component[] components = container.getComponents();
            for (Component c : components) {
                c.setEnabled(enable);
                enableAllComponents(c, enable);
            }
        } else {
            component.setEnabled(enable);
        }
    }

    public static void rollbackAndCloseSession(Session session) {
        try {
            log.debug("Rolling back transaction");
            session.getTransaction().rollback();
            log.debug("Transaction successfully rolled back");

            log.debug("Closing session");
            session.close();
            log.info("Session successfully closed");
        } catch (Exception e) {
            log.warn("Could not rollback transaction", e);
        }
    }
}
