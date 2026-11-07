package io.github.taetae98coding.diary.compose.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarOverride
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarOverrideScope
import io.github.taetae98coding.diary.compose.core.appbar.LocalDiaryTopBarOverride
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenu
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuIcon
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuItem
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuOverride
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonMenuOverrideScope
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonOverride
import io.github.taetae98coding.diary.compose.core.button.DiaryFloatingActionButtonOverrideScope
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.button.LocalDiaryFloatingActionButtonMenuOverride
import io.github.taetae98coding.diary.compose.core.button.LocalDiaryFloatingActionButtonOverride
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryComponentOverrideTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `상단 바는 제공된 Override로 그린다`() {
        var scope: DiaryTopBarOverrideScope? = null
        val override =
            object : DiaryTopBarOverride {
                @Composable
                override fun DiaryTopBarOverrideScope.TopBar() {
                    scope = this
                }
            }
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalDiaryTopBarOverride provides override) {
                    DiaryTopBar(title = {}, isTitleCentered = true)
                }
            }
        }

        scope?.isTitleCentered shouldBe true
    }

    @Test
    fun `떠 있는 버튼은 이름과 진행 상태와 누름을 Override에 그대로 넘긴다`() {
        val contentDescription = Arb.nonBlankString().next()
        var clickCount = 0
        var scope: DiaryFloatingActionButtonOverrideScope? = null
        val override =
            object : DiaryFloatingActionButtonOverride {
                @Composable
                override fun DiaryFloatingActionButtonOverrideScope.FloatingActionButton() {
                    scope = this
                }
            }
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalDiaryFloatingActionButtonOverride provides override) {
                    FloatingAddButton(onClick = { clickCount += 1 }, contentDescription = contentDescription, isInProgressProvider = { true })
                }
            }
        }

        val captured = checkNotNull(scope)
        captured.contentDescription shouldBe contentDescription
        captured.isInProgressProvider() shouldBe true
        captured.onClick()
        clickCount shouldBe 1
    }

    @Test
    fun `떠 있는 메뉴 버튼은 항목을 순서대로 Override에 넘긴다`() {
        val texts = List(3) { Arb.nonBlankString().next() }
        val clicked = mutableListOf<Int>()
        var scope: DiaryFloatingActionButtonMenuOverrideScope? = null
        val override =
            object : DiaryFloatingActionButtonMenuOverride {
                @Composable
                override fun DiaryFloatingActionButtonMenuOverrideScope.FloatingActionButtonMenu() {
                    scope = this
                }
            }
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalDiaryFloatingActionButtonMenuOverride provides override) {
                    DiaryFloatingActionButtonMenu(
                        expanded = false,
                        onExpandedChange = {},
                        icon = DiaryFloatingActionButtonMenuIcon.CHECKLIST,
                        contentDescription = texts.first(),
                        expandedContentDescription = texts.last(),
                        items = texts.mapIndexed { index, text -> DiaryFloatingActionButtonMenuItem(text = text, icon = DiaryFloatingActionButtonMenuIcon.SELECT_ALL, onClick = { clicked += index }) },
                    )
                }
            }
        }

        val items = checkNotNull(scope).items
        items.map { item -> item.text } shouldBe texts
        items.forEach { item -> item.onClick() }
        clicked shouldBe texts.indices.toList()
    }
}
