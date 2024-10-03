package com.rvkernel.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvCpu {

    private String[] clockTexts;
    private int[] clockValues;

    private void loadClockValues() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c cat " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            String[] freqArray = sb.toString().trim().split("\\s+");
            clockValues = new int[freqArray.length];
            clockTexts = new String[freqArray.length];

            for (int i = 0; i < freqArray.length; i++) {
                clockValues[i] = Integer.parseInt(freqArray[i]);
                clockTexts[i] = (clockValues[i] / 1000) + " MHz";
            }

        } catch (IOException e) {
            e.printStackTrace();
            clockValues = new int[] {-1};
            clockTexts = new String[] {"error"};
        }
    }

    private String getClockText(int clockValue) {
        for (int i = 0; i < clockValues.length; i++) {
            if (clockValues[i] == clockValue) {
                return clockTexts[i];
            }
        }
        return "Unknown";
    }
}