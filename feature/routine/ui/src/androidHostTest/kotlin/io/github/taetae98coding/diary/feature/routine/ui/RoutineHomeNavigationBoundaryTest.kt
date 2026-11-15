package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.filterCaseRoutineCard
import io.github.taetae98coding.diary.feature.routine.ui.home.hasFilterCaseStateDescription
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.kotest.matchers.collections.shouldBeEmpty
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

// 루틴 목록을 떠났다 돌아오거나 필터를 여닫는 경로는 제품 routineEntry로 NavDisplay를 구성해 지난다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeNavigationBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    // 앱을 다시 실행하는 테스트는 다시 만든 전환 이력을 쓴다.
    private var currentBackStack: NavBackStack<ScreenNavKey> = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-ee15484d-6677-4b8f-9366-5005f128ad47 목록 보기에서 필터를 열면 태그와 선택 여부가 표시된다`() {
        val (selected, unselected) = setSelectedAndUnselectedTag()

        openFilter()

        composeRule.onNodeWithText(selected.detail.title).assertIsSelected()
        composeRule.onNodeWithText(unselected.detail.title).assertIsNotSelected()
    }

    @Test
    fun `TC-ee15484d-6677-4b8f-9366-5005f128ad47 달력 보기에서 필터를 열면 태그와 선택 여부가 표시된다`() {
        val (selected, unselected) = setSelectedAndUnselectedTag()
        showCalendar()

        openFilter()

        composeRule.onNodeWithText(selected.detail.title).assertIsSelected()
        composeRule.onNodeWithText(unselected.detail.title).assertIsNotSelected()
    }

    @Test
    fun `TC-4820510e-4b81-4843-8a5c-5d16899442e3 목록 보기에서 태그를 선택하면 연결된 루틴만 즉시 표시되고 필터는 열린 채로 남는다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val tagB = Arb.tag(title = TAG_B_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val routineY = routine(title = ROUTINE_Y_TITLE)
        environment.tagList = listOf(tagA, tagB)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id), routineY.id to setOf(tagB.id))
        setNavDisplay(routineList = listOf(routineX, routineY))
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertExists()
        openFilter()

        composeRule.onNodeWithText(TAG_A_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()
        composeRule.onNodeWithText(TAG_B_TITLE).assertIsNotSelected()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).assertExists()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertDoesNotExist()
        backStack.last() shouldBe RoutineHomeFilterNavKey
    }

    @Test
    fun `TC-b2819662-a1d0-425f-a8cb-bfa8e7fba927 달력 보기에서 태그를 선택하면 보던 달을 둔 채 연결된 루틴의 기록만 표시된다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val tagB = Arb.tag(title = TAG_B_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val routineY = routine(title = ROUTINE_Y_TITLE)
        environment.tagList = listOf(tagA, tagB)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id), routineY.id to setOf(tagB.id))
        setNavDisplay(
            routineList = listOf(routineX, routineY),
            recordList =
                listOf(
                    routineListDetailRecord(routine = routineX, date = SEPTEMBER_THIRD, amount = 300),
                    routineListDetailRecord(routine = routineX, date = SEPTEMBER_TENTH, amount = 500),
                    routineListDetailRecord(routine = routineY, date = SEPTEMBER_THIRD, amount = 700),
                    routineListDetailRecord(routine = routineY, date = SEPTEMBER_TENTH, amount = 900),
                ),
        )
        showCalendar()
        moveToNextMonth()
        moveToNextMonth()
        // 9월 10일이 든 주는 9월의 달력에만 놓인다.
        waitUntilExists(calendarRecord(title = ROUTINE_Y_TITLE, amountText = AMOUNT_900_ML_TEXT))
        composeRule.onNode(calendarRecord(title = ROUTINE_Y_TITLE, amountText = AMOUNT_900_ML_TEXT)).assertIsDisplayed()

        openFilter()
        composeRule.onNodeWithText(TAG_A_TITLE).performClick()
        composeRule.waitForIdle()
        closeFilter()

        composeRule.onNode(calendarRecord(title = ROUTINE_X_TITLE, amountText = "500ml")).assertIsDisplayed()
        composeRule.onAllNodes(calendarRecord(title = ROUTINE_X_TITLE, amountText = "300ml")).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodes(calendarRecord(title = ROUTINE_Y_TITLE, amountText = "700ml")).fetchSemanticsNodes().shouldBeEmpty()
        composeRule.onAllNodes(calendarRecord(title = ROUTINE_Y_TITLE, amountText = AMOUNT_900_ML_TEXT)).fetchSemanticsNodes().shouldBeEmpty()
    }

    @Test
    fun `TC-f62e5739-96d3-4ba4-a49c-677d4be78f62 마지막 태그의 선택을 해제하면 선택 표시가 사라지고 태그와 관계없이 루틴을 표시한다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val routineY = routine(title = ROUTINE_Y_TITLE)
        environment.tagList = listOf(tagA)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id))
        setNavDisplay(routineList = listOf(routineX, routineY))
        selectTag(tagA)
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertDoesNotExist()

        composeRule.onNodeWithText(TAG_A_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_A_TITLE).assertIsNotSelected()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).assertExists()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertExists()
    }

    @Test
    fun `TC-00f9ccc5-62c8-4f55-8a1f-5ea23dcd2137 필터를 닫았다가 다시 열어도 선택을 표시하고 목록은 같은 범위로 좁혀져 있다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val tagB = Arb.tag(title = TAG_B_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val routineY = routine(title = ROUTINE_Y_TITLE)
        val untagged = routine(title = UNTAGGED_TITLE)
        environment.tagList = listOf(tagA, tagB)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id), routineY.id to setOf(tagB.id))
        setNavDisplay(routineList = listOf(routineX, routineY, untagged))
        selectTag(tagA)
        composeRule.onNodeWithText(TAG_B_TITLE).performClick()
        composeRule.waitForIdle()

        closeFilter()
        composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        openFilter()

        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()
        composeRule.onNodeWithText(TAG_B_TITLE).assertIsSelected()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).assertExists()
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertExists()
        composeRule.onNode(filterCaseRoutineCard(UNTAGGED_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-20f11ddc-de59-47c7-b8ce-7b8c5bd7e4c5 목록 보기에서 고른 필터 선택을 달력 보기가 함께 쓴다`() {
        val tagA = setSharedFilterFixture()

        selectTag(tagA)
        closeFilter()
        showCalendar()

        assertFilterApplied()
        waitUntilExists(calendarRecord(title = ROUTINE_X_TITLE, amountText = "300ml"))
        composeRule.onAllNodes(calendarRecord(title = ROUTINE_Y_TITLE, amountText = "700ml")).fetchSemanticsNodes().shouldBeEmpty()
        openFilter()
        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()
    }

    @Test
    fun `TC-20f11ddc-de59-47c7-b8ce-7b8c5bd7e4c5 달력 보기에서 고른 필터 선택을 목록 보기가 함께 쓴다`() {
        val tagA = setSharedFilterFixture()
        showCalendar()

        selectTag(tagA)
        closeFilter()
        showList()

        assertFilterApplied()
        waitUntilExists(filterCaseRoutineCard(ROUTINE_X_TITLE))
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_Y_TITLE)).assertDoesNotExist()
        openFilter()
        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()
    }

    @Test
    fun `TC-43a00cd3-b527-412b-ad64-2f863fcd1e49 태그 추가를 실행하면 필터가 닫히고 TagAdd로 한 번 이동하며 선택은 그대로다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val untagged = routine(title = UNTAGGED_TITLE)
        environment.tagList = listOf(tagA)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id))
        setNavDisplay(routineList = listOf(routineX, untagged))
        selectTag(tagA)
        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()

        composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, TagAddNavKey())
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()
        pressBack()
        assertFilterApplied()
        composeRule.onNode(filterCaseRoutineCard(UNTAGGED_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-3578dfe1-15bc-4b19-9d97-d0e03d284f4a TagAdd에서 태그를 추가하고 돌아와도 필터가 열리지 않고 추가한 태그는 선택되지 않는다`() {
        val selected = Arb.tag(title = TAG_A_TITLE).next()
        val added = Arb.tag(title = ADDED_TAG_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        environment.tagList = listOf(selected)
        environment.routineTagMap = mapOf(routineX.id to setOf(selected.id))
        setNavDisplay(routineList = listOf(routineX))
        selectTag(selected)
        composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()

        // TagAdd 화면에서 태그가 저장되면 선택할 수 있는 태그에 더해진다.
        environment.tagList = listOf(selected, added)
        pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        openFilter()
        composeRule.onNodeWithText(ADDED_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(TAG_A_TITLE).assertIsSelected()
    }

    @Test
    fun `TC-18ff80b1-d6b4-469e-b9cb-eb5db3dffcfc 아직 루틴이 없는 빈 상태에서도 필터 열기와 보기 전환을 실행할 수 있다`() {
        setNavDisplay(routineList = emptyList())
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        assertFilterAndViewModeAvailable()
    }

    @Test
    fun `TC-18ff80b1-d6b4-469e-b9cb-eb5db3dffcfc 오늘 할 루틴이 없는 빈 상태에서도 필터 열기와 보기 전환을 실행할 수 있다`() {
        // 완료되지 않은 루틴은 있지만 오늘 할 루틴 조회가 돌려주는 루틴이 없다.
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = TODAY, titleList = emptyList())
        environment.setRoutineList(routineList = listOf(routine(title = UNTAGGED_TITLE).let { routine -> routine.copy(detail = routine.detail.copy(startDate = SEPTEMBER_THIRD)) }))
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        waitUntilExists(hasText(DEFAULT_TODAY_EMPTY_TITLE))

        assertFilterAndViewModeAvailable()
    }

    @Test
    fun `TC-18ff80b1-d6b4-469e-b9cb-eb5db3dffcfc 조건에 맞는 루틴이 없는 빈 상태에서도 필터 열기와 보기 전환을 실행할 수 있다`() {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        environment.tagList = listOf(tagA)
        setNavDisplay(routineList = listOf(routine(title = UNTAGGED_TITLE)))
        selectTag(tagA)
        closeFilter()
        waitUntilExists(hasText(DEFAULT_FILTERED_EMPTY_TITLE))

        assertFilterAndViewModeAvailable()
    }

    @Test
    fun `TC-6f62c520-9cc4-4033-8ce6-531f49a12030 삭제 안내가 보이는 동안 화면을 떠났다 돌아오면 안내가 닫히고 되돌릴 수 없다`() {
        val deleted = routine(title = ROUTINE_X_TITLE)
        val other = routine(title = ROUTINE_Y_TITLE)
        setNavDisplay(routineList = listOf(deleted, other))
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe RoutineAddNavKey()
        pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        environment.homeViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.restore(id = any()) } }
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp")
    fun `TC-81daa578-3961-422f-a5d2-5198be3c26e9 상세에 열린 루틴을 목록에서 밀어 삭제해도 상세 영역은 그대로다`() {
        val target = routine(title = ROUTINE_X_TITLE)
        val other = routine(title = ROUTINE_Y_TITLE)
        setNavDisplay(routineList = listOf(target, other))
        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = target.id))
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()

        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNode(filterCaseRoutineCard(ROUTINE_X_TITLE)).assertDoesNotExist()
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = target.id))
        // 목록에서 카드가 사라졌으므로 남은 제목은 상세 영역의 것이다.
        composeRule.onAllNodesWithText(ROUTINE_X_TITLE).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_DEFAULT_CONTENT).assertDoesNotExist()
    }

    @Test
    fun `TC-c3ac5635-c24d-44ed-804c-a1c62dab49c7 목록 보기에서 뒤로가기를 실행하면 기본 목적지로 돌아간다`() {
        setNavDisplay(routineList = listOf(routine(title = ROUTINE_X_TITLE)))

        pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey)
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_DEFAULT_CONTENT).assertIsDisplayed()
    }

    @Test
    fun `TC-c3ac5635-c24d-44ed-804c-a1c62dab49c7 달력 보기에서 뒤로가기를 실행하면 목록 보기로 바뀌지 않고 기본 목적지로 돌아간다`() {
        setNavDisplay(routineList = listOf(routine(title = ROUTINE_X_TITLE)))
        showCalendar()

        pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey)
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_DEFAULT_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertDoesNotExist()
    }

    @Test
    fun `TC-4aaba15d-5d44-4ee6-9138-b78fb32cf1ea 루틴 추가에 다녀와도 보던 목록 위치를 유지한다`() {
        val routineList = positionRoutineList()
        setNavDisplay(routineList = routineList)
        scrollToPosition(routineList)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe RoutineAddNavKey()
        pressBack()

        assertPositionDisplayed(routineList)
    }

    @Test
    fun `TC-4aaba15d-5d44-4ee6-9138-b78fb32cf1ea 루틴 상세에 다녀와도 보던 목록 위치를 유지한다`() {
        val routineList = positionRoutineList()
        setNavDisplay(routineList = routineList)
        scrollToPosition(routineList)

        composeRule.onNode(filterCaseRoutineCard(routineList[POSITION_SCROLL_INDEX].detail.title)).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe RoutineDetailNavKey(id = routineList[POSITION_SCROLL_INDEX].id)
        pressBack()

        assertPositionDisplayed(routineList)
    }

    @Test
    fun `TC-4aaba15d-5d44-4ee6-9138-b78fb32cf1ea 앱이 백그라운드에 다녀와도 보던 목록 위치를 유지한다`() {
        val routineList = positionRoutineList()
        setNavDisplay(routineList = routineList)
        scrollToPosition(routineList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertPositionDisplayed(routineList)
    }

    @Test
    fun `TC-ff209542-6500-409c-a923-13086046df13 TC-875e0a2f-30d2-40fc-9359-65925970d3f9 다른 주요 목적지에 다녀오면 필터 선택을 유지한 채 맨 위부터 보여 준다`() {
        val (tag, routineList, untagged) = filteredPositionFixture()
        setNavDisplay(routineList = routineList + untagged)
        selectTag(tag)
        closeFilter()
        scrollToPosition(routineList)

        // 공통 내비게이션에서 다른 주요 목적지를 고르면 전환 이력이 그 목적지의 것으로 바뀐다.
        composeRule.runOnIdle {
            backStack.clear()
            backStack.addAll(listOf(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_OTHER_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.addAll(listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey))
        }
        composeRule.waitForIdle()

        assertTopWithFilter(tag = tag, routineList = routineList, untagged = untagged)
    }

    @Test
    fun `TC-ff209542-6500-409c-a923-13086046df13 앱을 다시 실행해 진입하면 필터 선택을 유지한 채 맨 위부터 보여 준다`() {
        val (tag, routineList, untagged) = filteredPositionFixture()
        environment.setRoutineList(routineList = routineList + untagged)
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            // 앱을 다시 실행하면 저장해 둔 화면 상태와 ViewModel이 없으므로 새 키로 전환 이력과 ViewModel을 다시 만든다.
            key(launchCount) {
                val relaunchedBackStack = remember { NavBackStack<ScreenNavKey>(RoutineListDetailDefaultNavKey, RoutineHomeNavKey) }
                currentBackStack = relaunchedBackStack
                environment.Content(navBackStack = relaunchedBackStack, viewModelStoreOwner = remember { routineListDetailViewModelStoreOwner() })
            }
        }
        composeRule.waitForIdle()
        waitUntilExists(filterCaseRoutineCard(routineList.first().detail.title))
        selectTag(tag)
        closeFilter()
        scrollToPosition(routineList)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        assertTopWithFilter(tag = tag, routineList = routineList, untagged = untagged)
    }

    private fun routine(title: String): Routine = routineListDetailRoutine(title = title, today = TODAY, unit = "ml")

    private fun setSelectedAndUnselectedTag(): Pair<Tag, Tag> {
        val selected = Arb.tag(title = TAG_A_TITLE).next()
        val unselected = Arb.tag(title = TAG_B_TITLE).next()
        environment.tagList = listOf(selected, unselected)
        setNavDisplay(routineList = listOf(routine(title = ROUTINE_X_TITLE)))
        selectTag(selected)
        closeFilter()

        return selected to unselected
    }

    // 태그 A와 연결된 루틴 X와 어느 태그와도 연결되지 않은 루틴 Y가 오늘이 속한 달에 기록을 갖는다.
    private fun setSharedFilterFixture(): Tag {
        val tagA = Arb.tag(title = TAG_A_TITLE).next()
        val routineX = routine(title = ROUTINE_X_TITLE)
        val routineY = routine(title = ROUTINE_Y_TITLE)
        environment.tagList = listOf(tagA)
        environment.routineTagMap = mapOf(routineX.id to setOf(tagA.id))
        setNavDisplay(
            routineList = listOf(routineX, routineY),
            recordList =
                listOf(
                    routineListDetailRecord(routine = routineX, date = TODAY, amount = 300),
                    routineListDetailRecord(routine = routineY, date = TODAY, amount = 700),
                ),
        )

        return tagA
    }

    private fun assertFilterAndViewModeAvailable() {
        openFilter()
        closeFilter()

        showCalendar()
    }

    private fun assertFilterApplied() {
        composeRule.onNode(hasContentDescription(DEFAULT_FILTER_DESCRIPTION) and hasFilterCaseStateDescription(DEFAULT_FILTER_APPLIED)).assertExists()
    }

    private fun openFilter() {
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_TAG_FILTER_ADD_LABEL).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 필터 선택은 기기에 저장되므로 필터를 열어 태그를 고른다. 필터는 열린 채로 남는다.
    private fun selectTag(tag: Tag) {
        openFilter()
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    // 필터 Bottom Sheet는 별도 창이라 화면의 뒤로가기 대신 필터 화면을 전환 이력에서 빼서 닫는다.
    private fun closeFilter() {
        composeRule.runOnIdle { currentBackStack.remove(RoutineHomeFilterNavKey) shouldBe true }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_TAG_FILTER_ADD_LABEL).assertDoesNotExist()
    }

    private fun showCalendar() {
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertDoesNotExist()
    }

    private fun showList() {
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).assertExists()
    }

    private fun moveToNextMonth() {
        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeRule.waitForIdle()
    }

    private fun calendarRecord(
        title: String,
        amountText: String,
    ): SemanticsMatcher = hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription("$title $amountText, routine")

    private fun filteredPositionFixture(): Triple<Tag, List<Routine>, Routine> {
        val tag = Arb.tag(title = TAG_A_TITLE).next()
        val routineList = positionRoutineList()
        val untagged = routine(title = "00-untagged")
        environment.tagList = listOf(tag)
        environment.routineTagMap = routineList.associate { routine -> routine.id to setOf(tag.id) }

        return Triple(tag, routineList, untagged)
    }

    private fun assertTopWithFilter(
        tag: Tag,
        routineList: List<Routine>,
        untagged: Routine,
    ) {
        waitUntilExists(filterCaseRoutineCard(routineList.first().detail.title))
        composeRule.onNode(filterCaseRoutineCard(routineList.first().detail.title)).assertIsDisplayed()
        composeRule.onNode(filterCaseRoutineCard(routineList[POSITION_SCROLL_INDEX].detail.title)).assertDoesNotExist()
        composeRule.onNode(filterCaseRoutineCard(untagged.detail.title)).assertDoesNotExist()
        assertFilterApplied()
        openFilter()
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    private fun setNavDisplay(
        routineList: List<Routine>,
        recordList: List<RoutineRecord> = emptyList(),
    ) {
        environment.setRoutineList(routineList = routineList, recordList = recordList)
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        routineList.firstOrNull()?.let { routine -> waitUntilExists(filterCaseRoutineCard(routine.detail.title)) }
    }

    private fun scrollToPosition(routineList: List<Routine>) {
        waitUntilExists(filterCaseRoutineCard(routineList.first().detail.title))
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionDisplayed(routineList)
    }

    private fun assertPositionDisplayed(routineList: List<Routine>) {
        waitUntilExists(filterCaseRoutineCard(routineList[POSITION_SCROLL_INDEX].detail.title))
        composeRule.onNode(filterCaseRoutineCard(routineList[POSITION_SCROLL_INDEX].detail.title)).assertIsDisplayed()
        composeRule.onNode(filterCaseRoutineCard(routineList.first().detail.title)).assertDoesNotExist()
    }

    private fun waitUntilExists(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val SEPTEMBER_THIRD: LocalDate = LocalDate(year = 2026, month = 9, day = 3)
        val SEPTEMBER_TENTH: LocalDate = LocalDate(year = 2026, month = 9, day = 10)
        const val DEFAULT_TODAY_EMPTY_TITLE = "No routines for today"
        const val LIST_TITLE = "boundary-routine"
        const val ROUTINE_X_TITLE = "X-$LIST_TITLE"
        const val ROUTINE_Y_TITLE = "Y-$LIST_TITLE"
        const val UNTAGGED_TITLE = "Z-untagged-routine"
        const val TAG_A_TITLE = "tag-a"
        const val TAG_B_TITLE = "tag-b"
        const val ADDED_TAG_TITLE = "tag-added"
        const val DEFAULT_ADD_DESCRIPTION = "Add routine"
        const val DEFAULT_ADD_TITLE = "Add Routine"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
        const val DEFAULT_FILTER_APPLIED = "Tag filter applied"
        const val DEFAULT_TAG_FILTER_ADD_LABEL = "Add tag"
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        const val DEFAULT_LIST_VIEW_MODE_DESCRIPTION = "Show list"
        const val DEFAULT_EMPTY_TITLE = "No routines yet"
        const val DEFAULT_FILTERED_EMPTY_TITLE = "No routines match the filter"
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val POSITION_COUNT = 30
        const val POSITION_SCROLL_INDEX = 25
        const val TIMEOUT_MILLIS = 5_000L

        fun positionRoutineList(): List<Routine> =
            List(POSITION_COUNT) { index ->
                routineListDetailRoutine(title = "${index.toString().padStart(length = 2, padChar = '0')}-$LIST_TITLE", today = TODAY)
            }
    }
}

private const val AMOUNT_900_ML_TEXT = "900ml"
