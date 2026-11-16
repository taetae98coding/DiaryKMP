package io.github.taetae98coding.diary.core.data.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity

public fun ContactLocalEntity.toListItem(nameQualifier: String = ""): ContactListItemLocalEntity = ContactListItemLocalEntity(contact = this, nameQualifier = nameQualifier)
