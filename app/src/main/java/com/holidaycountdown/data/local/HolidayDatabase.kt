package com.holidaycountdown.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HolidayEntity::class, SpecialWorkdayEntity::class, CalendarOverrideEntity::class, MetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HolidayDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile private var instance: HolidayDatabase? = null
        fun get(context: Context): HolidayDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, HolidayDatabase::class.java, "holiday-countdown.db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}
