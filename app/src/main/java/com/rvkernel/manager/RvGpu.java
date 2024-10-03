package com.rvkernel.manager;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvGpu {

    private String[] boostTexts = {"Off", "Low", "Medium", "High"};
    private int[] boostValues = {0, 1, 2, 3};

    public void showAdrenoBoostMode(Context context, Button btnAdrenoBoostMode) {
        int currentMode = loadAdrenoBoostMode();
        btnAdrenoBoostMode.setText(boostTexts[currentMode]);

        btnAdrenoBoostMode.setOnClickListener(
                v -> {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(context, R.style.RoundedDialog);
                    builder.setTitle("Adreno Boost");

                    builder.setItems(
                            boostTexts,
                            (dialog, which) -> {
                                int selectedValue = boostValues[which];

                                if (setAdrenoBoostMode(selectedValue)) {
                                    btnAdrenoBoostMode.setText(boostTexts[which]);
                                } else {
                                    btnAdrenoBoostMode.setText("error");
                                }
                            });

                    builder.show();
                });
    }

    private boolean setAdrenoBoostMode(int value) {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec(
                                    "su -c echo " + value + " > " + "/sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost");
            process.waitFor();

            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadAdrenoBoostMode() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost");
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
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateAdrenoBoostModeRT;

    public void startAdrenoBoostPolling(final Button btnAdrenoBoostMode) {
        updateAdrenoBoostModeRT =
                new Runnable() {
                    @Override
                    public void run() {
                        int currentAdrenoBoostMode = loadAdrenoBoostMode();
                        btnAdrenoBoostMode.setText(
                                boostTexts[
                                        currentAdrenoBoostMode]);
                        handler.postDelayed(this, 1000);
                    }
                };

        handler.post(updateAdrenoBoostModeRT);
    }

    public void stopAdrenoBoostPolling() {
        if (handler != null && updateAdrenoBoostModeRT != null) {
            handler.removeCallbacks(updateAdrenoBoostModeRT);
        }
    }

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
}
