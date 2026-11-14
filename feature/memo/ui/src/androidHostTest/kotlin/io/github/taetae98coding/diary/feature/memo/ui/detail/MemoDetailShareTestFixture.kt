package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

internal fun screenTestContentSharer(
    isTextSupported: Boolean = false,
    isCalendarFileSupported: Boolean = isTextSupported,
    result: ShareResult = ShareResult.COMPLETED,
): ContentSharer {
    val sharer = mockk<ContentSharer>()
    every { sharer.isTextSupported } returns isTextSupported
    every { sharer.isFileSupported(type = any()) } returns false
    every { sharer.isFileSupported(type = ShareFileType.CALENDAR) } returns isCalendarFileSupported
    coEvery { sharer.share(content = any()) } returns result
    return sharer
}

internal fun screenTestContentSharerFactory(sharer: ContentSharer = screenTestContentSharer()): ContentSharerFactory =
    object : ContentSharerFactory {
        @Composable
        override fun rememberContentSharer(): ContentSharer = sharer
    }
