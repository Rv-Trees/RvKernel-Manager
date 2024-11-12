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

    private String[] availableGovernors;
    private String currentGovernor;

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
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", String.valueOf(value), ">", "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMinCPU0freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq");
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
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", String.valueOf(value), ">", "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loadMaxCPU0freq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq");
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
                                    "su -c cat " + "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies");
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

    public void showAvailableLittleGovernors(Context context, Button btnLittleGovernor) {
        loadAvailableGovernors();
        currentGovernor = loadCurrentLittleGovernor();
        btnLittleGovernor.setText(currentGovernor);
        btnLittleGovernor.setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                    builder.setTitle("Available Governors");
                    builder.setItems(
                            availableGovernors,
                            (dialog, which) -> {
                                String selectedGovernor = availableGovernors[which];

                                if (setCurrentGovernor(selectedGovernor)) {
                                    btnLittleGovernor.setText(selectedGovernor);
                                }
                            });
                    builder.show();
                });
    }

    private boolean setCurrentGovernor(String governor) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", governor, ">", "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String loadCurrentLittleGovernor() {
        try {
            Process process = Runtime.getRuntime()
                    .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy0/scaling_governor");
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
                    .exec("su -c cat /sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors");
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
