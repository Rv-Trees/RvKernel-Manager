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
            "4.19.322-RvKernel-Mi8937-v1.4"
    };

    private ExecutorService executor;
    private Handler mainHandler;

    // Scheduler
    private RvScheduler rvScheduler;
    private Switch switchSchedAutoGroup;
    private Switch switchSchedChildRunFirst;

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
    private MaterialButton btnMinGPUfreq;
    private MaterialButton btnMaxGPUfreq;

    // Device Info
    private TextView deviceCodename;
    private TextView ramInfo;
    private TextView kernelVersion;

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
    }

    private void setupUI() {
        // Banner
        ShapeableImageView bannerImageView = findViewById(R.id.rvkernelBanner);
        RvBanner.RvBannerTheme(bannerImageView, this);

        // Scheduler
        setupScheduler();

        // CPU
        setupCPU();

        // GPU
        setupGPU();

        // Device Info
        setupDeviceInfo();
    }

    private void setupScheduler() {
        switchSchedAutoGroup = findViewById(R.id.switchSchedAutoGroup);
        switchSchedChildRunFirst = findViewById(R.id.switchSchedChildRunFirst);

        rvScheduler = new RvScheduler();
        rvScheduler.schedAutoGroupSwitch(this, switchSchedAutoGroup);
        rvScheduler.schedChildRunFirstSwitch(this, switchSchedChildRunFirst);
    }

    private void setupCPU() {
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
    }

    private void setupGPU() {
        btnMinGPUfreq = findViewById(R.id.btnMinGPUfreq);
        btnMaxGPUfreq = findViewById(R.id.btnMaxGPUfreq);
        gpuThrottlingSwitch = findViewById(R.id.gpuThrottlingSwitch);

        rvGpu = new RvGpu();
        rvGpu.showMinGPUfreq(this, btnMinGPUfreq);
        rvGpu.showMaxGPUfreq(this, btnMaxGPUfreq);
        rvGpu.gpuThrottlingSwitch(this, gpuThrottlingSwitch);
    }

    private void setupDeviceInfo() {
        deviceCodename = findViewById(R.id.deviceCodename);
        ramInfo = findViewById(R.id.ramInfo);
        kernelVersion = findViewById(R.id.kernelVersion);

        deviceCodename.setText(RvDeviceInfo.DeviceCodename());
        ramInfo.setText(RvDeviceInfo.RamInfo());
        kernelVersion.setText(RvDeviceInfo.KernelVersion());
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
