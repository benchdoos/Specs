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

package com.mmz.specs.application.gui.client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.client.ClientConstants;
import com.mmz.specs.application.core.security.SecurityManager;
import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.DetailEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TooManyListenersException;

import static com.mmz.specs.application.core.ApplicationConstants.TMP_IMAGE_FOLDER;
import static com.mmz.specs.application.utils.SupportedExtensionsConstants.SUPPORTED_IMAGE_EXTENSIONS;

public class EditImageWindow extends JDialog {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final String NO_IMAGE = "Нет изображения";

    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel detailIconLabel;
    private JButton uploadButton;
    private JButton deleteButton;
    private JButton restoreImageButton;
    private DetailEntity detailEntity;

    public EditImageWindow(DetailEntity detailEntity) {
        this.detailEntity = detailEntity;

        if (detailEntity == null) {
            return;
        }
        initGui();

        initDragAndDrop();

        initListeners();

        updateIcon();

        initKeyBindings();

        pack();
        setResizable(false);
    }

    private void initGui() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setModal(true);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/pictureEdit64.png")));
        setTitle("Редактирование изображения");
    }

    private void initKeyBindings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onPaste(), KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void updateIcon() {
        String imagePath = detailEntity.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            FtpUtils ftp = FtpUtils.getInstance();
            BufferedImage image = ftp.getImage(detailEntity.getId());
            updateIconFromBufferedImage(image);
        } else {
            if (!detailEntity.getImagePath().equalsIgnoreCase(ClientConstants.IMAGE_REMOVE_KEY)) {
                File file = new File(imagePath);
                try {
                    BufferedImage image = ImageIO.read(file);
                    updateIconFromBufferedImage(image);
                } catch (IOException e) {
                    log.warn("Could not load image for entity: {}", detailEntity, e);
                }
            } else {
                updateIconFromBufferedImage(null);
            }
        }
    }

    private void initListeners() {
        uploadButton.addActionListener(e -> onUpload());
        deleteButton.addActionListener(e -> onDelete());
        restoreImageButton.addActionListener(e -> onRestoreImage());
        buttonOK.addActionListener(e -> onOK());
    }

    private void onRestoreImage() {
        detailEntity.setImagePath(null);
        updateIcon();
    }

    private void onPaste() {
        try {
            log.debug("Pasting image from clipboard");
            Image image = CommonUtils.getImageFromClipboard();
            if (image != null) {
                final String filename = SecurityManager.generatePassword();
                final boolean ignore = new File(TMP_IMAGE_FOLDER).mkdirs();

                File file = new File(TMP_IMAGE_FOLDER + File.separator + filename + ".jpg");
                log.debug("TMP image file location: " + file);
                ImageIO.write(CommonUtils.getBufferedImage(image), "jpg", file);
                detailEntity.setImagePath(file.getAbsolutePath());
                updateIcon();
            }
        } catch (Exception e) {
            log.warn("Could not paste image from clipboard", e);
        }
    }

    private void onOK() {

        dispose();
    }

    private void updateIconFromBufferedImage(BufferedImage image) {
        if (image != null) {
            BufferedImage scaledImage = Scalr.resize(image, 128);
            detailIconLabel.setIcon(new ImageIcon(scaledImage));
            detailIconLabel.setText("");

            FrameUtils.removeAllComponentListeners(detailIconLabel);

            detailIconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        FrameUtils.onShowImage(FrameUtils.findWindow(EditImageWindow.super.getRootPane()), true, image,
                                "Изображение " + detailEntity.getCode() + detailEntity.getDetailTitleByDetailTitleId().getTitle());
                    }
                }
            });
        } else {
            detailIconLabel.setIcon(null);
            setDefaultImage();
        }
    }

    private void onUpload() {
        JFileChooser chooser = new JFileChooser(new File(ApplicationConstants.USER_HOME_LOCATION));
        chooser.setDialogTitle("Выберите изображение (" + Arrays.toString(SUPPORTED_IMAGE_EXTENSIONS) + ")");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        FileFilter fileFilter = new FileNameExtensionFilter("Изображения "
                + Arrays.toString(SUPPORTED_IMAGE_EXTENSIONS),
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(fileFilter);
        int returnValue = chooser.showDialog(this, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.exists()) {
                if (file.isFile()) {
                    if (file.length() <= FtpUtils.MAX_IMAGE_FILE_SIZE) {
                        log.debug("User dropped a file: {}", file);
                        detailEntity.setImagePath(file.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Размер файла изображения больше 5 мегабайт!",
                                "Ошибка", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        updateIcon();
    }

    private void setDefaultImage() {
        Icon icon = detailIconLabel.getIcon();
        detailIconLabel.setBorder(BorderFactory.createEmptyBorder());
        if (icon == null) {
            detailIconLabel.setText(NO_IMAGE);
            detailIconLabel.setIcon(null);
        } else {
            detailIconLabel.setText(null);
            detailIconLabel.setIcon(icon);
        }
    }

    private void initDragAndDrop() {
        Component component = this;
        final DropTarget dropTarget = new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);

                    final Object transferData = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    List<?> list = (List<?>) transferData;
                    if (list.size() == 1) {
                        if (list.get(0) instanceof File) {
                            File file = (File) list.get(0);
                            if (file.exists() && FtpUtils.getInstance().isImage(file)) {
                                if (file.length() <= FtpUtils.MAX_IMAGE_FILE_SIZE) {
                                    detailEntity.setImagePath(file.getAbsolutePath());
                                } else {
                                    JOptionPane.showMessageDialog(component,
                                            "Размер файла изображения больше 5 мегабайт!",
                                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(component, "Возможна загрузка только изображений форматов:\n" +
                                        Arrays.toString(SUPPORTED_IMAGE_EXTENSIONS), "Ошибка загрузки", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        updateIcon();
                    }
                } catch (Exception ex) {
                    log.warn("Could not drag and drop image", ex);
                } finally {
                    detailIconLabel.setBorder(BorderFactory.createEmptyBorder());
                }
            }
        };
        try {
            dropTarget.addDropTargetListener(new DropTargetAdapter() {

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    setDefaultImage();
                }

                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                    detailIconLabel.setBorder(new LineBorder(Color.RED, 5));
                    detailIconLabel.setText("Бросить сюда");
                    detailIconLabel.setIcon(null);
                    super.dragEnter(dtde);
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                    setDefaultImage();
                    super.dragExit(dte);
                }
            });
        } catch (TooManyListenersException e) {
            log.warn("Can not init drag and drop dropTarget", e);
        }
        contentPane.setDropTarget(dropTarget);
    }

    private void onDelete() {
        detailEntity.setImagePath(ClientConstants.IMAGE_REMOVE_KEY);
        updateIcon();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        restoreImageButton = new JButton();
        restoreImageButton.setIcon(new ImageIcon(getClass().getResource("/img/gui/refresh.png")));
        restoreImageButton.setText("Восстановить");
        restoreImageButton.setMnemonic('В');
        restoreImageButton.setDisplayedMnemonicIndex(0);
        panel1.add(restoreImageButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        detailIconLabel = new JLabel();
        detailIconLabel.setForeground(new Color(-10395295));
        detailIconLabel.setHorizontalAlignment(0);
        detailIconLabel.setHorizontalTextPosition(0);
        detailIconLabel.setText("Нет изображения");
        panel3.add(detailIconLabel, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        uploadButton = new JButton();
        uploadButton.setText("Загрузить");
        uploadButton.setMnemonic('З');
        uploadButton.setDisplayedMnemonicIndex(0);
        panel3.add(uploadButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Удалить");
        deleteButton.setMnemonic('У');
        deleteButton.setDisplayedMnemonicIndex(0);
        panel3.add(deleteButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}