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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FtpUtils {
    public static final int MAX_IMAGE_FILE_SIZE = 1024 * 1024 * 5; //5MB
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());


    private static final FtpUtils ourInstance = new FtpUtils();
    private static final int FTP_PORT = 21;
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    private static int getImageCounter = 0;
    private final FTPClient ftpClient = new FTPClient();
    private String postfix;
    private String connectionUrl;
    private String username;
    private String password;

    private FtpUtils() {
    }

    public static FtpUtils getInstance() {
        return ourInstance;
    }


    public void connect(String connectionUrl, String username, String password) {
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;

        log.info("Creating FTP connection to " + connectionUrl
                + " for " + username + " with password length: " + password.length());
        try {
            ftpClient.connect(connectionUrl, FTP_PORT);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("Operation failed. Server reply code: " + replyCode);
                return;
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
            }
        } catch (Exception e) {
            log.warn("Could not establish FTP connection to " + connectionUrl
                    + " for " + username + " with password length: " + password.length(), e);
        }
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public BufferedImage getImage(int id) {
        getImageCounter++;
        log.info("Loading image for DetailEntity id: " + id);

        if (getImageCounter <= 5) {
            if (!ftpClient.isConnected()) return null;
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

                log.debug("Loading image at path: " + connectionUrl + postfix + id + DEFAULT_IMAGE_EXTENSION);
                ftpClient.retrieveFile(postfix + id + DEFAULT_IMAGE_EXTENSION, output);
                byte[] data = output.toByteArray();
                ByteArrayInputStream input = new ByteArrayInputStream(data);
                BufferedImage image = ImageIO.read(input);

                log.info("Got image for DetailEntity id: " + id + " " + image);

                input.close();
                getImageCounter = 0;
                return image;
            } catch (IOException e) {
                log.warn("Could not return image, trying to create connection again", e);
                connect(connectionUrl, username, password);
                return getImage(id);
            }
        } else return null;
    }


    public boolean uploadImage(int id, String path) throws IOException {
        /*log.debug("Uploading image from path: " + path + " by id: " + id);
        final FileInputStream local = new FileInputStream(path);
        boolean result = ftpClient.storeFile(postfix + id + DEFAULT_IMAGE_EXTENSION, local);
        local.close();
        return result;*/

        File localFile = new File(path);
        String remoteFile = postfix + id + DEFAULT_IMAGE_EXTENSION;

        log.debug("Uploading image for id:{} exists:{} with path:{} ", id, localFile.exists(), path);
        byte[] bytesIn = new byte[4096];
        int read = 0;

        try (InputStream inputStream = new FileInputStream(localFile);
             OutputStream outputStream = ftpClient.storeFileStream(remoteFile)) {
            while ((read = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
        }
        return ftpClient.completePendingCommand();
    }

    public void deleteImage(int id) throws IOException {
        log.debug("Removing image by id: {}" + id);
        ftpClient.deleteFile(postfix + id + DEFAULT_IMAGE_EXTENSION);
        log.info("Image was successfully removed: " + postfix + id + DEFAULT_IMAGE_EXTENSION);
    }
}
