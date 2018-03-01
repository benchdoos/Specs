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

package com.mmz.specs.application.core.server.service;

import com.mmz.specs.application.utils.CommonUtils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

import static com.mmz.specs.application.utils.SystemMonitoringInfoUtils.*;

public class ServerMonitoringBackgroundService {
    private static final int MEMORY_LENGTH = 60;
    private static final int MONITORING_TIMER_DELAY = 1000;
    private static ServerMonitoringBackgroundService ourInstance = new ServerMonitoringBackgroundService();

    private static Timer serverStateUpdateTimer;

    private long serverStartDateSeconds = Calendar.getInstance().getTime().getTime() / 1000;

    public ArrayList<Float> getMemoryLoadValues() {
        return memoryLoadValues;
    }

    public ArrayList<Float> getCpuLoadValues() {
        return cpuLoadValues;
    }

    public ArrayList<Float> getCpuLoadByServerValues() {
        return cpuLoadByServerValues;
    }

    public ArrayList<Float> getCpuTemperatureValue() {
        return cpuTemperatureValue;
    }

    private ArrayList<Float> memoryLoadValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuLoadValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuLoadByServerValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuTemperatureValue = new ArrayList<>(MEMORY_LENGTH);


    private ServerMonitoringBackgroundService() {
        ActionListener listener = getMonitorTimerActionListener();
        serverStateUpdateTimer = new Timer(MONITORING_TIMER_DELAY, listener);
        serverStateUpdateTimer.setRepeats(true);
    }

    public static ServerMonitoringBackgroundService getInstance() {
        return ourInstance;
    }

    public void startMonitoring() {
        if (!serverStateUpdateTimer.isRunning()) {
            serverStateUpdateTimer.start();
        }
    }

    public void stopMonitoring() {
        if (serverStateUpdateTimer.isRunning()) {
            serverStateUpdateTimer.stop();
            System.out.println("stopped?");
        }
    }

    private ActionListener getMonitorTimerActionListener() {
        return e -> {
            getOnlineUsersCount();
            /*updateActiveThreadCounterLabel();*/

            updateCpuLoad();
            updateCpuLoadByServer();
            updateMemoryLoad();
            updateTemperature();

            System.out.println("working...");
        };
    }

    private void updateCpuLoadByServer() {
        final double cpuUsageByApplication = getCpuUsageByApplication();
        cpuLoadByServerValues = updateGraphicValue(cpuLoadByServerValues, cpuUsageByApplication);
    }

    private void updateMemoryLoad() {
        final long runtimeUsedMemory = getRuntimeUsedMemory();
        final long runtimeMaxMemory = getRuntimeMaxMemory();

        double usedMemory = CommonUtils.round(runtimeUsedMemory / (double) runtimeMaxMemory * 100, 2);

        memoryLoadValues = updateGraphicValue(memoryLoadValues, usedMemory);
    }

    private void updateCpuLoad() {
        cpuLoadValues = updateGraphicValue(cpuLoadValues, getProcessCpuLoad());
    }

    private void updateTemperature() {
        cpuTemperatureValue = updateGraphicValue(cpuTemperatureValue, getCpuTemperature());
    }

    public long getServerOnlineTimeInSeconds() {
        return Calendar.getInstance().getTime().getTime() / 1000 - serverStartDateSeconds;
    }

    public int getOnlineUsersCount() {
        ServerMainBackgroundService backgroundService = ServerMainBackgroundService.getInstance();
        return backgroundService.getOnlineUsersCount();
    }

    private ArrayList<Float> updateGraphicValue(ArrayList<Float> oldValues, double newValue) {
        if (oldValues.size() != MEMORY_LENGTH) {
            for (int i = 0; i < MEMORY_LENGTH; i++) {
                oldValues.add(0f);
            }
        }

        ArrayList<Float> result = new ArrayList<>(MEMORY_LENGTH);

        if (oldValues.size() == MEMORY_LENGTH) {
            for (int i = 1; i < oldValues.size(); i++) {
                result.add(oldValues.get(i));
            }
            result.add((float) newValue);
        }
        return result;
    }

    ArrayList<Float> getGraphicXAges() {
        ArrayList<Float> result = new ArrayList<>(MEMORY_LENGTH);
        for (int i = 0; i < MEMORY_LENGTH; i++) {
            result.add((float) i);
        }
        return result;
    }
}
