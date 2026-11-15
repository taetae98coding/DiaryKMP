package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

internal const val DEFAULT_SHARE_BUTTON_DESCRIPTION = "Share"
internal const val DEFAULT_SHARE_FAILED_MESSAGE = "Couldn't share."
internal const val DEFAULT_SHARE_COPIED_MESSAGE = "Copied to clipboard."

internal fun testContentSharer(
    isTextSupported: Boolean = true,
    result: ShareResult = ShareResult.COMPLETED,
): ContentSharer =
    mockk<ContentSharer> {
        every { this@mockk.isTextSupported } returns isTextSupported
        every { isFileSupported(any()) } returns isTextSupported
        coEvery { share(any()) } returns result
    }

internal fun contentSharerFactoryOf(contentSharer: ContentSharer = testContentSharer()): ContentSharerFactory =
    object : ContentSharerFactory {
        @Composable
        override fun rememberContentSharer(): ContentSharer = contentSharer
    }

internal fun ComposeContentTestRule.sharedContent(contentSharer: ContentSharer): ShareContent.Text {
    val content = slot<ShareContent>()

    runOnIdle { coVerify(exactly = 1) { contentSharer.share(capture(content)) } }

    return content.captured.shouldBeInstanceOf<ShareContent.Text>()
}

internal fun ComposeContentTestRule.sharedText(contentSharer: ContentSharer): String = sharedContent(contentSharer = contentSharer).text
