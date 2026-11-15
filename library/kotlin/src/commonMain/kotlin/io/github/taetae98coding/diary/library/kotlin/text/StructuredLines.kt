package io.github.taetae98coding.diary.library.kotlin.text

// vCard와 iCalendar의 줄을 다룬다. 공백이나 탭으로 시작하는 줄은 앞 줄에 이어진 줄이라 한 항목으로 묶고, 고치지 않은 줄은 글자 그대로 되돌려 쓴다.
public class StructuredLines private constructor(
    private val lineBreak: String,
    private val physicalLines: MutableList<String>,
) {
    public val entries: List<Entry>
        get() {
            val result = mutableListOf<Entry>()
            physicalLines.forEachIndexed { index, line ->
                val last = result.lastOrNull()
                if (last != null && line.isContinuation()) {
                    result[result.lastIndex] = last.copy(text = last.text + line.drop(1), endExclusive = index + 1)
                } else {
                    result += Entry(text = line, start = index, endExclusive = index + 1)
                }
            }
            return result
        }

    public val size: Int
        get() = physicalLines.size

    public fun find(
        name: String,
        range: IntRange = physicalLines.indices,
    ): Entry? = entries.firstOrNull { entry -> entry.start in range && entry.property?.name == name }

    // component 묶음 자체의 항목만 준다. 그 안에서 다시 시작하는 묶음(일정 안의 알림 등)의 항목은 뺀다.
    public fun blockEntries(component: String): List<Entry> {
        val entryList = entries
        val start = entryList.indexOfFirst { entry -> entry.text.equals("BEGIN:$component", ignoreCase = true) }
        if (start < 0) return emptyList()

        val result = mutableListOf<Entry>()
        var depth = 0
        for (entry in entryList.drop(start + 1)) {
            when {
                entry.text.startsWith(BEGIN_PREFIX, ignoreCase = true) -> depth += 1
                entry.text.startsWith(END_PREFIX, ignoreCase = true) -> if (depth == 0) break else depth -= 1
                depth == 0 -> result += entry
            }
        }
        return result
    }

    public fun indexOfLine(
        text: String,
        from: Int = 0,
    ): Int? = entries.firstOrNull { entry -> entry.start >= from && entry.text.equals(text, ignoreCase = true) }?.start

    public fun replace(
        entry: Entry,
        line: String,
    ) {
        repeat(entry.endExclusive - entry.start) { physicalLines.removeAt(entry.start) }
        physicalLines.add(entry.start, line)
    }

    public fun remove(entry: Entry) {
        repeat(entry.endExclusive - entry.start) { physicalLines.removeAt(entry.start) }
    }

    public fun insert(
        index: Int,
        line: String,
    ) {
        physicalLines.add(index, line)
    }

    override fun toString(): String = physicalLines.joinToString(separator = lineBreak)

    public data class Entry(
        val text: String,
        val start: Int,
        val endExclusive: Int,
    ) {
        public val property: Property?
            get() {
                val colon = text.indexOf(':')
                if (colon < 0) return null

                val head = text.substring(0, colon)
                val nameWithGroup = head.substringBefore(';')
                return Property(
                    name = nameWithGroup.substringAfterLast('.').uppercase(),
                    head = head,
                    parameters = head.substringAfter(';', missingDelimiterValue = ""),
                    value = text.substring(colon + 1),
                )
            }
    }

    public data class Property(
        val name: String,
        val head: String,
        val parameters: String,
        val value: String,
    )

    public companion object {
        public fun parse(raw: String): StructuredLines {
            val lineBreak =
                when {
                    raw.contains(LINE_BREAK) -> LINE_BREAK
                    raw.contains('\n') -> "\n"
                    raw.contains('\r') -> "\r"
                    else -> LINE_BREAK
                }

            return StructuredLines(lineBreak = lineBreak, physicalLines = raw.split(lineBreak).toMutableList())
        }
    }
}

private const val LINE_BREAK = "\r\n"
private const val BEGIN_PREFIX = "BEGIN:"
private const val END_PREFIX = "END:"

private fun String.isContinuation(): Boolean = startsWith(' ') || startsWith('\t')

public fun String.unescapeStructuredText(): String =
    buildString {
        var index = 0
        val text = this@unescapeStructuredText
        while (index < text.length) {
            val char = text[index]
            if (char == '\\' && index + 1 < text.length) {
                val next = text[index + 1]
                append(if (next == 'n' || next == 'N') '\n' else next)
                index += 2
            } else {
                append(char)
                index += 1
            }
        }
    }

public fun String.splitUnescaped(delimiter: Char): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var index = 0
    while (index < length) {
        val char = this[index]
        when {
            char == '\\' && index + 1 < length -> {
                current.append(char).append(this[index + 1])
                index += 2
                continue
            }

            char == delimiter -> {
                result += current.toString()
                current.clear()
            }

            else -> current.append(char)
        }
        index += 1
    }
    result += current.toString()
    return result
}

// N, ADR처럼 ;로 나뉜 값에서 비어 있지 않은 부분을 공백으로 잇는다.
public fun String.joinStructuredComponents(): String =
    splitUnescaped(';')
        .map(String::unescapeStructuredText)
        .filter(String::isNotEmpty)
        .joinToString(separator = " ")

public fun String.firstStructuredComponent(): String = splitUnescaped(';').first().unescapeStructuredText()
