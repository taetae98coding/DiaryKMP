@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.qr.ui

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.api.qrNavKeys
import io.github.taetae98coding.diary.feature.qr.ui.add.INFO_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.QR_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddUiState
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddViewModel
import io.github.taetae98coding.diary.feature.qr.ui.add.descriptionInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.editableText
import io.github.taetae98coding.diary.feature.qr.ui.add.isTabSelected
import io.github.taetae98coding.diary.feature.qr.ui.add.onDescriptionInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrFieldInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrTextInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onTitleInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTextInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.selectFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.selectTab
import io.github.taetae98coding.diary.feature.qr.ui.add.titleInputText
import io.github.taetae98coding.diary.feature.qr.ui.card.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.qr.ui.detail.QrDetailEffect
import io.github.taetae98coding.diary.feature.qr.ui.detail.QrDetailUiState
import io.github.taetae98coding.diary.feature.qr.ui.detail.QrDetailViewModel
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeViewModel
import io.github.taetae98coding.diary.feature.qr.ui.home.qrPagingDataOf
import io.github.taetae98coding.diary.feature.qr.ui.home.testQr
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 qrEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class QrListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(QrListDetailMoreNavKey, QrHomeNavKey)
    private val resultEventBus = ResultEventBus()
    private val homeViewModelList = mutableListOf<QrHomeViewModel>()
    private var homeQrList: List<Qr> = emptyList()

    private var directiveOverride by mutableStateOf<PaneScaffoldDirective?>(null)
    private val homePagingData = MutableStateFlow(qrPagingDataOf(emptyList()))

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
    fun `TC-e550c67e-bef5-47fc-901f-1aa289f145cd 목록과 상세를 함께 쓰는 환경에서는 QR 목록과 QR 추가를 함께 표시한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)

        setQrNavDisplay(qrList = listOf(qr))

        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-9f938363-dcba-49b2-a6a7-b5700b7ecbca 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        composeRule.onNode(card(qr)).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(card(qr)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
    }

    @Test
    fun `TC-0bef3603-ac7c-4223-9a7c-a4f739706bbb 목록과 함께 표시되면 상세 영역의 화면에서 뒤로가기 수단을 제공하지 않는다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        assertAddOnDetailPane()
        composeRule.onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)

        selectQr(qr)

        assertDetailOnDetailPane()
        composeRule.onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-67f801e9-f1eb-454b-8117-d81c79f7e028 단독으로 표시되면 그 화면에서 뒤로가기 수단을 제공한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)

        selectQr(qr)
        assertDetailOnDetailPane()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
    }

    @Test
    fun `TC-840ed486-7f62-4dbc-b388-d96e66516e6e 목록에서 QR을 선택하면 목록을 유지한 채 상세 영역이 그 QR의 상세로 바뀐다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        selectQr(qr)

        backStack.last() shouldBe QrDetailNavKey(id = qr.id)
        composeRule.onNode(card(qr)).assertExists()
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-083fc33a-16a8-4821-95f5-5de278b4d54e 상세 영역에서 뒤로가면 QR 추가로 되돌아간다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-e0087465-5eba-4381-bc81-2e02b458583d 상세에서 QR을 삭제하면 상세 영역이 QR 추가로 되돌아간다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        composeRule.onNode(detailDeleteButton()).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-4a0f92d9-f28c-46c2-b467-95f532ffa0e0 QR 상세가 놓인 동안 QR 추가를 실행하면 상세 영역이 QR 추가로 바뀐다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        openAddOnDetail(qr)

        backStack.last() shouldBe QrAddNavKey
        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-d6dce3f1-eb44-4386-b3ff-b64bfeaf5db3 QR 상세 위에서 연 QR 추가에서 뒤로가면 이전 QR 상세로 돌아간다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        openAddOnDetail(qr)

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id))
        composeRule.onNode(card(qr)).assertExists()
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-747eb9b9-40e9-4d85-bd64-d216527532a4 QR 상세 위에서 연 QR 추가는 빈 입력으로 시작한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        fillPlaceholder()

        openAddOnDetail(qr)

        backStack.last() shouldBe QrAddNavKey
        assertAddInputEmpty()
    }

    @Test
    fun `TC-f9e861c8-4958-4974-8fe7-3e57a52a5998 QR 상세가 놓인 동안 다른 QR을 고르면 교체하고 뒤로가면 QR 추가로 돌아간다`() {
        val first = testQr(titlePrefix = FIRST_PREFIX)
        val second = testQr(titlePrefix = SECOND_PREFIX)
        setQrNavDisplay(qrList = listOf(first, second))

        selectQr(first)
        selectQr(second)

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = second.id))
        composeRule.onAllNodesWithText(detailTitle(second)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodesWithText(detailTitle(first)).fetchSemanticsNodes().isEmpty() shouldBe true

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        composeRule.onNode(card(first)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-76f07609-186c-4cf3-b52b-7fa4181c4896 QR 상세 위에서 연 QR 추가가 놓인 동안 다른 QR을 고르면 추가를 걷어내고 그 상세로 바꾼다`() {
        val first = testQr(titlePrefix = FIRST_PREFIX)
        val second = testQr(titlePrefix = SECOND_PREFIX)
        setQrNavDisplay(qrList = listOf(first, second))
        openAddOnDetail(first)
        composeRule.onTitleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        selectQr(second)
        assertDetailOnDetailPane()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = second.id))

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInputEmpty()
    }

    @Test
    fun `TC-76f07609-186c-4cf3-b52b-7fa4181c4896 좁은 창에서 연 QR 추가가 창이 넓어져 상세 영역에 놓인 동안 QR을 고르면 추가를 걷어내고 그 상세로 바꾼다`() {
        val qr = testQr(titlePrefix = FIRST_PREFIX)
        directiveOverride = singlePaneDirective()
        setQrNavDisplay(qrList = listOf(qr))
        waitUntilDisplayed(qr)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(card(qr)).assertDoesNotExist()
        composeRule.onTitleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { directiveOverride = null }
        composeRule.waitForIdle()
        selectQr(qr)
        assertDetailOnDetailPane()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id))

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInputEmpty()
    }

    @Test
    fun `TC-9ef7bec1-0b07-4a30-8565-cbbc5fd64f18 QR을 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.onNode(card(qr)).assertDoesNotExist()
    }

    @Test
    fun `TC-1976a6bb-5dd0-4256-a91e-da1ee3230f45 선택 전 QR 추가에서 연 QR 스캔은 단독으로 표시하고 돌아오면 같은 배치로 돌아온다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))

        openQrScan()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrScanNavKey)
        composeRule.onNode(card(qr)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertDoesNotExist()
        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-1976a6bb-5dd0-4256-a91e-da1ee3230f45 QR 상세에서 연 QR 스캔은 단독으로 표시하고 돌아오면 같은 QR 상세가 놓인 배치로 돌아온다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        openQrScan()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id), QrScanNavKey)
        composeRule.onNode(card(qr)).assertDoesNotExist()
        composeRule.onNode(detailDeleteButton()).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id))
        composeRule.onNode(card(qr)).assertExists()
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-64b617f5-fa25-4fc0-b4fd-d3d1b1482067 선택 전 QR 추가에서 연 QrScan이 읽은 값을 돌려주면 돌아온 상세 영역의 QR 추가에 반영된다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        openQrScan()

        composeRule.runOnIdle { backStack.navigateUpWithQrScannedValue(resultEventBus = resultEventBus, value = SCANNED_VALUE) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        composeRule.onNode(card(qr)).assertExists()
        assertAddOnDetailPane()
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.qrTextInputText() shouldBe SCANNED_VALUE
    }

    @Test
    fun `TC-75ce6921-30f2-4bf4-8343-b6e86cb0a42a QR 상세가 함께 놓여 있어도 목록의 뒤로가기는 더보기로 돌아간다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-b4d13c9d-cb8d-4427-92eb-abf059a39745 상세에 열린 QR을 목록에서 밀어 삭제해도 상세 영역은 그 QR의 상세로 남는다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        composeRule.onNode(card(qr)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { homeViewModelList.last().delete(id = qr.id) }
        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id))
        composeRule.onNode(detailDeleteButton()).assertExists()
    }

    @Test
    fun `TC-044c27d7-9eed-402e-a01c-3ab8a6d5c412 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(QrHomeNavKey) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-c633b20c-d4b5-4078-a691-8c438a203be3 더보기로 돌아갔다가 다시 진입하면 상세 영역의 QR 추가는 입력이 모두 비어 있는 상태로 시작한다`() {
        setQrNavDisplay(qrList = listOf(testQr(titlePrefix = LIST_PREFIX)))
        fillPlaceholder()

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(QrHomeNavKey) }
        composeRule.waitForIdle()

        assertAddInputEmpty()
    }

    @Test
    fun `TC-0042406f-0ca1-486d-994f-1c074f3cc67c 배치를 떠나기 전에는 상세 영역에서 QR 상세를 보고 돌아와도 QR 추가의 입력이 남는다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        fillPlaceholder()

        selectQr(qr)
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        pressBack()

        assertAddOnDetailPane()
        assertPlaceholderInputRetained()
    }

    @Test
    fun `TC-2a25cc4e-0bfc-4f05-9e7c-a3366ddd8a3f 배치를 떠나기 전에는 QrScan 화면에 다녀와도 상세 영역의 QR 추가의 입력이 남는다`() {
        setQrNavDisplay(qrList = listOf(testQr(titlePrefix = LIST_PREFIX)))
        fillPlaceholder()

        openQrScan()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        assertAddOnDetailPane()
        assertPlaceholderInputRetained()
    }

    @Test
    fun `TC-2d86ffd1-bb64-48a2-b93b-c113586e0f33 선택 전 QR 추가에서 추가에 성공해도 상세 영역은 QR 추가로 남고 추가한 QR이 목록에 나타난다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        composeRule.onTitleInput().performTextInput(ADDED_TITLE)
        composeRule.onQrTextInput().performTextInput(PLACEHOLDER_VALUE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
        composeRule.titleInputText() shouldBe ""
        composeRule.qrTextInputText() shouldBe ""
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(QR_CARD_TEST_TAG) and hasText(ADDED_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(card(qr)).assertExists()
    }

    @Test
    fun `TC-43bfeeaa-05eb-44d2-afec-e13bf4f9ce14 목록에서 연 QR 추가를 뒤로가기로 닫아도 선택 전 QR 추가의 작성 상태가 남는다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        fillPlaceholder()
        openAddOnDetail(qr)
        assertAddInputEmpty()

        pressBack()
        assertDetailOnDetailPane()
        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
        assertPlaceholderInputRetained()
    }

    @Test
    fun `TC-43bfeeaa-05eb-44d2-afec-e13bf4f9ce14 목록에서 연 QR 추가를 다른 QR을 골라 걷어내도 선택 전 QR 추가의 작성 상태가 남는다`() {
        val first = testQr(titlePrefix = FIRST_PREFIX)
        val second = testQr(titlePrefix = SECOND_PREFIX)
        setQrNavDisplay(qrList = listOf(first, second))
        fillPlaceholder()
        openAddOnDetail(first)
        assertAddInputEmpty()

        selectQr(second)
        assertDetailOnDetailPane()
        pressBack()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey)
        assertAddOnDetailPane()
        assertPlaceholderInputRetained()
    }

    @Test
    fun `TC-38782318-2135-41a3-b7d6-fd35353c8741 화면이 재생성되어도 상세 선택을 유지한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorableNavDisplay(qrList = listOf(qr), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        selectQr(qr)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(QrHomeNavKey, QrDetailNavKey(id = qr.id))
        waitUntilDisplayed(qr)
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(QrHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-bfc566f4-a387-4134-9da0-215cd5a25ec9 시스템이 앱을 정리했다가 다시 만들어도 상세 선택과 뒤로가기 기록을 유지한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorableNavDisplay(qrList = listOf(qr), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        openAddOnDetail(qr)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(QrHomeNavKey, QrDetailNavKey(id = qr.id), QrAddNavKey)
        waitUntilDisplayed(qr)
        assertAddOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(QrHomeNavKey, QrDetailNavKey(id = qr.id))
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(QrHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-2990d483-92d2-4a6f-8c37-f867d289d0f7 앱이 백그라운드에 다녀와도 상세 선택을 유지한다`() {
        val qr = testQr(titlePrefix = LIST_PREFIX)
        setQrNavDisplay(qrList = listOf(qr))
        selectQr(qr)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrListDetailMoreNavKey, QrHomeNavKey, QrDetailNavKey(id = qr.id))
        composeRule.onNode(card(qr)).assertExists()
        assertDetailOnDetailPane()
        pressBack()
        assertAddOnDetailPane()
    }

    // 텍스트 QR 값을 적은 뒤 URL 포맷의 입력을 고쳐 되돌릴 기록을 남기고, 마지막에 정보 탭을 본다.
    private fun fillPlaceholder() {
        composeRule.onTitleInput().performTextInput(PLACEHOLDER_TITLE)
        composeRule.onDescriptionInput().performTextInput(PLACEHOLDER_DESCRIPTION)
        composeRule.onQrTextInput().performTextInput(PLACEHOLDER_VALUE)
        composeRule.waitForIdle()
        composeRule.selectFormat(formatLabel = DEFAULT_FORMAT_LABEL, formatName = URL_FORMAT_NAME)
        composeRule.waitForIdle()
        composeRule.onQrFieldInput(URL_FORMAT_NAME).performTextInput(PLACEHOLDER_URL)
        composeRule.waitForIdle()
        composeRule.selectTab(INFO_TAB_INDEX)
        composeRule.waitForIdle()
    }

    // 탭을 고르는 입력 조회보다 먼저 보고 있던 탭과 되돌리기를 확인한다.
    private fun assertPlaceholderInputRetained() {
        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.onNode(undoButton()).assertIsEnabled()
        composeRule.titleInputText() shouldBe PLACEHOLDER_TITLE
        composeRule.descriptionInputText() shouldBe PLACEHOLDER_DESCRIPTION
        composeRule.onQrFieldInput(URL_FORMAT_NAME).editableText() shouldBe PLACEHOLDER_URL
    }

    // QrAdd `진입`과 같은 상태: 정보 탭, 빈 제목·설명·QR 값, 텍스트 포맷, 되돌릴 기록 없음.
    private fun assertAddInputEmpty() {
        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.onNode(undoButton()).assertIsNotEnabled()
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.qrTextInputText() shouldBe ""
        composeRule.onNode(hasSetTextAction() and hasText(URL_FORMAT_NAME)).assertDoesNotExist()
    }

    private fun assertAddOnDetailPane() {
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNode(detailDeleteButton()).assertDoesNotExist()
    }

    private fun assertDetailOnDetailPane() {
        composeRule.onNode(detailDeleteButton()).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
    }

    private fun openQrScan() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        composeRule.onNode(hasContentDescription(DEFAULT_SCAN_DESCRIPTION) and hasClickAction()).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertExists()
    }

    private fun selectQr(qr: Qr) {
        waitUntilDisplayed(qr)
        composeRule.onNode(card(qr)).performClick()
        composeRule.waitForIdle()
    }

    private fun openAddOnDetail(qr: Qr) {
        selectQr(qr)
        assertDetailOnDetailPane()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun waitUntilDisplayed(qr: Qr) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(qr.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun singlePaneDirective(): PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 1)

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setQrNavDisplay(qrList: List<Qr>) {
        homeQrList = qrList
        homePagingData.value = qrPagingDataOf(qrList)
        val module = viewModelModule()
        composeRule.setContent {
            QrListDetailTestHost(module = module) {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies =
                            listOf(
                                rememberDiaryListDetailSceneStrategy(
                                    directive = directiveOverride ?: calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
                                ),
                            ),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<QrListDetailMoreNavKey> { Text(text = MORE_CONTENT) }
                                qrEntry(backStack = backStack)
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        qrList.firstOrNull()?.let(::waitUntilDisplayed)
    }

    // 시스템이 되살릴 때 전환 이력이 저장된 상태에서 다시 만들어지도록 제품 내비게이션 키 직렬화 구성으로 전환 이력을 기억한다.
    private fun setRestorableNavDisplay(
        qrList: List<Qr>,
        restorationTester: StateRestorationTester,
        onBackStack: (NavBackStack<ScreenNavKey>) -> Unit,
    ) {
        homeQrList = qrList
        homePagingData.value = qrPagingDataOf(qrList)
        val module = viewModelModule()
        restorationTester.setContent {
            QrListDetailTestHost(module = module) {
                val restorableBackStack =
                    rememberSerializable(
                        configuration = qrSavedStateConfiguration,
                        serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                    ) { NavBackStack<ScreenNavKey>(QrHomeNavKey) }
                val holder = rememberListDetailPlaceholderStateHolder()
                onBackStack(restorableBackStack)

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = restorableBackStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider = entryProvider { qrEntry(backStack = restorableBackStack) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        qrList.firstOrNull()?.let(::waitUntilDisplayed)
    }

    @Composable
    private fun QrListDetailTestHost(
        module: Module,
        content: @Composable () -> Unit,
    ) {
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
            KoinApplication(configuration = koinConfiguration { modules(module) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun viewModelModule(): Module =
        module {
            factory {
                mockk<QrHomeViewModel>(relaxed = true)
                    .apply {
                        every { qrPagingData } returns homePagingData
                        every { effect } returns emptyFlow()
                    }.also(homeViewModelList::add)
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val qr = homeQrList.first { qr -> qr.id == id }
                val effectChannel = Channel<QrDetailEffect>(capacity = Channel.BUFFERED)
                mockk<QrDetailViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(QrDetailUiState.Content(id = id, detail = qr.detail.copy(title = detailTitle(qr))))
                    every { effect } returns effectChannel.receiveAsFlow()
                    every { delete() } answers { effectChannel.trySend(QrDetailEffect.DeleteSucceeded).getOrThrow() }
                }
            }
            factory {
                val effectChannel = Channel<QrAddEffect>(capacity = Channel.BUFFERED)
                mockk<QrAddViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(QrAddUiState())
                    every { effect } returns effectChannel.receiveAsFlow()
                    // 추가가 저장되면 목록이 다시 조회되는 것과 같게 추가한 QR을 목록 조회 결과에 더한다.
                    every { add(detail = any()) } answers {
                        homeQrList = homeQrList + testQr().copy(detail = firstArg<QrDetail>())
                        homePagingData.value = qrPagingDataOf(homeQrList)
                        effectChannel.trySend(QrAddEffect.AddSucceeded).getOrThrow()
                    }
                }
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val LIST_PREFIX = "list-"
        const val FIRST_PREFIX = "a-$LIST_PREFIX"
        const val SECOND_PREFIX = "b-$LIST_PREFIX"
        const val DETAIL_PREFIX = "detail-"
        const val ADDED_TITLE = "added-title"
        const val PLACEHOLDER_URL = "https://a.com"
        const val URL_FORMAT_NAME = "URL"
        const val DEFAULT_FORMAT_LABEL = "Format"
        const val DEFAULT_UNDO_DESCRIPTION = "Undo"
        const val PLACEHOLDER_TITLE = "placeholder-title"
        const val PLACEHOLDER_DESCRIPTION = "placeholder-description"
        const val PLACEHOLDER_VALUE = "placeholder-value"
        const val SCANNED_VALUE = "scanned-value"
        const val TYPED_ADD_TITLE = "typed-add-title"
        const val DEFAULT_ADD_TITLE = "Add QR code"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val DEFAULT_DELETE_DESCRIPTION = "Delete QR code"
        const val DEFAULT_SCAN_DESCRIPTION = "Scan QR code"
        const val DEFAULT_SCAN_TITLE = "Scan QR code"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L

        val qrSavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { qrNavKeys() }
                    }
            }

        fun detailTitle(qr: Qr): String = DETAIL_PREFIX + qr.detail.title

        fun undoButton(): SemanticsMatcher = hasContentDescription(DEFAULT_UNDO_DESCRIPTION) and hasClickAction()

        fun card(qr: Qr): SemanticsMatcher = hasTestTag(QR_CARD_TEST_TAG) and hasText(qr.detail.title)

        fun detailDeleteButton(): SemanticsMatcher = hasContentDescription(DEFAULT_DELETE_DESCRIPTION) and hasClickAction()
    }
}

// QR 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object QrListDetailMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
