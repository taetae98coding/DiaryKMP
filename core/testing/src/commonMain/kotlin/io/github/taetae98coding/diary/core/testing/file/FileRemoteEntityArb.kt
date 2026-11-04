package io.github.taetae98coding.diary.core.testing.file

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.continuedFileUploadRemoteEntity(): Arb<ContinuedFileUploadRemoteEntity> =
    arbitrary {
        ContinuedFileUploadRemoteEntity(
            name = Arb.string().bind(),
            contentLength = Arb.long().bind(),
            sentBytes = Arb.long().bind(),
        )
    }

public fun Arb.Companion.fileCursorRemoteEntity(): Arb<FileCursorRemoteEntity> =
    arbitrary {
        FileCursorRemoteEntity(
            createdAt = Arb.secondPrecisionInstant().bind(),
            id = Arb.kotlinUuid().bind(),
        )
    }

public fun Arb.Companion.fileRemoteEntity(): Arb<FileRemoteEntity> =
    arbitrary {
        FileRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            mimeType = Arb.string().bind(),
            size = Arb.long().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
