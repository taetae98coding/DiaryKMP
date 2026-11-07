package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactListItem

// 이름이 같은 연락처가 없는 목록 항목이 기본이므로 구분 경로는 비워 둔다.
public fun Contact.toListItem(nameQualifier: String = ""): ContactListItem = ContactListItem(contact = this, nameQualifier = nameQualifier)

public fun ContactLocalEntity.toListItem(nameQualifier: String = ""): ContactListItemLocalEntity = ContactListItemLocalEntity(contact = this, nameQualifier = nameQualifier)
