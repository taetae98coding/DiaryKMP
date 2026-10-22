package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.kotest.matchers.shouldBe
import io.mockk.every
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
    fun `TC-7bed6726-c5fb-4bbb-a9cd-d3fbb9b763d4 조회에 성공하면 저장된 내용을 채운다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.titleInput().assert(hasText(STORED_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule
            .onNode(hasText(STORED_TITLE) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText))
            .assertExists()
    }

    @Test
    fun `TC-418fdb5f-3a74-468a-a30a-7110fb585c24 조회가 끝나기 전에는 조회 중 상태를 유지한다`() {
        setMusicDetailScreen(uiState = MutableStateFlow(MusicDetailUiState.Loading))

        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size shouldBe 0
        composeRule.nodeCount(DEFAULT_OPEN_IN_NEW_DESCRIPTION) shouldBe 0
        composeRule.nodeCount(DEFAULT_DELETE_BUTTON_DESCRIPTION) shouldBe 0
        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-9db55803-5dc0-41bb-a928-7db9c2550243 화면에 처음 진입하면 어느 입력에도 초점이 없다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.titleInput().assertIsNotFocused()
        composeRule.artistInput().assertIsNotFocused()
        composeRule.linkInput().assertIsNotFocused()
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 저장된 내용과 같으면 수정 동작을 제공하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-96f55c24-2ceb-4f1a-b665-c2917e7180aa TC-e87e45b5-3283-4a07-be65-5181370ada06 입력을 바꾸면 그 값이 표시되고 수정 동작을 제공한다`() {
        val viewModel = setMusicDetailScreen(uiState = contentUiState())

        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(CHANGED_LINK))
        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1

        composeRule.clickUpdate()

        verify(exactly = 1) { viewModel.update(detail = testMusicDetail(title = EDITING_TITLE, artist = EDITING_ARTIST, link = CHANGED_LINK)) }
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 링크를 비워도 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 가수를 바꾸면 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 링크를 바꾸면 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 저장된 링크 앞뒤에 공백만 붙여도 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().performTextReplacement(" $STORED_LINK ")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 제목을 비워도 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.titleInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-e87e45b5-3283-4a07-be65-5181370ada06 가수를 비워도 수정 동작을 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.artistInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_UPDATE_BUTTON_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-0048b9b6-69e2-47f9-8551-c7ad8a5c3e4f 다시 불러오기에 성공하면 제목과 가수를 덮어쓰고 링크와 썸네일은 그대로 둔다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val viewModel = setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())

        composeRule.clickFetch()

        verify(exactly = 1) { viewModel.fetchLink(link = STORED_LINK) }

        composeRule.runOnIdle {
            effect.value =
                MusicDetailEffect.LinkFetched(
                    link = STORED_LINK,
                    title = FETCHED_TITLE,
                    artist = FETCHED_ARTIST,
                )
        }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-35fea53e-49a6-47cc-94eb-3ebdf5fc59ed 다시 불러오는 동안 링크를 다른 영상으로 바꾸면 도착한 결과를 채우지 않는다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())

        composeRule.clickFetch()
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()
        val textListBeforeResult = composeRule.visibleTextList()
        composeRule.runOnIdle {
            effect.value = MusicDetailEffect.LinkFetched(link = STORED_LINK, title = FETCHED_TITLE, artist = FETCHED_ARTIST)
        }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(STORED_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(CHANGED_LINK))
        composeRule.visibleTextList() shouldBe textListBeforeResult
    }

    @Test
    fun `TC-35fea53e-49a6-47cc-94eb-3ebdf5fc59ed 다시 불러오는 동안 링크를 바꿨다가 되돌리면 도착한 결과를 채운다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())

        composeRule.clickFetch()
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.linkInput().performTextReplacement(STORED_LINK)
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            effect.value = MusicDetailEffect.LinkFetched(link = STORED_LINK, title = FETCHED_TITLE, artist = FETCHED_ARTIST)
        }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-b9be762c-8a86-467c-9062-b057bc73c1d4 저장된 영상 링크의 썸네일 미리보기를 표시한다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-b9be762c-8a86-467c-9062-b057bc73c1d4 저장된 링크가 비어 있으면 미리보기를 표시하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = "")))

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-b9be762c-8a86-467c-9062-b057bc73c1d4 저장된 링크가 영상을 가리키지 않으면 미리보기를 표시하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = YOUTUBE_CHANNEL_LINK)))

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-cf7c16f3-c1a3-4854-adf4-f6f159edccce 링크를 지우면 수정을 반영하지 않아도 미리보기가 사라진다`() {
        val viewModel = setMusicDetailScreen(uiState = contentUiState())

        composeRule.linkInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-cf7c16f3-c1a3-4854-adf4-f6f159edccce 링크가 비어 있던 곡에 영상 링크를 입력하면 미리보기가 나타난다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = "")))

        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
    }

    @Test
    fun `TC-30f4786d-2613-49b9-bddc-feaed84c3891 썸네일만 바꾸거나 지우는 조작을 두지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState())

        composeRule.onNodeWithContentDescription(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION).assertHasNoClickAction()
    }

    @Test
    fun `TC-8b685fc9-8590-4eef-9aea-3a4a99bae7d8 TC-e6891209-618c-45fe-b2f1-c30b5e83ee94 외부로 열기는 저장된 링크를 연다`() {
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
    fun `TC-83e89b56-1fa9-41aa-82e3-67156043187b 외부로 열어도 입력 중이던 내용을 유지한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setMusicDetailScreen(uiState = contentUiState(), uriHandler = uriHandler)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
    }

    @Test
    fun `TC-bab1ec5b-8853-435d-bda1-5f02f6adafdf 앱 밖에서 열지 못하면 화면을 유지하고 링크를 열지 못했다고 알린다`() {
        var navigateUpCount = 0
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalStateException("no browser")
            }
        setMusicDetailScreen(uiState = contentUiState(), navigateUp = { navigateUpCount += 1 }, uriHandler = uriHandler)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri(STORED_LINK) }
        navigateUpCount shouldBe 0
        composeRule.onNodeWithText("Couldn't open the link.").assertExists()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-357cb158-ffce-4531-8b75-1b02484fb538 저장된 링크가 비어 있으면 외부로 열기를 제공하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = "")))

        composeRule.nodeCount(DEFAULT_OPEN_IN_NEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-0dacd42f-2ac3-48ee-ab55-6affc71255e5 삭제를 선택하면 되묻지 않고 삭제를 요청한다`() {
        val viewModel = setMusicDetailScreen(uiState = contentUiState())

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.delete() }
        composeRule.titleInput().assert(hasText(STORED_TITLE))
    }

    @Test
    fun `TC-ca8e572f-1080-4a8e-91c0-e4ac4de48a43 삭제에 성공하면 화면을 떠난다`() {
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
    fun `TC-89f6613c-c279-49ee-8650-6cfd279aa80c 입력을 바꾼 채 뒤로가면 돌아가기 행동을 한 번 전달하고 수정하지 않는다`() {
        var navigateUpCount = 0

        val viewModel = setMusicDetailScreen(uiState = contentUiState(), navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-1b34c164-6cc9-496d-b3ec-6c173ac0afd6 저장된 제목이 바뀌면 화면 제목만 갱신하고 입력은 덮어쓰지 않는다`() {
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
    fun `TC-547d9f26-187d-467b-bc56-c6cc808ff1f4 화면이 재생성되어도 입력 중이던 내용을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val uiState = contentUiState()
        val viewModel = detailViewModel(uiState = uiState)

        restorationTester.setContent {
            DiaryTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(CHANGED_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 1
        verify(exactly = 0) { viewModel.fetchLink(link = any()) }
    }

    @Test
    fun `TC-38ef1c8b-7e7f-4b93-91c9-da835e8e2daa 수정하지 않고 떠났다가 같은 곡에 다시 진입하면 저장된 내용으로 시작한다`() {
        val viewModel = detailViewModel(uiState = contentUiState())
        var isVisible by mutableStateOf(true)

        composeRule.setContent {
            DiaryTheme {
                if (isVisible) {
                    MusicDetailScreen(
                        navigateUp = {},
                        componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.linkInput().performTextReplacement(CHANGED_LINK)
        composeRule.waitForIdle()

        composeRule.runOnIdle { isVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isVisible = true }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(STORED_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-d7bfce7e-9215-4a4b-b359-bc67b377b3ce 링크가 비어 있으면 다시 불러오기 뒤 링크 입력을 알리고 초점을 옮긴다`() {
        assertFetchLinkInvalid(link = "", effect = MusicDetailEffect.LinkBlank, message = DEFAULT_LINK_BLANK_MESSAGE)
    }

    @Test
    fun `TC-d7bfce7e-9215-4a4b-b359-bc67b377b3ce 링크가 공백뿐이면 다시 불러오기 뒤 링크 입력을 알리고 초점을 옮긴다`() {
        assertFetchLinkInvalid(link = "   ", effect = MusicDetailEffect.LinkBlank, message = DEFAULT_LINK_BLANK_MESSAGE)
    }

    @Test
    fun `TC-d7bfce7e-9215-4a4b-b359-bc67b377b3ce 링크가 YouTube 주소가 아니면 다시 불러오기 뒤 YouTube 영상 링크를 알리고 초점을 옮긴다`() {
        assertFetchLinkInvalid(link = NOT_YOUTUBE_LINK, effect = MusicDetailEffect.LinkNotYoutube, message = DEFAULT_LINK_NOT_YOUTUBE_MESSAGE)
    }

    @Test
    fun `TC-1975ff66-3587-47d3-b1e7-57d24a284022 영상 정보를 가져오지 못하면 실패를 알리고 입력과 초점을 유지한다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val viewModel = setMusicDetailScreen(uiState = contentUiState(), effect = effect.filterNotNull())
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.waitForIdle()
        composeRule.artistInput().assertIsFocused()

        composeRule.clickFetch()
        verify(exactly = 1) { viewModel.fetchLink(link = STORED_LINK) }
        composeRule.runOnIdle { effect.value = MusicDetailEffect.LinkFetchFailed }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_LINK_FETCH_FAILED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        composeRule.artistInput().assertIsFocused()
    }

    private fun assertFetchLinkInvalid(
        link: String,
        effect: MusicDetailEffect,
        message: String,
    ) {
        val effectFlow = MutableStateFlow<MusicDetailEffect?>(null)
        setMusicDetailScreen(uiState = contentUiState(), effect = effectFlow.filterNotNull())
        composeRule.linkInput().performTextReplacement(link)
        composeRule.waitForIdle()

        composeRule.clickFetch()
        composeRule.runOnIdle { effectFlow.value = effect }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertExists()
        composeRule.linkInput().assertIsFocused()
        composeRule.titleInput().assert(hasText(STORED_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
    }

    private fun ComposeContentTestRule.visibleTextList(): List<String> =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

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
        private const val NOT_YOUTUBE_LINK = "https://vimeo.com/76979871"
        private const val EDITING_ARTIST = "MusicDetailEditingArtist"
        private const val DEFAULT_LINK_BLANK_MESSAGE = "Please enter a link."
        private const val DEFAULT_LINK_NOT_YOUTUBE_MESSAGE = "Please enter a YouTube video link."
        private const val DEFAULT_LINK_FETCH_FAILED_MESSAGE = "Could not fetch music info from the link."

        private fun contentUiState(
            id: Uuid = Uuid.random(),
            detail: MusicDetail = testMusicDetail(),
        ): MutableStateFlow<MusicDetailUiState> = MutableStateFlow(MusicDetailUiState.Content(id = id, detail = detail))
    }
}
