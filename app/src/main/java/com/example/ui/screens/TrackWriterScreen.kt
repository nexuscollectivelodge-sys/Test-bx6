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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.decoder.TrackDecoder
import com.example.ui.EasyMSRViewModel
import com.example.ui.theme.PurpleOnPrimary
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TrackWriterScreen(
    viewModel: EasyMSRViewModel,
    modifier: Modifier = Modifier
) {
    var panInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var expiryInput by remember { mutableStateOf("") }
    var serviceCodeInput by remember { mutableStateOf("") }

    val generatedT1 = TrackDecoder.buildTrack1(panInput, nameInput, expiryInput, serviceCodeInput)
    val generatedT2 = TrackDecoder.buildTrack2(panInput, expiryInput, serviceCodeInput)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("track_writer_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SophisticatedPillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PurplePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "X6BT CARD ENCODER & WRITER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Build ISO-7811 compliant track strings and execute hardware write / erase on X6BT Mini DX magnetic head.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // Form Fields
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "CARD ENCODING DATA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            OutlinedTextField(
                value = panInput,
                onValueChange = { panInput = it.filter { char -> char.isDigit() } },
                label = { Text("Primary Account Number (PAN)", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("writer_pan_input"),
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

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it.uppercase() },
                label = { Text("Cardholder Name (LAST/FIRST)", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("writer_name_input"),
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

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = expiryInput,
                    onValueChange = { expiryInput = it.filter { char -> char.isDigit() }.take(4) },
                    label = { Text("Expiry (YYMM)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("writer_expiry_input"),
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

                OutlinedTextField(
                    value = serviceCodeInput,
                    onValueChange = { serviceCodeInput = it.filter { char -> char.isDigit() }.take(3) },
                    label = { Text("Service Code", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("writer_service_code_input"),
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
            }
        }

        // Live Generated ISO Track Preview
        Text(
            text = "GENERATED ENCODING PREVIEW",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("TRACK 1 ISO-7811 ENCODING:", style = MaterialTheme.typography.labelSmall, color = PurplePrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SophisticatedBg)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = generatedT1,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("TRACK 2 ISO-7811 ENCODING:", style = MaterialTheme.typography.labelSmall, color = PurplePrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SophisticatedBg)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = generatedT2,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.writeTracksToDevice(panInput, nameInput, expiryInput, serviceCodeInput)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("execute_write_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = PurpleOnPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write Card", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.eraseCard() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("erase_card_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedPillBg,
                    contentColor = RoseError
                )
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Erase Card")
            }
        }
    }
}
