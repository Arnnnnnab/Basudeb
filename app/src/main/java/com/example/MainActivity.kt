package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RathaYatraViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent

class MainActivity : ComponentActivity() {

    private val viewModel: RathaYatraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: RathaYatraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isVolunteerUnlocked by viewModel.isVolunteerMode.collectAsState()
    val context = LocalContext.current

    // Rapid click tracker to unlock volunteer mode
    var versionTapCount by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing, // Perfect Safe Area edge-to-edge margins
        bottomBar = {
            // Hide navigation bar if in specialized webview or full success modes
            if (currentScreen != "web_view" && currentScreen != "success") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars) // Safe bottom bar margin
                        .testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "updates",
                        onClick = { viewModel.navigateTo("updates") },
                        icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                        label = { Text("Updates", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_btn_updates")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "map",
                        onClick = { viewModel.navigateTo("map") },
                        icon = { Icon(Icons.Outlined.Navigation, contentDescription = null) },
                        label = { Text("Chariot Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_btn_map")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "seva" || currentScreen == "check_out",
                        onClick = { viewModel.navigateTo("seva") },
                        icon = { Icon(Icons.Outlined.CardGiftcard, contentDescription = null) },
                        label = { Text("Book Seva", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_btn_seva")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "wallet",
                        onClick = { viewModel.navigateTo("wallet") },
                        icon = { Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null) },
                        label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_btn_wallet")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "gallery",
                        onClick = { viewModel.navigateTo("gallery") },
                        icon = { Icon(Icons.Outlined.Collections, contentDescription = null) },
                        label = { Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_btn_gallery")
                    )

                    // Displays Gate Scanning tab only if unlocked via rapid clicks
                    if (isVolunteerUnlocked) {
                        NavigationBarItem(
                            selected = currentScreen == "volunteer",
                            onClick = { viewModel.navigateTo("volunteer") },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                            label = { Text("Gate Scan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color(0xFF2E7D32),
                                indicatorColor = Color(0xFF2E7D32),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_btn_volunteer")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant brand banner on top of standard views (non-payment contexts)
            if (currentScreen != "web_view" && currentScreen != "success") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛕 ISKCON ASANSOL RATHA YATRA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.clickable {
                            versionTapCount++
                            if (versionTapCount >= 7) {
                                viewModel.toggleVolunteerMode()
                                Toast.makeText(context, "Volunteer Scanning Tab Unlocked!", Toast.LENGTH_SHORT).show()
                                versionTapCount = 0
                            } else if (versionTapCount >= 3) {
                                Toast.makeText(context, "Tap ${7 - versionTapCount} more times to open Gate Scanning Mode", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Text(
                        text = "JULY 16, 2026",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Screen router
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    "updates" -> UpdatesScreen(viewModel)
                    "map" -> MapScreen(viewModel)
                    "seva" -> SevaScreen(viewModel)
                    "check_out" -> SevaScreen(viewModel)
                    "wallet" -> WalletScreen(viewModel)
                    "gallery" -> GalleryScreen(viewModel)
                    "volunteer" -> VolunteerScreen(viewModel)
                    "web_view" -> PaymentWebViewSimulatorScreen(viewModel)
                    "success" -> PaymentSuccessScreen(viewModel)
                    else -> UpdatesScreen(viewModel)
                }
            }
        }
    }
}
