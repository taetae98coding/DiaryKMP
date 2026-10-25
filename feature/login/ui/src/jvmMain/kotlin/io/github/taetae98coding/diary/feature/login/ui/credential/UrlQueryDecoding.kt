package io.github.taetae98coding.diary.feature.login.ui.credential

import java.net.URLDecoder

internal fun String.decodeUrlQuery(): Map<String, String> =
    split('&')
        .filter { it.isNotEmpty() }
        .associate { parameter ->
            val name = parameter.substringBefore('=')
            val value = parameter.substringAfter('=', missingDelimiterValue = "")

            URLDecoder.decode(name, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
        }
