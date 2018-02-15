package com.mmz.specs.application.utils;

public class SystemUtils {

    public static String getRealSystemArch() {
        if (SystemUtils.isWindows()) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            return arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
        } else if (SystemUtils.isUnix()) {
            return System.getProperty("os.arch");
        } else {
            return "";
        }
    }

    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase();
    }

    private static boolean isWindows() {
        return (getOsName().contains("win"));
    }

    private static boolean isMac() {
        return (getOsName().contains("mac"));
    }

    private static boolean isUnix() {
        return (getOsName().contains("nix")
                || getOsName().contains("nux")
                || getOsName().contains("aix"));
    }

    private static boolean isSolaris() {
        return (getOsName().contains("sunos"));
    }
}
