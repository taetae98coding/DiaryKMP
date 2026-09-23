package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-001 조회에 성공하면 저장된 내용을 채운다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.titleInput().assert(hasText(STORED_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
        composeRule
            .onNode(hasText(STORED_TITLE) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText))
            .assertExists()
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-002 조회가 끝나기 전에는 조회 중 상태를 유지한다`() {
        setMusicDetailScreen(uiState = MutableStateFlow(MusicDetailUiState.Loading))

        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size shouldBe 0
        composeRule.nodeCount(DEFAULT_OPEN_IN_NEW_DESCRIPTION) shouldBe 0
        composeRule.nodeCount(DEFAULT_DELETE_BUTTON_DESCRIPTION) shouldBe 0
        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-004 화면에 처음 진입하면 어느 입력에도 초점이 없다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().assertIsNotFocused()
        composeRule.titleInput().assertIsNotFocused()
        composeRule.artistInput().assertIsNotFocused()
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-006 저장된 내용과 같으면 수정 동작을 제공하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-005 TC-MUSIC-DETAIL-FEATURE-006 입력을 바꾸면 그 값이 표시되고 수정 동작을 제공한다`() {
        val viewModel = setMusicDetailScreen(uiState = contentUiState())

        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1

        composeRule.clickUpdate()

        verify(exactly = 1) { viewModel.update(detail = testMusicDetail(title = EDITING_TITLE)) }
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-006 링크를 비워도 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-006 다시 불러오기로 썸네일이 바뀌면 수정 동작을 제공한다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())

        composeRule.runOnIdle {
            effect.value =
                MusicDetailEffect.LinkFetched(
                    title = STORED_TITLE,
                    artist = STORED_ARTIST,
                    thumbnail = FETCHED_THUMBNAIL,
                )
        }
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-012 다시 불러오기에 성공하면 제목과 가수를 덮어쓴다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val viewModel = setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())

        composeRule.clickFetch()

        verify(exactly = 1) { viewModel.fetchLink(link = STORED_LINK) }

        composeRule.runOnIdle {
            effect.value =
                MusicDetailEffect.LinkFetched(
                    title = FETCHED_TITLE,
                    artist = FETCHED_ARTIST,
                    thumbnail = FETCHED_THUMBNAIL,
                )
        }
        composeRule.waitForIdle()

        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-014 저장된 썸네일이 비어 있으면 미리보기를 표시하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(thumbnail = "")))

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-015 썸네일만 바꾸거나 지우는 조작을 두지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.onNodeWithContentDescription(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION).assertHasNoClickAction()
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-016 TC-MUSIC-DETAIL-DOMAIN-009 외부로 열기는 저장된 링크를 연다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setMusicDetailScreen(uiState = contentUiState(), uriHandler = uriHandler)
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri(STORED_LINK) }
        verify(exactly = 0) { uriHandler.openUri(CHANGED_LINK) }
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-017 외부로 열어도 입력 중이던 내용을 유지한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setMusicDetailScreen(uiState = contentUiState(), uriHandler = uriHandler)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-018 저장된 링크가 비어 있으면 외부로 열기를 제공하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = "")))

        composeRule.nodeCount(DEFAULT_OPEN_IN_NEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-019 삭제를 선택하면 되묻지 않고 삭제를 요청한다`() {
        val viewModel = setMusicDetailScreen(uiState = contentUiState())

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.delete() }
        composeRule.linkInput().assert(hasText(STORED_LINK))
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-021 삭제에 성공하면 화면을 떠난다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        var navigateUpCount = 0

        setMusicDetailScreen(
            uiState = contentUiState(),
            effect = effect.filterNotNull(),
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.runOnIdle { effect.value = MusicDetailEffect.DeleteSucceeded }
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-027 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        var navigateUpCount = 0

        setMusicDetailScreen(uiState = contentUiState(), navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-023 저장된 제목이 바뀌면 화면 제목만 갱신하고 입력은 덮어쓰지 않는다`() {
        val id = Uuid.random()
        val uiState = contentUiState(id = id)

        setMusicDetailScreen(uiState = uiState)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value = MusicDetailUiState.Content(id = id, detail = testMusicDetail(title = CHANGED_TITLE))
        }
        composeRule.waitForIdle()

        composeRule
            .onNode(hasText(CHANGED_TITLE) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText))
            .assertExists()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-025 화면이 재생성되어도 입력 중이던 내용을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val uiState = contentUiState()

        restorationTester.setContent {
            DiaryTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    viewModel = detailViewModel(uiState = uiState),
                )
            }
        }
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
    }

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-026 다른 곡으로 바뀌면 저장된 내용으로 다시 시작한다`() {
        val uiState = contentUiState()

        setMusicDetailScreen(uiState = uiState)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            uiState.value = MusicDetailUiState.Content(id = Uuid.random(), detail = testMusicDetail(title = CHANGED_TITLE))
        }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(CHANGED_TITLE))
    }

    private fun setMusicDetailScreen(
        uiState: MutableStateFlow<MusicDetailUiState>,
        effect: Flow<MusicDetailEffect> = emptyFlow(),
        navigateUp: () -> Unit = {},
        uriHandler: UriHandler = mockk(relaxed = true),
    ): MusicDetailViewModel {
        val viewModel = detailViewModel(uiState = uiState, effect = effect)

        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    MusicDetailScreen(
                        navigateUp = navigateUp,
                        componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()

        return viewModel
    }

    private companion object {
        private const val CHANGED_LINK = "https://www.youtube.com/watch?v=ArmDp-zijuc"

        private fun contentUiState(
            id: Uuid = Uuid.random(),
            detail: MusicDetail = testMusicDetail(),
        ): MutableStateFlow<MusicDetailUiState> = MutableStateFlow(MusicDetailUiState.Content(id = id, detail = detail))
    }
}
