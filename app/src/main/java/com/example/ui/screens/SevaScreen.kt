package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RathaYatraViewModel
import com.example.ui.SevaOffer
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SevaScreen(viewModel: RathaYatraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (currentScreen) {
        "check_out" -> CheckoutFormScreen(viewModel)
        else -> SevaListScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SevaListScreen(viewModel: RathaYatraViewModel) {
    val customAmount by viewModel.customDonationAmount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Devotional Offerings (Seva)",
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("seva_list_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Custom General Donation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🙏", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "General Chariot Donation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contribute any custom amount directly to logistics and high-volume prasadam distribution. 80G tax receipts are issueable immediately after transaction completes.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customAmount,
                                onValueChange = { viewModel.customDonationAmount.value = it },
                                label = { Text("Amount (₹)", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("custom_donation_input")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.selectCustomDonationCheckout() },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("custom_donate_submit_button")
                            ) {
                                Text("Donate", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Standard Sevas Catalog Headers
            item {
                Text(
                    text = "Select Pooja, Feasts, or Rigging Seva:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SaffronDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Catalog list items
            items(viewModel.sevaCatalog) { seva ->
                SevaCard(seva = seva, onSelect = { viewModel.selectSevaForCheckout(seva) })
            }
        }
    }
}

@Composable
fun SevaCard(seva: SevaOffer, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                RoundedCornerShape(24.dp)
            )
            .clickable { onSelect() }
            .testTag("seva_card_${seva.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = seva.icon, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = seva.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = seva.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = seva.displayPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutFormScreen(viewModel: RathaYatraViewModel) {
    val context = LocalContext.current
    val selectedSeva by viewModel.selectedSeva.collectAsState()
    
    val name by viewModel.donorName.collectAsState()
    val phone by viewModel.donorPhone.collectAsState()
    val email by viewModel.donorEmail.collectAsState()
    val isPanClaim by viewModel.isPanRequired.collectAsState()
    val panVal by viewModel.donorPan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Details", fontWeight = FontWeight.Bold, color = SaffronDark) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("seva") }) {
                        Text(text = "Back", color = SaffronPrimary, fontWeight = FontWeight.Bold)
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
                .testTag("checkout_form_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Seva summary
            selectedSeva?.let { seva ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = seva.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = seva.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SaffronDark)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Sponsorship: " + seva.displayPrice, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaffronPrimary)
                            }
                        }
                    }
                }
            }

            // Input details form fields
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Devotee Contact Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SaffronDark)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.donorName.value = it },
                            label = { Text("Full Name", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.donorPhone.value = it },
                            label = { Text("Phone Number (+91)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.donorEmail.value = it },
                            label = { Text("Email Address (Optional)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                        )
                    }
                }
            }

            // 80G PAN Details checking block
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isPanClaim,
                                onCheckedChange = { viewModel.isPanRequired.value = it },
                                colors = CheckboxDefaults.colors(checkedColor = SaffronPrimary),
                                modifier = Modifier.testTag("80g_exemption_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Claim Section 80G Tax Exemption", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Temple issues PAN validation receipts.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        if (isPanClaim) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = panVal,
                                onValueChange = { viewModel.donorPan.value = it },
                                label = { Text("PAN Number (10 alphanumeric digits)", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pan_number_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronPrimary)
                            )
                        }
                    }
                }
            }

            // Proceed Action
            item {
                Button(
                    onClick = { viewModel.initiateCheckoutPayment(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("pay_webview_submit_button")
                ) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pay with ICICI Eazypay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
