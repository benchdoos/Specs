package com.mmz.specs.application.core;

import com.mmz.specs.application.utils.Logging;

public class Main {

    public static void main(final String[] args) {
        //Main.args = args;
        //SpringApplication.run(Main.class, args);

//        setHeadless();

        new Logging(args);

        /*ApplicationContext ctx = SpringApplication.run(Main.class, args);*/
        /*ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("/spring/ApplicationContext.xml");
        Application bean = ctx.getBean(Application.class);
        bean.initApplication(args);*/
        Application application = new Application();
        application.initApplication(args);
    }

    /**
     * Sets if the application is headless and should not instantiate AWT. Defaults to
     * {@code true} to prevent java icons appearing.
     *
     */
    private static void setHeadless() {
        final String HEADLESS_PROPERTY_KEY = "java.awt.headless";
        final boolean HEADLESS_DEFAULT_PROPERTY_VALUE = false;
        System.setProperty(HEADLESS_PROPERTY_KEY, Boolean.toString(HEADLESS_DEFAULT_PROPERTY_VALUE));
    }
}