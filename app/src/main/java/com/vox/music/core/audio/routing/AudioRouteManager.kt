package com.vox.music.core.audio.routing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AudioRouteState(
    val isBluetoothConnected: Boolean = false,
    val bluetoothDeviceName: String = "",
    val bluetoothBatteryLevel: Int = -1,
    val activeDeviceName: String = "Phone Speaker",
    val activeDeviceType: Int = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
    val isRoutingToSpeaker: Boolean = false,
    val isSpatialAudioAvailable: Boolean = false,
    val isSpatialAudioEnabled: Boolean = false,
    val isDolbyAtmosAvailable: Boolean = false,
    val isDolbyAtmosEnabled: Boolean = false
)

@Singleton
class AudioRouteManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())

    private val _routeState = MutableStateFlow(AudioRouteState())
    val routeState: StateFlow<AudioRouteState> = _routeState.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            updateRouteState()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            updateRouteState()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if ("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" == action) {
                val level = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                if (level in 0..100) {
                    _routeState.value = _routeState.value.copy(bluetoothBatteryLevel = level)
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED == action ||
                       BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
                updateRouteState()
            }
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, handler)
        val filter = IntentFilter().apply {
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        try {
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            // Ignore
        }
        updateRouteState()
    }

    @SuppressLint("MissingPermission")
    private fun fetchConnectedBluetoothBatteryLevel(): Int {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return -1
            if (!adapter.isEnabled) return -1

            val bonded = adapter.bondedDevices ?: return -1
            for (device in bonded) {
                try {
                    val method = device.javaClass.getMethod("getBatteryLevel")
                    val level = method.invoke(device) as? Int ?: -1
                    if (level in 0..100) {
                        return level
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return -1
    }

    fun updateRouteState() {
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val bluetoothDevice = outputDevices.firstOrNull { isBluetoothDevice(it) }

        val isBtConnected = bluetoothDevice != null
        val btName = bluetoothDevice?.productName?.toString() ?: ""

        val currentBattery = if (isBtConnected) {
            val queried = fetchConnectedBluetoothBatteryLevel()
            if (queried in 0..100) queried else _routeState.value.bluetoothBatteryLevel
        } else {
            -1
        }

        val isRoutingToSpeaker = _routeState.value.isRoutingToSpeaker

        val activeName = when {
            isBtConnected && !isRoutingToSpeaker -> btName.ifBlank { "Bluetooth Audio" }
            outputDevices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET } -> "Wired Headphones"
            outputDevices.any { it.type == AudioDeviceInfo.TYPE_USB_HEADSET || it.type == AudioDeviceInfo.TYPE_USB_DEVICE } -> "USB Audio"
            else -> "Phone Speaker"
        }

        val activeType = when {
            isBtConnected && !isRoutingToSpeaker -> bluetoothDevice!!.type
            else -> AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        }

        // Spatial Audio & Dolby Atmos Capabilities Check
        var spatialAvailable = false
        var spatialEnabled = _routeState.value.isSpatialAudioEnabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val spatializer = audioManager.spatializer
                spatialAvailable = spatializer.isAvailable
                if (spatialAvailable && !_routeState.value.isSpatialAudioEnabled) {
                    spatialEnabled = spatializer.isEnabled
                }
            } catch (e: Exception) {
                spatialAvailable = false
            }
        }

        // Check for Dolby Atmos / Samsung SoundAlive or virtualizer effects
        var dolbyAvailable = false
        try {
            val effects = AudioEffect.queryEffects()
            dolbyAvailable = effects.any {
                it.name.contains("Dolby", ignoreCase = true) ||
                it.name.contains("SoundAlive", ignoreCase = true) ||
                it.implementor.contains("Dolby", ignoreCase = true)
            }
            if (!dolbyAvailable && spatialAvailable) {
                dolbyAvailable = true
            }
        } catch (e: Exception) {
            dolbyAvailable = spatialAvailable
        }

        _routeState.value = _routeState.value.copy(
            isBluetoothConnected = isBtConnected,
            bluetoothDeviceName = btName,
            bluetoothBatteryLevel = currentBattery,
            activeDeviceName = activeName,
            activeDeviceType = activeType,
            isSpatialAudioAvailable = spatialAvailable,
            isSpatialAudioEnabled = spatialEnabled,
            isDolbyAtmosAvailable = dolbyAvailable,
            isDolbyAtmosEnabled = _routeState.value.isDolbyAtmosEnabled || spatialEnabled
        )
    }

    fun routeToSpeaker() {
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val speakerDevice = outputDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && speakerDevice != null) {
            try {
                audioManager.setCommunicationDevice(speakerDevice)
            } catch (e: Exception) {
                audioManager.isSpeakerphoneOn = true
            }
        } else {
            audioManager.isSpeakerphoneOn = true
        }

        _routeState.value = _routeState.value.copy(
            isRoutingToSpeaker = true,
            activeDeviceName = "Phone Speaker",
            activeDeviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        )
    }

    fun routeToBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                audioManager.clearCommunicationDevice()
            } catch (e: Exception) {
                audioManager.isSpeakerphoneOn = false
            }
        } else {
            audioManager.isSpeakerphoneOn = false
        }

        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val bluetoothDevice = outputDevices.firstOrNull { isBluetoothDevice(it) }

        _routeState.value = _routeState.value.copy(
            isRoutingToSpeaker = false,
            activeDeviceName = bluetoothDevice?.productName?.toString() ?: "Bluetooth Audio",
            activeDeviceType = bluetoothDevice?.type ?: AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        )
    }

    fun toggleSpatialAudio(enabled: Boolean) {
        _routeState.value = _routeState.value.copy(isSpatialAudioEnabled = enabled)
    }

    fun toggleDolbyAtmos(enabled: Boolean) {
        _routeState.value = _routeState.value.copy(
            isDolbyAtmosEnabled = enabled,
            isSpatialAudioEnabled = enabled
        )
    }

    private fun isBluetoothDevice(device: AudioDeviceInfo): Boolean {
        return device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
               device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
               (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                   device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                   device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                   device.type == AudioDeviceInfo.TYPE_BLE_BROADCAST ||
                   device.type == AudioDeviceInfo.TYPE_HEARING_AID
               ))
    }
}
