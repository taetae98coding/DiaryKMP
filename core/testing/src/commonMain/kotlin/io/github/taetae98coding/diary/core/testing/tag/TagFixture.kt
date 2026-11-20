package io.github.taetae98coding.diary.core.testing.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map

public fun Arb.Companion.tag(
    isFinished: Boolean,
    isDeleted: Boolean,
): Arb<Tag> = Arb.tag().map { tag -> tag.copy(isFinished = isFinished, isDeleted = isDeleted) }

public fun Arb.Companion.tag(title: String): Arb<Tag> =
    arbitrary {
        Arb.tag().bind().copy(detail = Arb.tagDetail().bind().copy(emoji = "", title = title))
    }

public fun Arb.Companion.tag(
    title: String,
    isFinished: Boolean,
): Arb<Tag> = Arb.tag(title = title).map { tag -> tag.copy(isFinished = isFinished, isDeleted = false) }
