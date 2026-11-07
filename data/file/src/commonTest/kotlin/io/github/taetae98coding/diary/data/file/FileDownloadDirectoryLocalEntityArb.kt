package io.github.taetae98coding.diary.data.file

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.FileDownloadDirectoryLocalEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.fileDownloadDirectoryLocalEntity(): Arb<FileDownloadDirectoryLocalEntity> =
    arbitrary {
        FileDownloadDirectoryLocalEntity(
            uri = Arb.string().bind(),
            name = Arb.string().bind(),
            bookmark = Arb.string().bind(),
        )
    }
