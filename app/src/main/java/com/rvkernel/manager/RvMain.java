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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.rvkernel.manager.databinding.RvmainBinding;
import java.io.File;

public class RvMain extends AppCompatActivity {

    private RvmainBinding binding;
    private RvTuning rvTuning;
    private MaterialButton rvTuningButton;

    public static final String[] REQUIRED_KERNEL = {
            "4.9.337-RvKernel-Be4-v0.6"
    };

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
        binding = RvmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!RvRoot.RootAccess()) {
            RvRoot.RootAccessDenied(this);
            return;
        }

        if (!RvKernel.KernelCheck()) {
            RvKernel.KernelNotSupported(this);
            return;
        }

        requestStoragePermission();
        initializeRvTuning();
        setupRvUI();
        
        Button btnSetAdrenoBoost = findViewById(R.id.btnSetAdrenoBoost);
        Switch switchGpuThrottling = findViewById(R.id.switchGpuThrottling);
        Switch switchSchedAutoGroup = findViewById(R.id.switchSchedAutoGroup);
        Switch switchSchedChildRunFirst = findViewById(R.id.switchSchedChildRunFirst);
        Switch switchBypassCharging = findViewById(R.id.switchBypassCharging);
        Switch switchFastCharging = findViewById(R.id.switchFastCharging);
        Switch switchDisableThermalCharging = findViewById(R.id.switchDisableThermalCharging);
        RvGpu rvGpu = new RvGpu();
        RvScheduler rvScheduler = new RvScheduler();
        RvCharging rvCharging = new RvCharging();
        rvGpu.showAdrenoBoost(this, btnSetAdrenoBoost);
        rvGpu.throttlingSwitch(this, switchGpuThrottling);
        rvScheduler.schedAutoGroupSwitch(this, switchSchedAutoGroup);
        rvScheduler.schedChildRunFirstSwitch(this, switchSchedChildRunFirst);
        rvCharging.bypassChargingSwitch(this, switchBypassCharging);
        rvCharging.fastChargingSwitch(this, switchFastCharging);
        rvCharging.disableThermalChargingSwitch(this, switchDisableThermalCharging);
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

    private void initializeRvTuning() {
        rvTuningButton = findViewById(R.id.rvTuningButton);
        rvTuning = new RvTuning(this, rvTuningButton);
        rvTuning.initRvTuningButton();
        rvTuning.updateButtonWithSavedMode();
    }

    private void setupRvUI() {
        ShapeableImageView imageView = binding.RvKernelBanner.rvkernelBanner;
        RvBanner.RvBannerTheme(imageView, this);

        binding.RvDeviceInfo.kernelVersion.setText(RvKernel.KernelVersion());
        binding.RvDeviceInfo.ramInfo.setText(RvRam.RamInfo());
        binding.RvDeviceInfo.codename.setText(getDeviceCodename());
    }

    private String getDeviceCodename() {
        return Build.DEVICE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        rvTuning = null;
    }
}