package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedTrack1
import com.example.model.ParsedTrack2
import com.example.model.ParsedTrack3
import com.example.ui.theme.GreenConnected
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
fun TrackBreakdownCard(
    trackNumber: Int,
    rawTrack: String,
    parsed1: ParsedTrack1? = null,
    parsed2: ParsedTrack2? = null,
    parsed3: ParsedTrack3? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current

    val isValid = when (trackNumber) {
        1 -> parsed1?.isValid == true
        2 -> parsed2?.isValid == true
        3 -> parsed3?.isValid == true
        else -> false
    }

    val trackAccent = when (trackNumber) {
        1 -> PurplePrimary
        2 -> PurpleSecondary
        else -> TextMuted
    }

    val badgeColor = if (rawTrack.isEmpty()) TextMuted else if (isValid) GreenConnected else RoseError

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("track_${trackNumber}_card"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
        shape = RoundedCornerShape(24.dp),
        border = if (rawTrack.isEmpty()) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Bar matching theme HTML
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(if (expanded && rawTrack.isNotEmpty()) 180.dp else 64.dp)
                    .background(if (rawTrack.isNotEmpty()) trackAccent else Color.Transparent)
            )

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(trackAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "T$trackNumber",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = trackAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TRACK $trackNumber",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = trackAccent
                            )
                            Text(
                                text = if (rawTrack.isEmpty()) "No data present" else if (isValid) "ISO-7811 Format OK" else "Format Check Needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rawTrack.isEmpty()) TextMuted else if (isValid) GreenConnected else RoseError
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rawTrack.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(rawTrack))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Track $trackNumber",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            tint = TextMuted
                        )
                    }
                }

                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        if (rawTrack.isEmpty()) {
                            Text(
                                text = "No magnetic track data detected on track $trackNumber.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            // Raw ASCII Box
                            Text(
                                text = "RAW TRACK DATA",
                                style = MaterialTheme.typography.labelSmall,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
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
                                    text = rawTrack,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Specific Parsed Fields
                            if (trackNumber == 1 && parsed1 != null) {
                                Track1ParsedDetails(parsed1)
                            } else if (trackNumber == 2 && parsed2 != null) {
                                Track2ParsedDetails(parsed2)
                            } else if (trackNumber == 3 && parsed3 != null) {
                                Track3ParsedDetails(parsed3)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Track1ParsedDetails(p1: ParsedTrack1) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "DECODED FIELD STRUCTURE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FieldChip("Format Code", "${p1.formatCode}")
            FieldChip("PAN Number", p1.pan)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FieldChip("Cardholder Name", p1.cardholderName ?: "N/A")
            FieldChip("Expiry (YYMM)", p1.expirationYYMM ?: "N/A")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FieldChip("Service Code", p1.serviceCode ?: "N/A")
            FieldChip("Discretionary Data", p1.discretionaryData ?: "None")
        }
    }
}

@Composable
private fun Track2ParsedDetails(p2: ParsedTrack2) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "DECODED FIELD STRUCTURE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FieldChip("PAN Number", p2.pan)
            FieldChip("Expiry (YYMM)", p2.expirationYYMM ?: "N/A")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FieldChip("Service Code", p2.serviceCode ?: "N/A")
            FieldChip("Discretionary / CVV", p2.discretionaryData ?: "None")
        }
    }
}

@Composable
private fun Track3ParsedDetails(p3: ParsedTrack3) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "DECODED FIELD STRUCTURE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        FieldChip("Account / Encrypted Data", p3.accountData ?: "Raw Track 3 Bytes")
    }
}

@Composable
private fun FieldChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
