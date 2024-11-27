package com.rvkernel.manager;

import android.app.Activity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RvKernel {

    public static boolean KernelCheck() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("uname -r");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String kernelName = reader.readLine();
                if (kernelName != null) {
                    for (String requiredKernel : RvMain.REQUIRED_KERNEL) {
                        if (kernelName.contains(requiredKernel)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void KernelNotSupported(Activity activity) {
        hideContent(activity);
        new AlertDialog.Builder(activity, R.style.RoundedDialog)
                .setTitle("Kernel not supported")
                .setMessage("Must use RvKernel Mi8937 v1.4 to use RvKernel Manager.")
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, which) -> activity.finish())
                .setOnDismissListener(dialog -> showContent(activity))
                .show();
    }

    private static void hideContent(Activity activity) {
        activity.findViewById(android.R.id.content).setVisibility(View.GONE);
    }

    private static void showContent(Activity activity) {
        activity.findViewById(android.R.id.content).setVisibility(View.VISIBLE);
    }
}
