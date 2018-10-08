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

import com.mmz.specs.io.FileInfo;

import javax.swing.*;
import java.text.SimpleDateFormat;

public class FileInfoWindow extends JDialog {
    private JPanel contentPane;
    private JLabel descriptionLabel;
    private JLabel createdLabel;
    private JLabel authorLabel;
    private JLabel sizeLabel;
    private JLabel imageLabel;
    private FileInfo fileInfo;

    public FileInfoWindow(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        initGui();
        fillInfo();

    }

    private void fillInfo() {
        if (fileInfo != null) {
            if (fileInfo.getImage() != null) {
                imageLabel.setText("");
                imageLabel.setIcon(fileInfo.getImage());
            }

            descriptionLabel.setText(fileInfo.getDescription());

            String datePattern = "dd.MM.yyyy HH.mm";
            SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

            createdLabel.setText(dateFormatter.format(fileInfo.getCreated()));
            authorLabel.setText(fileInfo.getAuthor());
            sizeLabel.setText(fileInfo.getSize() + " байт");
        }
    }

    private void initGui() {
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
