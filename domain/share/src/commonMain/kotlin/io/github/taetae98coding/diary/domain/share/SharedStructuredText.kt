package io.github.taetae98coding.diary.domain.share

import io.github.taetae98coding.diary.library.kotlin.text.StructuredLines
import io.github.taetae98coding.diary.library.kotlin.text.decodeQuotedPrintable

internal fun StructuredLines.firstBlockPropertyList(component: String): List<StructuredLines.Property> {
    val result = mutableListOf<StructuredLines.Property>()
    var softBrokenText = ""
    blockEntries(component).forEach { entry ->
        val text = softBrokenText + entry.text
        val property = entry.copy(text = text).property
        if (property != null && property.isQuotedPrintable() && text.endsWith(SOFT_LINE_BREAK)) {
            softBrokenText = text.dropLast(1)
        } else {
            softBrokenText = ""
            if (property != null) result += property.decoded()
        }
    }
    StructuredLines.Entry(text = softBrokenText, start = 0, endExclusive = 0).property?.let { property -> result += property.decoded() }

    return result
}

internal fun List<StructuredLines.Property>.valueList(name: String): List<String> = filter { property -> property.name == name }.map(StructuredLines.Property::value)

internal fun List<StructuredLines.Property>.first(name: String): StructuredLines.Property? = firstOrNull { property -> property.name == name }

private fun StructuredLines.Property.decoded(): StructuredLines.Property = if (isQuotedPrintable()) copy(value = value.decodeQuotedPrintable()) else this

// vCard 2.1은 매개변수 이름 없이 QUOTED-PRINTABLE만 적기도 한다.
private fun StructuredLines.Property.isQuotedPrintable(): Boolean = parameters.split(';').any { parameter -> parameter.substringAfter('=').equals(QUOTED_PRINTABLE, ignoreCase = true) }

private const val QUOTED_PRINTABLE = "QUOTED-PRINTABLE"
private const val SOFT_LINE_BREAK = '='
