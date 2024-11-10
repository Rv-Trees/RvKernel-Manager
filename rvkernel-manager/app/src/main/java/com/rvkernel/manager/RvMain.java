package com.rvkernel.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.view.Window;
import android.view.WindowInsetsController;

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
            "4.19.322-RvKernel-Mi8937-v1.5"
    };

    private ExecutorService executor;
    private Handler mainHandler;

    // Scheduler
    private RvScheduler rvScheduler;
    private Switch switchSchedAutoGroup;
    private Switch switchSchedChildRunFirst;

    // CPU
    private RvCPU rvCPU;
    private MaterialButton btnMinCPUfreq;
    private MaterialButton btnMaxCPUfreq;
    private MaterialButton btnCpuGovernor;

    // GPU
    private RvGPU rvGPU;
    private Switch gpuThrottlingSwitch;
    private MaterialButton btnMinGPUfreq;
    private MaterialButton btnMaxGPUfreq;
    private MaterialButton btnGpuGovernor;

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

        Window window = getWindow();
        if (window != null) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                } else {
                    insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }

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
        btnMinCPUfreq = findViewById(R.id.btnMinCPUfreq);
        btnMaxCPUfreq = findViewById(R.id.btnMaxCPUfreq);
	btnCpuGovernor = findViewById(R.id.btnCpuGovernor);

        rvCPU = new RvCPU();
        rvCPU.showMinCPUfreq(this, btnMinCPUfreq);
        rvCPU.showMaxCPUfreq(this, btnMaxCPUfreq);
	rvCPU.showAvailableCpuGovernors(this, btnCpuGovernor);
    }

    private void setupGPU() {
        btnMinGPUfreq = findViewById(R.id.btnMinGPUfreq);
        btnMaxGPUfreq = findViewById(R.id.btnMaxGPUfreq);
        gpuThrottlingSwitch = findViewById(R.id.gpuThrottlingSwitch);
	btnGpuGovernor = findViewById(R.id.btnGpuGovernor);

        rvGPU = new RvGPU();
        rvGPU.showMinGPUfreq(this, btnMinGPUfreq);
        rvGPU.showMaxGPUfreq(this, btnMaxGPUfreq);
        rvGPU.gpuThrottlingSwitch(this, gpuThrottlingSwitch);
	rvGPU.showAvailableGpuGovernors(this, btnGpuGovernor);
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
	if (executor != null && !executor.isShutdown()) {
            executor.execute(() -> {
                int minCPUFreq = rvCPU.loadMinCPUfreq();
                int maxCPUFreq = rvCPU.loadMaxCPUfreq();

		String currentCpuGovernor = rvCPU.loadCurrentCpuGovernor();

                mainHandler.post(() -> {
                    if (btnMinCPUfreq != null) {
                        btnMinCPUfreq.setText((minCPUFreq / 1000) + " MHz");
                    }
                    if (btnMaxCPUfreq != null) {
                        btnMaxCPUfreq.setText((maxCPUFreq / 1000) + " MHz");
                    }
		    if (btnCpuGovernor != null) {
			btnCpuGovernor.setText(currentCpuGovernor);
		    }
                });
            });
        }
    }

    private void updateGPUButtonUI() {
	if (executor != null && !executor.isShutdown()) {
            executor.execute(() -> {
                int minGPUFreq = rvGPU.loadMinGPUfreq();
                int maxGPUFreq = rvGPU.loadMaxGPUfreq();

		String currentGpuGovernor = rvGPU.loadCurrentGpuGovernor();

                mainHandler.post(() -> {
                    if (btnMinGPUfreq != null) {
                        btnMinGPUfreq.setText(minGPUFreq + " MHz");
                    }
                    if (btnMaxGPUfreq != null) {
                        btnMaxGPUfreq.setText(maxGPUFreq + " MHz");
                    }
		    if (btnGpuGovernor != null) {
                        btnGpuGovernor.setText(currentGpuGovernor);
                    }
                });
            });
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
