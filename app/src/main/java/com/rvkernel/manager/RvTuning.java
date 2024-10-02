package com.rvkernel.manager;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import java.io.DataOutputStream;
import java.io.File;

public class RvTuning {

    private final Context context;
    private final Button rvTuningButton;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RvTuning(Context context, Button rvTuningButton) {
        this.context = context;
        this.rvTuningButton = rvTuningButton;
        initRvTuningButton();
        updateButtonWithSavedMode();
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
                    rvTuningButton.setText(selectedMode);
                    saveSelectedMode(selectedMode);
                    executeShellScript(getRvTuningScript(selectedMode));
                })
                .show();
    }

    private String getRvTuningScript(String mode) {
        return mode.toLowerCase() + ".sh";
    }

    private void updateButtonWithSavedMode() {
        String savedMode = loadSelectedMode();
        rvTuningButton.setText(savedMode);
    }

    private void executeShellScript(String script) {
        File scriptFile = new File(Environment.getExternalStorageDirectory(),
                "RvKernel Manager/RvTuning/" + script);
        
        if (!scriptFile.exists()) {
            showAlertDialog("Script Not Found", "The script " + script + " does not exist.");
            return;
        }

        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("su");
                try (DataOutputStream os = new DataOutputStream(process.getOutputStream())) {
                    os.writeBytes("sh '" + scriptFile.getAbsolutePath() + "'\n");
                    os.writeBytes("exit\n");
                    os.flush();
                }

                process.waitFor();
                if (process.exitValue() != 0) {
                    showAlertDialog("Execution Failed", "Error executing the script.");
                } else {
                    mainHandler.post(this::updateButtonWithSavedMode);
                }
            } catch (Exception e) {
                showAlertDialog("Error", e.getMessage());
            }
        }).start();
    }

    private void showAlertDialog(String title, String message) {
        mainHandler.post(() -> 
            new AlertDialog.Builder(context, R.style.RoundedDialog)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
        );
    }

    private void saveSelectedMode(String mode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvKernelPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("selected_mode", mode).apply();
    }

    private String loadSelectedMode() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvKernelPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("selected_mode", "Select Mode");
    }
}