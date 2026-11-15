package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicDetailShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c2c20dfc-cc66-409a-93bd-a162223f778e 링크가 저장된 곡에서 공유를 제공한다`() {
        val sharer = testContentSharer()
        setMusicDetailScreen(uiState = contentUiState(), sharer = sharer)

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
        composeRule.clickShare()

        coVerify(exactly = 1) { sharer.share(content = any()) }
    }

    @Test
    fun `TC-c2c20dfc-cc66-409a-93bd-a162223f778e 저장된 링크가 비어 있어도 공유를 제공한다`() {
        setMusicDetailScreen(uiState = contentUiState(detail = testMusicDetail(link = "")))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
        composeRule.nodeCount(DEFAULT_OPEN_IN_NEW_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-d522a534-9bbe-4b17-b0a4-fe827290a10b 조회 중에는 공유를 실행할 수 없다`() {
        setMusicDetailScreen(uiState = MutableStateFlow(MusicDetailUiState.Loading), sharer = testContentSharer())

        composeRule.nodeCount(DEFAULT_SHARE_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-b4753de1-0490-4984-b02d-9bf17ab9f25e 글을 공유할 수 없는 환경에서는 공유를 제공하지 않는다`() {
        setMusicDetailScreen(uiState = contentUiState(), sharer = testContentSharer(isTextSupported = false))

        composeRule.nodeCount(DEFAULT_SHARE_DESCRIPTION) shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-581e2ac7-4d9d-479a-9a3a-2fe33be22194 반영하지 않은 입력으로 곡 줄과 링크 줄을 보낸다`() {
        val sharer = testContentSharer()
        val viewModel = setMusicDetailScreen(uiState = contentUiState(), sharer = sharer)
        composeRule.editAll()

        composeRule.clickShare()

        coVerify(exactly = 1) {
            sharer.share(content = ShareContent.Text(text = "$EDITING_TITLE - $EDITING_ARTIST\n$CHANGED_LINK", title = EDITING_TITLE))
        }
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-3aa7cd2a-1a3f-41dc-8a00-b79eccc0e4b7 링크가 비어 있으면 공유하지 않고 링크 입력을 알린다`() {
        assertShareLinkInvalid(link = "", message = DEFAULT_LINK_BLANK_MESSAGE)
    }

    @Test
    fun `TC-3aa7cd2a-1a3f-41dc-8a00-b79eccc0e4b7 링크가 공백뿐이면 공유하지 않고 링크 입력을 알린다`() {
        assertShareLinkInvalid(link = "   ", message = DEFAULT_LINK_BLANK_MESSAGE)
    }

    @Test
    fun `TC-3aa7cd2a-1a3f-41dc-8a00-b79eccc0e4b7 링크가 YouTube 주소가 아니면 공유하지 않고 알린다`() {
        assertShareLinkInvalid(link = NOT_YOUTUBE_LINK, message = DEFAULT_LINK_NOT_YOUTUBE_MESSAGE)
    }

    @Test
    fun `TC-39bb4562-be45-4d0d-ae86-4983c96ff72b 복사로 대신해도 링크가 비면 복사하지 않고 알린다`() {
        assertShareLinkInvalid(link = "", message = DEFAULT_LINK_BLANK_MESSAGE, result = ShareResult.COPIED)
    }

    @Test
    fun `TC-39bb4562-be45-4d0d-ae86-4983c96ff72b 복사로 대신해도 YouTube 링크가 아니면 복사하지 않는다`() {
        assertShareLinkInvalid(link = NOT_YOUTUBE_LINK, message = DEFAULT_LINK_NOT_YOUTUBE_MESSAGE, result = ShareResult.COPIED)
    }

    @Test
    fun `TC-e5d72ef0-4d0e-4cc1-a84a-5b405b9d306c 공유 시트를 열지 못하면 화면을 유지하고 알린다`() {
        var navigateUpCount = 0
        setMusicDetailScreen(
            uiState = contentUiState(),
            sharer = testContentSharer(result = ShareResult.FAILED),
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.clickShare()

        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-335d6aa8-6c12-43ea-a56d-d53a2dfbca81 복사로 대신하면 화면을 유지하고 복사했다고 알린다`() {
        val sharer = testContentSharer(result = ShareResult.COPIED)
        var navigateUpCount = 0
        setMusicDetailScreen(uiState = contentUiState(), sharer = sharer, navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.clickShare()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-2dc0b60d-97ef-4cfc-b9ce-7934915f0ceb 공유를 마치면 안내 없이 입력과 수정 동작을 유지한다`() {
        assertShareEndedSilently(result = ShareResult.COMPLETED)
    }

    @Test
    fun `TC-2dc0b60d-97ef-4cfc-b9ce-7934915f0ceb 공유 시트를 닫으면 안내 없이 입력과 수정 동작을 유지한다`() {
        assertShareEndedSilently(result = ShareResult.DISMISSED)
    }

    @Test
    fun `TC-f04cdadc-ca31-4cbd-b587-36eda12f97b2 수정을 처리하는 동안에도 공유할 수 있다`() {
        assertShareDuringRequest { content -> content.copy(isUpdateInProgress = true) }
    }

    @Test
    fun `TC-f04cdadc-ca31-4cbd-b587-36eda12f97b2 삭제를 처리하는 동안에도 공유할 수 있다`() {
        assertShareDuringRequest { content -> content.copy(isDeleteInProgress = true) }
    }

    @Test
    fun `TC-0e767a98-b0d5-456b-9af1-a7343705b520 재생성되어도 공유를 다시 시작하지 않는다`() {
        val sharer = testContentSharer()
        coEvery { sharer.share(content = any()) } coAnswers { awaitCancellation() }
        val viewModel = detailViewModel(uiState = contentUiState())
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MusicTagTestTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = viewModel,
                    tagViewModel = detailTagScreenTestViewModel(),
                    contentSharerFactory = testContentSharerFactory(sharer = sharer),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.clickShare()
        coVerify(exactly = 1) { sharer.share(content = any()) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
    }

    private fun assertShareLinkInvalid(
        link: String,
        message: String,
        result: ShareResult = ShareResult.COMPLETED,
    ) {
        val sharer = testContentSharer(result = result)
        setMusicDetailScreen(uiState = contentUiState(), sharer = sharer)
        composeRule.linkInput().performTextReplacement(link)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.waitForIdle()

        composeRule.clickShare()

        coVerify(exactly = 0) { sharer.share(content = any()) }
        composeRule.onNodeWithText(message).assertExists()
        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertDoesNotExist()
        composeRule.linkInput().assertIsFocused()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(link))
    }

    private fun assertShareEndedSilently(result: ShareResult) {
        val sharer = testContentSharer(result = result)
        var navigateUpCount = 0
        setMusicDetailScreen(uiState = contentUiState(), sharer = sharer, navigateUp = { navigateUpCount += 1 })
        composeRule.editAll()

        composeRule.clickShare()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(CHANGED_LINK))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_LINK_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertDoesNotExist()
        navigateUpCount shouldBe 0
    }

    private fun assertShareDuringRequest(inProgress: (MusicDetailUiState.Content) -> MusicDetailUiState.Content) {
        val sharer = testContentSharer()
        val uiState = contentUiState()
        setMusicDetailScreen(uiState = uiState, sharer = sharer)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()
        composeRule.runOnIdle { uiState.value = inProgress(uiState.value as MusicDetailUiState.Content) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertIsEnabled()
        composeRule.clickShare()

        coVerify(exactly = 1) { sharer.share(content = any()) }
    }

    private fun ComposeContentTestRule.editAll() {
        titleInput().performTextReplacement(EDITING_TITLE)
        artistInput().performTextReplacement(EDITING_ARTIST)
        linkInput().performTextReplacement(CHANGED_LINK)
        waitForIdle()
    }

    private fun ComposeContentTestRule.clickShare() {
        onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        waitForIdle()
    }

    private fun setMusicDetailScreen(
        uiState: MutableStateFlow<MusicDetailUiState>,
        sharer: ContentSharer = testContentSharer(),
        navigateUp: () -> Unit = {},
    ): MusicDetailViewModel {
        val viewModel = detailViewModel(uiState = uiState)

        composeRule.setContent {
            MusicTagTestTheme {
                MusicDetailScreen(
                    navigateUp = navigateUp,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = viewModel,
                    tagViewModel = detailTagScreenTestViewModel(),
                    contentSharerFactory = testContentSharerFactory(sharer = sharer),
                )
            }
        }
        composeRule.waitForIdle()

        return viewModel
    }

    private companion object {
        private const val CHANGED_LINK = "https://youtu.be/11cta61wi0g"
        private const val NOT_YOUTUBE_LINK = "https://example.com"
        private const val EDITING_ARTIST = "MusicDetailEditingArtist"
        private const val DEFAULT_LINK_BLANK_MESSAGE = "Please enter a link."
        private const val DEFAULT_LINK_NOT_YOUTUBE_MESSAGE = "Please enter a YouTube video link."
        private const val DEFAULT_SHARE_FAILED_MESSAGE = "Couldn't share."
        private const val DEFAULT_SHARE_COPIED_MESSAGE = "Copied to clipboard."

        private fun contentUiState(detail: MusicDetail = testMusicDetail()): MutableStateFlow<MusicDetailUiState> = MutableStateFlow(MusicDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = detail))
    }
}
