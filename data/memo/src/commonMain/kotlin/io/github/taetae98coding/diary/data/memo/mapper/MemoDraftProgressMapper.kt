package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress

private const val TITLE_KEY = "title"
private const val DESCRIPTION_KEY = "description"
private const val HEX_RADIX = 16
private const val UNICODE_ESCAPE_LENGTH = 4

// 조각을 이어 붙인 중간 결과는 아직 닫히지 않은 JSON이라 파서로 읽을 수 없으므로, 최상위 객체의 글 값만 끊긴 곳까지 직접 읽는다.
internal fun String.toMemoDraftProgress(): MemoDraftProgress {
    val valueMap = PartialJsonObjectReader(text = this).readTopLevelStringMap()

    return MemoDraftProgress(
        title = valueMap[TITLE_KEY].orEmpty(),
        description = valueMap[DESCRIPTION_KEY].orEmpty(),
    )
}

private class PartialJsonObjectReader(
    private val text: String,
) {
    private var index = 0
    private val valueMap = mutableMapOf<String, String>()

    fun readTopLevelStringMap(): Map<String, String> {
        skipWhitespace()
        var isReading = consume('{')

        while (isReading) {
            isReading = readEntry()
        }

        return valueMap
    }

    // 키와 값 하나를 끝까지 읽었으면 true, 객체가 끝났거나 글이 끊겨 더 읽을 수 없으면 false를 돌려준다.
    private fun readEntry(): Boolean {
        skipWhitespace(isCommaSkipped = true)
        if (peek() != '"') return false

        val key = readString()
        skipWhitespace()

        return key != null && consume(':') && readValue(key = key)
    }

    // 값을 끝까지 읽었으면 true, 글이 끊겨 더 읽을 수 없으면 false를 돌려준다.
    private fun readValue(key: String): Boolean {
        skipWhitespace()
        if (peek() != '"') return skipNonStringValue()

        val partial = StringBuilder()
        val isComplete = readStringInto(partial)
        valueMap[key] = partial.toString()

        return isComplete
    }

    private fun readString(): String? {
        val builder = StringBuilder()

        return if (readStringInto(builder)) builder.toString() else null
    }

    // 여는 따옴표부터 읽어 닫는 따옴표를 만나면 true, 그 전에 글이 끊기면 그때까지 읽은 내용을 남기고 false를 돌려준다.
    private fun readStringInto(builder: StringBuilder): Boolean {
        index += 1
        var isClosed = false

        while (index < text.length && !isClosed) {
            val char = text[index]
            when (char) {
                '"' -> {
                    index += 1
                    isClosed = true
                }

                '\\' -> if (!appendEscape(builder)) index = text.length

                else -> {
                    builder.append(char)
                    index += 1
                }
            }
        }

        return isClosed
    }

    // 이스케이프가 끊겨 다 읽을 수 없으면 반쯤 읽은 글자를 남기지 않고 false를 돌려준다.
    private fun appendEscape(builder: StringBuilder): Boolean {
        val escaped = text.getOrNull(index + 1)
        val unescaped =
            when (escaped) {
                null -> null

                'u' ->
                    text
                        .substring(minOf(index + 2, text.length), minOf(index + 2 + UNICODE_ESCAPE_LENGTH, text.length))
                        .takeIf { hex -> hex.length == UNICODE_ESCAPE_LENGTH }
                        ?.toIntOrNull(radix = HEX_RADIX)
                        ?.toChar()

                else -> escaped.toUnescaped()
            }

        if (unescaped != null) {
            builder.append(unescaped)
            index += if (escaped == 'u') 2 + UNICODE_ESCAPE_LENGTH else 2
        }

        return unescaped != null
    }

    // 객체·배열·숫자 같은 글이 아닌 값을 건너뛴다. 값 안의 글에 든 괄호는 세지 않는다.
    private fun skipNonStringValue(): Boolean {
        var depth = 0
        var isComplete: Boolean? = null

        while (isComplete == null && index < text.length) {
            val char = text[index]

            if (char == '"') {
                if (readString() == null) isComplete = false
            } else {
                depth += char.toDepthChange()
                if (depth < 0 || (depth == 0 && char == ',')) isComplete = true else index += 1
            }
        }

        return isComplete ?: false
    }

    private fun skipWhitespace(isCommaSkipped: Boolean = false) {
        while (text.getOrNull(index)?.isSkippable(isCommaSkipped = isCommaSkipped) == true) index += 1
    }

    private fun peek(): Char? = text.getOrNull(index)

    private fun consume(char: Char): Boolean {
        if (peek() != char) return false

        index += 1
        return true
    }
}

private fun Char.toUnescaped(): Char =
    when (this) {
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        'b' -> '\b'
        'f' -> '\u000C'
        else -> this
    }

private fun Char.toDepthChange(): Int =
    when (this) {
        '{', '[' -> 1
        '}', ']' -> -1
        else -> 0
    }

private fun Char.isSkippable(isCommaSkipped: Boolean): Boolean = isWhitespace() || (isCommaSkipped && this == ',')
