package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RathaYatraApplication
import com.example.data.database.Announcement
import com.example.data.database.ChariotLocation
import com.example.data.database.ScheduleEvent
import com.example.data.database.SevaOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

data class SevaOffer(
    val id: String,
    val title: String,
    val description: String,
    val pricePaise: Long,
    val icon: String, // emoji or name
    val isAvailable: Boolean = true
) {
    val displayPrice: String get() = "₹${"%.2f".format(pricePaise / 100.0)}"
}

data class LostFoundItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "LOST" or "FOUND"
    val description: String,
    val contactName: String,
    val contactPhone: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

class RathaYatraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RathaYatraApplication).repository

    // Reactive DB sources
    val scheduleEvents: StateFlow<List<ScheduleEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<SevaOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chariotLocation: StateFlow<ChariotLocation?> = repository.chariotLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Seva Catalog Definition
    val sevaCatalog = listOf(
        SevaOffer("seva_1", "Maha Aarati Archana", "Complete aarti tray offering with deep ghee lamps, incense, fruits, and tulsi leaves, chanted in your family name.", 50100L, "🪔"),
        SevaOffer("seva_2", "Royal Chariot Sweeping Platter", "Sponsorship toward golden broom and aromatic camphor water sweeping coordinates.", 100100L, "🧹"),
        SevaOffer("seva_3", "Pilgrims Feast (100 Devotees)", "Feeds 100 pilgrims hot sumptuous khichdi mahanandaprasadm during afternoon GT road halts.", 250100L, "🍲"),
        SevaOffer("seva_4", "Golden Rope Pulling Support", "Provides heavy jute ropes, dynamic safety vests for volunteers, and direct chariot rigging.", 150100L, "🎗️"),
        SevaOffer("seva_5", "Traditional Flower Garland Canopy", "Exquisite marigold and jasmine canopy styling for Baladeva and Subhadra's massive deck.", 310000L, "🌸")
    )

    // State Variables
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isVolunteerMode = MutableStateFlow(false)
    val isVolunteerMode = _isVolunteerMode.asStateFlow()

    private val _volunteerPincode = MutableStateFlow("")
    val volunteerPincode = _volunteerPincode.asStateFlow()

    private val _isChariotTrackingActive = MutableStateFlow(true)
    val isChariotTrackingActive = _isChariotTrackingActive.asStateFlow()

    private val _selectedSeva = MutableStateFlow<SevaOffer?>(null)
    val selectedSeva = _selectedSeva.asStateFlow()

    // Donor info bound to checkout
    val donorName = MutableStateFlow("")
    val donorPhone = MutableStateFlow("")
    val donorEmail = MutableStateFlow("")
    val isPanRequired = MutableStateFlow(false)
    val donorPan = MutableStateFlow("")
    val customDonationAmount = MutableStateFlow("")

    // WebView Payment screen parameters
    private val _currentWebViewOrder = MutableStateFlow<SevaOrder?>(null)
    val currentWebViewOrder = _currentWebViewOrder.asStateFlow()

    private val _paymentWebViewUrl = MutableStateFlow<String?>(null)
    val paymentWebViewUrl = _paymentWebViewUrl.asStateFlow()

    // Screen navigation helpers
    private val _currentScreen = MutableStateFlow("updates") // updates, map, seva, orders, check_out, web_view, success, emergency, lost_found
    val currentScreen = _currentScreen.asStateFlow()

    private val _activeSuccessOrder = MutableStateFlow<SevaOrder?>(null)
    val activeSuccessOrder = _activeSuccessOrder.asStateFlow()

    // GPS Simulation offsets
    private var gpsOffsetIndex = 0
    private val gpsSimulationCoordinates = listOf(
        Pair(23.6821, 87.0112), // Temple Gate Start
        Pair(23.6845, 87.0145), // GT Road Crossing
        Pair(23.6872, 87.0180), // Police Chowki Station
        Pair(23.6901, 87.0211), // Grand Food Halt
        Pair(23.6934, 87.0249), // Evening Gundicha Rest Station
    )

    // Local Lost and Found items (In-memory mock connected with user state)
    private val _lostFoundItems = MutableStateFlow<List<LostFoundItem>>(emptyList())
    val lostFoundItems = _lostFoundItems.asStateFlow()

    init {
        // Core initialization: Start chariot GPS tracking with static/sampled values
        viewModelScope.launch {
            repository.saveChariotLocation(23.6821, 87.0112, 94, false)
        }

        // Add pre-populated lost-and-found items
        _lostFoundItems.value = listOf(
            LostFoundItem("lf1", "LOST", "Black leather wallet near the Grand Food Pandal containing Aadhar Card and Keys.", "Ashok Sharma", "+919876543210", System.currentTimeMillis() - 7200000, false),
            LostFoundItem("lf2", "FOUND", "Silver wrist bangle, found laying near Temple Gate Chariot Chasis.", "Pujari Rajesh", "+919933221100", System.currentTimeMillis() - 14400000, false)
        )
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectSevaForCheckout(seva: SevaOffer) {
        _selectedSeva.value = seva
        _currentScreen.value = "check_out"
    }

    fun selectCustomDonationCheckout() {
        val amount = customDonationAmount.value.toDoubleOrNull() ?: 100.0
        val seva = SevaOffer(
            id = "custom_donation",
            title = "Direct Temple Seva Donation",
            description = "General financial sponsorship support directly helping logistics. Optional 80G print certificate generated.",
            pricePaise = (amount * 100).toLong(),
            icon = "🙏"
        )
        _selectedSeva.value = seva
        _isChariotTrackingActive.value = false // simple reuse
        _currentScreen.value = "check_out"
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.forceRefreshData()
            delay(1500)
            _isRefreshing.value = false
            triggerHaptic(HapticType.SUCCESS)
        }
    }

    // GPS custom simulated broadcast helper
    fun moveChariotSimulated() {
        gpsOffsetIndex = (gpsOffsetIndex + 1) % gpsSimulationCoordinates.size
        val coords = gpsSimulationCoordinates[gpsOffsetIndex]
        val battery = 94 - gpsOffsetIndex * 5
        viewModelScope.launch {
            repository.saveChariotLocation(coords.first, coords.second, battery, false)
            triggerHaptic(HapticType.HEAVY)
        }
    }

    // Toggle Volunteer Mode
    fun toggleVolunteerMode() {
        _isVolunteerMode.value = !_isVolunteerMode.value
        triggerHaptic(HapticType.HEAVY)
    }

    // Initiate payment checkout flow
    fun initiateCheckoutPayment(context: Context) {
        val seva = _selectedSeva.value ?: return
        if (donorName.value.isBlank() || donorPhone.value.isBlank()) {
            Toast.makeText(context, "Please enter your Name and Phone Number", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val orderId = UUID.randomUUID().toString()
            val clientTxnId = UUID.randomUUID().toString()
            
            val newOrder = SevaOrder(
                id = orderId,
                serverId = "srv_${1000 + (100..999).random()}",
                orderType = if (seva.id == "custom_donation") "DONATION" else "SEVA",
                itemId = seva.id,
                itemName = seva.title,
                quantity = 1,
                amountPaise = seva.pricePaise,
                status = "PAYMENT_INITIATED",
                clientTxnId = clientTxnId,
                donorName = donorName.value,
                donorPhone = donorPhone.value,
                donorEmail = donorEmail.value,
                createdAt = System.currentTimeMillis()
            )

            // Save order in Room local SQLite
            repository.insertOrder(newOrder)
            _currentWebViewOrder.value = newOrder

            // ICICI Eazypay simulation URL endpoint parameters as detailed in PRD v2.1
            val amountInRupees = seva.pricePaise / 100.0
            val paymentSimUrl = "https://eazypay.icicibank.com/payment/simulator?" +
                    "orderId=${orderId}" +
                    "&clientTxnId=${clientTxnId}" +
                    "&amount=${amountInRupees}" +
                    "&customerName=${donorName.value}" +
                    "&productDesc=${seva.title}"

            _paymentWebViewUrl.value = paymentSimUrl
            _currentScreen.value = "web_view"
            triggerHaptic(HapticType.SUCCESS)
        }
    }

    // Simulates the payment webhook response coming from ICICI server
    fun simulatePaymentSuccess(orderId: String) {
        viewModelScope.launch {
            val order = repository.getOrderByIdSync(orderId) ?: return@launch
            
            // Generate standard HMAC hashed signature
            val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 Hours expiry
            val rawSalt = "ISKCON_RATH_YATRA_SALT_ROTATION_2026_JULY_16"
            val stringToHash = "$orderId:${order.clientTxnId}:${expiresAt}:$rawSalt"
            val hexHash = MessageDigest.getInstance("SHA-256")
                .digest(stringToHash.toByteArray())
                .fold("") { str, it -> str + "%02x".format(it) }
                .substring(0, 16)

            val updatedOrder = order.copy(
                status = "PAID",
                qrCodeHash = hexHash,
                qrExpiresAt = expiresAt
            )

            repository.updateOrder(updatedOrder)
            _activeSuccessOrder.value = updatedOrder
            _paymentWebViewUrl.value = null
            _currentScreen.value = "success"
            triggerHaptic(HapticType.HEAVY)
        }
    }

    fun simulatePaymentFailure(orderId: String) {
        viewModelScope.launch {
            val order = repository.getOrderByIdSync(orderId) ?: return@launch
            val updatedOrder = order.copy(status = "FAILED")
            repository.updateOrder(updatedOrder)
            _paymentWebViewUrl.value = null
            _currentScreen.value = "seva"
            triggerHaptic(HapticType.ERROR)
        }
    }

    // Physical Fulfillment scans & validations executed by volunteers
    suspend fun processVolumeScanValidation(qrDataString: String): ScanResult {
        // QR schema: payloadStr|signature -> orderId:clientTxnId:expiresAt|signature
        val parts = qrDataString.split("|")
        if (parts.size != 2) {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Invalid QR Format", "Malformed QR wrapper.")
        }
        val qrPayload = parts[0] // orderId:clientTxnId:expiresAt
        val signatureHash = parts[1]

        val payloadParts = qrPayload.split(":")
        if (payloadParts.size != 3) {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Malformed Data", "Data is unreadable.")
        }
        val orderId = payloadParts[0]
        val clientTxnId = payloadParts[1]
        val expiresAt = payloadParts[2].toLongOrNull() ?: 0L

        // Signature check
        val rawSalt = "ISKCON_RATH_YATRA_SALT_ROTATION_2026_JULY_16"
        val stringToHash = "$orderId:$clientTxnId:$expiresAt:$rawSalt"
        val expectedHash = MessageDigest.getInstance("SHA-256")
            .digest(stringToHash.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
            .substring(0, 16)

        if (signatureHash != expectedHash) {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Decryption Error", "Signature verification failed. Potential fraud.")
        }

        if (expiresAt < System.currentTimeMillis()) {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Ticket Expired", "This QR ticket expired at: ${java.text.SimpleDateFormat("h:mm a").format(expiresAt)}")
        }

        // Check double redemption in local SQLite
        val order = repository.getOrderByIdSync(orderId)
        if (order == null) {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Ticket Not Found", "This ticket is not stored in local master records.")
        }

        if (order.status == "REDEEMED") {
            triggerHaptic(HapticType.ERROR)
            return ScanResult(false, "Already Redeemed", "Redeemed: ${java.text.SimpleDateFormat("h:mm a").format(order.redeemedAt ?: 0L)}")
        }

        // Success - update local database state offline-first
        val redeemedTime = System.currentTimeMillis()
        val updated = order.copy(
            status = "REDEEMED",
            redeemedAt = redeemedTime,
            redeemedBy = "Volunteer_Asansol_07"
        )
        repository.updateOrder(updated)
        triggerHaptic(HapticType.SUCCESS)
        return ScanResult(true, "Redeemed Successfully!", "Approved for ${order.itemName}.", order)
    }

    // Emergency SMS broadcast pipeline
    fun triggerSOSAlert(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Trigger physical haptic alarm
            triggerHaptic(HapticType.HEAVY)
            
            val message = "EMERGENCY: Devotee triggered alert at Ratha Yatra Asansol coordinates: " +
                    "https://maps.google.com/?q=${latitude},${longitude} . Seeking medical/security team."
            
            // Simulates actual server insert
            val alertId = UUID.randomUUID().toString()
            val sosAnnouncement = Announcement(
                id = alertId,
                title = "⚠️ EMERGENCY CALL IN PROCESS...",
                content = "Devotee SOS alert broadcasted at coords: ($latitude, $longitude). Security marshals dispatched.",
                priority = "CRITICAL",
                timestamp = System.currentTimeMillis()
            )
            repository.insertAnnouncement(sosAnnouncement)
            triggerHaptic(HapticType.HEAVY)
        }
    }

    // Lost and Found handling
    fun submitLostFoundItem(type: String, desc: String, name: String, phone: String) {
        if (desc.isBlank() || name.isBlank()) return
        val item = LostFoundItem(
            type = type,
            description = desc,
            contactName = name,
            contactPhone = phone
        )
        _lostFoundItems.value = listOf(item) + _lostFoundItems.value
        triggerHaptic(HapticType.SUCCESS)
    }

    fun resolveLostFoundItem(itemId: String) {
        _lostFoundItems.value = _lostFoundItems.value.map {
            if (it.id == itemId) it.copy(isResolved = true) else it
        }
        triggerHaptic(HapticType.SUCCESS)
    }

    private fun triggerHaptic(type: HapticType) {
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timing = when(type) {
                HapticType.SUCCESS -> VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 120, 100), intArrayOf(0, 255, 0, 255), -1)
                HapticType.HEAVY -> VibrationEffect.createOneShot(400, 255)
            }
            vibrator.vibrate(timing)
        } else {
            @Suppress("DEPRECATION")
            when(type) {
                HapticType.SUCCESS -> vibrator.vibrate(120)
                HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 120, 100), -1)
                HapticType.HEAVY -> vibrator.vibrate(400)
            }
        }
    }

    enum class HapticType { SUCCESS, ERROR, HEAVY }
}

data class ScanResult(
    val isValid: Boolean,
    val title: String,
    val message: String,
    val order: SevaOrder? = null
)
