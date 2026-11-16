package io.github.taetae98coding.diary.core.data.testing.tag

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map

public fun Arb.Companion.localTag(
    isFinished: Boolean,
    isDeleted: Boolean,
    title: String? = null,
): Arb<TagLocalEntity> =
    arbitrary {
        val tag = Arb.tagLocalEntity().bind()

        tag.copy(
            detail = tag.detail.copy(title = title ?: tag.detail.title),
            isFinished = isFinished,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.remoteTag(
    isFinished: Boolean,
    isDeleted: Boolean,
): Arb<TagRemoteEntity> = Arb.tagRemoteEntity().map { tag -> tag.copy(isFinished = isFinished, isDeleted = isDeleted) }
