package com.mmz.specs.application.core;

import com.mmz.specs.application.utils.CoreUtils;
import com.mmz.specs.application.utils.Logging;

public class Main {

    public static void main(final String[] args) {
        new Logging(args);
        CoreUtils.enableLookAndFeel();
        CoreUtils.localizeFileChooser();
        CoreUtils.manageArguments(args);
    }
}