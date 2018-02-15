package com.mmz.specs.application.gui.server;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ServerMainWindowTest {


    @Test
    public void getServerOnlineString() {
        ServerMainWindow serverMainWindow = new ServerMainWindow();
        long difference = 25 * 60 * 60; // 25 часов == 1 день 1 час :00
        String expected = "1д. 1ч. 0м. 0с.";
        assertEquals(expected, serverMainWindow.getServerOnlineString(difference));


        difference = 2664190; // 30 дней, 20 часов, 3 минуты, 10 сек
        System.out.println(">>"+difference);
        expected = "30д. 20ч. 3м. 10с.";
        assertEquals(expected, serverMainWindow.getServerOnlineString(difference));

        difference = (1600 * 24 * 60 * 60 + 2 * 60 * 60 + 33 * 60 + 2); // 1600 дней, 2 часа, 33 минуты, 2 сек
        expected = "1600д. 2ч. 33м. 2с.";
        assertEquals(expected, serverMainWindow.getServerOnlineString(difference));

        difference = 2147483647; //max long
        expected = "24855д. 3ч. 14м. 7с.";
        assertEquals(expected, serverMainWindow.getServerOnlineString(difference));
    }
}