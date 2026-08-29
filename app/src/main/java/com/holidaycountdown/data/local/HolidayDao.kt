package com.holidaycountdown.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY startDate") fun observeHolidays(): Flow<List<HolidayEntity>>
    @Query("SELECT * FROM special_workdays") fun observeWorkdays(): Flow<List<SpecialWorkdayEntity>>
    @Query("SELECT * FROM calendar_overrides") fun observeOverrides(): Flow<List<CalendarOverrideEntity>>
    @Query("SELECT * FROM holidays ORDER BY startDate") suspend fun holidays(): List<HolidayEntity>
    @Query("SELECT * FROM special_workdays") suspend fun workdays(): List<SpecialWorkdayEntity>
    @Query("SELECT * FROM metadata WHERE `key` = :key LIMIT 1") suspend fun metadata(key: String): MetadataEntity?
    @Query("SELECT COUNT(*) FROM holidays") suspend fun holidayCount(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHolidays(items: List<HolidayEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWorkdays(items: List<SpecialWorkdayEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOverride(item: CalendarOverrideEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMetadata(item: MetadataEntity)
    @Query("DELETE FROM calendar_overrides WHERE date = :date") suspend fun deleteOverride(date: String)
    @Query("DELETE FROM calendar_overrides") suspend fun clearOverrides()
    @Query("DELETE FROM holidays") suspend fun clearHolidays()
    @Query("DELETE FROM special_workdays") suspend fun clearWorkdays()

    @Transaction
    suspend fun replaceBaseData(
        holidays: List<HolidayEntity>,
        workdays: List<SpecialWorkdayEntity>,
        revision: Long,
        publishedAt: String,
        source: String
    ) {
        clearHolidays()
        clearWorkdays()
        insertHolidays(holidays)
        insertWorkdays(workdays)
        insertMetadata(MetadataEntity("revision", revision.toString()))
        insertMetadata(MetadataEntity("publishedAt", publishedAt))
        insertMetadata(MetadataEntity("source", source))
    }
}
