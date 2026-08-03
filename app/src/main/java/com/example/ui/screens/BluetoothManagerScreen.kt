package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BluetoothDeviceInfo
import com.example.ui.EasyMSRViewModel
import com.example.ui.theme.GreenConnected
import com.example.ui.theme.PurpleOnPrimary
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun BluetoothManagerScreen(
    viewModel: EasyMSRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isConnected by viewModel.deviceManager.isConnected.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.deviceManager.isDemoMode.collectAsStateWithLifecycle()
    val isScanning by viewModel.deviceManager.isScanning.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.deviceManager.connectedDevice.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.deviceManager.discoveredDevices.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
            .testTag("bluetooth_manager_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SophisticatedPillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Status",
                            tint = if (isConnected) GreenConnected else PurplePrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "X6BT & MiniDX Hardware",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) GreenConnected else RoseError)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isConnected) "SPP LINK ACTIVE" else "NO DEVICE CONNECTED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) GreenConnected else RoseError,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Virtual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { viewModel.deviceManager.toggleDemoMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SophisticatedBg,
                            checkedTrackColor = PurplePrimary
                        )
                    )
                }
            }
        }

        // Action Row (Scan & Disconnect)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.deviceManager.startBluetoothDiscovery(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scan_bluetooth_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PurpleOnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning...", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = PurpleOnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan X6BT Devices", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (isConnected) {
                OutlinedButton(
                    onClick = { viewModel.deviceManager.disconnectSpp() },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("disconnect_bluetooth_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SophisticatedPillBg,
                        contentColor = RoseError
                    )
                ) {
                    Text("Disconnect", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Connected Device Details Card if connected
        connectedDevice?.let { dev ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenConnected.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenConnected)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dev.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = GreenConnected.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTIVE LINK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GreenConnected,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MAC: ${dev.address} | Type: ${dev.deviceType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Protocol: RFCOMM SPP UUID 00001101-0000-1000-8000-00805F9B34FB",
                        style = MaterialTheme.typography.labelSmall,
                        color = PurplePrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Discovered Hardware Devices List Section
        Text(
            text = "DISCOVERED & PAIRED HARDWARE (${discoveredDevices.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PurplePrimary,
            modifier = Modifier.padding(top = 4.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(discoveredDevices) { device ->
                BluetoothDeviceCardRow(
                    device = device,
                    isCurrentlyConnected = isConnected && connectedDevice?.address == device.address,
                    onConnectClick = { viewModel.deviceManager.connectToDeviceSpp(context, device) }
                )
            }
        }
    }
}

@Composable
private fun BluetoothDeviceCardRow(
    device: BluetoothDeviceInfo,
    isCurrentlyConnected: Boolean,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isCurrentlyConnected) onConnectClick() },
        colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
        shape = RoundedCornerShape(20.dp),
        border = if (isCurrentlyConnected) androidx.compose.foundation.BorderStroke(1.dp, GreenConnected) else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentlyConnected) GreenConnected.copy(alpha = 0.2f) else SophisticatedPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = if (isCurrentlyConnected) GreenConnected else PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${device.address} • ${device.deviceType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal",
                        tint = PurplePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${device.signalDbm}dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = PurplePrimary,
                        fontSize = 11.sp
                    )
                }

                if (isCurrentlyConnected) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = GreenConnected.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenConnected,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onConnectClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedPillBg,
                            contentColor = PurplePrimary
                        )
                    ) {
                        Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
