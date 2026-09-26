package io.github.taetae98coding.diary.feature.qr.ui.home

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
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.card.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class QrHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-025 QR 카드를 삭제 방향으로 밀면 삭제되고 안내와 실행 취소가 표시된다`() {
        val environment = setQrHomeScreen()

        swipeCardLeft(qr = environment.first)

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.id) }
        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-025 기본 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = setQrHomeScreen()

        swipeCardLeft(qr = environment.first)

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-026 삭제를 실행 취소하면 그 QR의 삭제가 되돌려진다`() {
        val environment = setQrHomeScreen()

        swipeCardLeft(qr = environment.first)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-027 QR 카드를 반대 방향으로 밀면 아무 동작도 실행하지 않는다`() {
        val environment = setQrHomeScreen()

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(environment.first.detail.title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(environment.first.detail.title)).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-029 안내가 보이는 동안 다른 QR을 삭제하면 마지막 삭제의 안내만 남는다`() {
        val environment = setQrHomeScreen()

        swipeCardLeft(qr = environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        environment.effectChannel.trySend(QrHomeEffect.Deleted(id = environment.second.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-030 삭제가 저장되지 못하면 안내를 표시하지 않는다`() {
        val first = testQr()
        val pageQrUseCase = mockk<PageQrUseCase>()
        every { pageQrUseCase(parameter = Unit) } returns flowOf(Result.success(qrPagingDataOf(listOf(first))))
        val deleteQrUseCase = mockk<DeleteQrUseCase>()
        coEvery { deleteQrUseCase(parameter = any()) } returns Result.failure(IllegalStateException(qrTestFixtureMonkey.giveMeOne<String>()))
        val viewModel =
            QrHomeViewModel(
                pageQrUseCase = pageQrUseCase,
                deleteQrUseCase = deleteQrUseCase,
                restoreQrUseCase = mockk(),
            )
        setQrHomeScreenContent(viewModel = viewModel, first = first)

        swipeCardLeft(qr = first)

        coVerify(exactly = 1) { deleteQrUseCase(parameter = first.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-032 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setQrHomeScreen()

        swipeCardLeft(qr = environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-031 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setQrHomeScreen(isShownProvider = { isShown })

        swipeCardLeft(qr = environment.first)
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
    fun `목록에서 QR을 삭제해도 화면을 이동하지 않는다`() {
        var navigateCount = 0
        val environment = setQrHomeScreen(onNavigate = { navigateCount += 1 })

        swipeCardLeft(qr = environment.first)

        navigateCount shouldBe 0
    }

    private fun swipeCardLeft(qr: Qr) {
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun setQrHomeScreen(
        isShownProvider: () -> Boolean = { true },
        onNavigate: () -> Unit = {},
    ): Environment {
        val first = testQr()
        val second = testQr()
        val effectChannel = Channel<QrHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<QrHomeViewModel>()
        every { viewModel.qrPagingData } returns MutableStateFlow(qrPagingDataOf(listOf(first, second)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(QrHomeEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restore(id = any()) }

        setQrHomeScreenContent(
            viewModel = viewModel,
            first = first,
            isShownProvider = isShownProvider,
            onNavigate = onNavigate,
        )

        return Environment(
            viewModel = viewModel,
            effectChannel = effectChannel,
            first = first,
            second = second,
        )
    }

    private fun setQrHomeScreenContent(
        viewModel: QrHomeViewModel,
        first: Qr,
        isShownProvider: () -> Boolean = { true },
        onNavigate: () -> Unit = {},
    ) {
        val syncViewModel = mockk<QrHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(QrHomeUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    QrHomeScreen(
                        navigateUp = onNavigate,
                        navigateToAdd = onNavigate,
                        qrViewModel = viewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(first.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private class Environment(
        val viewModel: QrHomeViewModel,
        val effectChannel: Channel<QrHomeEffect>,
        val first: Qr,
        val second: Qr,
    )

    private companion object {
        const val DEFAULT_DELETED_MESSAGE = "QR code deleted."
        const val KOREAN_DELETED_MESSAGE = "QR이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
    }
}
