package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.add.actionNameList
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.add.visibleTextList
import io.github.taetae98coding.diary.feature.qr.ui.card.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class QrHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setQrHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setQrHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-019 한국어 환경에서 QR 추가는 접근성 이름으로 제공된다`() {
        setQrHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(KOREAN_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-019 기본 환경에서 QR 추가는 접근성 이름으로 제공된다`() {
        setQrHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(DEFAULT_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-015 계정에 저장된 QR을 목록으로 표시하고 각 항목에 제목과 QR 값을 담은 그림을 보여 준다`() {
        val qrList = listOf(testQr(), testQr())
        setQrHomeScaffold(pagingData = qrPagingDataOf(qrList))

        qrList.forEach { qr -> composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).assertExists() }
        composeRule.qrCodeValueList() shouldContainExactlyInAnyOrder qrList.map { qr -> qr.detail.value }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-033 제목, 뒤로가기, QR 목록과 QR 추가 외의 정보와 동작을 두지 않는다`() {
        val qr = testQr()
        setQrHomeScaffold(pagingData = qrPagingDataOf(listOf(qr)))

        composeRule.visibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, qr.detail.title)
        composeRule.qrCodeValueList() shouldBe listOf(qr.detail.value)
        composeRule.actionNameList() shouldContainExactlyInAnyOrder listOf(KOREAN_NAVIGATE_UP_DESCRIPTION, KOREAN_ADD_DESCRIPTION)
    }

    @Test
    fun `TC-QR-HOME-FEATURE-016 QR의 설명과 QR 값의 글자는 목록에 표시하지 않는다`() {
        val qr =
            qrTestFixtureMonkey.qr(
                isDeleted = false,
                detail = qrTestFixtureMonkey.qrDetail(description = "description-${qrTestFixtureMonkey.giveMeOne<String>()}"),
            )
        setQrHomeScaffold(pagingData = qrPagingDataOf(listOf(qr)))

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).assertExists()
        composeRule.onNodeWithText(qr.detail.description, substring = true, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(qr.detail.value, substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-018 목록의 QR을 선택해도 아무 일도 일어나지 않는다`() {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        val snackbarHostState = SnackbarHostState()
        val qr = testQr()
        setQrHomeScaffold(pagingData = qrPagingDataOf(listOf(qr)), onEvent = eventList::add, snackbarHostState = snackbarHostState)

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).performClick()
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).assertExists()
        composeRule.runOnIdle { snackbarHostState.currentSnackbarData shouldBe null }
    }

    @Test
    fun `QR 카드는 누를 수 있는 요소로 두지 않는다`() {
        setQrHomeScaffold(pagingData = qrPagingDataOf(listOf(testQr())))

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasClickAction()).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-020 표시할 QR이 없으면 빈 상태 안내를 표시한다`() {
        setQrHomeScaffold(pagingData = qrPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-020 기본 환경에서 빈 상태 안내를 표시한다`() {
        setQrHomeScaffold(pagingData = qrPagingDataOf(emptyList()))

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-021 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setQrHomeScaffold(pagingData = loadingQrPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-022 표시할 QR이 있으면 빈 상태 안내를 표시하지 않는다`() {
        val qr = testQr()
        setQrHomeScaffold(pagingData = qrPagingDataOf(listOf(qr)))

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-DATA-002 목록을 조회하지 못하면 빈 상태 안내를 표시하고 오류를 알리지 않는다`() {
        val snackbarHostState = SnackbarHostState()
        setQrHomeScaffold(pagingData = failedQrPagingData(), snackbarHostState = snackbarHostState)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.runOnIdle { snackbarHostState.currentSnackbarData shouldBe null }
    }

    @Test
    fun `TC-QR-HOME-FEATURE-023 QR이 표시된 목록을 당기면 새로고침을 요청한다`() {
        assertRefreshRequested(pagingData = qrPagingDataOf(listOf(testQr())))
    }

    @Test
    fun `TC-QR-HOME-FEATURE-023 빈 상태 안내가 표시된 목록을 당기면 새로고침을 요청한다`() {
        assertRefreshRequested(pagingData = qrPagingDataOf(emptyList()))
    }

    @Test
    fun `TC-QR-HOME-FEATURE-024 새로고침이 실행되는 동안 진행이 표시되고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setQrHomeScaffold(
            pagingData = qrPagingDataOf(listOf(testQr())),
            uiStateProvider = { QrHomeUiState(isRefreshing = isRefreshing.value) },
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `빈 상태에서도 QR 추가를 실행할 수 있다`() {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        setQrHomeScaffold(pagingData = qrPagingDataOf(emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        setQrHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrHomeScaffoldEvent.ClickNavigateUp)
    }

    private fun assertRefreshRequested(pagingData: PagingData<Qr>) {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        setQrHomeScaffold(pagingData = pagingData, onEvent = eventList::add)

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrHomeScaffoldEvent.Refresh)
    }

    private fun setQrHomeScaffold(
        pagingData: PagingData<Qr> = qrPagingDataOf(emptyList()),
        onEvent: (QrHomeScaffoldEvent) -> Unit = {},
        snackbarHostState: SnackbarHostState = SnackbarHostState(),
        uiStateProvider: () -> QrHomeUiState = { QrHomeUiState() },
    ) {
        val qrPagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                QrHomeScaffold(
                    onEvent = onEvent,
                    snackbarHostState = snackbarHostState,
                    qrPagingItems = qrPagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = uiStateProvider,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val KOREAN_TITLE = "QR"
        const val DEFAULT_TITLE = "QR"
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val KOREAN_EMPTY_TITLE = "아직 QR이 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 QR을 만들 수 있습니다"
        const val DEFAULT_EMPTY_TITLE = "No QR codes yet"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a QR code."
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
