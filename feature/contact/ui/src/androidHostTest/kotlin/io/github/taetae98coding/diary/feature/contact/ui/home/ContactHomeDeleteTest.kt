package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-026 연락처 카드를 삭제 방향으로 밀면 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val environment = setContactHomeScreen()

        swipeFirstCardLeft()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-HOME-FEATURE-026 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        setContactHomeScreen()

        swipeFirstCardLeft()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-028 연락처 카드를 반대 방향으로 밀면 삭제를 요청하지 않고 안내도 표시하지 않는다`() {
        val environment = setContactHomeScreen()

        composeRule.onNode(hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(FIRST_NAME)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNodeWithText(FIRST_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-027 삭제를 실행 취소하면 그 연락처의 삭제 되돌리기를 요청한다`() {
        val environment = setContactHomeScreen()

        swipeFirstCardLeft()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-030 안내가 보이는 동안 다른 연락처를 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val environment = setContactHomeScreen()

        swipeFirstCardLeft()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        environment.effectChannel.trySend(ContactHomeEffect.Deleted(id = environment.second.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-031 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setContactHomeScreen()

        swipeFirstCardLeft()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-033 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setContactHomeScreen(isShownProvider = { isShown })

        swipeFirstCardLeft()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-034 목록에서 연락처를 삭제해도 화면을 이동하지 않는다`() {
        var navigateCount = 0
        setContactHomeScreen(onNavigate = { navigateCount += 1 })

        swipeFirstCardLeft()

        navigateCount shouldBe 0
    }

    private fun swipeFirstCardLeft() {
        composeRule.onNode(hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(FIRST_NAME)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun setContactHomeScreen(
        isShownProvider: () -> Boolean = { true },
        onNavigate: () -> Unit = {},
    ): Environment {
        val first = testContact(name = FIRST_NAME)
        val second = testContact(name = SECOND_NAME)
        val effectChannel = Channel<ContactHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<ContactHomeViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.NAME)
        every { viewModel.contactPagingData } returns MutableStateFlow(contactPagingDataOf(listOf(first, second)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(ContactHomeEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restore(id = any()) }
        val syncViewModel = mockk<ContactHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(ContactHomeUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    ContactHomeScreen(
                        navigateUp = onNavigate,
                        navigateToAdd = onNavigate,
                        navigateToDetail = { onNavigate() },
                        componentVisibleProvider = { ContactHomeScaffoldComponentVisible() },
                        contactViewModel = viewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_NAME).fetchSemanticsNodes().isNotEmpty()
        }

        return Environment(viewModel = viewModel, effectChannel = effectChannel, first = first, second = second)
    }

    private class Environment(
        val viewModel: ContactHomeViewModel,
        val effectChannel: Channel<ContactHomeEffect>,
        val first: Contact,
        val second: Contact,
    )

    private companion object {
        const val FIRST_NAME = "ContactHomeDeleteFirst"
        const val SECOND_NAME = "ContactHomeDeleteSecond"
        const val DEFAULT_DELETED_MESSAGE = "Contact deleted."
        const val KOREAN_DELETED_MESSAGE = "연락처가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
