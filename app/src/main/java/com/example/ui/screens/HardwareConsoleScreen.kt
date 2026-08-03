package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.model.ConsoleLogEntry
import com.example.ui.EasyMSRViewModel
import com.example.ui.theme.GreenConnected
import com.example.ui.theme.PurpleOnPrimary
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun HardwareConsoleScreen(
    viewModel: EasyMSRViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.deviceManager.consoleLogs.collectAsStateWithLifecycle()
    var customHexInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
            .testTag("hardware_console_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Console Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SophisticatedPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = PurplePrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "X6BT PROTOCOL TERMINAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            IconButton(
                onClick = { viewModel.deviceManager.clearConsole() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SophisticatedCardBg)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Logs", tint = TextMuted)
            }
        }

        // Quick Command Macro Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.deviceManager.sendRawCommand("1B 73", "ESC s (Read)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = PurplePrimary
                )
            ) {
                Text("ESC s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.deviceManager.sendRawCommand("1B 77", "ESC w (Write)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = PurplePrimary
                )
            ) {
                Text("ESC w", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.deviceManager.sendRawCommand("1B 65", "ESC e (Erase)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = PurplePrimary
                )
            ) {
                Text("ESC e", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.deviceManager.sendRawCommand("1B 76", "ESC v (Version)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = PurplePrimary
                )
            ) {
                Text("ESC v", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Terminal Log View Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(SophisticatedCardBg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "No hardware packet logs...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { entry ->
                        ConsoleLogItemRow(entry)
                    }
                }
            }
        }

        // Custom Command Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customHexInput,
                onValueChange = { customHexInput = it },
                placeholder = { Text("Hex string (e.g. 1B 82)", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("console_hex_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = SophisticatedPillBg,
                    focusedContainerColor = SophisticatedCardBg,
                    unfocusedContainerColor = SophisticatedCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Button(
                onClick = {
                    if (customHexInput.isNotBlank()) {
                        viewModel.deviceManager.sendRawCommand(customHexInput, "Custom Command")
                    }
                },
                modifier = Modifier
                    .height(52.dp)
                    .testTag("console_send_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = PurpleOnPrimary)
            }
        }
    }
}

@Composable
private fun ConsoleLogItemRow(entry: ConsoleLogEntry) {
    val dirTag = if (entry.isOutgoing) "TX →" else "RX ←"
    val dirColor = if (entry.isOutgoing) PurplePrimary else GreenConnected

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dirTag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = dirColor,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SophisticatedPillBg
                ) {
                    Text(
                        text = entry.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = entry.timestampFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "[HEX] ${entry.hexData}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            fontSize = 12.sp
        )

        if (entry.asciiData.isNotEmpty()) {
            Text(
                text = "[ASCII] ${entry.asciiData}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
