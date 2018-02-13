package com.mmz.specs.application.utils;

import com.mmz.specs.application.core.ApplicationArgumentsConstants;
import com.mmz.specs.application.core.server.Server;
import com.mmz.specs.application.core.server.ServerStartException;
import com.mmz.specs.application.gui.server.ServerMainWindow;
import com.mmz.specs.application.managers.ModeManager;
import com.mmz.specs.application.managers.SettingsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.Arrays;

public class CoreUtils {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static void manageArguments(final String[] args) {
        if (args.length > 0) {
            final String firstArgument = args[0].toLowerCase();
            log.debug("Got arguments: " + Arrays.toString(args));
            switch (firstArgument) {
                case ApplicationArgumentsConstants.CLIENT:
                    log.debug("Argument is for " + ModeManager.MODE.CLIENT);
                    break;
                case ApplicationArgumentsConstants.SERVER:
                    log.debug("Argument is for " + ModeManager.MODE.SERVER);
                    SettingsManager settingsManager = SettingsManager.getInstance();
                    ServerMainWindow serverMainWindow = new ServerMainWindow();
                    serverMainWindow.setVisible(true);
                    try {
                        Server server = new Server();
                    } catch (ServerStartException e) {
                        log.warn("Could not start server in background", e);

                    }
                    break;
                default:
                    log.debug("Unknown argument: " + firstArgument);
                    log.debug("Starting default mode: " + ModeManager.DEFAULT_MODE);
                    break;
            }
        } else {
            log.debug("Found no arguments. Starting default mode: " + ModeManager.DEFAULT_MODE);
            ModeManager.setCurrentMode(ModeManager.MODE.CLIENT);
        }
    }

    /**
     * Enables LookAndFeel for current OS.
     *
     * @see javax.swing.UIManager.LookAndFeelInfo
     */
    public static void enableLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.warn("Unable to enable LookAndFeel", e);
        }
    }
}
