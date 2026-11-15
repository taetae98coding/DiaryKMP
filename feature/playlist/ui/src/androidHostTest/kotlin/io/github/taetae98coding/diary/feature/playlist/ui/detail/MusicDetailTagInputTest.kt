package io.github.taetae98coding.diary.feature.playlist.ui.detail

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
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.DEFAULT_ENTITY_TAG_LABEL
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.playlist.ui.sendTagAddedResult
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
class MusicDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-605a20b3-192d-467c-9b0a-c1c82657f1f5 조회 중에는 태그 입력을 표시하지 않는다`() {
        setMusicDetailScreen(uiState = MusicDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-605a20b3-192d-467c-9b0a-c1c82657f1f5 조회가 끝나면 태그 입력을 표시한다`() {
        setMusicDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-f2665b48-4c2a-4497-9581-eeab34b5efe5 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag))
        setMusicDetailScreen(tagViewModel = tagViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.add(tagId = selectableTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-f2665b48-4c2a-4497-9581-eeab34b5efe5 태그를 해제해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag))
        setMusicDetailScreen(tagViewModel = tagViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        unlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { tagViewModel.remove(tagId = linkedTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ad46b11e-52be-47d3-9237-1c9199233c02 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val navigatedIdList = mutableListOf<Uuid>()
        setMusicDetailScreen(
            tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag)),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-b89f9dcc-15dd-48f8-bcbf-5617a10ecbbd 태그를 연결해도 입력 중인 내용과 썸네일은 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val musicViewModel = detailViewModel(uiState = MutableStateFlow(content()))
        setMusicDetailScreen(
            musicViewModel = musicViewModel,
            tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag)),
        )
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
        verify(exactly = 0) { musicViewModel.update(detail = any()) }
        verify(exactly = 0) { musicViewModel.fetchLink(link = any()) }
    }

    @Test
    fun `TC-b89f9dcc-15dd-48f8-bcbf-5617a10ecbbd 태그를 해제해도 입력 중인 내용과 썸네일은 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val musicViewModel = detailViewModel(uiState = MutableStateFlow(content()))
        setMusicDetailScreen(
            musicViewModel = musicViewModel,
            tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag)),
        )
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        unlinkTag(title = LINKED_TAG_TITLE)

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
        verify(exactly = 0) { musicViewModel.update(detail = any()) }
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 상세 대상 곡에 연결한다`() {
        val addedTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val tagViewModel = detailTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        setMusicDetailScreen(tagViewModel = tagViewModel, resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `연결할 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setMusicDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

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

    private fun setMusicDetailScreen(
        uiState: MusicDetailUiState = content(),
        musicViewModel: MusicDetailViewModel = detailViewModel(uiState = MutableStateFlow(uiState)),
        tagViewModel: EntityDetailTagViewModel = detailTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        navigateToTagDetail: (Uuid) -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            MusicTagTestTheme(resultEventBus = resultEventBus) {
                MusicDetailScreen(
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = musicViewModel,
                    tagViewModel = tagViewModel,
                    contentSharerFactory = testContentSharerFactory(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val LINKED_TAG_TITLE = "MusicDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "MusicDetailSelectableTag"

        fun content(): MusicDetailUiState = MusicDetailUiState.Content(id = Uuid.random(), detail = testMusicDetail())
    }
}
