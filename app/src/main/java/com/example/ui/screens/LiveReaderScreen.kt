package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DecodedCardData
import com.example.ui.EasyMSRViewModel
import com.example.ui.components.CreditCardVisual
import com.example.ui.components.TrackBreakdownCard
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SignalCellularAlt
import com.example.ui.theme.GreenConnected
import com.example.ui.theme.PurpleOnPrimary
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveReaderScreen(
    viewModel: EasyMSRViewModel,
    modifier: Modifier = Modifier
) {
    val decodedCard by viewModel.decodedCardData.collectAsStateWithLifecycle()
    val isConnected by viewModel.deviceManager.isConnected.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.deviceManager.isDemoMode.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.deviceManager.connectedDevice.collectAsStateWithLifecycle()

    var showRawInputDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("live_reader_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sophisticated Dark Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SophisticatedPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC Hardware",
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "X6bt Mini Interface",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) GreenConnected else RoseError)
                        )
                        Text(
                            text = if (isConnected) "DEVICE CONNECTED" else "DISCONNECTED",
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
                    text = "Demo Mode",
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
                    ),
                    modifier = Modifier.testTag("demo_mode_switch")
                )
            }
        }

        // Active Hardware Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hardware_status_banner"),
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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Active Hardware",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Text(
                        text = if (isConnected) (connectedDevice?.name ?: "Connected Device") else "No Device Connected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Battery5Bar,
                            contentDescription = "Battery",
                            tint = if (isConnected) PurplePrimary else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isConnected) "82%" else "--",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) PurplePrimary else TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Signal",
                            tint = if (isConnected) PurplePrimary else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isConnected) "${connectedDevice?.signalDbm ?: -50}dB" else "--",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) PurplePrimary else TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Demo Simulator Preset Bar
        if (isDemoMode) {
            Column {
                Text(
                    text = "SIMULATE CARD SWIPE (X6BT DEMO)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = decodedCard.cardBrand.displayName == "Visa",
                        onClick = { viewModel.simulatePresetSwipe(0) },
                        label = { Text("Visa Swipe") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = PurpleOnPrimary,
                            containerColor = SophisticatedPillBg,
                            labelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = decodedCard.cardBrand.displayName == "Mastercard",
                        onClick = { viewModel.simulatePresetSwipe(1) },
                        label = { Text("Mastercard") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = PurpleOnPrimary,
                            containerColor = SophisticatedPillBg,
                            labelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = decodedCard.cardBrand.displayName == "American Express",
                        onClick = { viewModel.simulatePresetSwipe(2) },
                        label = { Text("Amex Platinum") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = PurpleOnPrimary,
                            containerColor = SophisticatedPillBg,
                            labelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = decodedCard.cardBrand.displayName == "Discover",
                        onClick = { viewModel.simulatePresetSwipe(3) },
                        label = { Text("Discover") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = PurpleOnPrimary,
                            containerColor = SophisticatedPillBg,
                            labelColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Visual 3D Credit Card Render
        CreditCardVisual(cardData = decodedCard)

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.saveCurrentSwipe() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("save_swipe_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = PurpleOnPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Swipe", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showRawInputDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("raw_input_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = TextPrimary
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Raw Input")
            }

            IconButton(
                onClick = { viewModel.deviceManager.triggerBeeperAndLed() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SophisticatedPillBg)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Test Hardware Tone", tint = PurplePrimary)
            }
        }

        // Decoded Tracks Breakdown Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DECODED TRACK DATA",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = SophisticatedPillBg
            ) {
                Text(
                    text = "ISO/IEC 7811",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp
                )
            }
        }

        TrackBreakdownCard(
            trackNumber = 1,
            rawTrack = decodedCard.rawTrack1,
            parsed1 = decodedCard.track1
        )

        TrackBreakdownCard(
            trackNumber = 2,
            rawTrack = decodedCard.rawTrack2,
            parsed2 = decodedCard.track2
        )

        TrackBreakdownCard(
            trackNumber = 3,
            rawTrack = decodedCard.rawTrack3,
            parsed3 = decodedCard.track3
        )

        // Service Code & Security Inspector Card
        if (decodedCard.serviceCodeInfo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SERVICE CODE INSPECTOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                        Text(
                            text = "CODE: ${decodedCard.serviceCodeInfo?.code}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    decodedCard.serviceCodeInfo?.let { info ->
                        Text(
                            text = "• Interchange: ${info.interchange}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Processing: ${info.authorization}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• PIN Restrictions: ${info.pinService}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }

    // Raw Track Input Dialog
    if (showRawInputDialog) {
        var inputT1 by remember { mutableStateOf(decodedCard.rawTrack1) }
        var inputT2 by remember { mutableStateOf(decodedCard.rawTrack2) }
        var inputT3 by remember { mutableStateOf(decodedCard.rawTrack3) }

        AlertDialog(
            onDismissRequest = { showRawInputDialog = false },
            title = { Text("Enter Raw Track Data", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputT1,
                        onValueChange = { inputT1 = it },
                        label = { Text("Track 1 (%B...?)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SophisticatedPillBg
                        )
                    )
                    OutlinedTextField(
                        value = inputT2,
                        onValueChange = { inputT2 = it },
                        label = { Text("Track 2 (;...?)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SophisticatedPillBg
                        )
                    )
                    OutlinedTextField(
                        value = inputT3,
                        onValueChange = { inputT3 = it },
                        label = { Text("Track 3 (;...?) Optional") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SophisticatedPillBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onProcessRawInput(inputT1, inputT2, inputT3)
                        showRawInputDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Decode", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRawInputDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SophisticatedCardBg
        )
    }
}
