package com.rvkernel.manager;

import android.content.Context;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvLittleCPU {

    private String[] clockTexts;
    private int[] clockValues;

    public void showMinCPU0freq(Context context, Button btnMinCPU0freq) {
        loadClockValues();
        int currentClock = loadMinCPU0freq();
        btnMinCPU0freq.setText(getClockText(currentClock));
        btnMinCPU0freq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Minimum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMinCPU0freq(selectedValue)) {
                                        btnMinCPU0freq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMinCPU0freq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadMinCPU0freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clockValues[0];
    }

    public void showMaxCPU0freq(Context context, Button btnMaxCPU0freq) {
        loadClockValues();
        int currentClock = loadMaxCPU0freq();
        btnMaxCPU0freq.setText(getClockText(currentClock));
        btnMaxCPU0freq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Maximum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMaxCPU0freq(selectedValue)) {
                                        btnMaxCPU0freq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMaxCPU0freq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadMaxCPU0freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clockValues[0];
    }

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