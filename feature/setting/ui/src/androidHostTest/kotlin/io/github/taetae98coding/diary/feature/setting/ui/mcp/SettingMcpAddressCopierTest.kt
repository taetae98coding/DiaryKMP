package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingMcpAddressCopierTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-96afd6dd-77e2-4fe8-8ded-56b705adb68d 주소를 복사하고 복사 안내를 한 번 표시한다`() {
        val clipboard = mockk<Clipboard>()
        val clipEntry = slot<ClipEntry>()
        coEvery { clipboard.setClipEntry(capture(clipEntry)) } just runs
        val snackbarHostState = SnackbarHostState()
        val copier = SettingMcpAddressCopier(clipboard = clipboard, snackbarHostState = snackbarHostState, copiedMessage = COPIED_MESSAGE)

        composeRule.setContent {
            DiaryTheme {
                SnackbarHost(hostState = snackbarHostState)
                LaunchedEffect(Unit) { copier.copy(address = ADDRESS) }
            }
        }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { clipboard.setClipEntry(any()) }
        clipEntry.captured.clipData
            .getItemAt(0)
            .text
            .toString() shouldBe ADDRESS
        snackbarHostState.currentSnackbarData?.visuals?.message shouldBe COPIED_MESSAGE
        composeRule.onAllNodesWithText(COPIED_MESSAGE).assertCountEquals(1)
    }

    public companion object {
        private const val ADDRESS = "http://127.0.0.1:38450/mcp"
        private const val COPIED_MESSAGE = "Address copied."
    }
}
