package io.github.taetae98coding.diary.core.testing.contact

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import kotlin.uuid.Uuid

public fun FixtureMonkey.contact(
    name: String,
    isFavorite: Boolean,
    isDeleted: Boolean,
): Contact =
    Contact(
        id = giveMeOne<Uuid>(),
        detail = giveMeKotlinBuilder<ContactDetail>().sample().copy(name = name),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = giveMeOne(),
        createdAt = giveMeOne(),
    )
