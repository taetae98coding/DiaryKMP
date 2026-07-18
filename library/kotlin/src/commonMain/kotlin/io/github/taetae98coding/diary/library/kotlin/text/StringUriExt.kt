package io.github.taetae98coding.diary.library.kotlin.text

private const val UNRESERVED = "-_.~"
private const val PERCENT_RADIX = 16
private const val PERCENT_LENGTH = 2
private const val BYTE_MASK = 0xFF

// 공용 Kotlin에는 URL 인코딩이 없어 RFC 3986의 unreserved 문자만 남기고 UTF-8 바이트를 퍼센트 인코딩한다.
public fun String.encodeUriComponent(): String =
    encodeToByteArray().joinToString(separator = "") { byte ->
        val code = byte.toInt() and BYTE_MASK
        val char = code.toChar()

        if (char.isUnreserved()) {
            char.toString()
        } else {
            "%${code.toString(radix = PERCENT_RADIX).uppercase().padStart(length = PERCENT_LENGTH, padChar = '0')}"
        }
    }

private fun Char.isUnreserved(): Boolean = this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9' || this in UNRESERVED
