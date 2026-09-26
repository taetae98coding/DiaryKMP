package io.github.taetae98coding.diary.feature.qr.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddUiState
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddViewModel
import io.github.taetae98coding.diary.feature.qr.ui.add.descriptionInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.onDescriptionInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrValueInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onTitleInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrCodeValue
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.add.qrValueInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.titleInputText
import io.github.taetae98coding.diary.feature.qr.ui.card.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.home.QR_HOME_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeSyncViewModel
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeUiState
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeViewModel
import io.github.taetae98coding.diary.feature.qr.ui.home.qrPagingDataOf
import io.github.taetae98coding.diary.feature.qr.ui.home.testQr
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(QrNavDisplayMoreNavKey, QrHomeNavKey)
    private val resultEventBus = ResultEventBus()
    private val addViewModelList = mutableListOf<QrAddViewModel>()
    private var homeQrList: List<Qr> = emptyList()

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트 앞뒤로 전역 Koin을 정리한다.
    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-015 QrScan 화면에서 읽지 않고 돌아오면 입력 값을 그대로 둔다`() {
        val value = qrTestFixtureMonkey.qrDetail().value
        setQrNavDisplay()
        openQrScanWithValue(value = value)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrNavDisplayMoreNavKey, QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    @Test
    fun `TC-QR-ADD-FEATURE-014 QrScan 화면에서 QR을 읽어 돌아오면 입력 값을 읽은 값으로 바꾸고 제목과 설명은 그대로 둔다`() {
        val scannedValue = qrTestFixtureMonkey.qrDetail().value
        val detail = qrTestFixtureMonkey.qrDetail()
        setQrNavDisplay()
        openQrScanWithValue(value = detail.value, title = detail.title, description = detail.description)

        composeRule.runOnIdle { backStack.navigateUpWithQrScannedValue(resultEventBus = resultEventBus, value = scannedValue) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrNavDisplayMoreNavKey, QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe scannedValue
        composeRule.qrCodeValue() shouldBe scannedValue
        composeRule.titleInputText() shouldBe detail.title
        composeRule.descriptionInputText() shouldBe detail.description
    }

    @Test
    fun `TC-QR-SCAN-FEATURE-007 QR을 읽으면 읽은 값을 넘기고 이전 화면으로 돌아간다`() {
        val scannedValue = qrTestFixtureMonkey.qrDetail().value
        setQrNavDisplay()
        openQrScanWithValue(value = qrTestFixtureMonkey.qrDetail().value)

        composeRule.runOnIdle { backStack.navigateUpWithQrScannedValue(resultEventBus = resultEventBus, value = scannedValue) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertDoesNotExist()
        backStack.toList() shouldBe listOf(QrNavDisplayMoreNavKey, QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe scannedValue
    }

    @Test
    fun `TC-QR-ADD-FEATURE-017 뒤로가기로 떠났다가 다시 들어오면 입력이 비어 있고 QR은 추가되지 않는다`() {
        setQrNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        val detail = qrTestFixtureMonkey.qrDetail()
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        composeRule.onQrValueInput().performTextInput(detail.value)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(QrNavDisplayMoreNavKey, QrHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrNavDisplayMoreNavKey, QrHomeNavKey, QrAddNavKey)
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.qrValueInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
        addViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(detail = any()) } }
    }

    @Test
    fun `TC-QR-HOME-DOMAIN-013 QR 추가로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        val qrList = positionQrList()
        setQrNavDisplay(qrList = qrList)
        scrollToPositionQr(qrList)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(QR_HOME_LIST_TEST_TAG).assertDoesNotExist()
        pressBack()

        assertPositionQrDisplayed(qrList)
    }

    @Test
    fun `TC-QR-HOME-DOMAIN-014 더보기로 나갔다 QR 바로가기로 다시 진입하면 목록의 맨 위부터 보여 준다`() {
        val qrList = positionQrList()
        setQrNavDisplay(qrList = qrList)
        scrollToPositionQr(qrList)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(QrHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onNode(qrCard(qrList.first())).assertIsDisplayed()
        composeRule.onNode(qrCard(qrList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-DOMAIN-014 앱을 다시 실행해 진입하면 목록의 맨 위부터 보여 준다`() {
        val qrList = positionQrList()

        setQrNavDisplay(qrList = qrList)
        waitUntilQrIsDisplayed(qrList.first())

        composeRule.onNode(qrCard(qrList.first())).assertIsDisplayed()
        composeRule.onNode(qrCard(qrList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    private fun scrollToPositionQr(qrList: List<Qr>) {
        waitUntilQrIsDisplayed(qrList.first())
        composeRule.onNodeWithTag(QR_HOME_LIST_TEST_TAG).performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionQrDisplayed(qrList)
    }

    private fun assertPositionQrDisplayed(qrList: List<Qr>) {
        composeRule.onNode(qrCard(qrList[POSITION_SCROLL_INDEX])).assertIsDisplayed()
        composeRule.onNode(qrCard(qrList.first())).assertDoesNotExist()
    }

    private fun waitUntilQrIsDisplayed(qr: Qr) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(qr.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun openQrScanWithValue(
        value: String,
        title: String = "",
        description: String = "",
    ) {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        if (title.isNotEmpty()) composeRule.onTitleInput().performTextInput(title)
        if (description.isNotEmpty()) composeRule.onDescriptionInput().performTextInput(description)
        composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(QrScanNavKey) }
        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertExists()
    }

    private fun setQrNavDisplay(qrList: List<Qr> = emptyList()) {
        homeQrList = qrList
        composeRule.setContent {
            QrNavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<QrNavDisplayMoreNavKey> { Text(text = MORE_CONTENT) }
                            qrEntry(backStack = backStack)
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun QrNavDisplayTestHost(content: @Composable () -> Unit) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalResultEventBus provides resultEventBus,
        ) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory {
                mockk<QrHomeViewModel>(relaxed = true).apply {
                    every { qrPagingData } returns MutableStateFlow(qrPagingDataOf(homeQrList))
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<QrHomeSyncViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(QrHomeUiState())
                }
            }
            factory {
                mockk<QrAddViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(QrAddUiState())
                        every { effect } returns emptyFlow()
                    }.also(addViewModelList::add)
            }
        }

    public companion object {
        private const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SCAN_TITLE = "Scan QR code"
        private const val MORE_CONTENT = "MoreContent"
        private const val POSITION_QR_COUNT = 30
        private const val POSITION_SCROLL_INDEX = 25
        private const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L

        private fun qrCard(qr: Qr): SemanticsMatcher = hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)

        private fun positionQrList(): List<Qr> =
            List(POSITION_QR_COUNT) { index ->
                testQr(titlePrefix = "${index.toString().padStart(length = 2, padChar = '0')}-")
            }
    }
}

// QR 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object QrNavDisplayMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
