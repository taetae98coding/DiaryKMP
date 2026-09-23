package io.github.taetae98coding.diary.compose.core.input

private val FENCE_START_REGEX = Regex("^ {0,3}(`{3,}|~{3,})")

/**
 * 줄 끝에 hard line break(공백 두 개)를 붙인다. 다음 줄이 이어지지 않는 줄(마지막 줄, 빈 줄 앞)에 붙인 공백은
 * 줄바꿈이 아니라 트레일링 공백으로 렌더링되므로, 비어 있지 않은 다음 줄이 이어질 때만 붙인다.
 */
internal fun String.withMarkdownHardLineBreak(): String {
    val lineList = lines()
    var openFenceMarker: String? = null

    return lineList
        .mapIndexed { index, line ->
            val fenceMarker = FENCE_START_REGEX.find(line)?.groupValues?.get(1)
            val marker = openFenceMarker

            when {
                marker != null -> {
                    if (line.isClosingFence(marker = marker)) {
                        openFenceMarker = null
                    }

                    line
                }

                fenceMarker != null -> {
                    openFenceMarker = fenceMarker
                    line
                }

                line.isBlank() -> line

                lineList.getOrNull(index + 1)?.isNotBlank() == true -> "$line  "

                else -> line
            }
        }.joinToString(separator = "\n")
}

private fun String.isClosingFence(marker: String): Boolean {
    val content = trim()

    return content.length >= marker.length && content.all { it == marker.first() }
}
