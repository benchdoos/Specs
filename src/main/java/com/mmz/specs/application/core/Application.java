package com.mmz.specs.application.core;

import com.mmz.specs.application.utils.CoreUtils;

public class Application {
    public void initApplication(final String[] args){
        CoreUtils.enableLookAndFeel();
        CoreUtils.localizeFileChooser();
        CoreUtils.manageArguments(args);
    }
}