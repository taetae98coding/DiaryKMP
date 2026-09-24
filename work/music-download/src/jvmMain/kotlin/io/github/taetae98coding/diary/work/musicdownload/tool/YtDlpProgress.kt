package io.github.taetae98coding.diary.work.musicdownload.tool

private val PERCENT_REGEX = Regex("""\[download]\s+([0-9.]+)%""")
private const val DESTINATION_PREFIX = "[download] Destination:"

private const val PERCENT_SCALE = 100F
private const val FIRST_PHASE_CEILING = 0.9F
private const val LATER_PHASE_CEILING = 0.99F

/**
 * yt-dlp는 영상과 소리를 따로 받아 백분율이 스트림마다 0부터 다시 시작한다.
 * 스펙(docs/spec/music-download.md의 `곡의 다운로드 상태`)이 백분율은 줄어들지 않는다고 정했으므로,
 * 첫 스트림을 0~90%에, 뒤따르는 스트림을 90~99%에 나누어 담아 한 방향으로만 늘어나게 한다.
 * 첫 스트림이 영상이라 대부분의 용량을 차지하므로 실제 진행과도 크게 어긋나지 않는다.
 */
internal class YtDlpProgress {
    private var phase = 0
    private var lastProgress = 0F

    fun onLine(line: String): Float? {
        if (line.startsWith(DESTINATION_PREFIX)) phase += 1

        val progress = line.toPercentOrNull()?.toProgress()

        return progress
            ?.takeIf { value -> value > lastProgress }
            ?.also { value -> lastProgress = value }
    }

    private fun String.toPercentOrNull(): Float? =
        PERCENT_REGEX
            .find(this)
            ?.groupValues
            ?.get(1)
            ?.toFloatOrNull()

    private fun Float.toProgress(): Float {
        val ratio = (this / PERCENT_SCALE).coerceIn(0F, 1F)

        return if (phase <= 1) {
            ratio * FIRST_PHASE_CEILING
        } else {
            FIRST_PHASE_CEILING + ratio * (LATER_PHASE_CEILING - FIRST_PHASE_CEILING)
        }
    }
}
