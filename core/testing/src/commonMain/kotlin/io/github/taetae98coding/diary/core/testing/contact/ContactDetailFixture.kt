package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

/** 이름과 설명만 입력하고 선택 값은 모두 비워 둔 연락처 내용이다. 이름은 앞뒤 공백 없이 비어 있지 않다. */
public fun Arb.Companion.contactDetailWithoutOption(): Arb<ContactDetail> =
    arbitrary {
        ContactDetail(
            name = Arb.alphanumericString(prefix = "name").bind(),
            description = Arb.string().bind(),
            height = null,
            footSize = null,
            birthday = null,
            birthYear = null,
            hometown = "",
            howWeMet = "",
            phoneNumberList = emptyList(),
        )
    }

public fun Arb.Companion.localContact(
    isFavorite: Boolean,
    isDeleted: Boolean,
): Arb<ContactLocalEntity> = Arb.contactLocalEntity().map { contact -> contact.copy(isFavorite = isFavorite, isDeleted = isDeleted) }
