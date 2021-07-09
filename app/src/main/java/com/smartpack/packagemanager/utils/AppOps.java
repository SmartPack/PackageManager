/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;
import android.os.Build;

import com.smartpack.packagemanager.R;

import java.util.ArrayList;
import java.util.Locale;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class AppOps {

    public static ArrayList<RecycleViewAppOpsItem> getOps(Context context) {
        ArrayList<RecycleViewAppOpsItem> mData = new ArrayList<>();
        for (String line : Utils.runAndGetOutput(getCommandPrefix() + " appops get " +
                Common.getApplicationID()).split("\\r?\\n")) {
            String[] splitOp = line.split(":");
            String name = splitOp[0];
            /*
             * We don't need a single "No operations." item if operations are empty.
             * Also, "Uid mode" needs more work (and likely never work)
             */
            if (!line.equals("No operations.") && !name.equals("Uid mode")) {
                mData.add(new RecycleViewAppOpsItem(name, getDescription(name, context), (line.contains("allow") || line.contains("ignore"))));
            }
        }
        return mData;
    }

    public static String getCommandPrefix() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return "cmd";
        } else {
            return  "";
        }
    }

    private static String getDescription(String operation, Context context) {
        switch (operation) {
            case "COARSE_LOCATION":
                return context.getString(R.string.operations_coarse_location);
            case "FINE_LOCATION":
            case "ACCESS_FINE_LOCATION":
                return context.getString(R.string.operations_fine_location);
            case "GPS":
                return context.getString(R.string.operations_gps);
            case "REQUEST_INSTALL_PACKAGES":
                return context.getString(R.string.operations_install_packages);
            case "REQUEST_DELETE_PACKAGES":
                return context.getString(R.string.operations_delete_packages);
            case "VIBRATE":
                return context.getString(R.string.operations_vibrate);
            case "READ_CONTACTS":
                return context.getString(R.string.operations_read_contacts);
            case "WRITE_CONTACTS":
                return context.getString(R.string.operations_write_contacts);
            case "READ_CALL_LOG":
                return context.getString(R.string.operations_read_call_log);
            case "WRITE_CALL_LOG":
                return context.getString(R.string.operations_write_call_log);
            case "CALL_PHONE":
                return context.getString(R.string.operations_call_phone);
            case "READ_PHONE_NUMBERS":
                return context.getString(R.string.operations_read_phone_numbers);
            case "OP_READ_PHONE_STATE":
                return context.getString(R.string.operations_read_phone_state);
            case "PROCESS_OUTGOING_CALLS":
                return context.getString(R.string.operations_process_outgoing_calls);
            case "READ_CALENDAR":
                return context.getString(R.string.operations_read_calender);
            case "WRITE_CALENDAR":
                return context.getString(R.string.operations_write_calender);
            case "WIFI_SCAN":
                return context.getString(R.string.operations_wifi_scan);
            case "CHANGE_WIFI_STATE":
                return context.getString(R.string.operations_change_wifi);
            case "ACCESS_NOTIFICATIONS":
                return context.getString(R.string.operations_access_notification);
            case "AUDIO_NOTIFICATION_VOLUME":
                return context.getString(R.string.operations_audio_notification);
            case "POST_NOTIFICATION":
                return context.getString(R.string.operations_post_notification);
            case "NEIGHBORING_CELLS":
                return context.getString(R.string.operations_neighbouring_cells);
            case "READ_SMS":
                return context.getString(R.string.operations_read_sms);
            case "WRITE_SMS":
            case "SEND_SMS":
                return context.getString(R.string.operations_write_sms);
            case "RECEIVE_SMS":
                return context.getString(R.string.operations_receive_sms);
            case "RECEIVE_EMERGENCY_SMS":
                return context.getString(R.string.operations_receive_emergency_sms);
            case "RECEIVE_MMS":
                return context.getString(R.string.operations_receive_mms);
            case "RECEIVE_WAP_PUSH":
                return context.getString(R.string.operations_receive_wap_push);
            case "WRITE_SETTINGS":
                return context.getString(R.string.operations_write_settings);
            case "SYSTEM_ALERT_WINDOW":
                return context.getString(R.string.operations_system_alert_window);
            case "CAMERA":
                return context.getString(R.string.operations_camera);
            case "RECORD_AUDIO":
                return context.getString(R.string.operations_record_audio);
            case "TAKE_AUDIO_FOCUS":
                return context.getString(R.string.operations_take_audio_focus);
            case "AUDIO_RING_VOLUME":
                return context.getString(R.string.operations_ring_volume);
            case "READ_CLIPBOARD":
                return context.getString(R.string.operations_read_clipboard);
            case "WRITE_CLIPBOARD":
                return context.getString(R.string.operations_write_clipboard);
            case "WAKE_LOCK":
                return context.getString(R.string.operations_wake_lock);
            case "GET_USAGE_STATS":
            case "PACKAGE_USAGE_STATS":
                return context.getString(R.string.operations_usage_status);
            case "MUTE_MICROPHONE":
                return context.getString(R.string.operations_mute_microphone);
            case "TOAST_WINDOW":
                return context.getString(R.string.operations_toast_window);
            case "WRITE_WALLPAPER":
                return context.getString(R.string.operations_write_wallpaper);
            case "USE_BIOMETRIC":
                return context.getString(R.string.operations_use_biometric);
            case "USE_FINGERPRINT":
                return context.getString(R.string.operations_use_fingerprint);
            case "BODY_SENSORS":
                return context.getString(R.string.operations_body_sensors);
            case "READ_EXTERNAL_STORAGE":
                return context.getString(R.string.operations_read_storage);
            case "WRITE_EXTERNAL_STORAGE":
                return context.getString(R.string.operations_write_storage);
            case "GET_ACCOUNTS":
                return context.getString(R.string.operations_get_accounts);
            case "RUN_IN_BACKGROUND":
                return context.getString(R.string.operations_run_background);
            case "BIND_ACCESSIBILITY_SERVICE":
                return context.getString(R.string.operations_accessibility_service);
            case "START_FOREGROUND":
                return context.getString(R.string.operations_start_foreground);
            case "BOOT_COMPLETED":
                return context.getString(R.string.operations_boot_completed);
            default:
                return context.getString(R.string.operations_unavailable, operation.toUpperCase(Locale.getDefault()));
        }
    }

}