package com.rvkernel.manager;

import java.io.IOException;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

public class RvRoot {
    
    public static boolean RootAccess() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            process.getOutputStream().close();
            int resultCode = process.waitFor();
            return resultCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
    
    public static void RootAccessDenied(Activity activity) {
        hideContent(activity);
        new AlertDialog.Builder(activity, R.style.RoundedDialog)
                .setTitle("Need Root Access")
                .setMessage("Root access is required to use all features.")
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