package io.github.taetae98coding.diary.core.testing.file

import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

// 제목은 비어 있으면 올리지 않으므로 앞에 고정 문자열을 붙인다.
public fun Arb.Companion.fileUploadContent(
    uri: FileUri? = null,
    title: String? = null,
    description: String? = null,
): Arb<FileUploadContent> =
    arbitrary {
        FileUploadContent(
            uri = uri ?: Arb.fileUri().bind(),
            title = title ?: "title-${Arb.string().bind()}",
            description = description ?: Arb.string().bind(),
        )
    }
