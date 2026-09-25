package io.github.taetae98coding.diary.core.testing.file

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri

public fun FixtureMonkey.fileUri(): FileUri = FileUri("file:///${giveMeOne<Int>()}")

public fun FixtureMonkey.diaryFile(
    name: String = "file-${giveMeOne<Int>()}.txt",
    size: Long = giveMeOne<DiaryFile>().size,
): DiaryFile = giveMeOne<DiaryFile>().copy(name = name, size = size)
