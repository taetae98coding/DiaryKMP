package io.github.taetae98coding.diary.domain.file

import io.github.taetae98coding.diary.library.kotlin.text.codePointLength

internal const val MAX_FILE_NAME_LENGTH: Int = 255

// 서버의 char_length와 같이 UTF-16 단위가 아니라 유니코드 문자 단위로 센다.
internal fun String.fileNameLength(): Int = codePointLength()
