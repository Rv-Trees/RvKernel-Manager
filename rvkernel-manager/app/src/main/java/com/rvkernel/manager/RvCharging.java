package com.rvkernel.manager;

import android.content.Context;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvCharging {
    public void bypassChargingSwitch(Context context, Switch bypassChargingSwitch) {
        int currentBypassChargingValue = loadBypassCharging();
        bypassChargingSwitch.setChecked(currentBypassChargingValue == 1);

        bypassChargingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 1 : 0;
            setBypassCharging(value);
        });
    }

    private boolean setBypassCharging(int value) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", String.valueOf(value), ">", "/sys/class/power_supply/battery/input_suspend");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadBypassCharging() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/class/power_supply/battery/input_suspend");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void fastChargingSwitch(Context context, Switch fastChargingSwitch) {
        int currentFastChargingValue = loadFastCharging();
        fastChargingSwitch.setChecked(currentFastChargingValue == 1);

        fastChargingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 1 : 0;
            setFastCharging(value);
        });
    }

    private boolean setFastCharging(int value) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", String.valueOf(value), ">", "/sys/kernel/fast_charge/force_fast_charge");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadFastCharging() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/kernel/fast_charge/force_fast_charge");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void disableThermalChargingSwitch(Context context, Switch disableThermalChargingSwitch) {
        int currentDisableThermalChargingValue = loadDisableThermalCharging();
        disableThermalChargingSwitch.setChecked(currentDisableThermalChargingValue == 1);

        disableThermalChargingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 1 : 0;
            setDisableThermalCharging(value);
        });
    }

    private boolean setDisableThermalCharging(int value) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "echo", String.valueOf(value), ">", "/sys/module/smb_lib/parameters/disable_thermal");
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadDisableThermalCharging() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/sys/module/smb_lib/parameters/disable_thermal");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
