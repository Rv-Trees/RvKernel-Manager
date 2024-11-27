package com.rvkernel.manager;

import android.content.Context;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvCPU {

    private String[] clockTexts;
    private int[] clockValues;

    public void showMinCPUfreq(Context context, Button btnMinCPUfreq) {
        loadClockValues();
        int currentClock = loadMinCPUfreq();
        btnMinCPUfreq.setText(getClockText(currentClock));
        btnMinCPUfreq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Minimum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMinCPUfreq(selectedValue)) {
                                        btnMinCPUfreq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMinCPUfreq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMinCPUfreq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq");
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

    public void showMaxCPUfreq(Context context, Button btnMaxCPUfreq) {
        loadClockValues();
        int currentClock = loadMaxCPUfreq();
        btnMaxCPUfreq.setText(getClockText(currentClock));
        btnMaxCPUfreq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Maximum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMaxCPUfreq(selectedValue)) {
                                        btnMaxCPUfreq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMaxCPUfreq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMaxCPUfreq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq");
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
                                    "su -c cat " + "sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies");
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