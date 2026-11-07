package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.music.MUSIC_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MUSIC_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_MUSIC_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.musicEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.musicPagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.musicViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.syncViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMusic
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
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
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailMusicScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-19730007-ea34-4bb3-a335-10ac4997e3cc 대상 태그의 상태와 무관하게 곡 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)

        listOf(
            tagDetailUiState(detail = tagDetail(TAG_TITLE)),
            tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
            TagDetailUiState.Loading,
        ).forEach { uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-e8b290e8-acc4-4ed1-82dd-722b9df7d57d 곡 추가를 선택하면 추가 화면 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-e8b290e8-acc4-4ed1-82dd-722b9df7d57d 한국어 환경 곡 추가 버튼의 접근성 이름은 곡 추가다`() {
        setScreenOnTab(tabDescription = KOREAN_MUSIC_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-d60d68a2-f4c6-4ef3-9464-523355f7f790 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-6d3cc2ac-de7e-48ee-ad7a-db3df72c3a62 빈 상태에서도 곡 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-707db9cd-6b90-49b8-be40-fa89dc442ac2 곡 추가는 곡 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-f1a10634-705e-479e-a6f0-6704bd2edf1f TC-59e8a837-bc1b-4dea-ae0d-473cb179be47 대상 태그를 조회하지 못해도 곡 목록과 동작을 제공한다`() {
        setScreenOnTab(
            uiState = TagDetailUiState.Loading,
            pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = ITEM_TITLE))),
        )

        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-13d845f3-f943-4de0-8c00-700b51190013 곡을 선택하면 그 곡의 상세 이동을 요청한다`() {
        val item = tagMusic(title = ITEM_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            navigateToDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(ITEM_TITLE).performClick()

        navigatedIdList shouldBe listOf(item.id)
    }

    @Test
    fun `TC-9ab5829e-0226-4ad9-9a1c-7c169518d342 곡 탭에서 목록을 당기면 계정 데이터 동기화를 한 번 요청한다`() {
        setScreenOnTab(pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = ITEM_TITLE))))
        waitForItem()

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
    }

    @Test
    fun `TC-9ab5829e-0226-4ad9-9a1c-7c169518d342 곡 탭이 빈 상태여도 목록을 당기면 계정 데이터 동기화를 한 번 요청한다`() {
        setScreenOnTab()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
    }

    @Test
    fun `TC-13f5cb56-189d-4536-9d2b-339c2028be0d TC-5c5b0403-8a85-454d-b486-04ff8a8c2c23 곡 카드를 삭제 방향으로 밀면 탭은 바뀌지 않고 그 곡 삭제를 요청하며 기본 안내와 실행 취소를 표시한다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_MUSIC_TAB_DESCRIPTION).assertIsSelected()
        emitEffect(MusicListEffect.Deleted(id = item.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-13f5cb56-189d-4536-9d2b-339c2028be0d 곡 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val item = swipeItem(tabDescription = KOREAN_MUSIC_TAB_DESCRIPTION)

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-756a123e-232e-45c9-9cde-d021fc248b8b TC-0ad9ffe8-1686-4386-9d4d-397acbd57d8c 곡 카드 위에서 오른쪽으로 밀면 탭은 바뀌지 않고 아무 상태 변경도 요청하지 않는다`() {
        swipeItem()

        composeRule.onNode(hasTestTag(MUSIC_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel().delete(id = any()) }
        verify(exactly = 0) { viewModel().restore(id = any()) }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MUSIC_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-a77376f1-8465-4033-8404-0312b6b8548a 삭제를 실행 취소하면 그 곡의 삭제 되돌리기를 요청한다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = item.id) }
    }

    @Test
    fun `TC-4ae63670-ee9b-4aee-ae1e-9f15bfc38883 안내가 보이는 동안 다른 곡을 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val item = swipeItem()
        val otherId = Arb.kotlinUuid().next()

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitEffect(MusicListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = otherId) }
        verify(exactly = 0) { viewModel().restore(id = item.id) }
    }

    @Test
    fun `TC-4c92fed5-0fdd-4a5d-a008-afa7b94b3c2b 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `TC-48395170-706b-4231-a906-b77e7b72db1b 곡 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val item = tagMusic(title = ITEM_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(pagingData = tagEntityPagingData(itemList = listOf(item)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        waitForItem()

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { viewModel().delete(id = item.id) }
        verify(exactly = 1) { viewModel().restore(id = item.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-41487067-4800-4c3f-ac73-5958a00720fe 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(MusicListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `곡 탭에서 Cmd A 단축키를 입력하면 곡 추가 이동을 한 번 요청하고 다른 탭에서는 요청하지 않는다`() {
        var navigateCount = 0
        setScreen(navigateToAdd = { navigateCount += 1 })
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onRoot().performAddShortcut()
        composeRule.waitForIdle()
        navigateCount shouldBe 0

        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        composeRule.onRoot().performAddShortcut()

        navigateCount shouldBe 1
    }

    private fun swipeItem(tabDescription: String = DEFAULT_MUSIC_TAB_DESCRIPTION): Music {
        val item = tagMusic(title = ITEM_TITLE)
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            tabDescription = tabDescription,
        )
        waitForItem()

        return item
    }

    private fun swipeCardLeft() {
        composeRule.onNode(hasTestTag(MUSIC_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun waitForItem() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitEffect(effect: MusicListEffect) {
        musicPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        musicEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun viewModel(): TagDetailMusicViewModel = requireNotNull(musicViewModelRef)

    private fun setScreenOnTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<Music> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        tabDescription: String = DEFAULT_MUSIC_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            pagingData = pagingData,
            navigateToAdd = navigateToAdd,
            navigateToDetail = navigateToDetail,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<Music> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            musicPagingData = pagingData,
            navigateToMusicAdd = navigateToAdd,
            navigateToMusicDetail = navigateToDetail,
        )
    }

    private companion object {
        const val ITEM_TITLE = "TagDetailScreenMusic"
        const val DEFAULT_EMPTY_TITLE = "No songs linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "곡 추가"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_DELETED_MESSAGE = "Song deleted."
        const val KOREAN_DELETED_MESSAGE = "곡이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
    }
}

private fun SemanticsNodeInteraction.performAddShortcut() {
    performKeyInput {
        keyDown(Key.MetaLeft)
        keyDown(Key.A)
        keyUp(Key.A)
        keyUp(Key.MetaLeft)
    }
}
