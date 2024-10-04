package com.rvkernel.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

public class RvMain extends AppCompatActivity {

    public static final String[] REQUIRED_KERNEL = {
            "4.9.337-RvKernel-Be4-v0.6"
    };

    // GPU
    private RvGpu rvGpu;
    private MaterialButton btnAdrenoBoostMode;
    private MaterialButton btnMinGPUfreq;
    private MaterialButton btnMaxGPUfreq;

    // RvTuning
    private MaterialButton btnRvTuning;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean readGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                        boolean manageGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                                Environment.isExternalStorageManager();

                        if (readGranted || manageGranted) {
                            createRvKernelManagerFolder();
                        } else {
                            showPermissionDeniedDialog();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rvmain);

        if (!RvRoot.RootAccess()) {
            RvRoot.RootAccessDenied(this);
            return;
        }

        if (!RvKernel.KernelCheck()) {
            RvKernel.KernelNotSupported(this);
            return;
        }

        requestStoragePermission();

        // Banner
        ShapeableImageView bannerImageView = findViewById(R.id.rvkernelBanner);
        RvBanner.RvBannerTheme(bannerImageView, this);
        
        // Scheduler
        Switch switchSchedAutoGroup = findViewById(R.id.switchSchedAutoGroup);
        Switch switchSchedChildRunFirst = findViewById(R.id.switchSchedChildRunFirst);

        RvScheduler rvScheduler = new RvScheduler();
        rvScheduler.schedAutoGroupSwitch(this, switchSchedAutoGroup);
        rvScheduler.schedChildRunFirstSwitch(this, switchSchedChildRunFirst);

        // Charging
        Switch switchBypassCharging = findViewById(R.id.switchBypassCharging);
        Switch switchFastCharging = findViewById(R.id.switchFastCharging);
        Switch switchDisableThermalCharging = findViewById(R.id.switchDisableThermalCharging);

        RvCharging rvCharging = new RvCharging();
        rvCharging.bypassChargingSwitch(this, switchBypassCharging);
        rvCharging.fastChargingSwitch(this, switchFastCharging);
        rvCharging.disableThermalChargingSwitch(this, switchDisableThermalCharging);

        // Little Cluster CPU
        MaterialButton btnMinCPU0freq = findViewById(R.id.btnMinCPU0freq);
        MaterialButton btnMaxCPU0freq = findViewById(R.id.btnMaxCPU0freq);

        RvLittleCPU rvLittleCPU = new RvLittleCPU();
        rvLittleCPU.showMinCPU0freq(this, btnMinCPU0freq);
        rvLittleCPU.showMaxCPU0freq(this, btnMaxCPU0freq);

        // Big Cluster CPU
        MaterialButton btnMinCPU4freq = findViewById(R.id.btnMinCPU4freq);
        MaterialButton btnMaxCPU4freq = findViewById(R.id.btnMaxCPU4freq);

        RvBigCPU rvBigCPU = new RvBigCPU();
        rvBigCPU.showMinCPU4freq(this, btnMinCPU4freq);
        rvBigCPU.showMaxCPU4freq(this, btnMaxCPU4freq);

        // GPU
        btnMinGPUfreq = findViewById(R.id.btnMinGPUfreq);
        btnMaxGPUfreq = findViewById(R.id.btnMaxGPUfreq);
        btnAdrenoBoostMode = findViewById(R.id.btnAdrenoBoostMode);
        Switch gpuThrottlingSwitch = findViewById(R.id.gpuThrottlingSwitch);

        rvGpu = new RvGpu();
        rvGpu.showMinGPUfreq(this, btnMinGPUfreq);
        rvGpu.showMaxGPUfreq(this, btnMaxGPUfreq);
        rvGpu.showAdrenoBoostMode(this, btnAdrenoBoostMode);
        rvGpu.gpuThrottlingSwitch(this, gpuThrottlingSwitch);
        
        // RvTuning
        btnRvTuning = findViewById(R.id.rvTuningButton);
        RvTuning rvTuning = new RvTuning(this, btnRvTuning);
        rvTuning.initBtnRvTuning();

        // Device Info
        TextView deviceCodename = findViewById(R.id.deviceCodename);
        TextView ramInfo = findViewById(R.id.ramInfo);
        TextView kernelVersion = findViewById(R.id.kernelVersion);

        deviceCodename.setText(RvDeviceInfo.DeviceCodename());
        ramInfo.setText(RvDeviceInfo.RamInfo());
        kernelVersion.setText(RvDeviceInfo.KernelVersion());
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } else {
                createRvKernelManagerFolder();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            } else {
                createRvKernelManagerFolder();
            }
        } else {
            createRvKernelManagerFolder();
        }
    }

    private void createRvKernelManagerFolder() {
        File rvKernelManagerFolder = new File(Environment.getExternalStorageDirectory(), "RvKernel Manager");
        if (!rvKernelManagerFolder.exists()) {
            rvKernelManagerFolder.mkdirs();
        }

        File rvTuningFolder = new File(rvKernelManagerFolder, "RvTuning");
        if (!rvTuningFolder.exists()) {
            rvTuningFolder.mkdirs();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this, R.style.RoundedDialog)
                .setTitle("Permission Denied")
                .setMessage("Storage permission is required for kernel profile.")
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rvGpu.startUpdateAdrenoBoost(btnAdrenoBoostMode);
        rvGpu.startUpdateMinGPUfreq(btnMinGPUfreq);
        rvGpu.startUpdateMaxGPUfreq(btnMaxGPUfreq);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rvGpu.stopUpdateAdrenoBoost();
        rvGpu.stopUpdateMinGPUfreq();
        rvGpu.stopUpdateMaxGPUfreq();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}