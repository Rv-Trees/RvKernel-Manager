package com.rvkernel.manager;

import android.content.Context;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvBigCPU {

    private String[] clockTexts;
    private int[] clockValues;

    private String[] availableGovernors;
    private String currentGovernor;

    public void showMinCPU4freq(Context context, Button btnMinCPU4freq) {
        loadClockValues();
        int currentClock = loadMinCPU4freq();
        btnMinCPU4freq.setText(getClockText(currentClock));
        btnMinCPU4freq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Minimum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMinCPU4freq(selectedValue)) {
                                        btnMinCPU4freq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMinCPU4freq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMinCPU4freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq");
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

    public void showMaxCPU4freq(Context context, Button btnMaxCPU4freq) {
        loadClockValues();
        int currentClock = loadMaxCPU4freq();
        btnMaxCPU4freq.setText(getClockText(currentClock));
        btnMaxCPU4freq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Maximum CPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMaxCPU4freq(selectedValue)) {
                                        btnMaxCPU4freq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMaxCPU4freq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMaxCPU4freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq");
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
                            .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy4/scaling_available_frequencies");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            String[] freqArray = sb.toString().trim().split("\\s+");
            clockValues = new int[freqArray.length + 1];
            clockTexts = new String[freqArray.length + 1];

            for (int i = 0; i < freqArray.length; i++) {
                clockValues[i] = Integer.parseInt(freqArray[i]);
                clockTexts[i] = (clockValues[i] / 1000) + " MHz";
            }

            process =
                    Runtime.getRuntime()
                            .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy4/cpuinfo_max_freq");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = reader.readLine();

            if (line != null) {
                int maxFreq = Integer.parseInt(line.trim());
                clockValues[freqArray.length] = maxFreq;
                clockTexts[freqArray.length] =
                        (maxFreq / 1000) + " MHz";
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
        return "unknown";
    }

    public void showAvailableBigGovernors(Context context, Button btnBigGovernor) {
        loadAvailableGovernors();
        currentGovernor = loadCurrentBigGovernor();
        btnBigGovernor.setText(currentGovernor);
        btnBigGovernor.setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                    builder.setTitle("Available Governors");
                    builder.setItems(
                            availableGovernors,
                            (dialog, which) -> {
                                String selectedGovernor = availableGovernors[which];

                                if (setCurrentGovernor(selectedGovernor)) {
                                    btnBigGovernor.setText(selectedGovernor);
                                }
                            });
                    builder.show();
                });
    }

    private boolean setCurrentGovernor(String governor) {
        try {
            Process process = Runtime.getRuntime()
                    .exec("su -c echo " + governor + " > /sys/devices/system/cpu/cpufreq/policy4/scaling_governor");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String loadCurrentBigGovernor() {
        try {
            Process process = Runtime.getRuntime()
                    .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy4/scaling_governor");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return line.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void loadAvailableGovernors() {
        try {
            Process process = Runtime.getRuntime()
                    .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy4/scaling_available_governors");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            availableGovernors = sb.toString().trim().split("\\s+");

        } catch (IOException e) {
            e.printStackTrace();
            availableGovernors = new String[]{"error"};
        }
    }
}
