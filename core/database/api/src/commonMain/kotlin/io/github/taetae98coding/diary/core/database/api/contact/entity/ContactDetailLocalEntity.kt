package io.github.taetae98coding.diary.core.database.api.contact.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate

public data class ContactDetailLocalEntity(
    @ColumnInfo(name = "name", defaultValue = "")
    val name: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "height_centimeter", defaultValue = "NULL")
    val heightCentimeter: Double?,
    @ColumnInfo(name = "foot_size_millimeter", defaultValue = "NULL")
    val footSizeMillimeter: Int?,
    @ColumnInfo(name = "birthday", defaultValue = "NULL")
    val birthday: LocalDate?,
    @ColumnInfo(name = "birthday_calendar", defaultValue = "NULL")
    val birthdayCalendar: ContactBirthdayCalendarLocalEntity?,
    @ColumnInfo(name = "hometown", defaultValue = "")
    val hometown: String,
    @ColumnInfo(name = "phone_number_list", defaultValue = "'[]'")
    val phoneNumberList: List<ContactPhoneNumberLocalEntity>,
)
