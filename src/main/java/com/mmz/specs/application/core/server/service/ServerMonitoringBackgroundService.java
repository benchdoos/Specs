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
    private static final int MEMORY_LENGTH = 60 * 10;
    private static final int MONITORING_TIMER_DELAY = 1000;
    private static final int MAXIMUM_LOG_HISTORY_LENGTH = 10000;
    private static volatile ServerMonitoringBackgroundService instance;
    private static Timer serverStateUpdateTimer;
    private final long serverStartDateSeconds = Calendar.getInstance().getTime().getTime() / 1000;
    private ArrayList<Float> memoryLoadValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuLoadValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuLoadByServerValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> cpuTemperatureValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<Float> usersConnectedValues = new ArrayList<>(MEMORY_LENGTH);
    private ArrayList<ServerLogMessage> serverLogMessages = new ArrayList<>(MAXIMUM_LOG_HISTORY_LENGTH);

    private ServerMonitoringBackgroundService() {
        ActionListener listener = getMonitorTimerActionListener();
        serverStateUpdateTimer = new Timer(MONITORING_TIMER_DELAY, listener);
        serverStateUpdateTimer.setRepeats(true);
    }

    public static ServerMonitoringBackgroundService getInstance() {
        ServerMonitoringBackgroundService localInstance = instance;
        if (localInstance == null) {
            synchronized (ServerMonitoringBackgroundService.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ServerMonitoringBackgroundService();
                }
            }
        }
        return localInstance;
    }

    private ActionListener getMonitorTimerActionListener() {
        return e -> {

            updateCpuLoad();
            updateCpuLoadByServer();
            updateMemoryLoad();
            updateTemperature();
            updateUsersCount();

        };
    }

    private void updateCpuLoad() {
        this.cpuLoadValues = updateGraphicValue(this.cpuLoadValues, getProcessCpuLoad());
    }

    private void updateCpuLoadByServer() {
        final double cpuUsageByApplication = getCpuUsageByApplication();
        this.cpuLoadByServerValues = updateGraphicValue(this.cpuLoadByServerValues, cpuUsageByApplication);
    }

    private void updateMemoryLoad() {
        final long runtimeUsedMemory = getRuntimeUsedMemory();
        final long runtimeMaxMemory = getRuntimeMaxMemory();

        double usedMemory = CommonUtils.round(runtimeUsedMemory / (double) runtimeMaxMemory * 100, 2);

        this.memoryLoadValues = updateGraphicValue(this.memoryLoadValues, usedMemory);
    }

    private void updateTemperature() {
        this.cpuTemperatureValues = updateGraphicValue(this.cpuTemperatureValues, getCpuTemperature());
    }

    private void updateUsersCount() {
        this.usersConnectedValues = updateGraphicValue(this.usersConnectedValues, ServerBackgroundService.getInstance().getOnlineUsersCount());
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

    ArrayList<Float> getMemoryLoadValues() {
        return this.memoryLoadValues;
    }

    public ArrayList<Float> getCpuLoadValues() {
        return this.cpuLoadValues;
    }

    public ArrayList<Float> getCpuLoadByServerValues() {
        return this.cpuLoadByServerValues;
    }

    public ArrayList<Float> getCpuTemperatureValues() {
        return this.cpuTemperatureValues;
    }

    public ArrayList<Float> getUsersConnectedValues() {
        return this.usersConnectedValues;
    }

    public void startMonitoring() {
        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Запускается сервис мониторинга состояния сервера",
                ServerLogMessage.ServerLogMessageLevel.INFO));

        if (!serverStateUpdateTimer.isRunning()) {
            serverStateUpdateTimer.start();
        }

        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Успешно запущен сервис мониторинга состояния сервера",
                ServerLogMessage.ServerLogMessageLevel.SUCCESS));
    }

    public void stopMonitoring() {
        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Завершается сервис мониторинга состояния сервера",
                ServerLogMessage.ServerLogMessageLevel.INFO));

        if (serverStateUpdateTimer.isRunning()) {
            serverStateUpdateTimer.stop();
            memoryLoadValues = new ArrayList<>(MEMORY_LENGTH);
            cpuLoadValues = new ArrayList<>(MEMORY_LENGTH);
            cpuLoadByServerValues = new ArrayList<>(MEMORY_LENGTH);
            cpuTemperatureValues = new ArrayList<>(MEMORY_LENGTH);
            usersConnectedValues = new ArrayList<>(MEMORY_LENGTH);

            updateCpuLoad();
            updateCpuLoadByServer();
            updateMemoryLoad();
            updateTemperature();
            updateUsersCount();
        }
        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Успешно завершен сервис мониторинга состояния сервера",
                ServerLogMessage.ServerLogMessageLevel.SUCCESS));
    }

    public void addMessage(ServerLogMessage message) {
        if (serverLogMessages.size() >= 10000) {
            serverLogMessages.clear();
        }
        serverLogMessages.add(message);

    }

    public ArrayList<ServerLogMessage> getServerLogMessages() {
        return serverLogMessages;
    }

    public long getServerOnlineTimeInSeconds() {
        return Calendar.getInstance().getTime().getTime() / 1000 - this.serverStartDateSeconds;
    }

    ArrayList<Float> getGraphicXAges() {
        ArrayList<Float> result = new ArrayList<>(MEMORY_LENGTH);
        for (int i = 0; i < MEMORY_LENGTH; i++) {
            result.add((float) i);
        }
        return result;
    }

    public void clearServerLogMessages() {
        serverLogMessages.clear();
    }
}
