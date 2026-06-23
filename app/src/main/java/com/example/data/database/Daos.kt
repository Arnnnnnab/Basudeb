package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {
    @Query("SELECT * FROM schedule_events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<ScheduleEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ScheduleEvent>)

    @Query("DELETE FROM schedule_events")
    suspend fun clearAll()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcements")
    suspend fun clearAll()
}

@Dao
interface SevaOrderDao {
    @Query("SELECT * FROM seva_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<SevaOrder>>

    @Query("SELECT * FROM seva_orders WHERE id = :id")
    fun getOrderById(id: String): Flow<SevaOrder?>

    @Query("SELECT * FROM seva_orders WHERE id = :id")
    suspend fun getOrderByIdSync(id: String): SevaOrder?

    @Query("SELECT * FROM seva_orders WHERE serverId = :serverId")
    suspend fun getOrderByServerIdSync(serverId: String): SevaOrder?

    @Query("SELECT * FROM seva_orders WHERE status = 'REDEEMED' AND syncedAt IS NULL")
    suspend fun getPendingSyncRedemptions(): List<SevaOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SevaOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<SevaOrder>)

    @Update
    suspend fun updateOrder(order: SevaOrder)

    @Query("UPDATE seva_orders SET status = :status, syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, syncedAt: Long?)
}

@Dao
interface ChariotLocationDao {
    @Query("SELECT * FROM chariot_locations WHERE id = 1")
    fun getLocationFlow(): Flow<ChariotLocation?>

    @Query("SELECT * FROM chariot_locations WHERE id = 1")
    suspend fun getLocationSync(): ChariotLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: ChariotLocation)
}
