package io.github.taetae98coding.diary.domain.file

internal const val MAX_FILE_NAME_LENGTH: Int = 255

// 서버의 char_length와 같이 UTF-16 단위가 아니라 유니코드 문자 단위로 센다.
internal fun String.fileNameLength(): Int {
    var length = 0
    var index = 0

    while (index < this.length) {
        val isSurrogatePair = this[index].isHighSurrogate() && index + 1 < this.length && this[index + 1].isLowSurrogate()

        index += if (isSurrogatePair) 2 else 1
        length++
    }

    return length
}
