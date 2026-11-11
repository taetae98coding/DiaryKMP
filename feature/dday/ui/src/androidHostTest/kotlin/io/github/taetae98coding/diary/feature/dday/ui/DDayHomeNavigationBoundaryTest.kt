package io.github.taetae98coding.diary.feature.dday.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeFilterNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 디데이 목록을 떠났다 돌아오는 경로는 다른 화면이 목록을 가리는 한 영역 환경에서 제품 dDayEntry로 NavDisplay를 구성해 지난다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeNavigationBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = DDayListDetailTestEnvironment()
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setListDetailToday(BEFORE_MIDNIGHT)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseListDetailToday()
    }

    @Test
    fun `TC-6be44675-929b-4495-928f-b5f75f6782d1 디데이 상세에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed { target ->
            composeRule.onNode(listDetailCard(target.detail.title)).performClick()
            composeRule.waitForIdle()
            backStack.last() shouldBe DDayDetailNavKey(id = target.id)
            crossMidnight()
            pressBack()
        }
    }

    @Test
    fun `TC-6be44675-929b-4495-928f-b5f75f6782d1 디데이 추가에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            backStack.last() shouldBe DDayAddNavKey()
            crossMidnight()
            pressBack()
        }
    }

    @Test
    fun `TC-6be44675-929b-4495-928f-b5f75f6782d1 필터의 태그 추가로 TagAdd에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            openFilter()
            composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(DDAY_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()
            crossMidnight()
            pressBack()
        }
    }

    @Test
    fun `TC-6be44675-929b-4495-928f-b5f75f6782d1 앱이 백그라운드에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
            composeRule.waitForIdle()
            crossMidnight()
            composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-6be44675-929b-4495-928f-b5f75f6782d1 더보기로 돌아간 뒤 다시 진입하면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
            crossMidnight()
            composeRule.runOnIdle { backStack.add(DDayHomeNavKey) }
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-428c37a8-e16b-478f-bb5d-4dcf3f645280 디데이 추가에 다녀와도 보던 목록 위치를 유지한다`() {
        val ddayList = positionDDayList()
        setNavDisplay(ddayList = ddayList)
        scrollToPosition(ddayList)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasScrollToIndexAction()).assertDoesNotExist()
        pressBack()

        assertPositionDisplayed(ddayList)
    }

    @Test
    fun `TC-428c37a8-e16b-478f-bb5d-4dcf3f645280 디데이 상세에 다녀와도 보던 목록 위치를 유지한다`() {
        val ddayList = positionDDayList()
        setNavDisplay(ddayList = ddayList)
        scrollToPosition(ddayList)

        composeRule.onNode(listDetailCard(ddayList[POSITION_SCROLL_INDEX].detail.title)).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasScrollToIndexAction()).assertDoesNotExist()
        pressBack()

        assertPositionDisplayed(ddayList)
    }

    @Test
    fun `TC-428c37a8-e16b-478f-bb5d-4dcf3f645280 앱이 백그라운드에 다녀와도 보던 목록 위치를 유지한다`() {
        val ddayList = positionDDayList()
        setNavDisplay(ddayList = ddayList)
        scrollToPosition(ddayList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertPositionDisplayed(ddayList)
    }

    @Test
    fun `TC-f367d77a-9592-4aa9-a39e-6668cf40ddaf 더보기로 나갔다 다시 진입하면 필터 선택을 유지한 채 맨 위부터 보여 준다`() {
        val (tag, ddayList, untagged) = filteredPositionFixture()
        setNavDisplay(ddayList = ddayList + untagged)
        scrollToPosition(ddayList)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(DDayHomeNavKey) }
        composeRule.waitForIdle()

        assertTopWithFilter(tag = tag, ddayList = ddayList, untagged = untagged)
    }

    @Test
    fun `TC-f367d77a-9592-4aa9-a39e-6668cf40ddaf 앱을 다시 실행해 진입하면 필터 선택을 유지한 채 맨 위부터 보여 준다`() {
        val (tag, ddayList, untagged) = filteredPositionFixture()
        environment.setDDayList(ddayList + untagged)
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            // 앱을 다시 실행하면 저장해 둔 화면 상태와 ViewModel이 없으므로 새 키로 전환 이력과 ViewModel을 다시 만든다.
            key(launchCount) {
                val relaunchedBackStack = remember { NavBackStack<ScreenNavKey>(DDayListDetailMoreNavKey, DDayHomeNavKey) }
                environment.Content(navBackStack = relaunchedBackStack, viewModelStoreOwner = remember { listDetailViewModelStoreOwner() })
            }
        }
        composeRule.waitForIdle()
        scrollToPosition(ddayList)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        assertTopWithFilter(tag = tag, ddayList = ddayList, untagged = untagged, checkBackStack = false)
    }

    @Test
    fun `TC-eb4427e6-1351-41f7-8bc0-70df67d3ff12 안내가 보이는 동안 화면을 떠났다 돌아오면 안내가 닫히고 되돌릴 수 없다`() {
        val deleted = listDetailDDay(title = FIRST_TITLE, baseDate = AFTER_MIDNIGHT)
        val other = listDetailDDay(title = SECOND_TITLE, baseDate = AFTER_MIDNIGHT)
        setNavDisplay(ddayList = listOf(deleted, other))
        composeRule.onNode(listDetailCard(deleted.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe DDayAddNavKey()
        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        environment.homeViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.restore(id = any()) } }
    }

    @Test
    fun `TC-c1e8b9d4-5123-4000-965c-4405cff2f850 태그 추가를 실행하면 필터가 닫히고 TagAdd로 한 번 이동하며 선택은 그대로다`() {
        val tag = Arb.tag(title = TAG_TITLE).next()
        val dday = listDetailDDay(title = FIRST_TITLE, baseDate = AFTER_MIDNIGHT)
        environment.tagList = listOf(tag)
        environment.ddayTagMap = mapOf(dday.id to setOf(tag.id))
        environment.selectTag(tag.id)
        setNavDisplay(ddayList = listOf(dday))
        openFilter()
        composeRule.onNodeWithText(TAG_TITLE).assertIsSelected()

        composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, TagAddNavKey())
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()
        environment.filterViewModelList.forEach { viewModel ->
            verify(exactly = 0) { viewModel.selectTag(id = any()) }
            verify(exactly = 0) { viewModel.unselectTag(id = any()) }
            verify(exactly = 0) { viewModel.unselectAllTag() }
        }
    }

    @Test
    fun `TC-2a2bbcb7-49a4-4bb3-9c07-8314b47f7f5f TagAdd에서 태그를 추가하고 돌아와도 필터가 열리지 않고 추가한 태그는 선택되지 않는다`() {
        val selected = Arb.tag(title = TAG_TITLE).next()
        val added = Arb.tag(title = ADDED_TAG_TITLE).next()
        val dday = listDetailDDay(title = FIRST_TITLE, baseDate = AFTER_MIDNIGHT)
        environment.tagList = listOf(selected)
        environment.ddayTagMap = mapOf(dday.id to setOf(selected.id))
        environment.selectTag(selected.id)
        setNavDisplay(ddayList = listOf(dday))
        openFilter()
        composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()

        // TagAdd 화면에서 태그가 저장되면 선택할 수 있는 태그에 더해진다.
        environment.tagList = listOf(selected, added)
        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
        openFilter()
        composeRule.onNodeWithText(ADDED_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(TAG_TITLE).assertIsSelected()
    }

    // 자정 전에 D-1이던 디데이가 목록을 벗어난 사이 자정이 지난 뒤 돌아오면 D-Day로 바뀌고 순서는 그대로다.
    private fun assertTodayRefreshed(leaveAndReturn: (DDay) -> Unit) {
        val target = listDetailDDay(title = FIRST_TITLE, baseDate = AFTER_MIDNIGHT)
        val other = listDetailDDay(title = SECOND_TITLE, baseDate = AFTER_MIDNIGHT)
        setNavDisplay(ddayList = listOf(target, other))
        composeRule.onNode(listDetailCard(title = target.detail.title, countText = "D-1")).assertExists()

        leaveAndReturn(target)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(title = target.detail.title, countText = TODAY_COUNT_TEXT)).fetchSemanticsNodes().isNotEmpty()
        }
        cardBounds(target).isBeforeInGrid(cardBounds(other)) shouldBe true
    }

    private fun crossMidnight() {
        setListDetailToday(AFTER_MIDNIGHT)
    }

    private fun openFilter(checkBackStack: Boolean = true) {
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_TAG_FILTER_ADD_LABEL).fetchSemanticsNodes().isNotEmpty()
        }
        if (checkBackStack) backStack.last() shouldBe DDayHomeFilterNavKey
    }

    private fun filteredPositionFixture(): Triple<Tag, List<DDay>, DDay> {
        val tag = Arb.tag(title = TAG_TITLE).next()
        val ddayList = positionDDayList()
        val untagged = listDetailDDay(title = "00-untagged", baseDate = AFTER_MIDNIGHT)
        environment.tagList = listOf(tag)
        environment.ddayTagMap = ddayList.associate { dday -> dday.id to setOf(tag.id) }
        environment.selectTag(tag.id)
        return Triple(tag, ddayList, untagged)
    }

    private fun assertTopWithFilter(
        tag: Tag,
        ddayList: List<DDay>,
        untagged: DDay,
        checkBackStack: Boolean = true,
    ) {
        waitUntilCard(ddayList.first())
        composeRule.onNode(listDetailCard(ddayList.first().detail.title)).assertIsDisplayed()
        composeRule.onNode(listDetailCard(ddayList[POSITION_SCROLL_INDEX].detail.title)).assertDoesNotExist()
        composeRule.onNode(listDetailCard(untagged.detail.title)).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(DEFAULT_FILTER_DESCRIPTION) and hasStateDescription(DEFAULT_FILTER_APPLIED)).assertExists()
        openFilter(checkBackStack = checkBackStack)
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    private fun setNavDisplay(ddayList: List<DDay>) {
        environment.setDDayList(ddayList)
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        ddayList.firstOrNull()?.let(::waitUntilCard)
    }

    private fun scrollToPosition(ddayList: List<DDay>) {
        waitUntilCard(ddayList.first())
        list().performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionDisplayed(ddayList)
    }

    private fun assertPositionDisplayed(ddayList: List<DDay>) {
        waitUntilCard(ddayList[POSITION_SCROLL_INDEX])
        composeRule.onNode(listDetailCard(ddayList[POSITION_SCROLL_INDEX].detail.title)).assertIsDisplayed()
        composeRule.onNode(listDetailCard(ddayList.first().detail.title)).assertDoesNotExist()
    }

    private fun list(): SemanticsNodeInteraction = composeRule.onNode(hasScrollToIndexAction())

    private fun cardBounds(dday: DDay): Rect =
        composeRule
            .onNode(listDetailCard(dday.detail.title))
            .fetchSemanticsNode()
            .boundsInRoot

    private fun waitUntilCard(dday: DDay) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(dday.detail.title)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private companion object {
        val BEFORE_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 1, day = 1)
        val AFTER_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 1, day = 2)
        const val TODAY_COUNT_TEXT = "D-Day"
        const val LIST_TITLE = "boundary-dday"
        const val FIRST_TITLE = "A-$LIST_TITLE"
        const val SECOND_TITLE = "B-$LIST_TITLE"
        const val TAG_TITLE = "tag-a"
        const val ADDED_TAG_TITLE = "tag-added"
        const val DEFAULT_ADD_DESCRIPTION = "Add D-Day"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter by tag"
        const val DEFAULT_FILTER_APPLIED = "Tag filter applied"
        const val DEFAULT_TAG_FILTER_ADD_LABEL = "Add tag"
        const val DEFAULT_DELETED_MESSAGE = "D-Day deleted."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val POSITION_COUNT = 30
        const val POSITION_SCROLL_INDEX = 25
        const val TIMEOUT_MILLIS = 5_000L

        fun positionDDayList(): List<DDay> =
            List(POSITION_COUNT) { index ->
                listDetailDDay(title = "${index.toString().padStart(length = 2, padChar = '0')}-$LIST_TITLE", baseDate = AFTER_MIDNIGHT)
            }

        fun hasStateDescription(value: String): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
    }
}
