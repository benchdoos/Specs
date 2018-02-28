package com.mmz.specs.application.core.security;

import com.mmz.specs.application.utils.Logging;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecurityManager {
    public static final int MINIMUM_PASSWORD_LENGTH = 6;
    public static final int MINIMUM_PASSWORD_STRENGTH = 6;
    private static final String ENCRYPTION_METHOD = "MD5";
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
        String password = null;
        int count = 0;
        while (!isPasswordStrong(password)) {
            count++;
            char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789").toCharArray();
            String firstPart = RandomStringUtils.random(MINIMUM_PASSWORD_LENGTH / 2, 0, possibleCharacters.length - 1,
                    false, false, possibleCharacters, new SecureRandom());
            String secondPart = RandomStringUtils.random(MINIMUM_PASSWORD_LENGTH / 2, 0, possibleCharacters.length - 1,
                    false, false, possibleCharacters, new SecureRandom());
            password = firstPart + "-" + secondPart;
        }
        if (count > 2) {
            log.warn("Generated password from " + count + "st time");
        }
        return password;
    }

    public static boolean isPasswordStrong(String password) {
        int iPasswordScore = 0;

        if (password == null) {
            return false;
        }

        if (password.length() < MINIMUM_PASSWORD_LENGTH)
            return false;
        else if (password.length() >= 10)
            iPasswordScore += 2;
        else
            iPasswordScore += 1;

        //if it contains one digit, add 2 to total score
        if (password.matches("(?=.*[0-9]).*"))
            iPasswordScore += 2;

        //if it contains one lower case letter, add 2 to total score
        if (password.matches("(?=.*[a-z]).*"))
            iPasswordScore += 2;

        //if it contains one upper case letter, add 2 to total score
        if (password.matches("(?=.*[A-Z]).*"))
            iPasswordScore += 2;

        //if it contains one special character, add 2 to total score
        if (password.matches("(?=.*[~!@#$%^&*()_-]).*"))
            iPasswordScore += 2;

        return iPasswordScore >= MINIMUM_PASSWORD_STRENGTH;

    }
}
