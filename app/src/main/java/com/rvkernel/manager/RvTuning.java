package com.rvkernel.manager;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class RvTuning {

    private final Context context;
    private Button rvTuningButton;

    public RvTuning(Context context) {
        this.context = context;
    }
    
    public RvTuning(Context context, Button rvTuningButton) {
        this.context = context;
        this.rvTuningButton = rvTuningButton;
    }

    public void initRvTuningButton() {
        if (rvTuningButton != null) {
            rvTuningButton.setOnClickListener(v -> showRvTuningModeDialog());
        }
    }

    private void showRvTuningModeDialog() {
        String[] modes = {"Battery", "Balance", "Gaming", "Performance"};

        new AlertDialog.Builder(context, R.style.RoundedDialog)
                .setTitle("Select Mode")
                .setItems(modes, (dialog, which) -> {
                    String selectedMode = modes[which];
                    if (rvTuningButton != null) {
                        rvTuningButton.setText(selectedMode);
                    }
                    saveSelectedMode(selectedMode);
                    String script = getRvTuningScript(selectedMode);
                    executeShellScript(script);
                })
                .create()
                .show();
    }

    public String getRvTuningScript(String mode) {
        switch (mode) {
            case "Battery": return "battery.sh";
            case "Balance": return "balance.sh";
            case "Gaming": return "gaming.sh";
            case "Performance": return "performance.sh";
            default: return null;
        }
    }

    public void updateButtonWithSavedMode() {
        if (rvTuningButton != null) {
            new Handler(Looper.getMainLooper()).post(() -> rvTuningButton.setText(loadSelectedMode()));
        }
    }

    public void executeShellScript(String script) {
        if (script == null) return;

        new Thread(() -> {
            File scriptFile = new File(Environment.getExternalStorageDirectory(),
                    "RvKernel Manager/RvTuning/" + script);
            if (scriptFile.exists()) {
                runScript(scriptFile);
            } else {
                showAlertDialog("Script Not Found", "The script " + script + " does not exist.");
            }
        }).start();
    }

    private void runScript(File scriptFile) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            try (DataOutputStream os = new DataOutputStream(process.getOutputStream())) {
                os.writeBytes("sh '" + scriptFile.getAbsolutePath() + "'\n");
                os.writeBytes("exit\n");
                os.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                process.waitFor();
            }
        } catch (Exception e) {
            showAlertDialog("", e.getMessage());
        }
    }

    private void showAlertDialog(String title, String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                new AlertDialog.Builder(context, R.style.RoundedDialog)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show());
    }

    public void saveSelectedMode(String mode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvKernelPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("selected_mode", mode).apply();
    }

    public String loadSelectedMode() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvKernelPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("selected_mode", "Select Mode");
    }
}