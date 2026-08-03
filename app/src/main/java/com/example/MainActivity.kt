package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.EasyMSRViewModel
import com.example.ui.screens.BluetoothManagerScreen
import com.example.ui.screens.CardHistoryScreen
import com.example.ui.screens.HardwareConsoleScreen
import com.example.ui.screens.LiveReaderScreen
import com.example.ui.screens.TrackWriterScreen
import com.example.ui.theme.EasyMSRTheme
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedPillActive
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {

    private val viewModel: EasyMSRViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EasyMSRTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: EasyMSRViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SophisticatedBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SophisticatedCardBg,
                contentColor = PurplePrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Live Reader") },
                    label = { Text("Terminal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = SophisticatedPillActive,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_reader")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth Hardware") },
                    label = { Text("Bluetooth") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = SophisticatedPillActive,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_bluetooth")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Swipe History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = SophisticatedPillActive,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_history")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Track Writer") },
                    label = { Text("Writer") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = SophisticatedPillActive,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_writer")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Hardware Console") },
                    label = { Text("Console") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = SophisticatedPillActive,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_console")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> LiveReaderScreen(viewModel = viewModel)
                1 -> BluetoothManagerScreen(viewModel = viewModel)
                2 -> CardHistoryScreen(viewModel = viewModel)
                3 -> TrackWriterScreen(viewModel = viewModel)
                4 -> HardwareConsoleScreen(viewModel = viewModel)
            }
        }
    }
}
