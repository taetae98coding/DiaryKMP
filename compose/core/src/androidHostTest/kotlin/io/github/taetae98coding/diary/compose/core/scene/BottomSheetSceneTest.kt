@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavEntry
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BottomSheetSceneTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Bottom Sheet 바깥 영역을 누르면 onBack을 호출한다`() {
        val key = fixtureMonkey.giveMeOne<String>()
        val contentText = fixtureMonkey.giveMeOne<String>()
        var onBackCount = 0
        val entry =
            NavEntry(
                key = key,
                contentKey = key,
                content = { Text(text = contentText) },
            )
        val scene =
            BottomSheetScene(
                key = key,
                previousEntries = emptyList(),
                overlaidEntries = emptyList(),
                entry = entry,
                modalBottomSheetProperties = ModalBottomSheetProperties(),
                onBack = { onBackCount += 1 },
            )

        composeRule.setContent {
            DiaryTheme {
                scene.content()
            }
        }

        composeRule.onNodeWithText(contentText).assertExists()
        composeRule.onNodeWithContentDescription(CLOSE_SHEET_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = DISMISS_TIMEOUT_MILLIS) {
            onBackCount == 1
        }
    }

    companion object {
        private const val CLOSE_SHEET_DESCRIPTION = "Close sheet"
        private const val DISMISS_TIMEOUT_MILLIS = 5_000L
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
