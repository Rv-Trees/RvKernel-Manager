package com.rvkernel.manager;

import com.rvkernel.manager.databinding.RvmainBinding;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;

public class RvSwitch {

    public static void RvSwitchLogic(RvmainBinding binding) {
        updateSwitchStates(binding);
        
        binding.RvScheduler.schedchildrunfirstSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeSchedChildRunFirst(isChecked ? "1" : "0"));

        binding.RvCharging.bypassSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeInputSuspend(isChecked ? "1" : "0"));

        binding.RvCharging.fastchargingSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeFastCharging(isChecked ? "1" : "0"));

        binding.RvCharging.disablethermalchargingSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeDisableThermalCharging(isChecked ? "1" : "0"));
    }

    private static void updateSwitchStates(RvmainBinding binding) {
        
        String schedChildRunFirstValue = getSchedChildRunFirstValue();
        binding.RvScheduler.schedchildrunfirstSwitch.setChecked(schedChildRunFirstValue.equals("1"));

        String inputSuspendValue = getInputSuspendValue();
        binding.RvCharging.bypassSwitch.setChecked(inputSuspendValue.equals("1"));

        String fastChargingValue = getFastChargingValue();
        binding.RvCharging.fastchargingSwitch.setChecked(fastChargingValue.equals("1"));

        String disableThermalChargingValue = getDisableThermalChargingValue();
        binding.RvCharging.disablethermalchargingSwitch.setChecked(disableThermalChargingValue.equals("1"));
    }
    
    private static String getSchedChildRunFirstValue() {
        return executeCommandWithResult("cat /proc/sys/kernel/sched_child_runs_first");
    }

    private static String getInputSuspendValue() {
        return executeCommandWithResult("cat /sys/class/power_supply/battery/input_suspend");
    }

    private static String getFastChargingValue() {
        return executeCommandWithResult("cat /sys/kernel/fast_charge/force_fast_charge");
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
    
    private static void changeSchedChildRunFirst(String value) {
        executeCommand("echo " + value + " > /proc/sys/kernel/sched_child_runs_first");
    }

    private static void changeInputSuspend(String value) {
        executeCommand("echo " + value + " > /sys/class/power_supply/battery/input_suspend");
    }

    private static void changeFastCharging(String value) {
        executeCommand("echo " + value + " > /sys/kernel/fast_charge/force_fast_charge");
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
