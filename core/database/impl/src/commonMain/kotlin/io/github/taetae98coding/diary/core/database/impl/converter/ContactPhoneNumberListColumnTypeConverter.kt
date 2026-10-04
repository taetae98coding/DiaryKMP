package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity

internal class ContactPhoneNumberListColumnTypeConverter {
    @ColumnTypeConverter
    fun phoneNumberListToText(phoneNumberList: List<ContactPhoneNumberLocalEntity>): String = codec.encode(phoneNumberList)

    @ColumnTypeConverter
    fun textToPhoneNumberList(value: String): List<ContactPhoneNumberLocalEntity> = codec.decode(value)

    private companion object {
        val codec: JsonListColumnCodec<ContactPhoneNumberLocalEntity> = JsonListColumnCodec(ContactPhoneNumberLocalEntity.serializer())
    }
}
