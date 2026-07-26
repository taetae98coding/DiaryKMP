package io.github.taetae98coding.diary.domain.core

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.mockk.every
import io.mockk.mockk

internal class RecordingDelegate(
    val delegate: DiaryLoggerDelegate,
    val logList: List<DiaryLog>,
)

internal fun recordingDelegate(): RecordingDelegate {
    val logList = mutableListOf<DiaryLog>()
    val delegate = mockk<DiaryLoggerDelegate>()

    every { delegate.log(log = any()) } answers {
        logList += firstArg<DiaryLog>()
    }

    return RecordingDelegate(delegate = delegate, logList = logList)
}
