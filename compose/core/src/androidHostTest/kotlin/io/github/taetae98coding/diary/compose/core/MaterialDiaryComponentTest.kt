package io.github.taetae98coding.diary.compose.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenu
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuIcon
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MaterialDiaryComponentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `떠 있는 메뉴 버튼은 펼치면 닫는 이름으로 바뀌고 항목을 누르면 그 동작을 부른다`() {
        val texts = List(3) { fixtureMonkey.nonBlankString() }
        var clickCount = 0
        var expanded by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryFloatingActionButtonMenu(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    icon = DiaryFloatingActionButtonMenuIcon.CHECKLIST,
                    contentDescription = texts[0],
                    expandedContentDescription = texts[1],
                    items = listOf(DiaryFloatingActionButtonMenuItem(text = texts[2], icon = DiaryFloatingActionButtonMenuIcon.SELECT_ALL, onClick = { clickCount += 1 })),
                )
            }
        }

        composeRule.onNodeWithContentDescription(texts[0]).performClick()
        composeRule.onNodeWithContentDescription(texts[1]).assertExists()
        composeRule.onNodeWithText(texts[2]).performClick()

        expanded shouldBe true
        clickCount shouldBe 1
    }
}
