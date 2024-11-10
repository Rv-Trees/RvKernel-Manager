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
    private final Button btnRvTuning;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RvTuning(Context context, Button btnRvTuning) {
        this.context = context;
        this.btnRvTuning = btnRvTuning;
        initBtnRvTuning();
        updateRvTuningMode();
    }

    public void initBtnRvTuning() {
        if (btnRvTuning != null) {
            btnRvTuning.setOnClickListener(v -> showRvTuningMode());
        }
    }

    private void showRvTuningMode() {
        String[] modes = {"Battery", "Balance", "Gaming", "Performance"};

        new AlertDialog.Builder(context, R.style.RoundedDialog)
                .setTitle("Select Mode")
                .setItems(modes, (dialog, which) -> {
                    String selectedMode = modes[which];
                    btnRvTuning.setText(selectedMode);
                    saveRvTuningMode(selectedMode);
                    executeShellScript(getRvTuningMode(selectedMode));
                })
                .show();
    }

    private String getRvTuningMode(String mode) {
        return mode.toLowerCase() + ".sh";
    }

    private void updateRvTuningMode() {
        String savedMode = loadRvTuningMode();
        btnRvTuning.setText(savedMode);
    }

    private void executeShellScript(String script) {
        File scriptFile = new File(Environment.getExternalStorageDirectory(),
                "RvKernel Manager/RvTuning/" + script);
        
        if (!scriptFile.exists()) {
            showAlert("Script Not Found", "The script " + script + " does not exist.");
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
                    showAlert("Failed", "Error executing the script.");
                } else {
                    mainHandler.post(this::updateRvTuningMode);
                }
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        mainHandler.post(() -> 
            new AlertDialog.Builder(context, R.style.RoundedDialog)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
        );
    }

    private void saveRvTuningMode(String mode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvTuningPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("selected_mode", mode).apply();
    }

    private String loadRvTuningMode() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RvTuningPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("selected_mode", "Select Mode");
    }
}