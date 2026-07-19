package io.github.taetae98coding.diary.core.database.api.contact.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class CalendarContactBirthdayLocalEntity(
    @ColumnInfo(name = "contact_id")
    val contactId: Uuid,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "birthday_date")
    val birthdayDate: LocalDate,
)
