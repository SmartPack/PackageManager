/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */
package com.smartpack.packagemanager.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.smartpack.packagemanager.R
import com.smartpack.packagemanager.adapters.AppOpsAdapter
import com.smartpack.packagemanager.adapters.OnAppOperationClickedListener
import com.smartpack.packagemanager.databinding.FragmentAppOpsBinding
import com.smartpack.packagemanager.utils.PackageData
import com.smartpack.packagemanager.utils.Utils
import com.smartpack.packagemanager.utils.AppOperation
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 */
class AppOpsFragment : Fragment(), OnAppOperationClickedListener {
    private var _binding: FragmentAppOpsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val appOpsAdapter: AppOpsAdapter by lazy {
        AppOpsAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppOpsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            setHasFixedSize(true)
            adapter = appOpsAdapter
        }

        lifecycleScope.launchWhenStarted {
            val isRooted = hasRootAccess() // Suspend
            if (isRooted) {
                updateList() // Suspend
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
            }

            binding.progressIndicator.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAppOperationClicked(operation: AppOperation) {
        val newState = !operation.enabled
        operation.enabled = newState

        lifecycleScope.launch {
            setAppOp(operation, newState)
            delay(200)
            updateList()
        }
    }

    private suspend fun updateList() {
        val operations = getOps()
        if (operations.isEmpty()) {
            binding.errorText.apply {
                setText(R.string.app_ops_no_op_found)
                visibility = View.VISIBLE
            }
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.errorText.visibility = View.GONE
        }

        appOpsAdapter.updateData(operations)
    }

    // TODO: Localization
    private fun findDescriptionFromOp(op: String): String {
        return knownOperations.getOrElse(op) {
            getString(R.string.no_description_available)
        }
    }

    private val commandPrefix: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) "cmd" else ""

