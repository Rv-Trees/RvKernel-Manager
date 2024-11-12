package com.rvkernel.manager;

import android.content.Context;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RvScheduler {
    public void schedAutoGroupSwitch(Context context, Switch schedAutoGroupSwitch) {
        int currentSchedAutoGroupValue = loadSchedAutoGroup();
        schedAutoGroupSwitch.setChecked(currentSchedAutoGroupValue == 1);

        schedAutoGroupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 1 : 0;
            setSchedAutoGroup(value);
        });
    }

    private boolean setSchedAutoGroup(int value) {
        try {
            String[] command = {"su", "-c", "echo", String.valueOf(value), ">", "/proc/sys/kernel/sched_autogroup_enabled"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadSchedAutoGroup() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/proc/sys/kernel/sched_autogroup_enabled");
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

    public void schedChildRunFirstSwitch(Context context, Switch schedChildRunFirstSwitch) {
        int currentSchedChildRunFirstValue = loadSchedChildRunFirst();
        schedChildRunFirstSwitch.setChecked(currentSchedChildRunFirstValue == 1);

        schedChildRunFirstSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int value = isChecked ? 1 : 0;
            setSchedChildRunFirst(value);
        });
    }

    private boolean setSchedChildRunFirst(int value) {
        try {
            String[] command = {"su", "-c", "echo", String.valueOf(value), ">", "/proc/sys/kernel/sched_child_runs_first"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loadSchedChildRunFirst() {
        try {
            Process process =
                    Runtime.getRuntime()
                            .exec("su -c cat " + "/proc/sys/kernel/sched_child_runs_first");
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
