package com.mmz.specs.application.core.security;

import com.mmz.specs.application.utils.Logging;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecurityManager {
    private static final String ENCRYPTION_METHOD = "MD5";
    private static final int DEFAULT_PASSWORD_GENERATE_LENGTH = 6;
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static String encryptPassword(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(ENCRYPTION_METHOD);
            md5.update(password.getBytes());
            byte[] messageDigestMD5 = md5.digest();
            StringBuilder stringBuffer = new StringBuilder();
            for (byte bytes : messageDigestMD5) {
                stringBuffer.append(String.format("%02x", bytes & 0xff));
            }
            return stringBuffer.toString();

        } catch (NoSuchAlgorithmException e) {
            log.warn("Could not establish " + ENCRYPTION_METHOD + ", returning password unencrypted!");
            return password;
        }
    }

    public static String generatePassword() {
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()-_=+[{]};:,<.>/?").toCharArray();
        String randomStr = RandomStringUtils.random(DEFAULT_PASSWORD_GENERATE_LENGTH, 0, possibleCharacters.length - 1,
                false, false, possibleCharacters, new SecureRandom());
        System.out.println(randomStr);
        return randomStr;
    }
}
