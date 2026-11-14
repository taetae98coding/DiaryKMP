package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

internal const val DEFAULT_SHARE_DESCRIPTION = "Share"
internal const val DEFAULT_SHARE_FAILED_MESSAGE = "Couldn't share."
internal const val DEFAULT_SHARE_COPIED_MESSAGE = "Copied to clipboard."

internal fun webDetailContentSharer(
    isTextSupported: Boolean = true,
    result: ShareResult = ShareResult.COMPLETED,
): ContentSharer =
    mockk {
        every { this@mockk.isTextSupported } returns isTextSupported
        every { isFileSupported(any()) } returns isTextSupported
        coEvery { share(any()) } returns result
    }

internal fun ContentSharer.asFactory(): ContentSharerFactory =
    object : ContentSharerFactory {
        @Composable
        override fun rememberContentSharer(): ContentSharer = this@asFactory
    }

internal fun testContentSharerFactory(): ContentSharerFactory = webDetailContentSharer().asFactory()
