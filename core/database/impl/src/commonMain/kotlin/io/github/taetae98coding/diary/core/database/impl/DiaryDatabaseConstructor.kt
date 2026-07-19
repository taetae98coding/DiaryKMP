package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
internal expect object DiaryDatabaseConstructor : RoomDatabaseConstructor<DiaryDatabase> {
    override fun initialize(): DiaryDatabase
}
