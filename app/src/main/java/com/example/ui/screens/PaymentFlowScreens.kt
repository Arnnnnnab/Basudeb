package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebViewSimulatorScreen(viewModel: RathaYatraViewModel) {
    val currentOrder by viewModel.currentWebViewOrder.collectAsState()
    val rawUrl by viewModel.paymentWebViewUrl.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ICICI Eazypay Secure",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("seva") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFECEFF1)) // Slate grey banking background
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("icici_payment_header_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ICICI BANK MERCHANT GATEWAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Merchant: ISKCON Asansol Ratha Committee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFECEFF1))
                    Spacer(modifier = Modifier.height(12.dp))

                    currentOrder?.let { ord ->
                        Text(text = "Sponsoring: ${ord.itemName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Amount Due: ${ord.displayAmount}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SaffronPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Customer Name: ${ord.donorName}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Order ID: ${ord.id}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Central status indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Simulator WebView Session Active. Webhook endpoints are ready to listen for responses.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Simulated control triggers
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        currentOrder?.let {
                            viewModel.simulatePaymentSuccess(it.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("simulate_payment_success_button")
                ) {
                    Text(
                        text = "TAP HERE: SIMULATE PAYMENT SUCCESS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        currentOrder?.let {
                            viewModel.simulatePaymentFailure(it.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("simulate_payment_failure_button")
                ) {
                    Text(
                        text = "TAP HERE: SIMULATE PAYMENT CANCEL/FAIL",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "🔒 256-Bit SSL Encryption Provided by ICICI Eazypay Node. Secure connection enforced.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSuccessScreen(viewModel: RathaYatraViewModel) {
    val successOrder by viewModel.activeSuccessOrder.collectAsState()
    val isClaimsExempt by viewModel.isPanRequired.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devotional Receipt", fontWeight = FontWeight.Bold, color = SaffronDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .testTag("payment_success_view"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Success icon animations card
            item {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success tick",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Hare Krishna! Block Booked!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = SaffronDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your sponsorship has been recorded in the temple logs successfully. Jai Jagannatha!",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Summary statistics card
            successOrder?.let { ord ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "OFFICIAL TEMPLE SPONSORSHIP RECEIPT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Sponsor Name:", fontSize = 12.sp, color = Color.Gray)
                                Text(text = ord.donorName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Sponsorship Item:", fontSize = 12.sp, color = Color.Gray)
                                Text(text = ord.itemName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Amount Paid:", fontSize = 12.sp, color = Color.Gray)
                                Text(text = ord.displayAmount, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SaffronPrimary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Receipt server ID:", fontSize = 11.sp, color = Color.Gray)
                                Text(text = ord.serverId ?: "N/A", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            if (isClaimsExempt) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Sec 80G Tax rebate:", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = "Claim Approved (PAN Checked)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Export receipt triggers
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Sponsorship receipt PDF downloaded under downloads folder!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Devotional receipt emailed to devotee address!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, SaffronPrimary, RoundedCornerShape(12.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Email Copy", fontSize = 12.sp)
                    }
                }
            }

            // Redirect navigates directly to local wallet
            item {
                Button(
                    onClick = { viewModel.navigateTo("wallet") },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("go_to_wallet_success_button")
                ) {
                    Text(
                        text = "VIEW ACTIVE QR PASS IN MY WALLET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                TextButton(onClick = { viewModel.navigateTo("schedule") }) {
                    Text("Return to chronology schedule", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
