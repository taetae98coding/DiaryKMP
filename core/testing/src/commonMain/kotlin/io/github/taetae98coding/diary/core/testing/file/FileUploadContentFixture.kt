package io.github.taetae98coding.diary.core.testing.file

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUri

// 제목은 비어 있으면 올리지 않으므로 앞에 고정 문자열을 붙인다.
public fun FixtureMonkey.fileUploadContent(
    uri: FileUri = fileUri(),
    title: String = "title-${giveMeOne<String>()}",
    description: String = giveMeOne<String>(),
): FileUploadContent =
    FileUploadContent(
        uri = uri,
        title = title,
        description = description,
    )
