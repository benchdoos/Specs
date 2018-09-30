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

import com.google.common.io.Resources;
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.model.ConstantsEntity;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.service.ConstantsService;
import com.mmz.specs.service.ConstantsServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.Properties;

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

    public static BufferedImage getBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    private static File getCurrentFile() {
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

    public static void rollbackAndCloseSession(Session session) {
        try {
            SessionUtils.closeSessionSilently(session);
        } catch (Exception e) {
            log.warn("Could not rollback transaction", e);
        }
        log.debug("Unbinding transaction");
        ClientBackgroundService.getInstance().unbindTransaction();
        log.debug("Transaction successfully unbinded");
    }

    public static Image getImageFromClipboard() throws Exception {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
        } else {
            return null;
        }
    }

    public static void deleteFolder(File file) {
        try {
            if (file.isDirectory()) {
                String[] entries = file.list();
                if (entries != null) {
                    for (String s : entries) {
                        File currentFile = new File(file.getPath(), s);
                        final boolean ignore = currentFile.delete();
                    }
                }
            }
            final boolean delete = file.delete();
            if (delete) {
                log.info("Folder {} successfully deleted", file);
            } else {
                log.warn("Could not delete folder: {}", file);
            }
        } catch (Exception e) {
            log.warn("Could not delete folder: {}", file, e);
        }
    }

    public static KeyListener getSmartKeyListener(Component component) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (component != null) {
                        component.dispatchEvent(e);
                    }
                }
            }
        };
    }

    public static String getFirstWordText(MaterialEntity materialByMaterialId) {
        String firstWord;
        try {
            firstWord = materialByMaterialId.getLongProfile().split(" ")[0];
        } catch (Exception ignore) {
            firstWord = materialByMaterialId.getLongProfile();
        }
        return firstWord;
    }

    public static String getLongProfile(MaterialEntity materialByMaterialId) {
        StringBuilder builder = new StringBuilder();
        final String[] split = materialByMaterialId.getLongProfile().split(" ");
        for (int i = 0; i < split.length; i++) {
            if (i > 0) {
                builder.append(split[i]);
                if (i != split.length - 1) {
                    builder.append(" ");
                }
            }
        }
        return builder.toString();
    }

    public static String getHtmlToolTip(MaterialEntity materialByMaterialId) {
        StringBuilder builder = new StringBuilder();

        try {
            URL url = Resources.getResource("html/MaterialFullText.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (IOException e) {
            log.warn("Could not load html for material", e);
        }

        String result = builder.toString();
        if (materialByMaterialId != null) {
            result = result.replace("{LongProfile}", materialByMaterialId.getLongProfile());
            result = result.replace("{LongMark}", materialByMaterialId.getLongMark());
        }

        return result;
    }

    public static Properties getConstantsToProperties(Session session) {
        final ConstantsService service = new ConstantsServiceImpl(session);
        final List<ConstantsEntity> constantsEntities = service.listConstants();
        Properties constants = new Properties();
        for (ConstantsEntity e : constantsEntities) {
            constants.put(e.getKey(), e.getValue());
        }
        return constants;
    }

    public static final String getFileExtension(File file) {
        String extension = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }
        return extension;
    }

    public static String getSmallFileName(File file) {
        String fileName = file.getName();
        if (fileName.length() > 30) {
            return fileName = fileName.substring(0, 27) + "..." + getFileExtension(file);
        }
        return fileName;
    }
}
