package com.rvkernel.manager;

import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RvDeviceInfo {
	
    public static String RamInfo() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    String[] parts = line.split("\\s+");
                    long ramInfoKb = Long.parseLong(parts[1]);
                    double ramInfoGb = ramInfoKb / 1024.0 / 1024.0;
                    ramInfoGb = Math.ceil(ramInfoGb);
                    return String.format("%.0f GB", ramInfoGb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    
    public static String KernelVersion() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] {"su", "-c", "cat /proc/version"});

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
    
    public static String DeviceCodename() {
        return Build.DEVICE;
    }
}