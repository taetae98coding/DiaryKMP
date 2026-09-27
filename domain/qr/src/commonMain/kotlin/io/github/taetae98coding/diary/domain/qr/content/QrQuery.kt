package io.github.taetae98coding.diary.domain.qr.content

// URI 쿼리의 항목을 글자 그대로 들고 있다가, 고친 항목만 바꿔 다시 잇는다.
internal class QrQuery private constructor(
    private val items: MutableList<Pair<String, String>>,
) {
    fun value(key: String): String? = items.firstOrNull { (name, _) -> name.equals(key, ignoreCase = true) }?.second?.percentDecode()

    fun set(
        key: String,
        value: String,
    ) {
        val index = items.indexOfFirst { (name, _) -> name.equals(key, ignoreCase = true) }
        when {
            value.isEmpty() && index >= 0 -> items.removeAt(index)
            value.isEmpty() -> Unit
            index >= 0 -> items[index] = items[index].first to value.percentEncode()
            else -> items += key to value.percentEncode()
        }
    }

    override fun toString(): String = items.joinToString(separator = "&") { (name, value) -> if (value.isEmpty() && name.isNotEmpty()) name else "$name=$value" }

    fun isEmpty(): Boolean = items.isEmpty()

    companion object {
        fun parse(query: String): QrQuery =
            QrQuery(
                items =
                    query
                        .split('&')
                        .filter(String::isNotEmpty)
                        .map { item -> item.substringBefore('=') to item.substringAfter('=', missingDelimiterValue = "") }
                        .toMutableList(),
            )
    }
}

internal fun String.percentDecode(): String {
    val bytes = mutableListOf<Byte>()
    var index = 0
    while (index < length) {
        val char = this[index]
        val hex = if (char == '%' && index + 2 <= lastIndex) substring(index + 1, index + PERCENT_ESCAPE_LENGTH).toIntOrNull(radix = HEX_RADIX) else null
        if (hex != null) {
            bytes += hex.toByte()
            index += PERCENT_ESCAPE_LENGTH
        } else {
            char.toString().encodeToByteArray().forEach { byte -> bytes += byte }
            index += 1
        }
    }
    return bytes.toByteArray().decodeToString().replace(LINE_BREAK, "\n")
}

private const val HEX_RADIX = 16

private const val PERCENT_ESCAPE_LENGTH = 3
