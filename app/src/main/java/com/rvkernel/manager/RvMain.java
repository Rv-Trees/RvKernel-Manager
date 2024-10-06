package com.rvkernel.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RvMain extends AppCompatActivity {

    public static final String[] REQUIRED_KERNEL = {
            "4.9.337-RvKernel-Be4-v0.6"
    };

    private ExecutorService executor;
    private Handler mainHandler;

    // Scheduler
    private RvScheduler rvScheduler;
    private Switch switchSchedAutoGroup;
    private Switch switchSchedChildRunFirst;

    // Charging
    private RvCharging rvCharging;
    private Switch switchBypassCharging;
    private Switch switchFastCharging;
    private Switch switchDisableThermalCharging;

    // Little Cluster CPU
    private RvLittleCPU rvLittleCPU;
    private MaterialButton btnMinCPU0freq;
    private MaterialButton btnMaxCPU0freq;

    // Big Cluster CPU
    private RvBigCPU rvBigCPU;
    private MaterialButton btnMinCPU4freq;
    private MaterialButton btnMaxCPU4freq;

    // GPU
    private RvGpu rvGpu;
    private Switch gpuThrottlingSwitch;
    private MaterialButton btnAdrenoBoostMode;
    private MaterialButton btnMinGPUfreq;
    private MaterialButton btnMaxGPUfreq;

    // RvTuning
    private RvTuning rvTuning;
    private MaterialButton btnRvTuning;

    // Device Info
    private TextView deviceCodename;
    private TextView ramInfo;
    private TextView kernelVersion;

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

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        setupUI();

        // Charging
        switchBypassCharging = findViewById(R.id.switchBypassCharging);
        switchFastCharging = findViewById(R.id.switchFastCharging);
        switchDisableThermalCharging = findViewById(R.id.switchDisableThermalCharging);
        
        rvCharging = new RvCharging();
        rvCharging.bypassChargingSwitch(this, switchBypassCharging);
        rvCharging.fastChargingSwitch(this, switchFastCharging);
        rvCharging.disableThermalChargingSwitch(this, switchDisableThermalCharging);

        // Little Cluster CPU
        btnMinCPU0freq = findViewById(R.id.btnMinCPU0freq);
        btnMaxCPU0freq = findViewById(R.id.btnMaxCPU0freq);

        rvLittleCPU = new RvLittleCPU();
        rvLittleCPU.showMinCPU0freq(this, btnMinCPU0freq);
        rvLittleCPU.showMaxCPU0freq(this, btnMaxCPU0freq);

        // Big Cluster CPU
        btnMinCPU4freq = findViewById(R.id.btnMinCPU4freq);
        btnMaxCPU4freq = findViewById(R.id.btnMaxCPU4freq);

        rvBigCPU = new RvBigCPU();
        rvBigCPU.showMinCPU4freq(this, btnMinCPU4freq);
        rvBigCPU.showMaxCPU4freq(this, btnMaxCPU4freq);

        // GPU
        btnMinGPUfreq = findViewById(R.id.btnMinGPUfreq);
        btnMaxGPUfreq = findViewById(R.id.btnMaxGPUfreq);
        btnAdrenoBoostMode = findViewById(R.id.btnAdrenoBoostMode);
        gpuThrottlingSwitch = findViewById(R.id.gpuThrottlingSwitch);

        rvGpu = new RvGpu();
        rvGpu.showMinGPUfreq(this, btnMinGPUfreq);
        rvGpu.showMaxGPUfreq(this, btnMaxGPUfreq);
        rvGpu.showAdrenoBoostMode(this, btnAdrenoBoostMode);
        rvGpu.gpuThrottlingSwitch(this, gpuThrottlingSwitch);
        
        // RvTuning
        btnRvTuning = findViewById(R.id.rvTuningButton);
        rvTuning = new RvTuning(this, btnRvTuning);
        rvTuning.initBtnRvTuning();

        // Device Info
        deviceCodename = findViewById(R.id.deviceCodename);
        ramInfo = findViewById(R.id.ramInfo);
        kernelVersion = findViewById(R.id.kernelVersion);

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
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
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

    private void setupUI() {
        // Banner
        ShapeableImageView bannerImageView = findViewById(R.id.rvkernelBanner);
        RvBanner.RvBannerTheme(bannerImageView, this);

        // Scheduler
        setupScheduler();
    }

    private void setupScheduler() {
        switchSchedAutoGroup = findViewById(R.id.switchSchedAutoGroup);
        switchSchedChildRunFirst = findViewById(R.id.switchSchedChildRunFirst);

        rvScheduler = new RvScheduler();
        rvScheduler.schedAutoGroupSwitch(this, switchSchedAutoGroup);
        rvScheduler.schedChildRunFirstSwitch(this, switchSchedChildRunFirst);
    }

    private void updateCPUButtonUI() {
        executor.execute(() -> {
            int minCPU0Freq = rvLittleCPU.loadMinCPU0freq();
            int maxCPU0Freq = rvLittleCPU.loadMaxCPU0freq();
            int minCPU4Freq = rvBigCPU.loadMinCPU4freq();
            int maxCPU4Freq = rvBigCPU.loadMaxCPU4freq();

            mainHandler.post(() -> {
                if (btnMinCPU0freq != null) {
                    btnMinCPU0freq.setText((minCPU0Freq / 1000) + " MHz");
                }
                if (btnMaxCPU0freq != null) {
                    btnMaxCPU0freq.setText((maxCPU0Freq / 1000) + " MHz");
                }
                if (btnMinCPU4freq != null) {
                    btnMinCPU4freq.setText((minCPU4Freq / 1000) + " MHz");
                }
                if (btnMaxCPU4freq != null) {
                    btnMaxCPU4freq.setText((maxCPU4Freq / 1000) + " MHz");
                }
            });
        });
    }

    private void updateGPUButtonUI() {
        mainHandler.post(
                () -> {
                    if (btnAdrenoBoostMode != null) {
                        rvGpu.startUpdateAdrenoBoost(btnAdrenoBoostMode);
                    }
                    if (btnMinGPUfreq != null) {
                        rvGpu.startUpdateMinGPUfreq(btnMinGPUfreq);
                    }
                    if (btnMaxGPUfreq != null) {
                        rvGpu.startUpdateMaxGPUfreq(btnMaxGPUfreq);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // GPU
        updateGPUButtonUI();
        
        // CPU
        updateCPUButtonUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (btnAdrenoBoostMode != null) {
            rvGpu.stopUpdateAdrenoBoost();
        }
        if (btnMinGPUfreq != null) {
            rvGpu.stopUpdateMinGPUfreq();
        }
        if (btnMaxGPUfreq != null) {
            rvGpu.stopUpdateMaxGPUfreq();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}