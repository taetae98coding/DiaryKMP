package io.github.taetae98coding.diary.core.calendar.database.impl

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
internal expect object CalendarDatabaseConstructor : RoomDatabaseConstructor<CalendarDatabase> {
    override fun initialize(): CalendarDatabase
}
