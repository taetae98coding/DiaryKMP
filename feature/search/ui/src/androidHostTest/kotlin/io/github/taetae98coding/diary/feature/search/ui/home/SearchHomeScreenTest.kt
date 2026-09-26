package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SEARCH_HOME_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `조회 결과가 없으면 네 유형의 결과에 카드가 하나도 없다`() {
        composeRule.setSearchHomeScreen()

        assertResultCardCount(testTag = MEMO_CARD_TEST_TAG, count = 0)

        listOf(
            TAG_TAB_LABEL to TAG_CARD_TEST_TAG,
            PLACE_TAB_LABEL to PLACE_CARD_TEST_TAG,
            WEB_TAB_LABEL to WEB_CARD_TEST_TAG,
        ).forEach { (label, cardTestTag) ->
            composeRule.selectSearchHomeTab(label)

            assertResultCardCount(testTag = cardTestTag, count = 0)
        }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 메모 시작 유형이면 메모 결과를 먼저 본다`() {
        val memo = resultMemo()
        val tag = resultTag()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tag.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 태그 시작 유형이면 태그 결과를 먼저 본다`() {
        val memo = resultMemo()
        val tag = resultTag()

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            tagList = listOf(tag),
            initialType = SearchHomeType.TAG,
        )

        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 장소 시작 유형이면 장소 결과를 먼저 본다`() {
        val memo = resultMemo()
        val place = resultPlace()

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            placeList = listOf(place),
            initialType = SearchHomeType.PLACE,
        )

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 웹 시작 유형이면 웹 결과를 먼저 본다`() {
        val memo = resultMemo()
        val web = resultWeb()

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            webList = listOf(web),
            initialType = SearchHomeType.WEB,
        )

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-015 시작 유형이 아니어도 다른 유형의 결과를 채운다`() {
        val memo = resultMemo()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), initialType = SearchHomeType.WEB)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 태그 유형으로 바꾸면 태그 결과를 본다`() {
        val memo = resultMemo()
        val tag = resultTag()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 장소 유형으로 바꾸면 장소 결과를 본다`() {
        val memo = resultMemo()
        val place = resultPlace()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), placeList = listOf(place))
        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 웹 유형으로 바꾸면 웹 결과를 본다`() {
        val memo = resultMemo()
        val web = resultWeb()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), webList = listOf(web))
        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-007 유형을 바꿔도 질의가 유지된다`() {
        val tag = resultTag()

        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        inputQuery()
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-011 메모 결과가 비어 있어도 태그 결과를 확인한다`() {
        val tag = resultTag()

        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        inputQuery()
        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()

        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(QUERY).assertExists()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-012 메모 결과를 선택하면 그 메모의 상세로 이동한다`() {
        val memo = resultMemo()
        val idList = mutableListOf<Uuid>()

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            navigateToMemoDetail = idList::add,
        )
        composeRule.onNodeWithText(memo.detail.title).performClick()
        composeRule.waitForIdle()

        idList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-012 태그 결과를 선택하면 그 태그의 상세로 이동한다`() {
        val tag = resultTag()
        val idList = mutableListOf<Uuid>()

        composeRule.setSearchHomeScreen(
            tagList = listOf(tag),
            initialType = SearchHomeType.TAG,
            navigateToTagDetail = idList::add,
        )
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()

        idList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-012 장소 결과를 선택하면 그 장소의 상세로 이동한다`() {
        val place = resultPlace()
        val idList = mutableListOf<Uuid>()

        composeRule.setSearchHomeScreen(
            placeList = listOf(place),
            initialType = SearchHomeType.PLACE,
            navigateToPlaceDetail = idList::add,
        )
        composeRule.onNodeWithText(place.detail.title).performClick()
        composeRule.waitForIdle()

        idList shouldBe listOf(place.id)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-012 웹 결과를 선택하면 그 웹 항목의 상세로 이동한다`() {
        val web = resultWeb()
        val idList = mutableListOf<Uuid>()

        composeRule.setSearchHomeScreen(
            webList = listOf(web),
            initialType = SearchHomeType.WEB,
            navigateToWebDetail = idList::add,
        )
        composeRule.onNodeWithText(web.detail.title).performClick()
        composeRule.waitForIdle()

        idList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-014 뒤로가기를 선택하면 화면을 벗어난다`() {
        var navigateUpCount = 0

        composeRule.setSearchHomeScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-021 화면에 들어오면 검색어 입력에 초점을 맞춘다`() {
        composeRule.setSearchHomeScreen()

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).assertIsFocused()
    }

    @Test
    fun `유형을 바꿔 돌아오면 내려가 있던 자리에서 이어서 본다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }

        composeRule.setSearchHomeScreen(memoList = memoList)
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()

        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-027 질의를 고쳤다가 반영되기 전에 원래 검색어로 되돌리면 보던 자리를 유지한다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }
        val extraText = "추가-${fixtureMonkey.giveMeOne<String>()}"

        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()

        setSearchQueryApplied(isApplied = false)
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(extraText)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextReplacement(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-028 결과를 선택해 상세 화면에 다녀와도 보던 자리를 유지한다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }
        var isShown by mutableStateOf(true)
        val navigatedIdList = mutableListOf<Uuid>()

        composeRule.setSearchHomeScreen(
            memoList = memoList,
            navigateToMemoDetail = { id ->
                navigatedIdList.add(id)
                isShown = false
            },
            isShownProvider = { isShown },
        )
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.last().detail.title).performClick()
        composeRule.waitForIdle()
        navigatedIdList shouldBe listOf(memoList.last().id)
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-029 고친 질의가 반영된 뒤 원래 검색어로 되돌리면 처음부터 본다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }

        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput("추가-${fixtureMonkey.giveMeOne<String>()}")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextReplacement(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-022 상세에서 돌아오면 질의 입력에 초점을 다시 두지 않는다`() {
        val memo = resultMemo()
        var isShown by mutableStateOf(true)

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            navigateToMemoDetail = { isShown = false },
            isShownProvider = { isShown },
        )
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).assertIsFocused()

        composeRule.onNodeWithText(memo.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).assertIsNotFocused()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-046 상세에서 돌아와도 질의와 보고 있던 유형이 그대로 유지된다`() {
        val tag = resultTag()
        val navigatedIdList = mutableListOf<Uuid>()
        var isShown by mutableStateOf(true)

        composeRule.setSearchHomeScreen(
            tagList = listOf(tag),
            navigateToTagDetail = { id ->
                navigatedIdList.add(id)
                isShown = false
            },
            isShownProvider = { isShown },
        )
        inputQuery()
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
        navigatedIdList shouldBe listOf(tag.id)
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(TAG_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
        searchTagViewModelRef?.appliedQuery?.value shouldBe QUERY
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-047 앱이 백그라운드에 다녀와도 질의와 보고 있던 유형이 그대로 유지된다`() {
        val place = resultPlace()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        composeRule.setSearchHomeScreen(
            placeList = listOf(place),
            lifecycleOwner = lifecycleOwner,
        )
        inputQuery()
        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        searchPlaceViewModelRef?.appliedQuery?.value shouldBe QUERY
    }

    @Test
    fun `TC-SEARCH-HOME-DOMAIN-019 메모리 정리 뒤 복원해도 질의와 유형을 유지하고 곧바로 그 질의의 결과를 보여 준다`() {
        val place = resultPlace()
        val restorationTester = StateRestorationTester(composeRule)
        composeRule.setSearchHomeScreen(placeList = listOf(place), restorationTester = restorationTester)
        inputQuery()
        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)
        val placeViewModelBeforeRestore = searchPlaceViewModelRef
        // 복원 뒤에는 입력을 멈춘 뒤의 반영이 일어나지 않게 막아, 결과가 곧바로 반영되었는지만 본다.
        setSearchQueryApplied(isApplied = false)
        clearAppliedSearchQuery()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        val placeViewModel = searchPlaceViewModelRef.shouldNotBeNull()
        (placeViewModel === placeViewModelBeforeRestore) shouldBe false
        verify(exactly = 1) { placeViewModel.showQuery(QUERY) }
        placeViewModel.appliedQuery.value shouldBe QUERY
        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-DOMAIN-020 메모리 정리 뒤 복원하면 각 유형에서 고른 정렬이 처음 정렬로 돌아간다`() {
        val place = resultPlace()
        val restorationTester = StateRestorationTester(composeRule)
        composeRule.setSearchHomeScreen(placeList = listOf(place), restorationTester = restorationTester)
        inputQuery()
        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)
        val placeViewModelBeforeRestore = searchPlaceViewModelRef.shouldNotBeNull()
        composeRule.runOnIdle { placeViewModelBeforeRestore.select(sort = ListSort.RECENTLY_UPDATED) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(RECENTLY_UPDATED_SORT_LABEL).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        val placeViewModel = searchPlaceViewModelRef.shouldNotBeNull()
        (placeViewModel === placeViewModelBeforeRestore) shouldBe false
        composeRule.onNodeWithText(PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(TITLE_SORT_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(RECENTLY_UPDATED_SORT_LABEL).assertDoesNotExist()
    }

    private fun assertResultCardCount(
        testTag: String,
        count: Int,
    ) {
        composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().size shouldBe count
    }

    private fun inputQuery() {
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-023 질의가 결과에 반영되기 전에는 결과 없음을 알리지 않는다`() {
        composeRule.setSearchHomeScreen(isQueryAppliedImmediately = false)
        inputQuery()

        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    private companion object {
        private const val QUERY = "여행"
        private const val RESULT_COUNT = 30
        private const val TITLE_SORT_LABEL = "Title"
        private const val RECENTLY_UPDATED_SORT_LABEL = "Recently updated"
        private const val EMPTY_TITLE = "No search results"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
