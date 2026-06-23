package com.example.data.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import java.util.UUID

class RathaYatraRepository(
    private val scheduleEventDao: ScheduleEventDao,
    private val announcementDao: AnnouncementDao,
    private val sevaOrderDao: SevaOrderDao,
    private val chariotLocationDao: ChariotLocationDao
) {
    val allEvents: Flow<List<ScheduleEvent>> = scheduleEventDao.getAllEvents()
        .onStart { populateInitialEventsIfNeeded() }

    val allAnnouncements: Flow<List<Announcement>> = announcementDao.getAllAnnouncements()
        .onStart { populateInitialAnnouncementsIfNeeded() }

    val allOrders: Flow<List<SevaOrder>> = sevaOrderDao.getAllOrders()

    val chariotLocation: Flow<ChariotLocation?> = chariotLocationDao.getLocationFlow()

    fun getOrderById(id: String): Flow<SevaOrder?> = sevaOrderDao.getOrderById(id)

    suspend fun getOrderByIdSync(id: String): SevaOrder? = sevaOrderDao.getOrderByIdSync(id)

    suspend fun getOrderByServerIdSync(serverId: String): SevaOrder? = sevaOrderDao.getOrderByServerIdSync(serverId)

    suspend fun insertOrder(order: SevaOrder) = sevaOrderDao.insertOrder(order)

    suspend fun updateOrder(order: SevaOrder) = sevaOrderDao.updateOrder(order)

    suspend fun getPendingSyncRedemptions(): List<SevaOrder> = sevaOrderDao.getPendingSyncRedemptions()

    suspend fun updateSyncStatus(orderId: String, status: String, syncedAt: Long?) {
        sevaOrderDao.updateSyncStatus(orderId, status, syncedAt)
    }

    suspend fun insertAnnouncement(announcement: Announcement) {
        announcementDao.insertAnnouncement(announcement)
    }

    // Populate helper functions with authentic, location-specific data for first launch
    private suspend fun populateInitialEventsIfNeeded() {
        val existing = scheduleEventDao.getAllEvents().first()
        if (existing.isEmpty()) {
            val baseTime = 1781512200000L // July 16, 2026 10:00 AM (Target Festival Day)
            val events = listOf(
                ScheduleEvent(
                    id = "ev_1",
                    title = "Mangala Aarati & Darshan",
                    description = "Early morning prayers and gorgeous first darshan of Lord Jagannatha, Baladeva, and Subhadra Devi in their ceremonial Ratha garments.",
                    startTime = baseTime - (4 * 60 * 60 * 1000), // 6:00 AM
                    endTime = baseTime - (2 * 60 * 60 * 1000), // 8:00 AM
                    location = "Main Temple Hall, Asansol",
                    eventType = "GENERAL",
                    priority = 1,
                    isLive = false
                ),
                ScheduleEvent(
                    id = "ev_2",
                    title = "Pahandi Ceremony (Festive Carrying)",
                    description = "The beautiful chanting deities are ceremonially carried out by the heavy swing of the Pujaris to their majestic chariots.",
                    startTime = baseTime, // 10:00 AM
                    endTime = baseTime + (2 * 60 * 60 * 1000), // 12:00 PM
                    location = "Main Temple Portal to Nandighosa Chariot",
                    eventType = "PAHANDI",
                    priority = 2,
                    isLive = true
                ),
                ScheduleEvent(
                    id = "ev_3",
                    title = "Chhera Pahanra (Royal Sweeping)",
                    description = "The premier sweep of the golden broom by traditional leaders to manifest pure devotion and humble service onto the path.",
                    startTime = baseTime + (2 * 60 * 60 * 1000) + 15 * 60 * 1000, // 12:15 PM
                    endTime = baseTime + (3 * 60 * 60 * 1000), // 1:00 PM
                    location = "Nandighosa and Chariot Decks",
                    eventType = "CHHERA_PAHANRA",
                    priority = 3,
                    isLive = false
                ),
                ScheduleEvent(
                    id = "ev_4",
                    title = "Chariot Pulling Commences",
                    description = "All devotees hold the sacred rope together and pull the Nandighosa chariot amidst celestial Hari Naam Sankirtan.",
                    startTime = baseTime + (3 * 12 * 10 * 60000), // 1:00 PM
                    endTime = baseTime + (6 * 60 * 60 * 1000), // 4:00 PM
                    location = "Grand Trunk Rd, Asansol",
                    eventType = "KIRTAN",
                    priority = 10,
                    isLive = false
                ),
                ScheduleEvent(
                    id = "ev_5",
                    title = "Prasadam Distribution",
                    description = "Lord Jagannatha's merciful Mahaprasdam is distributed freely to hundreds of thousands of congregation members.",
                    startTime = baseTime + (3 * 30 * 10 * 60000) + 30 * 60000, // 1:30 PM
                    endTime = baseTime + (8 * 60 * 60 * 1000), // 6:00 PM
                    location = "ISKCON Prasadam Pandal",
                    eventType = "PRASADAM",
                    priority = 5,
                    isLive = false
                ),
                ScheduleEvent(
                    id = "ev_6",
                    title = "Sandhya Aarati & Evening Bhajan",
                    description = "Melodious, traditional prayers and Hari Naam kirtan directly beside the chariot at its night resting destination.",
                    startTime = baseTime + (8 * 60 * 60 * 1000) + 30 * 60 * 1000, // 6:30 PM
                    endTime = baseTime + (10 * 60 * 60 * 1000), // 8:00 PM
                    location = "Gundicha Temple Station",
                    eventType = "KIRTAN",
                    priority = 6,
                    isLive = false
                )
            )
            scheduleEventDao.insertEvents(events)
        }
    }

    private suspend fun populateInitialAnnouncementsIfNeeded() {
        val existing = announcementDao.getAllAnnouncements().first()
        if (existing.isEmpty()) {
            val now = System.currentTimeMillis()
            val announcements = listOf(
                Announcement(
                    id = "ann_1",
                    title = "Pahandi Ceremony has started!",
                    content = "The ecstatic Pahandi ceremony is officially underway! The pujaris are elevating the deities to the chariot deck. Please proceed to the main gate with care.",
                    priority = "CRITICAL",
                    timestamp = now - 180000 // 3 min ago
                ),
                Announcement(
                    id = "ann_2",
                    title = "Special Rajbhog Offerings & Poojas",
                    content = "Devotees can book special Ratha Yatra fruit baskets and archana plate offerings online in the Seva tab. Receipt QR codes will be shown immediately upon success.",
                    priority = "SPIRITUAL",
                    timestamp = now - 900000 // 15 min ago
                ),
                Announcement(
                    id = "ann_3",
                    title = "Water Booth Locations",
                    content = "With peak humidity expected, 5 community water stalls have been established along GT Road from the Temple to Gundicha Station. Stay hydrated!",
                    priority = "NORMAL",
                    timestamp = now - 3600000 // 1 hour ago
                )
            )
            announcementDao.insertAnnouncements(announcements)
        }
    }

    suspend fun saveChariotLocation(latitude: Double, longitude: Double, batteryLevel: Int = 90, isStale: Boolean = false) {
        chariotLocationDao.insertLocation(
            ChariotLocation(
                id = 1,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis(),
                batteryLevel = batteryLevel,
                isStale = isStale
            )
        )
    }

    suspend fun forceRefreshData() {
        // Simulates pulling latest schedules and announcements from Headless CMS/Supabase API
        val now = System.currentTimeMillis()
        val latestAnnouncements = listOf(
            Announcement(
                id = "ann_new_2",
                title = "Live Chariot Sweeping Completed",
                content = "The Chhera Pahanra sweeping is complete, the ropes are secured. We are ready to commence pulling!",
                priority = "CRITICAL",
                timestamp = now
            ),
            Announcement(
                id = "ann_new_1",
                title = "Blissful Hari Naam on GT Road",
                content = "Kirtaneers from Mayapur have arrived and are leading ecstatic sankirtan directly in front of the Lord's chariot.",
                priority = "SPIRITUAL",
                timestamp = now - 300000
            )
        )
        announcementDao.insertAnnouncements(latestAnnouncements)
    }
}
