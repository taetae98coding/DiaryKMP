package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.contact.ui.DEFAULT_ENTITY_TAG_LABEL
import io.github.taetae98coding.diary.feature.contact.ui.add.descriptionInput
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.add.phoneNumberInput
import io.github.taetae98coding.diary.feature.contact.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.contact.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.contact.ui.sendTagAddedResult
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-925abc74-1a9e-41aa-b873-afc04d1e412d 조회 중에는 태그 입력을 표시하지 않는다`() {
        setContactDetailScreen(uiState = ContactDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-925abc74-1a9e-41aa-b873-afc04d1e412d 조회가 끝나면 태그 입력을 표시한다`() {
        setContactDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-ea0c4f54-a07e-41cc-a919-3eba4ab43e1e 태그 입력은 연락처 디테일 탭에서만 제공한다`() {
        setContactDetailScreen()

        composeRule.selectContactDetailTab(contentDescription = DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()

        composeRule.selectContactDetailTab(contentDescription = DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-054b9e48-af6b-43aa-b640-c472b7cd65cc 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag))
        setContactDetailScreen(tagViewModel = tagViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.add(tagId = selectableTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-054b9e48-af6b-43aa-b640-c472b7cd65cc 태그를 해제해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag))
        setContactDetailScreen(tagViewModel = tagViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        unlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.remove(tagId = linkedTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-089ebcf4-6943-4716-8c4c-d73f3e4b739d 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val navigatedIdList = mutableListOf<Uuid>()
        setContactDetailScreen(
            tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag)),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-99f36b93-9374-4283-80f9-2a706946b0a9 태그를 연결해도 입력 중인 내용과 즐겨찾기 여부는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(isFavorite = true)))
        setContactDetailScreen(
            viewModel = viewModel,
            tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag)),
        )
        editForm()

        linkTag(title = SELECTABLE_TAG_TITLE)

        assertEditedForm()
        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_DESCRIPTION).assertExists()
        verify(exactly = 0) { viewModel.toggleFavorite() }
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-99f36b93-9374-4283-80f9-2a706946b0a9 태그를 해제해도 입력 중인 내용과 즐겨찾기 여부는 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(isFavorite = true)))
        setContactDetailScreen(
            viewModel = viewModel,
            tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag)),
        )
        editForm()

        unlinkTag(title = LINKED_TAG_TITLE)

        assertEditedForm()
        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_DESCRIPTION).assertExists()
        verify(exactly = 0) { viewModel.toggleFavorite() }
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 상세 대상 연락처에 연결한다`() {
        val addedTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        setContactDetailScreen(tagViewModel = tagViewModel, resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `연결할 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setContactDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    // 이름, 설명과 전화번호를 저장 내용과 다르게 고쳐 둔다.
    private fun editForm() {
        composeRule.nameInput().performTextReplacement(TYPED_NAME)
        composeRule.descriptionInput().performTextReplacement(TYPED_DESCRIPTION)
        composeRule.phoneNumberInput().performTextReplacement(TYPED_PHONE_NUMBER)
        composeRule.waitForIdle()
    }

    private fun assertEditedForm() {
        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.phoneNumberInput().assert(hasText(TYPED_PHONE_NUMBER))
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

    private fun setContactDetailScreen(
        uiState: ContactDetailUiState = content(),
        viewModel: ContactDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(uiState)),
        tagViewModel: EntityDetailTagViewModel = detailTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        navigateToTagDetail: (Uuid) -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContactDetailScreen(
            viewModel = viewModel,
            navigateToTagAdd = navigateToTagAdd,
            navigateToTagDetail = navigateToTagDetail,
            tagViewModel = tagViewModel,
            resultEventBus = resultEventBus,
        )
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val LINKED_TAG_TITLE = "ContactDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "ContactDetailSelectableTag"
        const val TYPED_NAME = "ContactDetailTagTypedName"
        const val TYPED_DESCRIPTION = "ContactDetailTagTypedDescription"
        const val STORED_PHONE_NUMBER = "010-1234-5678"
        const val TYPED_PHONE_NUMBER = "02-987-6543"
        const val DEFAULT_UNFAVORITE_DESCRIPTION = "Remove from favorites"

        fun content(isFavorite: Boolean = false): ContactDetailUiState = ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail(phoneNumberList = listOf(STORED_PHONE_NUMBER)), isFavorite = isFavorite)
    }
}
