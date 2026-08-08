package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
internal expect object HolidayDatabaseConstructor : RoomDatabaseConstructor<HolidayDatabase> {
    override fun initialize(): HolidayDatabase
}
