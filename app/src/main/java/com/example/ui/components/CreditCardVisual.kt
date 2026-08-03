package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardBrand
import com.example.model.DecodedCardData
import com.example.ui.theme.CardAmexEnd
import com.example.ui.theme.CardAmexStart
import com.example.ui.theme.CardGenericEnd
import com.example.ui.theme.CardGenericStart
import com.example.ui.theme.CardMasterEnd
import com.example.ui.theme.CardMasterStart
import com.example.ui.theme.CardVisaEnd
import com.example.ui.theme.CardVisaStart

@Composable
fun CreditCardVisual(
    cardData: DecodedCardData,
    modifier: Modifier = Modifier,
    onCopyClick: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current

    val cardBrush = when (cardData.cardBrand) {
        CardBrand.VISA -> Brush.linearGradient(listOf(CardVisaStart, CardVisaEnd))
        CardBrand.MASTERCARD -> Brush.linearGradient(listOf(CardMasterStart, CardMasterEnd))
        CardBrand.AMEX -> Brush.linearGradient(listOf(CardAmexStart, CardAmexEnd))
        else -> Brush.linearGradient(listOf(CardGenericStart, CardGenericEnd))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .testTag("credit_card_visual"),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Row: Brand & Service Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp, 26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.85f))
                                .border(1.dp, Color(0xFFB8860B), RoundedCornerShape(6.dp))
                        ) {
                            // Chip contact lines simulation
                            Box(
                                modifier = Modifier
                                    .size(16.dp, 12.dp)
                                    .align(Alignment.Center)
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(2.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = cardData.cardBrand.displayName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (cardData.serviceCodeInfo != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "SC: ${cardData.serviceCodeInfo.code}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val allData = "PAN: ${cardData.primaryAccountNumber}\nNAME: ${cardData.cardholderName}\nEXP: ${cardData.expiryFormatted}\nTRACK1: ${cardData.rawTrack1}\nTRACK2: ${cardData.rawTrack2}\nTRACK3: ${cardData.rawTrack3}"
                                clipboardManager.setText(AnnotatedString(allData))
                                onCopyClick?.invoke()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("card_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Card Details",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Card Number (PAN)
                val panDisplay = if (cardData.maskedPan.isNotEmpty()) {
                    cardData.maskedPan
                } else {
                    "•••• •••• •••• ••••"
                }

                Text(
                    text = panDisplay,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier.clickable {
                        if (cardData.primaryAccountNumber.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(cardData.primaryAccountNumber))
                            onCopyClick?.invoke()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Row: Name, Expiry, Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = cardData.cardholderName.ifEmpty { "DECODED SWIPE" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cardData.expiryFormatted.ifEmpty { "MM/YY" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cardData.isExpired) Color(0xFFF87171) else Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            if (cardData.isExpired) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Expired Card",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
