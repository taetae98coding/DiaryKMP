package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
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
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SEARCH_HOME_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.place.SEARCH_HOME_PLACE_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SEARCH_HOME_TAG_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.web.SEARCH_HOME_WEB_LIST_TEST_TAG
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
    fun `TC-a91b6b2d-d824-4dae-b830-f4b893849a0f 메모 시작 유형이면 메모 결과를 먼저 본다`() {
        val memo = resultMemo()
        val tag = resultTag()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tag.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-a91b6b2d-d824-4dae-b830-f4b893849a0f 태그 시작 유형이면 태그 결과를 먼저 본다`() {
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
    fun `TC-a91b6b2d-d824-4dae-b830-f4b893849a0f 장소 시작 유형이면 장소 결과를 먼저 본다`() {
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
    fun `TC-a91b6b2d-d824-4dae-b830-f4b893849a0f 웹 시작 유형이면 웹 결과를 먼저 본다`() {
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
    fun `TC-81dbea76-4318-43e8-a5b1-c48cd824b574 시작 유형이 아니어도 다른 유형의 결과를 채운다`() {
        val memo = resultMemo()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), initialType = SearchHomeType.WEB)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-ac000104-817c-41bb-a7b8-d10d0eccf796 보고 있지 않은 유형의 결과를 미리 준비하지 않는다`() {
        val memo = resultMemo()

        composeRule.setSearchHomeScreen(memoList = listOf(memo))
        inputQuery()

        // 유형별 결과는 그 유형의 ViewModel이 만들어져 조회를 구독해야 찾기 시작하므로, ViewModel이 없으면 준비도 시작되지 않은 것이다.
        val memoViewModel = searchMemoViewModelRef.shouldNotBeNull()
        verify { memoViewModel.updateQuery(QUERY) }
        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        searchTagViewModelRef shouldBe null
        searchPlaceViewModelRef shouldBe null
        searchWebViewModelRef shouldBe null
        composeRule.runOnIdle {
            val subscriptionCount = searchResultSubscriptionCount()
            (checkNotNull(subscriptionCount["memo"]) > 0) shouldBe true
            subscriptionCount.filterKeys { type -> type != "memo" }.values.toSet() shouldBe setOf(0)
        }
    }

    @Test
    fun `TC-2293da84-b04b-4a4e-8235-49d8370b94e2 태그 유형으로 바꾸면 태그 결과를 본다`() {
        val memo = resultMemo()
        val tag = resultTag()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-2293da84-b04b-4a4e-8235-49d8370b94e2 장소 유형으로 바꾸면 장소 결과를 본다`() {
        val memo = resultMemo()
        val place = resultPlace()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), placeList = listOf(place))
        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-2293da84-b04b-4a4e-8235-49d8370b94e2 웹 유형으로 바꾸면 웹 결과를 본다`() {
        val memo = resultMemo()
        val web = resultWeb()

        composeRule.setSearchHomeScreen(memoList = listOf(memo), webList = listOf(web))
        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memo.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-10cbe4a5-b4e2-4025-aff1-050b0ed7dbcb 유형을 바꿔도 질의가 유지된다`() {
        val tag = resultTag()

        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        inputQuery()
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-0fa9270b-5438-40f2-b9ee-c5656931b232 메모 결과가 비어 있어도 태그 결과를 확인한다`() {
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
    fun `TC-af25c0b5-1b0a-4c9a-815c-f9ab7983a4cd 메모 결과를 선택하면 그 메모의 상세로 이동한다`() {
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
    fun `TC-af25c0b5-1b0a-4c9a-815c-f9ab7983a4cd 태그 결과를 선택하면 그 태그의 상세로 이동한다`() {
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
    fun `TC-af25c0b5-1b0a-4c9a-815c-f9ab7983a4cd 장소 결과를 선택하면 그 장소의 상세로 이동한다`() {
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
    fun `TC-af25c0b5-1b0a-4c9a-815c-f9ab7983a4cd 웹 결과를 선택하면 그 웹 항목의 상세로 이동한다`() {
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
    fun `TC-2408d17d-c93d-4e9d-88e9-73c3e1fbcfa0 뒤로가기를 선택하면 화면을 벗어난다`() {
        var navigateUpCount = 0

        composeRule.setSearchHomeScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-ca8f6ff7-7dcc-4863-9b93-f82f6f60fea9 화면에 들어오면 검색어 입력에 초점을 맞춘다`() {
        composeRule.setSearchHomeScreen()

        composeRule.onNode(hasSetTextAction()).assertIsFocused()
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
    fun `TC-5ba18e02-61f0-43c9-9907-aab1af80dc42 질의를 고쳤다가 반영되기 전에 원래 검색어로 되돌리면 보던 자리를 유지한다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }
        val extraText = "추가-${fixtureMonkey.giveMeOne<String>()}"

        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()

        setSearchQueryApplied(isApplied = false)
        composeRule.onNode(hasSetTextAction()).performTextInput(extraText)
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-23a5a27a-c358-46a0-b89e-bd14c1dab6ab 결과를 선택해 상세 화면에 다녀와도 보던 자리를 유지한다`() {
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
    fun `TC-d7d17ab4-2d4a-4d77-9ede-15770879555d 고친 질의가 반영된 뒤 원래 검색어로 되돌리면 처음부터 본다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }

        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()

        composeRule.onNode(hasSetTextAction()).performTextInput("추가-${fixtureMonkey.giveMeOne<String>()}")
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-a6fb035f-f68b-4e5c-abf1-f347ca181f9a 상세에서 돌아오면 질의 입력에 초점을 다시 두지 않는다`() {
        val memo = resultMemo()
        var isShown by mutableStateOf(true)

        composeRule.setSearchHomeScreen(
            memoList = listOf(memo),
            navigateToMemoDetail = { isShown = false },
            isShownProvider = { isShown },
        )
        inputQuery()
        composeRule.onNode(hasSetTextAction()).assertIsFocused()

        composeRule.onNodeWithText(memo.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotFocused()
    }

    @Test
    fun `TC-4903adcf-92a1-4f19-9f69-a220f55b7260 상세에서 돌아와도 질의와 보고 있던 유형이 그대로 유지된다`() {
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
        searchTagViewModelRef?.uiState?.value?.appliedQuery shouldBe QUERY
    }

    @Test
    fun `TC-2cdbd59e-a0d9-4ce2-b161-c6ed4c46d540 앱이 백그라운드에 다녀와도 질의와 보고 있던 유형이 그대로 유지된다`() {
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
        searchPlaceViewModelRef?.uiState?.value?.appliedQuery shouldBe QUERY
    }

    @Test
    fun `TC-9ea84aad-f762-440e-81c7-030c8a398189 메모리 정리 뒤 복원해도 질의와 유형을 유지하고 곧바로 그 질의의 결과를 보여 준다`() {
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
        placeViewModel.uiState.value.appliedQuery shouldBe QUERY
        composeRule.onNodeWithText(QUERY).assertExists()
        composeRule.onNodeWithText(PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-21971de1-16bd-49cc-aec1-7b35f40b8bb7 메모리 정리 뒤 복원하면 각 유형에서 고른 정렬이 처음 정렬로 돌아간다`() {
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

    @Test
    fun `TC-6df52e63-e8a7-4818-bc66-f337fc2536c7 화면을 떠난 뒤 다시 들어오면 빈 질의와 그때 들어온 경로의 시작 유형으로 시작한다`() {
        var entryCount by mutableIntStateOf(0)
        var initialType by mutableStateOf(SearchHomeType.MEMO)
        composeRule.setSearchHomeScreen(
            initialTypeProvider = { initialType },
            entryKeyProvider = { entryCount },
        )
        inputQuery()
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)
        composeRule.onNodeWithText(TAG_TAB_LABEL).assertIsSelected()
        val placeViewModelBeforeLeave = searchPlaceViewModelRef

        composeRule.runOnIdle {
            initialType = SearchHomeType.PLACE
            entryCount += 1
        }
        composeRule.waitForIdle()

        queryInputText() shouldBe ""
        composeRule.onNodeWithText(PLACE_TAB_LABEL).assertIsSelected()
        val placeViewModel = searchPlaceViewModelRef.shouldNotBeNull()
        (placeViewModel === placeViewModelBeforeLeave) shouldBe false
        placeViewModel.uiState.value.appliedQuery shouldBe ""
        assertResultCardCount(testTag = PLACE_CARD_TEST_TAG, count = 0)
    }

    @Test
    fun `TC-da083658-e562-439a-846a-d1cbaa345eba 앱을 다시 실행하면 마지막에 검색한 질의가 남지 않는다`() {
        var launchCount by mutableIntStateOf(0)
        composeRule.setSearchHomeScreen(entryKeyProvider = { launchCount })
        inputQuery()
        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        composeRule.onNodeWithText(WEB_TAB_LABEL).assertIsSelected()
        val memoViewModelBeforeRelaunch = searchMemoViewModelRef

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        queryInputText() shouldBe ""
        composeRule.onNodeWithText(MEMO_TAB_LABEL).assertIsSelected()
        val memoViewModel = searchMemoViewModelRef.shouldNotBeNull()
        (memoViewModel === memoViewModelBeforeRelaunch) shouldBe false
        memoViewModel.uiState.value.appliedQuery shouldBe ""
        assertResultCardCount(testTag = MEMO_CARD_TEST_TAG, count = 0)
    }

    @Test
    fun `TC-3fa4c153-d620-4f53-aeea-3ccbfd75dbe5 결과 목록을 아래로 당겨도 새로고침이 시작되지 않는다`() {
        composeRule.setSearchHomeScreen(
            memoList = List(RESULT_COUNT) { resultMemo() },
            tagList = List(RESULT_COUNT) { resultTag() },
            placeList = List(RESULT_COUNT) { resultPlace() },
            webList = List(RESULT_COUNT) { resultWeb() },
        )
        inputQuery()

        listOf(
            MEMO_TAB_LABEL to SEARCH_HOME_MEMO_LIST_TEST_TAG,
            TAG_TAB_LABEL to SEARCH_HOME_TAG_LIST_TEST_TAG,
            PLACE_TAB_LABEL to SEARCH_HOME_PLACE_LIST_TEST_TAG,
            WEB_TAB_LABEL to SEARCH_HOME_WEB_LIST_TEST_TAG,
        ).forEach { (label, listTestTag) ->
            composeRule.selectSearchHomeTab(label)

            composeRule.onNodeWithTag(listTestTag).performTouchInput { swipeDown() }
            composeRule.waitForIdle()

            composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).assertDoesNotExist()
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo), useUnmergedTree = true)
                .assertCountEquals(0)
        }
    }

    private fun assertResultCardCount(
        testTag: String,
        count: Int,
    ) {
        composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().size shouldBe count
    }

    private fun queryInputText(): String =
        composeRule
            .onNode(hasSetTextAction())
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

    private fun inputQuery() {
        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-7cdf61eb-1e03-42c3-939a-207c24d54ddd 질의가 결과에 반영되기 전에는 결과 없음을 알리지 않는다`() {
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
