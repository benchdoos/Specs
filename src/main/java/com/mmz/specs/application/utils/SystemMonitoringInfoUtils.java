package com.mmz.specs.application.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;

public class SystemMonitoringInfoUtils {
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    public static final HardwareAbstractionLayer HARDWARE_ABSTRACTION_LAYER = SYSTEM_INFO.getHardware();
    public static final OperatingSystem OPERATING_SYSTEM = SYSTEM_INFO.getOperatingSystem();
    private static final int MEGABYTE = 1024 * 1024;
    private static final Sensors SENSORS = HARDWARE_ABSTRACTION_LAYER.getSensors();
    private static final CentralProcessor processor = HARDWARE_ABSTRACTION_LAYER.getProcessor();
    private static long previousProcessTime = -1;

    public static double getProcessCpuLoad() {
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double result = operatingSystemMXBean.getSystemCpuLoad();

        // returns a percentage value with 6 decimal point precision
        return ((int) (result * 1000) / 10.000000);
    }

    public static int getApplicationCurrentThreads() {
        return Thread.activeCount();
    }

    public static long getSystemTotalMemory() {
        long result = HARDWARE_ABSTRACTION_LAYER.getMemory().getTotal();
        return result / MEGABYTE;
    }

    public static long getRuntimeMaxMemory() {
        long result = RUNTIME.maxMemory();
        return result / MEGABYTE;
    }

    public static long getRuntimeFreeMemory() {
        long result = RUNTIME.freeMemory();
        return result / MEGABYTE;
    }

    public static long getRuntimeTotalMemory() {
        long result = RUNTIME.totalMemory();
        return result / MEGABYTE;
    }

    public static long getRuntimeUsedMemory() {
        return getRuntimeTotalMemory() - getRuntimeFreeMemory();
    }

    public static double getCpuTemperature() {
        return SENSORS.getCpuTemperature();
    }

    public static double getCpuVoltage() {
        return SENSORS.getCpuVoltage();
    }

    public static int[] getCpuFanSpeeds() {
        return SENSORS.getFanSpeeds();
    }

    public static double getCpuUsageByApplication() {
        int cpuNumber = processor.getLogicalProcessorCount();
        int pid = OPERATING_SYSTEM.getProcessId();
        OSProcess process = OPERATING_SYSTEM.getProcess(pid);
        String processInfo = "0%";
        long currentTime = 0;

        double cpu = 0.0;
        if (process != null) {
            // CPU
            currentTime = process.getKernelTime() + process.getUserTime();

            if (previousProcessTime != -1) {
                // If we have both a previous and a current time
                // we can calculate the CPU usage
                long timeDifference = currentTime - previousProcessTime;
                cpu = (100d * (timeDifference / ((double) 1000))) / cpuNumber;
                cpu = CommonUtils.round(cpu, 1);
            }

            previousProcessTime = currentTime;
        }
        return cpu;
    }
}
