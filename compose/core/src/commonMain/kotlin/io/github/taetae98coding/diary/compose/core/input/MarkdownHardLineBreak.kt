package io.github.taetae98coding.diary.compose.core.input

private val FENCE_START_REGEX = Regex("^ {0,3}(`{3,}|~{3,})")

/**
 * 마크다운 표준(CommonMark)은 엔터 한 번(soft line break)을 공백으로 해석하므로,
 * 사용자가 엔터로 나눈 줄이 미리보기에서도 줄바꿈으로 보이도록 줄 끝에
 * hard line break(공백 두 개)를 붙인다.
 *
 * 다음 줄이 이어지지 않는 줄(마지막 줄, 빈 줄 앞)에 붙인 공백은 줄바꿈 대신
 * 트레일링 공백으로 렌더링되므로, 비어 있지 않은 다음 줄이 이어지는 경우에만 붙인다.
 * 빈 줄(문단 구분)은 그대로 두고, 코드 블록 내부는 내용이 바뀌지 않도록 건드리지 않는다.
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