    private val knownOperations by lazy {
        mapOf(
            "COARSE_LOCATION" to "Allows an app to access approximate location",
            "FINE_LOCATION" to "Allows an application to access precise location",
            "ACCESS_FINE_LOCATION" to "Allows an application to access precise location",
            "GPS" to "Allows an application to access the GPS",
            "VIBRATE" to "Allows access to the vibrator",
            "READ_CONTACTS" to "Allows an application to read the user's contacts data",
            "WRITE_CONTACTS" to "Allows an application to write the user's contacts data",
            "READ_CALL_LOG" to "Allows an application to read the user's call log",
            "WRITE_CALL_LOG" to "Allows an application to write (but not read) the user's call log data",
            "READ_CALENDAR" to "Allows an application to read the user's calendar data",
            "WRITE_CALENDAR" to "Allows an application to write the user's calendar data",
            "WIFI_SCAN" to "Allows an application to access scan WiFi networks",
            "POST_NOTIFICATION" to "Allows an application to post notifications",
            "NEIGHBORING_CELLS" to "Allows an application to received Signal Strength and Cell ID location",
            "CALL_PHONE" to """Allows an application to initiate a phone call without going through 
                |the Dialer user interface for the user to confirm the call""".trimMargin(),
            "READ_SMS" to "Allows an application to read SMS messages",
            "WRITE_SMS" to "Allows an application to send SMS messages",
            "RECEIVE_SMS" to "Allows an application to receive SMS messages",
            "RECEIVE_EMERGENCY_SMS" to "Allows an application to receive Emergency SMS messages",
            "RECEIVE_MMS" to "Allows an application to monitor incoming MMS messages",
            "RECEIVE_WAP_PUSH" to "Allows an application to receive WAP push messages",
            "SEND_SMS" to "Allows an application to send SMS messages",
            "WRITE_SETTINGS" to "Read or write the system settings (there are limitations)",
            "SYSTEM_ALERT_WINDOW" to "Create windows and show them on top of all other apps",
            "ACCESS_NOTIFICATIONS" to "Allows a system app to access notifications",
            "CAMERA" to "Required to be able to access the camera device",
            "RECORD_AUDIO" to "Allows an application to record audio",
            "READ_CLIPBOARD" to "Read contents of the clipboard service",
            "WRITE_CLIPBOARD" to "Write data to the clipboard",
            "TAKE_AUDIO_FOCUS" to "Allows an application to be the only one holding audio focus",
            "AUDIO_RING_VOLUME" to "Change ring volume",
            "AUDIO_NOTIFICATION_VOLUME" to "Change notification volume",
            "WAKE_LOCK" to "Use PowerManager WakeLocks to keep processor from sleeping or screen from dimming",
            "GET_USAGE_STATS" to "Allows an application to collect component usage statistics",
            "PACKAGE_USAGE_STATS" to "Allows an application to collect component usage statistics",
            "MUTE_MICROPHONE" to "Allows an application to access mute the microphone",
            "TOAST_WINDOW" to "Show short duration messages on screen",
            "WRITE_WALLPAPER" to "Allows an application to set the wallpaper",
            "OP_READ_PHONE_STATE" to """Allows read only access to phone state, including the phone 
                |number of the device, current cellular network information, the status of any ongoing 
                |calls, and a list of any phone accounts registered on the device""".trimMargin(),
            "PROCESS_OUTGOING_CALLS" to """Allows an application to see the number being dialed during 
                |an outgoing call with the option to redirect the call to a different number or abort 
                |the call altogether""".trimMargin(),
            "USE_FINGERPRINT" to "Allows an app to use fingerprint hardware",
            "BODY_SENSORS" to """Access data from sensors that the user uses to measure what is
                | happening inside his/her body, such as heart rate""".trimMargin(),
            "READ_EXTERNAL_STORAGE" to "Allows an application to read from external storage",
            "WRITE_EXTERNAL_STORAGE" to "Allows an application to write to external storage",
            "GET_ACCOUNTS" to "Allows access to the list of accounts in the Accounts Service",
            "RUN_IN_BACKGROUND" to "Allows an application to run in the background",
            "READ_PHONE_NUMBERS" to """Allows read access to the device's phone number(s). This is a 
                |subset of the capabilities granted by READ_PHONE_STATE but is exposed to instant 
                |applications""".trimMargin(),
            "REQUEST_INSTALL_PACKAGES" to """Allows an application to install packages. 
                |Not for use by third-party applications""".trimMargin(),
            "CHANGE_WIFI_STATE" to "Allows applications to change Wi-Fi connectivity state",
            "REQUEST_DELETE_PACKAGES" to "Allows an application to request deleting packages",
            "BIND_ACCESSIBILITY_SERVICE" to """Required by an Accessibility Service, to ensure that 
                |only the system can bind to it""".trimMargin(),
            "START_FOREGROUND" to "Start foreground services. Requires FOREGROUND_SERVICE permission",
            "USE_BIOMETRIC" to "Use device supported biometric modalities",
            "BOOT_COMPLETED" to "Receive a broadcast indicating that the system has finished booting"
        )
    }

    // ================================== Background workers =======================================

    private suspend fun hasRootAccess(): Boolean = withContext(Dispatchers.Default) {
        return@withContext Shell.rootAccess()
    }

    private suspend fun getOps(): MutableList<AppOperation> = withContext(Dispatchers.Default) {
        val operations = mutableListOf<AppOperation>()
        val command = "$commandPrefix appops get ${PackageData.mApplicationID}".trim()

        Utils.runAndGetOutput(command).split("\n").forEach { line ->
            runCatching {
                val splitOp = line.split(":")
                val name = splitOp.first().trim()
                operations.add(
                    AppOperation(
                        name = name.toUpperCase(Locale.getDefault()),
                        description = findDescriptionFromOp(name),
                        writable = !line.contains("reject"),
                        enabled = line.contains("allow") || line.contains("ignore")
                    )
                )
            }
        }

        return@withContext operations.sortedBy {
            it.name
        }.reversed().distinctBy {
            it.name
        }.reversed().toMutableList()
    }

    private suspend fun setAppOp(appAppOperation: AppOperation, enable: Boolean) = withContext(Dispatchers.Default) {
        val state = if (enable) "allow" else "deny"
        Utils.runCommand(
            "$commandPrefix appops set ${PackageData.mApplicationID} ${appAppOperation.name} $state".trim()
        )
    }
}
