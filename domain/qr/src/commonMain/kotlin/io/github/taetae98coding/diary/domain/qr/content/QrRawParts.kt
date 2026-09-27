package io.github.taetae98coding.diary.domain.qr.content

internal class WifiEntries(
    val prefix: String,
    val items: MutableList<Pair<String, String>>,
) {
    fun value(key: String): String? = items.firstOrNull { (name, _) -> name.equals(key, ignoreCase = true) }?.second?.unescapeWifiText()

    companion object {
        fun parse(raw: String): WifiEntries =
            WifiEntries(
                prefix = raw.take(WIFI_PREFIX_LENGTH),
                items =
                    raw
                        .drop(WIFI_PREFIX_LENGTH)
                        .splitUnescaped(';')
                        .filter(String::isNotEmpty)
                        .map { item -> item.substringBefore(':') to item.substringAfter(':', missingDelimiterValue = "") }
                        .toMutableList(),
            )
    }
}

internal fun String.unescapeWifiText(): String =
    buildString {
        var index = 0
        val text = this@unescapeWifiText
        while (index < text.length) {
            if (text[index] == '\\' && index + 1 < text.length) {
                append(text[index + 1])
                index += 2
            } else {
                append(text[index])
                index += 1
            }
        }
    }

internal class GeoParts(
    val prefix: String,
    val coordinates: MutableList<String>,
    val suffix: String,
) {
    override fun toString(): String = prefix + coordinates.joinToString(separator = ",") + suffix

    companion object {
        fun parse(raw: String): GeoParts {
            val body = raw.drop(GEO_PREFIX_LENGTH)
            val end = body.indexOfFirst { char -> char == ';' || char == '?' }.takeIf { index -> index >= 0 } ?: body.length

            return GeoParts(
                prefix = raw.take(GEO_PREFIX_LENGTH),
                coordinates = body.substring(0, end).split(',').toMutableList(),
                suffix = body.substring(end),
            )
        }
    }
}
