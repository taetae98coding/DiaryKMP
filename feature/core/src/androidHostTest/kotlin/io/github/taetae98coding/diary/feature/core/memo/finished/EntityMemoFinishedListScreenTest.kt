package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid
import java.util.TimeZone as JavaTimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EntityMemoFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-010 완료된 메모가 하나도 없어도 화면을 사용할 수 있다`() {
        val viewModel = mockk<EntityMemoFinishedListViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
        every { viewModel.memoPagingData } returns MutableStateFlow(entityMemoPagingData(itemList = emptyList()))
        every { viewModel.effect } returns Channel<EntityMemoFinishedListEffect>(capacity = Channel.BUFFERED).receiveAsFlow()
        setEntityMemoFinishedListScreen(
            viewModel = viewModel,
            uiState = EntityMemoFinishedListUiState(title = ENTITY_NAME),
        )

        composeRule.onNodeWithText(ENTITY_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-016 다시 시작하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restart(id = environment.memo.id) }
        assertMemoIsNotDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-016 다시 시작하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-017 삭제하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.memo.id) }
        assertMemoIsNotDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-017 삭제하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-018 다시 시작 실행 취소는 메모 완료를 요청하고 메모가 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.memo.id) }
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(environment.memo.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-018 삭제 실행 취소는 메모 복구를 요청하고 메모가 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.memo.id) }
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(environment.memo.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-025 안내가 보이는 동안 다른 메모를 삭제하면 삭제 안내만 남고 실행 취소는 삭제에만 적용된다`() {
        val environment = screenTestEnvironment()
        val otherMemoId = fixtureId()
        justRun { environment.viewModel.restore(id = otherMemoId) }
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertExists()

        environment.effectChannel.trySend(EntityMemoFinishedListEffect.Deleted(id = otherMemoId)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = otherMemoId) }
        verify(exactly = 0) { environment.viewModel.finish(id = any()) }
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-026 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = screenTestEnvironment()
        setEntityMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.finish(id = any()) }
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-019 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val environment = screenTestEnvironment()
        var navigatedMemoId: Uuid? = null
        setEntityMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateToMemoDetail = { id -> navigatedMemoId = id },
        )
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performClick()

        navigatedMemoId shouldBe environment.memo.id
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-011 뒤로가기 버튼을 선택하면 화면을 떠난다`() {
        val environment = screenTestEnvironment()
        var navigateUpCount = 0
        setEntityMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-020 화면으로 돌아오면 바뀐 오늘에 맞춰 오늘 날짜 그룹 표시를 갱신한다`() {
        val originalTimeZone = JavaTimeZone.getDefault()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)

        try {
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(FIRST_TIME_ZONE))
            val firstToday = Clock.System.todayIn(TimeZone.currentSystemDefault())
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(SECOND_TIME_ZONE))
            val secondToday = Clock.System.todayIn(TimeZone.currentSystemDefault())
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(FIRST_TIME_ZONE))
            val secondTodayMemo = entityMemo(title = fixtureText(prefix = "SecondTodayMemo"), dateTime = MemoDateTime.AllDay(dateRange = secondToday..secondToday))
            val viewModel = mockk<EntityMemoFinishedListViewModel>(relaxed = true)
            every { viewModel.uiState } returns MutableStateFlow(EntityMemoFinishedListUiState())
            every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
            every { viewModel.memoPagingData } returns
                MutableStateFlow(
                    entityMemoPagingData(
                        itemList =
                            listOf(
                                MemoListItem.DateHeader(date = firstToday),
                                MemoListItem.Content(memo = entityMemo(title = fixtureText(prefix = "FirstTodayMemo"), dateTime = MemoDateTime.AllDay(dateRange = firstToday..firstToday))),
                                MemoListItem.DateHeader(date = secondToday),
                                MemoListItem.Content(memo = secondTodayMemo),
                            ),
                    ),
                )
            composeRule.setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    DiaryTheme {
                        EntityMemoFinishedListScreen(
                            navigateUp = {},
                            navigateToMemoDetail = {},
                            emptyTitle = EMPTY_TITLE,
                            memoViewModel = viewModel,
                            syncViewModel = screenTestSyncViewModel(),
                        )
                    }
                }
            }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
            waitUntilMemoIsDisplayed(title = secondTodayMemo.detail.title)
            val beforeHeaderTextList = dateHeaderTextList()
            beforeHeaderTextList.count { text -> text == DEFAULT_TODAY_HEADER } shouldBe 1

            // 화면을 벗어난 사이 날짜가 바뀐 것을 시간대 변경으로 재현한다.
            composeRule.runOnIdle {
                lifecycleOwner.currentState = Lifecycle.State.CREATED
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(SECOND_TIME_ZONE))
                lifecycleOwner.currentState = Lifecycle.State.RESUMED
            }
            composeRule.waitForIdle()

            val afterHeaderTextList = dateHeaderTextList()
            afterHeaderTextList.count { text -> text == DEFAULT_TODAY_HEADER } shouldBe 1
            val previousDateText = beforeHeaderTextList.single { text -> text != DEFAULT_TODAY_HEADER }
            afterHeaderTextList.contains(previousDateText) shouldBe false
            afterHeaderTextList.size shouldBe beforeHeaderTextList.size
        } finally {
            JavaTimeZone.setDefault(originalTimeZone)
        }
    }

    private fun dateHeaderTextList(): List<String> =
        composeRule
            .onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString { text -> text.text } }

    private fun setEntityMemoFinishedListScreen(
        viewModel: EntityMemoFinishedListViewModel,
        uiState: EntityMemoFinishedListUiState = EntityMemoFinishedListUiState(),
        navigateUp: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        every { viewModel.uiState } returns MutableStateFlow(uiState)

        composeRule.setContent {
            DiaryTheme {
                EntityMemoFinishedListScreen(
                    navigateUp = navigateUp,
                    navigateToMemoDetail = navigateToMemoDetail,
                    emptyTitle = EMPTY_TITLE,
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
    }

    private fun assertMemoIsNotDisplayed(title: String) {
        composeRule
            .onNodeWithText(
                text = title,
                useUnmergedTree = true,
            ).assertIsNotDisplayed()
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun screenTestEnvironment(): ScreenTestEnvironment {
        val memo = entityMemo(title = SCREEN_MEMO_TITLE)
        val pagingData =
            MutableStateFlow(
                entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            )
        val effectChannel = Channel<EntityMemoFinishedListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<EntityMemoFinishedListViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))

        every { viewModel.memoPagingData } returns pagingData
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.restart(id = memo.id) } answers {
            pagingData.value = entityMemoPagingData(itemList = emptyList())
            effectChannel.trySend(EntityMemoFinishedListEffect.Restarted(id = memo.id)).getOrThrow()
        }
        every { viewModel.delete(id = memo.id) } answers {
            pagingData.value = entityMemoPagingData(itemList = emptyList())
            effectChannel.trySend(EntityMemoFinishedListEffect.Deleted(id = memo.id)).getOrThrow()
        }
        every { viewModel.finish(id = memo.id) } answers {
            pagingData.value = entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)))
        }
        every { viewModel.restore(id = memo.id) } answers {
            pagingData.value = entityMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)))
        }

        return ScreenTestEnvironment(
            memo = memo,
            viewModel = viewModel,
            effectChannel = effectChannel,
        )
    }

    private companion object {
        const val EMPTY_TITLE = "EntityEmptyTitle"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val ENTITY_NAME = "EntityName"
        const val DEFAULT_SUBTITLE = "Finished Memos"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_RESTARTED_MESSAGE = "Memo restarted."
        const val KOREAN_RESTARTED_MESSAGE = "메모를 다시 시작했습니다."
        const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val SCREEN_MEMO_TITLE = "EntityMemoFinishedListScreenMemo"
        const val DEFAULT_TODAY_HEADER = "Today"
        const val FIRST_TIME_ZONE = "Pacific/Kiritimati"
        const val SECOND_TIME_ZONE = "Etc/GMT+12"
    }
}

private data class ScreenTestEnvironment(
    val memo: Memo,
    val viewModel: EntityMemoFinishedListViewModel,
    val effectChannel: Channel<EntityMemoFinishedListEffect>,
)
