package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner

internal enum class DownloadTool(
    val command: String,
    val formula: String,
) {
    YT_DLP(command = "yt-dlp", formula = "yt-dlp"),
    FFMPEG(command = "ffmpeg", formula = "ffmpeg"),
}

internal val downloadToolList: List<DownloadTool> =
    listOf(
        DownloadTool.YT_DLP,
        DownloadTool.FFMPEG,
    )

internal const val HOMEBREW_COMMAND: String = "brew"

internal suspend fun CommandRunner.findMissingDownloadToolList(): List<DownloadTool> = downloadToolList.filter { tool -> find(command = tool.command) == null }
