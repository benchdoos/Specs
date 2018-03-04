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