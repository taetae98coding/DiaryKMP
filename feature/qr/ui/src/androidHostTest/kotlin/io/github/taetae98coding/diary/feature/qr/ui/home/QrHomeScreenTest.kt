package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.card.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class QrHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-003 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setQrHomeScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-QR-HOME-FEATURE-013 QR이 표시되어 있을 때 QR 추가를 선택하면 QrAdd 화면으로 이동한다`() {
        var navigateToAddCount = 0
        val qr = testQr()
        setQrHomeScreen(
            pagingDataFlow = MutableStateFlow(qrPagingDataOf(listOf(qr))),
            navigateToAdd = { navigateToAddCount += 1 },
        )
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-QR-HOME-FEATURE-013 빈 상태 안내가 표시되어 있을 때 QR 추가를 선택하면 QrAdd 화면으로 이동한다`() {
        var navigateToAddCount = 0
        setQrHomeScreen(navigateToAdd = { navigateToAddCount += 1 })
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-QR-HOME-FEATURE-023 QR이 표시된 목록을 당기면 계정 데이터의 서버 동기화를 한 번 요청한다`() {
        val environment = setQrHomeScreen(pagingDataFlow = MutableStateFlow(qrPagingDataOf(listOf(testQr()))))

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-023 빈 상태 안내가 표시된 목록을 당기면 계정 데이터의 서버 동기화를 한 번 요청한다`() {
        val environment = setQrHomeScreen()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-QR-HOME-DATA-003 QrHome 진입만으로는 서버 동기화를 요청하지 않는다`() {
        val environment = setQrHomeScreen(pagingDataFlow = MutableStateFlow(qrPagingDataOf(listOf(testQr()))))

        composeRule.waitForIdle()

        verify(exactly = 0) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-017 QR이 새로 저장되면 별도 조작 없이 목록에 나타난다`() {
        val existing = testQr()
        val added = testQr()
        val pagingDataFlow = MutableStateFlow(qrPagingDataOf(listOf(existing)))
        setQrHomeScreen(pagingDataFlow = pagingDataFlow)
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(existing.detail.title)).assertExists()

        composeRule.runOnIdle { pagingDataFlow.value = qrPagingDataOf(listOf(existing, added)) }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(added.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(added.detail.title)).assertExists()
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(existing.detail.title)).assertExists()
    }

    private fun setQrHomeScreen(
        pagingDataFlow: MutableStateFlow<PagingData<Qr>> = MutableStateFlow(qrPagingDataOf(emptyList())),
        navigateUp: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
    ): Environment {
        val qrViewModel = mockk<QrHomeViewModel>(relaxed = true)
        every { qrViewModel.qrPagingData } returns pagingDataFlow
        every { qrViewModel.effect } returns emptyFlow()
        val syncViewModel = mockk<QrHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(QrHomeUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                QrHomeScreen(
                    navigateUp = navigateUp,
                    navigateToAdd = navigateToAdd,
                    qrViewModel = qrViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitForIdle()

        return Environment(syncViewModel = syncViewModel)
    }

    private class Environment(
        val syncViewModel: QrHomeSyncViewModel,
    )

    private companion object {
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
