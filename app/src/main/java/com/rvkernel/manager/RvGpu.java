package com.rvkernel.manager;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvGpu {

    private String[] clockTexts;
    private int[] clockValues;

    public void gpuThrottlingSwitch(Context context, Switch gpuThrottlingSwitch) {
        int currentGpuThrottlingValue = loadGpuThrottlingValue();
        gpuThrottlingSwitch.setChecked(currentGpuThrottlingValue == 0);

        gpuThrottlingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 0 : 1;
            setGpuThrottlingValue(value);
        });
    }

    private boolean setGpuThrottlingValue(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/class/kgsl/kgsl-3d0/throttling");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadGpuThrottlingValue() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/class/kgsl/kgsl-3d0/throttling");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showMinGPUfreq(Context context, Button btnMinGPUfreq) {
        loadClockValues();
        int currentClock = loadMinGPUfreq();
        btnMinGPUfreq.setText(getClockText(currentClock));
        btnMinGPUfreq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Minimum GPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMinGPUfreq(selectedValue)) {
                                        btnMinGPUfreq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMinGPUfreq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/class/kgsl/kgsl-3d0/min_clock_mhz");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadMinGPUfreq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/class/kgsl/kgsl-3d0/min_clock_mhz");
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

    public void showMaxGPUfreq(Context context, Button btnMaxGPUfreq) {
        loadClockValues();
        int currentClock = loadMinGPUfreq();
        btnMaxGPUfreq.setText(getClockText(currentClock));
        btnMaxGPUfreq.setOnClickListener(
                v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Maximum GPU Frequency");
                        builder.setItems(
                                clockTexts,
                                (dialog, which) -> {
                                    int selectedValue = clockValues[which];

                                    if (SetMaxGPUfreq(selectedValue)) {
                                        btnMaxGPUfreq.setText(clockTexts[which]);
                                    }
                                });
                        builder.show();
                });
    }

    private boolean SetMaxGPUfreq(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/class/kgsl/kgsl-3d0/max_clock_mhz");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadMaxGPUfreq() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/class/kgsl/kgsl-3d0/max_clock_mhz");
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
                            .exec("su -c cat " + "/sys/class/kgsl/kgsl-3d0/freq_table_mhz");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
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
                clockTexts[i] = freqArray[i] + " MHz";
            }

        } catch (IOException e) {
            e.printStackTrace();
            clockValues = new int[]{-1};
            clockTexts = new String[]{"error"};
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
