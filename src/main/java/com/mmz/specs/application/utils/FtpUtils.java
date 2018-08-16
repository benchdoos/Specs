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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

import static com.mmz.specs.application.utils.SupportedExtensionsConstants.FTP_IMAGE_FILE_EXTENSION;
import static com.mmz.specs.application.utils.SupportedExtensionsConstants.SUPPORTED_IMAGE_EXTENSIONS;

public class FtpUtils {
    public static final int MAX_IMAGE_FILE_SIZE = 1024 * 1024 * 5; //5MB

    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final int FTP_PORT = 21;
    private static volatile FtpUtils instance;
    private final FTPClient ftpClient = new FTPClient();
    private int counter = 0;
    private String postfix;
    private String connectionUrl;
    private String username;
    private String password;

    private FtpUtils() {
    }

    public static FtpUtils getInstance() {
        FtpUtils localInstance = instance;
        if (localInstance == null) {
            synchronized (FtpUtils.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FtpUtils();
                }
            }
        }
        return localInstance;
    }


    public void connect(String connectionUrl, String username, String password) {
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;


        new Thread(() -> createFtpConnection(connectionUrl, username, password)).start();
    }

    private void createFtpConnection(String connectionUrl, String username, String password) {
        if (counter > 20) {
            log.debug("Creating FTP connection at: {} user: {} password: ({}) postfix: {}",
                    connectionUrl, username, password.length(), postfix);
        }
        try {
            ftpClient.connect(connectionUrl, FTP_PORT);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("Operation failed. Server reply code: " + replyCode);
            }
            boolean success = ftpClient.login(username, password);
            if (!success) {
                log.warn("Could not login to FTP at " + connectionUrl
                        + " by " + username + " with password length: " + password.length());
            } else {
                log.info("Logged in to ftp server: " + connectionUrl
                        + " by " + username + " with password length: " + password.length());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);

                ftpClient.enterLocalPassiveMode();

                log.info("FTP successfully connected");
            }
        } catch (Exception e) {
            if (counter > 20) {
                log.warn("Could not establish FTP connection to " + connectionUrl
                        + " for " + username + " with password length: " + password.length(), e);
                counter = 0;
            }
            counter++;
        }
    }

    public boolean isConnected() {
        return ftpClient.isConnected() && ftpClient.isAvailable();
    }

    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public synchronized BufferedImage getImage(int id) {
        final String path = postfix + id + FTP_IMAGE_FILE_EXTENSION;

        log.debug("Loading image for detail with id: {}, path should be: {}", id, path);
        log.debug("FTP is connected: {}", ftpClient.isConnected());

        if (!ftpClient.isConnected()) return null;

        ByteArrayInputStream input = null;

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            retrieveFile(id, output);
            input = new ByteArrayInputStream(output.toByteArray());
            BufferedImage image = ImageIO.read(input);

            String imageInfo = getImageInfo(image);
            log.info("Got image for detail with id: {} image: {}", id, imageInfo);

            input.close();
            return image;
        } catch (IOException e) {
            log.warn("Could not load image, checking connection", e);
            final boolean connected = ftpClient.isConnected();
            log.debug("FTP connected: {}", connected);
            if (!connected) {
                connect(connectionUrl, username, password);
            }

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                log.warn("Could not close input stream", e);
            }
        }
        log.warn("Could not load image for id: {}, on path: {}", id, path);
        return null;
    }

    private void retrieveFile(int id, ByteArrayOutputStream output) throws IOException {
        try {
            ftpClient.retrieveFile(postfix + id + FTP_IMAGE_FILE_EXTENSION, output);
        } catch (IOException e) {
            ftpClient.disconnect();
            createFtpConnection(connectionUrl, username, password);
            ftpClient.retrieveFile(postfix + id + FTP_IMAGE_FILE_EXTENSION, output);
        }
    }

    private String getImageInfo(BufferedImage image) {
        if (image != null) {
            return image.getWidth() + "x" + image.getHeight();
        }
        return null;
    }

    public void uploadImage(int id, File localFile) throws IOException {
        if (localFile == null) {
            throw new IllegalArgumentException("Uploading local file can not be null");
        }
        if (!localFile.exists()) {
            throw new IllegalArgumentException("File does not exist! id: " + id + " path: " + localFile);
        }

        if (localFile.length() > MAX_IMAGE_FILE_SIZE) {
            throw new IOException("File " + localFile + " size (" + localFile.length() + " bites)is bigger then maximum (" + MAX_IMAGE_FILE_SIZE + " bites)");
        }

        if (!isImage(localFile)) {
            throw new IOException("Uploading image should be one of this extensions: " + Arrays.toString(SUPPORTED_IMAGE_EXTENSIONS) + " extension");
        }

        String remoteFile = postfix + id + FTP_IMAGE_FILE_EXTENSION;

        log.debug("Uploading image for id:{} exists:{} with path:{} to path:{}", id, localFile.exists(), localFile, remoteFile);
        byte[] bytesIn = new byte[4096];
        int read;


        deleteImage(id);

        try (InputStream inputStream = new FileInputStream(localFile);
             OutputStream outputStream = ftpClient.storeFileStream(remoteFile)) {
            while ((read = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
        }
        ftpClient.completePendingCommand();
    }

    public boolean isImage(File file) {
        try {
            if (file != null && file.isFile()) {
                if (file.exists()) {
                    for (String extension : SUPPORTED_IMAGE_EXTENSIONS) {
                        final String filePath = file.getAbsolutePath().toLowerCase();
                        if (filePath.contains(extension)) {
                            return isFileAnImage(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not detect, if file is an image:{}", file, e);
        }
        return false;
    }

    private boolean isFileAnImage(File file) {
        if (file != null && file.exists()) {
            String mimeType = new MimetypesFileTypeMap().getContentType(file);
            try {
                String type = mimeType.split("/")[0];
                log.debug("Mimetype for file {} is: {} and full is:{}", file, type, mimeType);
                if (file.getAbsolutePath().toLowerCase().endsWith("png")) {
                    return mimeType.contains("application/octet-stream");
                } else {
                    return type.equalsIgnoreCase("image");
                }
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    public void deleteImage(int id) throws IOException {
        log.debug("Removing image by id: {}" + id);
        ftpClient.deleteFile(postfix + id + FTP_IMAGE_FILE_EXTENSION);
        log.info("Image was successfully removed: " + postfix + id + FTP_IMAGE_FILE_EXTENSION);
    }
}
