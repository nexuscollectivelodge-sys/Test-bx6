package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CardSwipeEntity
import com.example.ui.BrandFilter
import com.example.ui.EasyMSRViewModel
import com.example.ui.TrackPresenceFilter
import com.example.ui.theme.PurpleOnPrimary
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardHistoryScreen(
    viewModel: EasyMSRViewModel,
    modifier: Modifier = Modifier
) {
    val swipes by viewModel.savedSwipes.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeBrandFilter by viewModel.brandFilter.collectAsStateWithLifecycle()
    val activeTrackFilter by viewModel.trackFilter.collectAsStateWithLifecycle()
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()
    val savedFilesList by viewModel.savedFilesList.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current

    var selectedSwipeForDetail by remember { mutableStateOf<CardSwipeEntity?>(null) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var newCopyFileName by remember { mutableStateOf("") }
    var fileToCopy by remember { mutableStateOf<File?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
            .testTag("card_history_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Storage Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search name, PAN, brand...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PurplePrimary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_search_input"),
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

            // Storage Manager Button
            IconButton(
                onClick = {
                    viewModel.refreshSavedFilesList()
                    showStorageDialog = true
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SophisticatedCardBg)
            ) {
                Icon(Icons.Default.Folder, contentDescription = "Storage Files", tint = PurplePrimary)
            }

            // Export CSV Button
            IconButton(
                onClick = { viewModel.exportToCsvFile() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SophisticatedCardBg)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = PurplePrimary)
            }
        }

        // Customizable Filters Section (Brand & Track Filters)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = PurplePrimary, modifier = Modifier.size(18.dp))

            // Favorites Filter
            FilterChip(
                selected = favoritesOnly,
                onClick = { viewModel.toggleFavoritesOnly() },
                label = { Text("Favorites") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PurplePrimary,
                    selectedLabelColor = PurpleOnPrimary,
                    containerColor = SophisticatedPillBg,
                    labelColor = TextPrimary
                )
            )

            // Brand Filter Chips
            BrandFilter.values().forEach { brand ->
                FilterChip(
                    selected = activeBrandFilter == brand,
                    onClick = { viewModel.setBrandFilter(brand) },
                    label = { Text(if (brand == BrandFilter.ALL) "All Brands" else brand.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = PurpleOnPrimary,
                        containerColor = SophisticatedPillBg,
                        labelColor = TextPrimary
                    )
                )
            }

            // Track Presence Filter Chips
            TrackPresenceFilter.values().forEach { track ->
                FilterChip(
                    selected = activeTrackFilter == track,
                    onClick = { viewModel.setTrackFilter(track) },
                    label = {
                        Text(
                            when (track) {
                                TrackPresenceFilter.ALL -> "All Tracks"
                                TrackPresenceFilter.TRACK1_ONLY -> "T1 Only"
                                TrackPresenceFilter.TRACK2_ONLY -> "T2 Only"
                                TrackPresenceFilter.BOTH_PRESENT -> "T1+T2"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = PurpleOnPrimary,
                        containerColor = SophisticatedPillBg,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        // Quick File I/O Action Buttons Bar (Write Database to File / Read Database from File)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.writeDatabaseToFile() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedPillBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write to File", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.refreshSavedFilesList()
                    showStorageDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedPillBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Read from File", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Swipes List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILTERED SWIPES (${swipes.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            if (swipes.isNotEmpty()) {
                Text(
                    text = "Tap card for details",
                    style = MaterialTheme.typography.labelSmall,
                    color = PurplePrimary
                )
            }
        }

        if (swipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (query.isEmpty()) "No card swipes match current filters" else "No matching card swipes found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(swipes, key = { it.id }) { swipe ->
                    CardSwipeItemRow(
                        swipe = swipe,
                        dateString = dateFormat.format(Date(swipe.timestamp)),
                        onClick = { selectedSwipeForDetail = swipe },
                        onFavoriteToggle = { viewModel.toggleFavorite(swipe) },
                        onDelete = { viewModel.deleteSwipe(swipe) }
                    )
                }
            }
        }
    }

    // Storage Management Dialog (Write, Read, Copy, Erase Files)
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File Storage Database", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Manage exported JSON & CSV storage database files. Select a file to read/restore, copy, or erase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.writeDatabaseToFile() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("New File", color = PurpleOnPrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportToCsvFile() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export CSV", fontSize = 12.sp)
                        }
                    }

                    Text("SAVED DATABASE FILES:", style = MaterialTheme.typography.labelSmall, color = PurplePrimary, fontWeight = FontWeight.Bold)

                    if (savedFilesList.isEmpty()) {
                        Text("No saved database files yet.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(savedFilesList) { file ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SophisticatedPillBg),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${file.length()} bytes",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }

                                        Row {
                                            // Read File
                                            IconButton(
                                                onClick = {
                                                    viewModel.readDatabaseFromFile(file)
                                                    showStorageDialog = false
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.FileUpload, contentDescription = "Read File", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                            }

                                            // Copy File
                                            IconButton(
                                                onClick = { fileToCopy = file },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.CopyAll, contentDescription = "Copy File", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                            }

                                            // Erase File
                                            IconButton(
                                                onClick = { viewModel.eraseSavedFile(file) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Erase File", tint = RoseError, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SophisticatedCardBg
        )
    }

    // Copy File Dialog
    fileToCopy?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToCopy = null },
            title = { Text("Copy Database File", color = TextPrimary) },
            text = {
                Column {
                    Text("Enter new file name for copy of ${file.name}:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCopyFileName,
                        onValueChange = { newCopyFileName = it },
                        placeholder = { Text("e.g. backup_copy.json") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCopyFileName.isNotBlank()) {
                            viewModel.copySavedFile(file, newCopyFileName)
                            fileToCopy = null
                            newCopyFileName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Copy", color = PurpleOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToCopy = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SophisticatedCardBg
        )
    }

    // Swipe Details Dialog
    selectedSwipeForDetail?.let { swipe ->
        AlertDialog(
            onDismissRequest = { selectedSwipeForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(swipe.cardTitle, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PAN: ${swipe.maskedPan}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = "Name: ${swipe.cardholderName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Expires: ${swipe.expiryFormatted} | Service Code: ${swipe.serviceCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TRACK 1:", style = MaterialTheme.typography.labelSmall, color = PurplePrimary)
                    Text(
                        text = swipe.rawTrack1.ifEmpty { "(None)" },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )

                    Text("TRACK 2:", style = MaterialTheme.typography.labelSmall, color = PurplePrimary)
                    Text(
                        text = swipe.rawTrack2.ifEmpty { "(None)" },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trackData = "T1: ${swipe.rawTrack1}\nT2: ${swipe.rawTrack2}"
                        clipboardManager.setText(AnnotatedString(trackData))
                        selectedSwipeForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Copy Tracks", color = PurpleOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSwipeForDetail = null }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SophisticatedCardBg
        )
    }
}

@Composable
private fun CardSwipeItemRow(
    swipe: CardSwipeEntity,
    dateString: String,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("swipe_item_${swipe.id}"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedCardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SophisticatedPillBg
                    ) {
                        Text(
                            text = swipe.cardBrand.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = swipe.maskedPan,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${swipe.cardholderName} • Exp ${swipe.expiryFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (swipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (swipe.isFavorite) RoseError else TextMuted
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}
