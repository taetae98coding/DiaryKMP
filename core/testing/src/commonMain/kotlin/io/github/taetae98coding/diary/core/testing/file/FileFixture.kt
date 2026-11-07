package io.github.taetae98coding.diary.core.testing.file

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.arbitrary.string

private const val MAX_UPLOAD_SIZE_BYTES = 52_428_800L

public fun Arb.Companion.fileUri(): Arb<FileUri> = Arb.int().map { value -> FileUri("file:///$value") }

public fun Arb.Companion.diaryFile(
    name: String? = null,
    size: Long? = null,
): Arb<DiaryFile> =
    arbitrary {
        DiaryFile(
            id = Arb.kotlinUuid().bind(),
            name = name ?: "file-${Arb.int().bind()}.txt",
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            mimeType = Arb.string().bind(),
            size = size ?: Arb.long().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

// 서버는 비어 있거나 공백뿐인 제목과 이름을 보관하지 않으므로, 서버가 돌려준 파일처럼 둘 다 있는 파일을 만든다.
public fun Arb.Companion.nonBlankDiaryFile(): Arb<DiaryFile> =
    arbitrary {
        Arb.diaryFile().bind().copy(
            title = Arb.nonBlankString().bind(),
            name = Arb.nonBlankString().bind(),
        )
    }

// 올릴 수 있는 크기 안에서 만든다. 크기 자체를 검증하는 테스트는 값을 직접 넘긴다.
public fun Arb.Companion.fileUploadSource(
    size: Long? = null,
    name: String? = null,
): Arb<FileUploadSource> =
    arbitrary {
        FileUploadSource(
            uri = Arb.fileUri().bind(),
            name = name ?: "file-${Arb.int().bind()}.txt",
            mimeType = "application/${Arb.nonNegativeInt().bind()}",
            size = size ?: Arb.long(0L until MAX_UPLOAD_SIZE_BYTES).bind(),
        )
    }
