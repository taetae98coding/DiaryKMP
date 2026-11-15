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

public fun String.decodeUriComponent(): String = decodeHexEscapes(marker = '%')

public fun String.decodeQuotedPrintable(): String = decodeHexEscapes(marker = '=')

// 표식과 16진수는 모두 ASCII라 UTF-8 바이트 단위로 다뤄도 다른 글자를 쪼개지 않는다.
private fun String.decodeHexEscapes(marker: Char): String {
    val source = encodeToByteArray()
    val target = ByteArray(source.size)
    val markerByte = marker.code.toByte()
    var size = 0
    var index = 0
    while (index < source.size) {
        val code = if (source[index] == markerByte) source.hexCodeOrNull(start = index + 1) else null
        if (code != null) {
            target[size] = code.toByte()
            index += 1 + PERCENT_LENGTH
        } else {
            target[size] = source[index]
            index += 1
        }
        size += 1
    }
    return target.decodeToString(endIndex = size)
}

private fun ByteArray.hexCodeOrNull(start: Int): Int? {
    val high = getOrNull(start)?.hexDigitOrNull()
    val low = getOrNull(start + 1)?.hexDigitOrNull()

    return if (high == null || low == null) null else high * PERCENT_RADIX + low
}

private fun Byte.hexDigitOrNull(): Int? = toInt().toChar().digitToIntOrNull(radix = PERCENT_RADIX)
