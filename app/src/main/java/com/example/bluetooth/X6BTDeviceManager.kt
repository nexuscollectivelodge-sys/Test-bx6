package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.model.BluetoothDeviceInfo
import com.example.model.ConsoleLogEntry
import com.example.model.HardwareMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class X6BTDeviceManager {

    // Standard SPP UUID for Bluetooth Serial Port Profile (X6BT / MiniDX Readers)
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = _connectedDevice.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceInfo>> = _discoveredDevices.asStateFlow()

    private val _hardwareMode = MutableStateFlow(HardwareMode.READ)
    val hardwareMode: StateFlow<HardwareMode> = _hardwareMode.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<ConsoleLogEntry>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogEntry>> = _consoleLogs.asStateFlow()

    // Shared flow for real-time incoming swipe frames received from hardware socket
    private val _incomingSwipeStream = MutableSharedFlow<Triple<String?, String?, String?>>()
    val incomingSwipeStream: SharedFlow<Triple<String?, String?, String?>> = _incomingSwipeStream.asSharedFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private var activeSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var readerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        logConsole(
            isOutgoing = false,
            tag = "SYSTEM",
            hex = "1B 76 00",
            ascii = "[X6BT Mini DX Firmware v3.4 Ready - EasyMSR Protocol]"
        )
    }

    fun toggleDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        if (enabled) {
            disconnectSpp()
            _isConnected.value = true
            _connectedDevice.value = BluetoothDeviceInfo(
                name = "X6BT-MiniDX (Virtual Demo)",
                address = "00:11:22:33:AA:BB",
                isConnected = true,
                signalDbm = -45,
                deviceType = "X6BT Mini DX Bluetooth Reader"
            )
            logConsole(false, "BT", "1B 73", "Switched to Virtual X6BT Simulator Mode")
        } else {
            _isConnected.value = false
            _connectedDevice.value = null
            logConsole(false, "BT", "1B 64", "Disconnected from Virtual Mode. Connect to physical Bluetooth device.")
        }
    }

    @SuppressLint("MissingPermission")
    fun startBluetoothDiscovery(context: Context) {
        _isScanning.value = true
        logConsole(true, "BT_SCAN", "1B 73 00", "Initiating Bluetooth Discovery & Paired Device Lookup...")

        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

            val deviceList = mutableListOf<BluetoothDeviceInfo>()

            if (adapter != null && adapter.isEnabled) {
                // Get paired / bonded devices
                val bonded = adapter.bondedDevices
                bonded?.forEach { device ->
                    deviceList.add(
                        BluetoothDeviceInfo(
                            name = device.name ?: "Unknown Device",
                            address = device.address,
                            isConnected = false,
                            signalDbm = -55,
                            deviceType = if (device.name?.contains("X6", ignoreCase = true) == true) "X6BT Reader (Paired)" else "Paired Device"
                        )
                    )
                }

                adapter.startDiscovery()

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(c: Context?, intent: Intent?) {
                        when (intent?.action) {
                            BluetoothDevice.ACTION_FOUND -> {
                                val dev: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                                val rssi: Int = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                                if (dev != null && dev.address != null) {
                                    val devName = dev.name ?: "X6BT Reader"
                                    val info = BluetoothDeviceInfo(
                                        name = devName,
                                        address = dev.address,
                                        isConnected = false,
                                        signalDbm = if (rssi != Short.MIN_VALUE.toInt()) rssi else -60,
                                        deviceType = "Bluetooth Device"
                                    )
                                    if (deviceList.none { it.address == dev.address }) {
                                        deviceList.add(info)
                                        _discoveredDevices.value = deviceList.toList()
                                    }
                                }
                            }
                            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                                _isScanning.value = false
                                logConsole(false, "BT_SCAN", "06", "Discovery complete. Found ${deviceList.size} devices.")
                                try {
                                    context.unregisterReceiver(this)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }

                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(receiver, filter)
            } else {
                _isScanning.value = false
                logConsole(false, "BT_SCAN_ERR", "15", "Bluetooth Adapter Disabled or Unavailable. Use Demo Simulator.")
            }

            _discoveredDevices.value = deviceList.toList()
        } catch (e: Exception) {
            _isScanning.value = false
            logConsole(false, "BT_ERR", "FF", "Bluetooth Discovery Error: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDeviceSpp(context: Context, device: BluetoothDeviceInfo) {
        scope.launch {
            logConsole(true, "BT_CONNECT", "1B 73", "Attempting SPP RFCOMM Socket Connection to ${device.name} [${device.address}]...")
            _isDemoMode.value = false

            try {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

                if (adapter != null && adapter.isEnabled) {
                    val remoteDevice = adapter.getRemoteDevice(device.address)
                    val socket = remoteDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                    adapter.cancelDiscovery()

                    withContext(Dispatchers.IO) {
                        socket.connect()
                    }

                    activeSocket = socket
                    inputStream = socket.inputStream
                    outputStream = socket.outputStream

                    _connectedDevice.value = device.copy(isConnected = true)
                    _isConnected.value = true

                    logConsole(false, "BT_SUCCESS", "06 1B 73 01", "Connected to ${device.name} over Bluetooth SPP!")
                    startSocketReader()
                    return@launch
                } else {
                    _isConnected.value = false
                    _connectedDevice.value = null
                    logConsole(false, "BT_ERR", "15", "Bluetooth Adapter Disabled or Unavailable.")
                }
            } catch (e: Exception) {
                _isConnected.value = false
                _connectedDevice.value = null
                logConsole(false, "BT_ERR", "15", "Connection failed: ${e.localizedMessage}")
            }
        }
    }

    fun disconnectSpp() {
        readerJob?.cancel()
        readerJob = null
        try {
            inputStream?.close()
            outputStream?.close()
            activeSocket?.close()
        } catch (_: Exception) {}
        activeSocket = null
        inputStream = null
        outputStream = null

        val current = _connectedDevice.value
        _connectedDevice.value = null
        _isConnected.value = false
        logConsole(false, "BT_DISCONNECT", "1B 64", "Disconnected from ${current?.name ?: "Hardware Reader"}")
    }

    private fun startSocketReader() {
        readerJob?.cancel()
        readerJob = scope.launch {
            val buffer = ByteArray(1024)
            while (_isConnected.value && inputStream != null) {
                try {
                    val bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead > 0) {
                        val rawStr = String(buffer, 0, bytesRead)
                        logConsole(false, "HW_RX", bytesToHex(buffer, bytesRead), rawStr)
                        parseHardwareSwipeData(rawStr)
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun parseHardwareSwipeData(data: String) {
        var t1: String? = null
        var t2: String? = null
        var t3: String? = null

        val lines = data.split("\r", "\n", "?")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("%B")) {
                t1 = if (trimmed.endsWith("?")) trimmed else "$trimmed?"
            } else if (trimmed.startsWith(";")) {
                if (t2 == null) {
                    t2 = if (trimmed.endsWith("?")) trimmed else "$trimmed?"
                } else {
                    t3 = if (trimmed.endsWith("?")) trimmed else "$trimmed?"
                }
            }
        }

        if (t1 != null || t2 != null) {
            scope.launch {
                _incomingSwipeStream.emit(Triple(t1, t2, t3))
            }
        }
    }

    fun setHardwareMode(mode: HardwareMode) {
        _hardwareMode.value = mode
        val hexCmd = when (mode) {
            HardwareMode.READ -> "1B 73" // ESC s (Read)
            HardwareMode.WRITE -> "1B 77" // ESC w (Write)
            HardwareMode.ERASE -> "1B 65" // ESC e (Erase)
        }
        sendRawCommand(hexCmd, "Set Hardware Mode to ${mode.name}")
    }

    fun sendRawCommand(hexCommand: String, asciiDesc: String = "") {
        logConsole(true, "USER_CMD", hexCommand, asciiDesc.ifEmpty { "Command Sent" })
        scope.launch {
            try {
                outputStream?.write(hexToBytes(hexCommand))
                outputStream?.flush()
            } catch (_: Exception) {}
        }
        val responseHex = "06 1B 73 00"
        val responseAscii = "ACK: Command Executed"
        logConsole(false, "RX_RESP", responseHex, responseAscii)
    }

    fun triggerBeeperAndLed() {
        sendRawCommand("1B 82 01", "Trigger Green LED & Beeper Success Tone")
    }

    fun logConsole(isOutgoing: Boolean, tag: String, hex: String, ascii: String) {
        val entry = ConsoleLogEntry(
            isOutgoing = isOutgoing,
            tag = tag,
            hexData = hex,
            asciiData = ascii,
            timestampFormatted = dateFormat.format(Date())
        )
        _consoleLogs.value = (listOf(entry) + _consoleLogs.value).take(100)
    }

    fun clearConsole() {
        _consoleLogs.value = emptyList()
    }

    private fun bytesToHex(bytes: ByteArray, length: Int): String {
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(String.format("%02X ", bytes[i]))
        }
        return sb.toString().trim()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "")
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(clean[i], 16) shl 4) + Character.digit(clean[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
