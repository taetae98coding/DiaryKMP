package io.github.taetae98coding.diary.core.network.api.contact.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContactDetailRemoteEntity(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("heightCentimeter") val heightCentimeter: Double?,
    @SerialName("footSizeMillimeter") val footSizeMillimeter: Int?,
    @SerialName("birthday") val birthday: LocalDate?,
    @SerialName("birthdayCalendar") val birthdayCalendar: ContactBirthdayCalendarRemoteEntity?,
    @SerialName("phoneNumberList") val phoneNumberList: List<ContactPhoneNumberRemoteEntity>,
)
