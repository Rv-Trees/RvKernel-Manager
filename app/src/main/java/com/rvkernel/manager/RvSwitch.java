package com.rvkernel.manager;

import com.rvkernel.manager.databinding.RvmainBinding;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;

public class RvSwitch {

    public static void RvSwitchLogic(RvmainBinding binding) {
        updateSwitchStates(binding);

        binding.RvCharging.disablethermalchargingSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeDisableThermalCharging(isChecked ? "1" : "0"));
    }

    private static void updateSwitchStates(RvmainBinding binding) {

        String disableThermalChargingValue = getDisableThermalChargingValue();
        binding.RvCharging.disablethermalchargingSwitch.setChecked(disableThermalChargingValue.equals("1"));
    }

    private static String getDisableThermalChargingValue() {
        return executeCommandWithResult("cat /sys/module/smb_lib/parameters/disable_thermal");
    }

    private static String executeCommandWithResult(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su -c " + command);
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (IOException e) {
            return "";
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void changeDisableThermalCharging(String value) {
        executeCommand("echo " + value + " > /sys/module/smb_lib/parameters/disable_thermal");
    }

    private static void executeCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            try (BufferedOutputStream out = new BufferedOutputStream(process.getOutputStream())) {
                out.write((command + "\n").getBytes());
                out.flush();
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
