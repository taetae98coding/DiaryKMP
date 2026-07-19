package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import kotlinx.serialization.json.Json

internal class ContactPhoneNumberListColumnTypeConverter {
    @ColumnTypeConverter
    fun phoneNumberListToText(phoneNumberList: List<ContactPhoneNumberLocalEntity>): String = json.encodeToString(phoneNumberList)

    @ColumnTypeConverter
    fun textToPhoneNumberList(value: String): List<ContactPhoneNumberLocalEntity> = json.decodeFromString(value)

    private companion object {
        val json: Json = Json
    }
}
