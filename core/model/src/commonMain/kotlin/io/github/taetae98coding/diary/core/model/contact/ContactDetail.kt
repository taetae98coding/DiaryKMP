package io.github.taetae98coding.diary.core.model.contact

import io.github.taetae98coding.diary.core.model.measure.Length

public data class ContactDetail(
    val name: String,
    val description: String,
    val height: Length?,
    val footSize: Length?,
    val birthday: ContactBirthday?,
    val phoneNumberList: List<ContactPhoneNumber>,
) {
    public companion object {
        public val EMPTY: ContactDetail =
            ContactDetail(
                name = "",
                description = "",
                height = null,
                footSize = null,
                birthday = null,
                phoneNumberList = emptyList(),
            )
    }
}
