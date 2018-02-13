package com.mmz.specs.application.managers;

public class ModeManager {
    private static MODE currentMode;
    public static final MODE DEFAULT_MODE = MODE.CLIENT;

    public static MODE getCurrentMode() {
        return currentMode;
    }

    public static void setCurrentMode(MODE currentMode) {
        ModeManager.currentMode = currentMode;
    }

    public enum MODE {CLIENT, SERVER}
}
