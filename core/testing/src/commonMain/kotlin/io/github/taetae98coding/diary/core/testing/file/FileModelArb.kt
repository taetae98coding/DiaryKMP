package io.github.taetae98coding.diary.core.testing.file

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.diaryFileDetail(): Arb<DiaryFileDetail> =
    arbitrary {
        DiaryFileDetail(
            name = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }

public fun Arb.Companion.fileDownloadDirectory(): Arb<FileDownloadDirectory> =
    arbitrary {
        FileDownloadDirectory(
            uri = Arb.string().bind(),
            name = Arb.string().bind(),
            bookmark = Arb.string().bind(),
        )
    }

public fun Arb.Companion.fileUploadStateUploading(): Arb<FileUploadState.Uploading> =
    arbitrary {
        FileUploadState.Uploading(
            percent = Arb.int().orNull().bind(),
        )
    }
