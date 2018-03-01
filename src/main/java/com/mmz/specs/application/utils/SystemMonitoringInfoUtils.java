package com.mmz.specs.application.utils;

import com.sun.management.OperatingSystemMXBean;
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
    private static final int PROCESS_ID = OPERATING_SYSTEM.getProcessId();
    private static final OSProcess PROCESS = OPERATING_SYSTEM.getProcess(PROCESS_ID);
    private static final int LOGICAL_PROCESSOR_COUNT = processor.getLogicalProcessorCount();
    private static long previousProcessTime = -1;
    private static long HARDWARE_TOTAL_RAM_MEMORY = HARDWARE_ABSTRACTION_LAYER.getMemory().getTotal();


    public static CentralProcessor getProcessor() {
        return processor;
    }

    public static double getProcessCpuLoad() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double result = operatingSystemMXBean.getSystemCpuLoad();

        // returns a percentage value with 6 decimal point precision
        return ((int) (result * 1000) / 10.00);
    }

    public static int getApplicationCurrentThreads() {
        return Thread.activeCount();
    }

    public static long getSystemTotalMemory() {
        return HARDWARE_TOTAL_RAM_MEMORY / MEGABYTE;
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
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double result = operatingSystemMXBean.getProcessCpuLoad();

        // returns a percentage value with 2 decimal point precision
        return ((int) (result * 1000) / 10.00);
    }
}
