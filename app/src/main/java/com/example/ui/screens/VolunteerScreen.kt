package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RathaYatraViewModel
import com.example.ui.ScanResult
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerScreen(viewModel: RathaYatraViewModel) {
    val isVolunteerUnlocked by viewModel.isVolunteerMode.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isVolunteerUnlocked) {
            VolunteerLockoutScreen(viewModel)
        } else {
            VolunteerDashboardScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerLockoutScreen(viewModel: RathaYatraViewModel) {
    var codeVal by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Gate Portal", fontWeight = FontWeight.Bold, color = SaffronDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(SaffronPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Secure Admin Access Required",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SaffronDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter secure committee PIN passcode to unlock gate scanning. Volunteers have physical authorization privileges.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = codeVal,
                onValueChange = { codeVal = it },
                label = { Text("Gate Coordinator PIN") },
                placeholder = { Text("Hint: 1008") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("volunteer_pin_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (codeVal.trim() == "1008") {
                        viewModel.toggleVolunteerMode()
                        Toast.makeText(context, "Welcome Admin Volunteer!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid authorized passcode!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("unlock_volunteer_submit")
            ) {
                Text("Verify Passcode Credentials", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDashboardScreen(viewModel: RathaYatraViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var lastRecordedScanResult by remember { mutableStateOf<ScanResult?>(null) }
    var manualInputString by remember { mutableStateOf("") }
    
    // Camera Permission tracker Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Camera linked successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission denied. Use Manual Verification override.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temple Gate Scanning", fontWeight = FontWeight.Bold, color = SaffronDark) },
                actions = {
                    TextButton(onClick = { viewModel.toggleVolunteerMode() }) {
                        Text("LOCK", color = Color.Red, fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .testTag("volunteer_scanner_panel"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Stats Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFC3E6CB), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Station ID: Gate 07 (GT Rd)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Offline Cache OK",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // QR Camera link preview block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "HART CAMERA LINK",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To test native mobile physical scanning on devices, authorize camera hardware permission.",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("camera_link_request_button")
                        ) {
                            Text("Unlock Camera", fontSize = 11.sp)
                        }
                    }
                }
            }

            // MANUAL VERIFICATION OVERRIDE (For perfect emulator testing and compliance)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Emulator Testing Override",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SaffronDark
                        )
                        Text(
                            text = "To simulate QR scans in the browser emulator, copy the raw QR payload from any paid ticket and paste it here to execute full validation signature pipeline.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = manualInputString,
                            onValueChange = { manualInputString = it },
                            label = { Text("Paste QR raw hash token", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("override_manual_clipboard_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (manualInputString.isNotBlank()) {
                                    coroutineScope.launch {
                                        val result = viewModel.processVolumeScanValidation(manualInputString)
                                        lastRecordedScanResult = result
                                        manualInputString = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_verify_button")
                        ) {
                            Text("Inject Scan Override", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Scan status response notice banner
            lastRecordedScanResult?.let { res ->
                item {
                    val statusColor = if (res.isValid) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val bgStatus = if (res.isValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .testTag("scan_result_alert_card"),
                        colors = CardDefaults.cardColors(containerColor = bgStatus),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = res.title,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.message,
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )

                            res.order?.let { ord ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Devotee Details:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(text = "• Name: ${ord.donorName}", fontSize = 12.sp)
                                Text(text = "• Phone: ${ord.donorPhone}", fontSize = 12.sp)
                                Text(text = "• Item: ${ord.itemName}", fontSize = 12.sp, color = SaffronDark, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { lastRecordedScanResult = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Acknowledge & Clear", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Helpful Instructions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pooja/Prasadam physical ticket barcodes are valid for 24 hours. The local SQLite database logs all redemptions immediately and flags duplicates locally. Delay synchronization updates with central cloud occurs every ten minutes.",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
