package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.database.RathaYatraRepository

class RathaYatraApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        RathaYatraRepository(
            database.scheduleEventDao(),
            database.announcementDao(),
            database.sevaOrderDao(),
            database.chariotLocationDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
