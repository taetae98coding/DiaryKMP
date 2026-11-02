package io.github.taetae98coding.diary.core.database.api.contact.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

public data class ContactListItemLocalEntity(
    @Embedded
    val contact: ContactLocalEntity,
    @ColumnInfo(name = "name_qualifier")
    val nameQualifier: String,
)
