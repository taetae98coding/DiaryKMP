package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-008 추가 처리 중 추가 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        setContactAddScaffold(uiState = ContactAddUiState(isInProgress = true))

        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION))),
                useUnmergedTree = true,
            ).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `추가 버튼을 누르면 추가 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<ContactAddScaffoldEvent>()
        setContactAddScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactAddScaffoldEvent.ClickAdd)
    }

    private fun setContactAddScaffold(
        onEvent: (ContactAddScaffoldEvent) -> Unit = {},
        uiState: ContactAddUiState = ContactAddUiState(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                ContactAddScaffold(
                    onEvent = onEvent,
                    uiStateProvider = { uiState },
                )
            }
        }
    }
}
