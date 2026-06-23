package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val startTime: Long, // timestamp
    val endTime: Long, // timestamp
    val location: String,
    val eventType: String, // PAHANDI, CHHERA_PAHANRA, KIRTAN, PRASADAM, GENERAL
    val priority: Int = 0,
    val isLive: Boolean = false,
    val imageUrl: String? = null
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val priority: String, // CRITICAL, NORMAL, SPIRITUAL
    val timestamp: Long,
    val expiresAt: Long? = null
)

@Entity(tableName = "seva_orders")
data class SevaOrder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val orderType: String, // SEVA, POOJA, PRASADAM, DONATION
    val itemId: String,
    val itemName: String,
    val quantity: Int = 1,
    val amountPaise: Long,
    val status: String, // PENDING, PAYMENT_INITIATED, PAID, FAILED, REDEEMED
    val clientTxnId: String,
    val qrCodeHash: String? = null,
    val qrExpiresAt: Long? = null,
    val redeemedAt: Long? = null,
    val redeemedBy: String? = null,
    val donorName: String,
    val donorPhone: String,
    val donorEmail: String,
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayAmount: String
        get() = "₹${"%.2f".format(amountPaise / 100.0)}"

    val isRedeemable: Boolean
        get() = (status == "PAID" || status == "REDEEMED") && redeemedAt == null
}

@Entity(tableName = "chariot_locations")
data class ChariotLocation(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val batteryLevel: Int = 100,
    val isStale: Boolean = false
)
