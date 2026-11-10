package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.qr.ui.DEFAULT_ENTITY_TAG_LABEL
import io.github.taetae98coding.diary.feature.qr.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.qr.ui.add.INFO_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.QR_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.editableText
import io.github.taetae98coding.diary.feature.qr.ui.add.isTabSelected
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrFieldInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onTitleInput
import io.github.taetae98coding.diary.feature.qr.ui.add.selectFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.selectTab
import io.github.taetae98coding.diary.feature.qr.ui.add.titleInputText
import io.github.taetae98coding.diary.feature.qr.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.AndroidQrActionLauncherFactory
import io.github.taetae98coding.diary.feature.qr.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.qr.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-5d89ff00-ceeb-4c24-a3d5-1848c5c6d489 조회 중에는 태그 입력을 표시하지 않는다`() {
        setQrDetailScreen(uiState = QrDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-5d89ff00-ceeb-4c24-a3d5-1848c5c6d489 조회가 끝나면 태그 입력을 표시한다`() {
        setQrDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-a12e8b7f-b512-4146-889a-e204972bc4da 태그 입력은 정보 탭에서만 제공한다`() {
        setQrDetailScreen()

        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()

        composeRule.selectTab(INFO_TAB_INDEX)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-fcadae2e-3edb-41eb-a5a3-9cccc8d90f99 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag))
        setQrDetailScreen(tagViewModel = tagViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.add(tagId = selectableTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-fcadae2e-3edb-41eb-a5a3-9cccc8d90f99 태그를 해제해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag))
        setQrDetailScreen(tagViewModel = tagViewModel)

        unlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.remove(tagId = linkedTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-fcab9320-1f82-4b91-8fac-10fe51cd39d1 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val navigatedIdList = mutableListOf<Uuid>()
        setQrDetailScreen(
            tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag)),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-6b058cc9-ad6f-4635-afc4-1f43c9de3021 태그를 연결해도 입력 중인 내용, 포맷, 보고 있던 탭과 되돌리기 기록은 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val qrViewModel = qrViewModel(uiState = content())
        setQrDetailScreen(qrViewModel = qrViewModel, tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag)))
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput(NETWORK_NAME)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UNDO_DESCRIPTION).assertIsEnabled()
        composeRule.selectTab(INFO_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
        composeRule.waitForIdle()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.titleInputText() shouldBe STORED_TITLE + APPENDED_TEXT
        composeRule.onNodeWithContentDescription(DEFAULT_UNDO_DESCRIPTION).assertIsEnabled()
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe NETWORK_NAME
        verify(exactly = 0) { qrViewModel.update(detail = any()) }
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 상세 대상 QR에 연결한다`() {
        val tagViewModel = detailTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        val tagId = Uuid.random()
        setQrDetailScreen(tagViewModel = tagViewModel, resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = tagId)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = tagId) }
    }

    @Test
    fun `연결할 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setQrDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    private fun linkTag(title: String) {
        composeRule.clickEntityTagInput()
        awaitPickerRow(title = title)
        composeRule.onNodeWithText(title).performClick()
        composeRule.waitForIdle()
    }

    // 연결된 태그는 연결 대상 표시와 선택 목록에 모두 나타나므로, 선택 목록의 마지막 노드를 눌러 해제한다.
    private fun unlinkTag(title: String) {
        composeRule.clickEntityTagInput()
        awaitPickerRow(title = title, count = 2)
        composeRule.onAllNodesWithText(title).onLast().performClick()
        composeRule.waitForIdle()
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitPickerRow(
        title: String,
        count: Int = 1,
    ) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().size >= count) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("태그 선택 목록에 $title 이 나타나지 않았다")
    }

    private fun setQrDetailScreen(
        uiState: QrDetailUiState = content(),
        qrViewModel: QrDetailViewModel = qrViewModel(uiState = uiState),
        tagViewModel: EntityDetailTagViewModel = detailTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        navigateToTagDetail: (Uuid) -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            ScreenContent(
                qrViewModel = qrViewModel,
                tagViewModel = tagViewModel,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
                resultEventBus = resultEventBus,
            )
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ScreenContent(
        qrViewModel: QrDetailViewModel,
        tagViewModel: EntityDetailTagViewModel,
        navigateToTagAdd: () -> Unit,
        navigateToTagDetail: (Uuid) -> Unit,
        resultEventBus: ResultEventBus,
    ) {
        DiaryTheme {
            QrDetailScreen(
                navigateUp = {},
                navigateToScan = {},
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                permissionManager = rememberPermissionManager(),
                qrActionLauncherFactory = remember { AndroidQrActionLauncherFactory() },
                resultEventBus = remember { resultEventBus },
                componentVisibleProvider = { QrDetailScaffoldComponentVisible() },
                qrViewModel = qrViewModel,
                tagViewModel = tagViewModel,
            )
        }
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val DEFAULT_UPDATE_DESCRIPTION = "Update QR code"
        const val DEFAULT_UNDO_DESCRIPTION = "Undo"
        const val LINKED_TAG_TITLE = "QrDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "QrDetailSelectableTag"
        const val STORED_TITLE = "QrDetailStoredTitle"
        const val STORED_VALUE = "hello"
        const val APPENDED_TEXT = "2"
        const val FORMAT_LABEL = "Format"
        const val WIFI_FORMAT = "Wi-Fi"
        const val NETWORK_NAME_LABEL = "Network name"
        const val NETWORK_NAME = "home"

        fun content(): QrDetailUiState = QrDetailUiState.Content(id = Uuid.random(), detail = QrDetail(title = STORED_TITLE, description = "", value = STORED_VALUE))

        fun qrViewModel(uiState: QrDetailUiState): QrDetailViewModel =
            mockk<QrDetailViewModel>(relaxed = true).apply {
                every { this@apply.uiState } returns MutableStateFlow(uiState)
                every { effect } returns emptyFlow()
            }
    }
}
