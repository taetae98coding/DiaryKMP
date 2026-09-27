package io.github.taetae98coding.diary.core.testing.file

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlin.math.absoluteValue

public fun FixtureMonkey.fileUri(): FileUri = FileUri("file:///${giveMeOne<Int>()}")

public fun FixtureMonkey.diaryFile(
    name: String = "file-${giveMeOne<Int>()}.txt",
    size: Long = giveMeOne<DiaryFile>().size,
): DiaryFile = giveMeOne<DiaryFile>().copy(name = name, size = size)

// 올릴 수 있는 크기 안에서 만든다. 크기 자체를 검증하는 테스트는 값을 직접 넘긴다.
public fun FixtureMonkey.fileUploadSource(
    size: Long = giveMeOne<Int>().toLong().absoluteValue % MAX_UPLOAD_SIZE_BYTES,
    name: String = "file-${giveMeOne<Int>()}.txt",
): FileUploadSource =
    FileUploadSource(
        uri = fileUri(),
        name = name,
        mimeType = "application/${giveMeOne<Int>().absoluteValue}",
        size = size,
    )

private const val MAX_UPLOAD_SIZE_BYTES = 52_428_800L
