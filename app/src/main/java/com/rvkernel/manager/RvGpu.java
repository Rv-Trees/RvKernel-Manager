package com.rvkernel.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvGpu {

    private static final String PREFS_NAME = "AdrenoBoostPrefs";
    private static final String KEY_ADRENO_MODE = "adreno_mode";

    private String[] boostDescriptions = {"Off", "Low", "Medium", "High"};
    private int[] boostValues = {0, 1, 2, 3};

    public void showAdrenoBoostDialog(Context context, Button btnSetAdrenoBoost) {
        int currentMode = loadAdrenoBoostFromFile();
        btnSetAdrenoBoost.setText(boostDescriptions[currentMode]);

        btnSetAdrenoBoost.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.RoundedDialog);
                        builder.setTitle("Adreno Boost");

                        builder.setItems(
                                boostDescriptions,
                                (dialog, which) -> {
                                    int selectedValue = boostValues[which];

                                    if (setAdrenoBoost(selectedValue)) {
                                        btnSetAdrenoBoost.setText(boostDescriptions[which]);
                                    }
                                });

                        builder.show();
                    }
                });
    }

    private boolean setAdrenoBoost(int value) {
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

    private int loadAdrenoBoostFromFile() {
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
}
