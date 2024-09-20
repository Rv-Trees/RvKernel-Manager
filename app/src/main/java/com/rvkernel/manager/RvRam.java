package com.rvkernel.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RvRam {

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
}