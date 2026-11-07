package io.github.taetae98coding.diary.core.network.impl

import io.github.taetae98coding.diary.core.network.impl.file.entity.FileUpdateRequestRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.fileUpdateRequestRemoteEntity(): Arb<FileUpdateRequestRemoteEntity> =
    arbitrary {
        FileUpdateRequestRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }
