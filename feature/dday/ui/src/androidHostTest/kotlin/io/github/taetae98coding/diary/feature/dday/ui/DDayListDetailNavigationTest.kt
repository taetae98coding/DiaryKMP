@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.dday.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeFilterNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.dDayNavKeys
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 dDayEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class DDayListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = DDayListDetailTestEnvironment()
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseListDetailToday()
    }

    @Test
    fun `TC-192054ac-1560-41c4-b962-cc43f17aa1c4 함께 쓰는 환경에서는 목록과 디데이 추가를 함께 표시한다`() {
        val dday = dday(title = FIRST_TITLE)

        setNavDisplay(ddayList = listOf(dday))

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-344fff4c-f9ab-436b-bb4b-13150bbef431 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()

        pressBack()
        selectDDay(dday)
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        titleInput().editableText() shouldBe dday.detail.title
    }

    @Test
    fun `TC-168d5f2f-9a83-44a0-8681-9b4d7d7c5836 상세 영역에 디데이 추가가 놓이면 목록에 추가 수단이 없고 단축키도 동작하지 않는다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))
        assertAddOnDetailPane()

        // 이름이 같은 디데이 추가의 저장 버튼만 상세 영역에 있고 목록 쪽에는 추가 동작이 없다.
        addActionLeftList().forEach { left -> (left >= detailPaneLeft()) shouldBe true }
        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-38ea0ab8-c88f-42a0-b637-3633db1f1ab7 상세 영역을 함께 표시하지 않으면 목록에서 디데이 추가를 제공한다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayAddNavKey())
    }

    @Test
    fun `TC-38ea0ab8-c88f-42a0-b637-3633db1f1ab7 상세 영역에 디데이 상세가 놓이면 목록에서 디데이 추가를 제공한다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        selectDDay(dday)

        addActionRightList().single() shouldBeLessThanDetailPane true
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = dday.id), DDayAddNavKey())
    }

    @Test
    fun `TC-d85b67f6-39f8-45bb-af6e-489cd82ce884 목록과 함께 표시되면 상세 영역의 화면에 뒤로가기 수단이 없다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        assertAddOnDetailPane()
        navigateUpRightList().forEach { right -> right shouldBeLessThanDetailPane true }

        selectDDay(dday)

        assertDetailOnDetailPane(dday)
        navigateUpRightList().forEach { right -> right shouldBeLessThanDetailPane true }
        composeRule.onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-4617590f-797a-4f4c-a926-47baaf18adfb 단독으로 표시되면 그 화면에 뒤로가기 수단이 있다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)

        selectDDay(dday)
        titleInput().editableText() shouldBe dday.detail.title
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
    }

    @Test
    fun `TC-49e4bb60-44bc-4d80-aa4e-aa3f0703e9f9 선택한 디데이가 없을 때의 디데이 추가는 처음 상태다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))

        assertAddOnDetailPane()
        assertAddInitial()
    }

    @Test
    fun `TC-4043d3df-0c2a-453c-a1d3-e00622eb6fd5 목록에서 디데이를 선택하면 목록을 유지한 채 상세 영역이 그 상세로 바뀐다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        selectDDay(dday)

        backStack.last() shouldBe DDayDetailNavKey(id = dday.id)
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertDetailOnDetailPane(dday)
    }

    @Test
    fun `TC-6fd322c5-60f6-4270-a64e-76f2adb0d9a8 상세 영역에서 뒤로가면 디데이 추가로 되돌아간다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        selectDDay(dday)

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-870230ae-f3ae-4a85-8904-5f4e31f26e37 상세에서 디데이를 삭제하면 목록에서 사라지고 상세 영역이 디데이 추가로 되돌아간다`() {
        val deleted = dday(title = FIRST_TITLE)
        val other = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(deleted, other))
        selectDDay(deleted)

        clickDetailDelete()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(deleted.detail.title)).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNode(listDetailCard(other.detail.title)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-d498c6d5-5f5b-467d-b7f3-54a94ccb4f5f 상세가 놓인 동안 디데이 추가를 실행하면 처음 상태의 디데이 추가로 바뀐다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        fillPlaceholder(fillEndDateAndTag = false)

        openAddOnDetail(dday)

        backStack.last() shouldBe DDayAddNavKey()
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertAddOnDetailPane()
        assertAddInitial()
    }

    @Test
    fun `TC-540d1fce-d043-4720-aac6-5ce497d2a1e6 상세 위에서 연 디데이 추가에서 뒤로가면 이전 디데이 상세로 돌아간다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        openAddOnDetail(dday)

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertDetailOnDetailPane(dday)
    }

    @Test
    fun `TC-bc75a624-537e-4c24-9821-601267390281 상세가 놓인 동안 다른 디데이를 고르면 교체하고 뒤로가면 디데이 추가로 돌아간다`() {
        val first = dday(title = FIRST_TITLE)
        val second = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(first, second))

        selectDDay(first)
        selectDDay(second)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = second.id))
        assertDetailOnDetailPane(second)

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.onNode(listDetailCard(first.detail.title)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-db2c9587-68c2-4531-b8e4-7974c519be40 상세 위에서 연 디데이 추가가 놓인 동안 디데이를 고르면 추가를 걷어낸다`() {
        val first = dday(title = FIRST_TITLE)
        val second = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(first, second))
        openAddOnDetail(first)
        titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        selectDDay(second)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = second.id))
        assertDetailOnDetailPane(second)

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInitial()
    }

    @Test
    fun `TC-db2c9587-68c2-4531-b8e4-7974c519be40 좁은 창에서 연 디데이 추가가 창이 넓어져 상세 영역에 놓인 동안 디데이를 고르면 추가를 걷어낸다`() {
        val dday = dday(title = FIRST_TITLE)
        environment.directiveOverride = singlePaneDirective()
        setNavDisplay(ddayList = listOf(dday))
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { environment.directiveOverride = null }
        composeRule.waitForIdle()
        selectDDay(dday)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        assertDetailOnDetailPane(dday)

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInitial()
    }

    @Test
    fun `TC-f93e1970-1e19-4b11-a316-f19e3bcb65f8 선택 전 디데이 추가에서 추가에 성공하면 디데이 추가로 남고 목록에 나타난다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        titleInput().performTextInput(ADDED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(title = ADDED_TITLE, countText = TODAY_COUNT_TEXT)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertAddOnDetailPane()
        assertAddInitial()
    }

    @Test
    fun `TC-afe591b8-0f1f-437f-8c40-ae6859ea4acc 상세 영역에서 수정하면 목록의 카드 문구와 자리가 같은 화면에서 바뀐다`() {
        val ga = dday(title = "가", baseDate = TODAY)
        val na = dday(title = "나")
        val da = dday(title = "다")
        setNavDisplay(ddayList = listOf(ga, na, da))
        selectDDay(ga)
        composeRule.onNode(listDetailCard(title = "가", countText = TODAY_COUNT_TEXT)).assertExists()

        titleInput().performTextReplacement("라")
        pickDate(currentText = TODAY_TEXT, date = TODAY.plus(1, DateTimeUnit.DAY))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe DDayDetailNavKey(id = ga.id)
        assertDetailOnDetailPane(title = "라")
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(title = "라", countText = "D-1")).fetchSemanticsNodes().isNotEmpty()
        }
        cardBoundsList(listOf("나", "다", "라")).zipWithNext().forEach { (before, after) -> before.isBeforeInGrid(after) shouldBe true }
    }

    @Test
    fun `TC-5c8da904-382b-4392-af4c-09d590b03fa7 디데이 추가가 놓여 있어도 필터를 쓸 수 있고 상세 영역과 제목은 그대로다`() {
        val (tag, tagged, untagged) = filterFixture()
        setNavDisplay(ddayList = listOf(tagged, untagged))
        titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        toggleFilterTag(tag)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(untagged.detail.title)).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNode(listDetailCard(tagged.detail.title)).assertExists()
        toggleFilterTag(tag)
        waitUntilCard(untagged)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayHomeFilterNavKey)
        assertAddOnDetailPane()
        titleInput().editableText() shouldBe TYPED_ADD_TITLE
    }

    @Test
    fun `TC-5c8da904-382b-4392-af4c-09d590b03fa7 디데이 상세가 놓여 있어도 필터를 쓸 수 있고 상세 영역은 그대로다`() {
        val (tag, tagged, untagged) = filterFixture()
        setNavDisplay(ddayList = listOf(tagged, untagged))
        selectDDay(tagged)

        toggleFilterTag(tag)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(untagged.detail.title)).fetchSemanticsNodes().isEmpty()
        }
        toggleFilterTag(tag)
        waitUntilCard(untagged)

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = tagged.id), DDayHomeFilterNavKey)
        assertDetailOnDetailPane(tagged)
    }

    @Test
    fun `TC-da4cfc24-359b-4553-8f7f-2e83aded515d 상세의 디데이가 필터에 맞지 않아 목록에서 사라져도 상세가 남는다`() {
        val (tag, tagged, untagged) = filterFixture()
        setNavDisplay(ddayList = listOf(tagged, untagged))
        selectDDay(untagged)

        toggleFilterTag(tag)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(untagged.detail.title)).fetchSemanticsNodes().isEmpty()
        }

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = untagged.id), DDayHomeFilterNavKey)
        assertDetailOnDetailPane(untagged)
    }

    @Test
    fun `TC-c0564f5d-b949-4a16-9209-92e775fb471f 디데이를 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-4fede1f8-2097-47ef-ae8b-15ab904755a1 다른 화면에서 진입한 디데이 상세는 목록 없이 단독으로 뒤로가기와 함께 표시한다`() {
        val dday = dday(title = FIRST_TITLE)
        environment.setDDayList(listOf(dday))
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(DDayListDetailMoreNavKey)
            backStack.add(DDayDetailNavKey(id = dday.id))
        }
        setNavDisplay(ddayList = listOf(dday))

        titleInput().editableText() shouldBe dday.detail.title
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
    }

    @Test
    fun `TC-4fede1f8-2097-47ef-ae8b-15ab904755a1 TagDetail 디데이 탭에서 진입한 디데이 추가는 목록 없이 단독으로 뒤로가기와 함께 표시한다`() {
        val tag = Arb.tag(title = TAG_TITLE).next()
        val dday = dday(title = FIRST_TITLE)
        environment.tagList = listOf(tag)
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(DDayListDetailMoreNavKey)
            backStack.add(DDayAddNavKey(initialTagId = tag.id))
        }
        setNavDisplay(ddayList = listOf(dday))

        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNode(listDetailCard(dday.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
    }

    @Test
    fun `TC-4ff90937-184a-449b-8fe6-ac1b9b0ef240 디데이 목록에서 이동한 디데이 추가는 상세 영역에 표시한다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        openAddOnDetail(dday)

        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertAddOnDetailPane()
        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().forEach { node ->
            (node.boundsInRoot.left >= detailPaneLeft()) shouldBe true
        }
    }

    @Test
    fun `TC-4ff90937-184a-449b-8fe6-ac1b9b0ef240 디데이 목록에서 이동한 디데이 상세는 상세 영역에 표시한다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))

        selectDDay(dday)

        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        (titleInput().fetchSemanticsNode().boundsInRoot.left >= detailPaneLeft()) shouldBe true
    }

    @Test
    fun `TC-a9409ff5-1fe1-4b07-b18c-6f41aac570e3 디데이 추가가 함께 표시되어도 목록의 뒤로가기는 더보기로 돌아간다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-a9409ff5-1fe1-4b07-b18c-6f41aac570e3 디데이 상세가 함께 표시되어도 목록의 뒤로가기는 더보기로 돌아간다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        selectDDay(dday)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-a9409ff5-1fe1-4b07-b18c-6f41aac570e3 단독으로 표시된 목록의 뒤로가기는 더보기로 돌아간다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey)
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-83eb349e-2a1b-4e05-b914-62c183504f07 상세에 열린 디데이를 목록에서 밀어 삭제해도 상세 영역은 그대로다`() {
        val dday = dday(title = FIRST_TITLE)
        val other = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(dday, other))
        selectDDay(dday)

        composeRule.onNode(listDetailCard(dday.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.homeViewModelList.last().delete(id = dday.id) }
        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        assertDetailOnDetailPane(dday)
    }

    @Test
    fun `TC-f6cb0bae-2350-47b2-ad84-d7c0efdbffac 함께 쓰는 동안 상세 영역의 화면이 바뀌어도 오늘을 다시 정하지 않는다`() {
        val tomorrow = dday(title = FIRST_TITLE, baseDate = TODAY.plus(1, DateTimeUnit.DAY))
        val other = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(tomorrow, other))
        composeRule.onNode(listDetailCard(title = tomorrow.detail.title, countText = "D-1")).assertExists()

        setListDetailToday(TODAY.plus(1, DateTimeUnit.DAY))
        selectDDay(other)
        assertDetailOnDetailPane(other)
        pressBack()
        assertAddOnDetailPane()

        composeRule.onNode(listDetailCard(title = tomorrow.detail.title, countText = "D-1")).assertExists()
    }

    @Test
    fun `TC-80d61084-ecfe-45b3-9504-dc298f6f68cb 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        selectDDay(dday)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(DDayHomeNavKey) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-8f9177f1-b6c1-426a-aff8-8b18da742c57 더보기로 돌아갔다가 다시 진입하면 디데이 추가는 처음 상태로 시작한다`() {
        setNavDisplay(ddayList = listOf(dday(title = FIRST_TITLE)))
        fillPlaceholder(fillEndDateAndTag = true)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DDAY_LIST_DETAIL_MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(DDayHomeNavKey) }
        composeRule.waitForIdle()

        assertAddOnDetailPane()
        assertAddInitial()
        environment.addTagViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-68d2ae5f-588d-4977-8224-fc2e85c2d68e 배치를 떠나기 전에는 상세를 보고 돌아와도 디데이 추가의 작성 내용이 남는다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        fillPlaceholder(fillEndDateAndTag = true)

        selectDDay(dday)
        assertDetailOnDetailPane(dday)
        pressBack()

        assertAddOnDetailPane()
        assertPlaceholderRetained(withEndDateAndTag = true)
    }

    @Test
    fun `TC-8a3867f2-3b89-4f30-b022-5bf3c373f14b 목록에서 연 디데이 추가를 뒤로가기로 닫아도 선택 전 디데이 추가의 작성 내용이 남는다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        fillPlaceholder(fillEndDateAndTag = false)
        openAddOnDetail(dday)
        assertAddInitial()

        pressBack()
        assertDetailOnDetailPane(dday)
        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        assertPlaceholderRetained(withEndDateAndTag = false)
    }

    @Test
    fun `TC-8a3867f2-3b89-4f30-b022-5bf3c373f14b 목록에서 연 디데이 추가를 다른 디데이를 골라 걷어내도 선택 전 디데이 추가의 작성 내용이 남는다`() {
        val first = dday(title = FIRST_TITLE)
        val second = dday(title = SECOND_TITLE)
        setNavDisplay(ddayList = listOf(first, second))
        fillPlaceholder(fillEndDateAndTag = false)
        openAddOnDetail(first)
        assertAddInitial()

        selectDDay(second)
        assertDetailOnDetailPane(second)
        pressBack()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey)
        assertPlaceholderRetained(withEndDateAndTag = false)
    }

    @Test
    fun `TC-4f9e2a8e-c7a6-417a-91f9-4d86d9a6bd12 화면이 재생성되어도 상세 선택을 유지한다`() {
        val dday = dday(title = FIRST_TITLE)
        environment.setDDayList(listOf(dday))
        val restorationTester = StateRestorationTester(composeRule)
        val viewModelStoreOwner = listDetailViewModelStoreOwner()
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        restorationTester.setContent {
            val restorableBackStack =
                rememberSerializable(
                    configuration = ddaySavedStateConfiguration,
                    serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                ) { NavBackStack<ScreenNavKey>(DDayHomeNavKey) }
            restoredBackStack = restorableBackStack
            environment.Content(navBackStack = restorableBackStack, viewModelStoreOwner = viewModelStoreOwner)
        }
        waitUntilCard(dday)
        selectDDay(dday)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        waitUntilCard(dday)
        assertDetailOnDetailPane(dday)
        pressBack()
        restoredBackStack.toList() shouldBe listOf(DDayHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-4f9e2a8e-c7a6-417a-91f9-4d86d9a6bd12 앱이 백그라운드에 다녀와도 상세 선택을 유지한다`() {
        val dday = dday(title = FIRST_TITLE)
        setNavDisplay(ddayList = listOf(dday))
        selectDDay(dday)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayListDetailMoreNavKey, DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        composeRule.onNode(listDetailCard(dday.detail.title)).assertExists()
        assertDetailOnDetailPane(dday)
        pressBack()
        assertAddOnDetailPane()
    }

    private fun setNavDisplay(ddayList: List<DDay>) {
        environment.setDDayList(ddayList)
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        ddayList.firstOrNull()?.takeIf { backStack.contains(DDayHomeNavKey) }?.let(::waitUntilCard)
    }

    private fun filterFixture(): Triple<Tag, DDay, DDay> {
        val tag = Arb.tag(title = TAG_TITLE).next()
        val tagged = dday(title = FIRST_TITLE)
        val untagged = dday(title = SECOND_TITLE)
        environment.tagList = listOf(tag)
        environment.ddayTagMap = mapOf(tagged.id to setOf(tag.id))
        return Triple(tag, tagged, untagged)
    }

    private fun toggleFilterTag(tag: Tag) {
        if (backStack.last() != DDayHomeFilterNavKey) {
            composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
                composeRule.onAllNodesWithText(tag.detail.title).fetchSemanticsNodes().isNotEmpty()
            }
        }
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
    }

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[0]

    private fun descriptionInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[1]

    private fun fillPlaceholder(fillEndDateAndTag: Boolean) {
        titleInput().performTextInput(PLACEHOLDER_TITLE)
        pickDate(currentText = TODAY_TEXT, date = PLACEHOLDER_BASE_DATE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_REPEAT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_FIRST_REPEAT_NUMBER_DESCRIPTION).performTextInput(PLACEHOLDER_REPEAT_NUMBER)
        composeRule.waitForIdle()
        if (fillEndDateAndTag) {
            val tag = Arb.tag(title = TAG_TITLE).next()
            environment.tagList = listOf(tag)
            pickDate(currentText = DEFAULT_END_DATE_NOT_SET, date = PLACEHOLDER_END_DATE)
            composeRule.runOnIdle { environment.addTagViewModelList.last().add(tagId = tag.id) }
            composeRule.waitForIdle()
            composeRule.onNodeWithText(TAG_TITLE).assertExists()
        }
        assertPlaceholderRetained(withEndDateAndTag = fillEndDateAndTag)
    }

    private fun assertPlaceholderRetained(withEndDateAndTag: Boolean) {
        assertAddOnDetailPane()
        titleInput().editableText() shouldBe PLACEHOLDER_TITLE
        composeRule.onNodeWithText(PLACEHOLDER_BASE_DATE_TEXT).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FIRST_REPEAT_NUMBER_DESCRIPTION).editableText() shouldBe PLACEHOLDER_REPEAT_NUMBER
        composeRule.onNodeWithText(DEFAULT_REPEAT_UNIT_DAY).assertExists()
        if (withEndDateAndTag) {
            composeRule.onNodeWithText(PLACEHOLDER_END_DATE_TEXT).assertExists()
            composeRule.onNodeWithText(TAG_TITLE).assertExists()
            environment.addTagViewModelList shouldHaveSize 1
        }
    }

    private fun assertAddInitial() {
        titleInput().editableText() shouldBe ""
        descriptionInput().editableText() shouldBe ""
        composeRule.onNodeWithText(TODAY_TEXT).assertExists()
        composeRule.onNode(hasText(DEFAULT_COUNTS_BASE_DAY_LABEL) and isToggleable()).assertIsOn()
        composeRule.onNodeWithContentDescription(DEFAULT_FIRST_REPEAT_NUMBER_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_END_DATE_NOT_SET).assertExists()
        composeRule.onNodeWithText(DEFAULT_OVERFLOW_CLAMP).assertExists()
        composeRule.onNodeWithText(TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    // 날짜 선택 창을 글자 입력으로 바꿔 날짜를 적고 확인한다.
    private fun pickDate(
        currentText: String,
        date: LocalDate,
    ) {
        composeRule.onNodeWithText(currentText).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SWITCH_TO_INPUT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextClearance()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextInput(date.toInputText())
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
    }

    private fun assertAddOnDetailPane() {
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
    }

    private fun assertDetailOnDetailPane(dday: DDay) {
        assertDetailOnDetailPane(title = dday.detail.title)
    }

    private fun assertDetailOnDetailPane(title: String) {
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        titleInput().editableText() shouldBe title
    }

    private fun selectDDay(dday: DDay) {
        waitUntilCard(dday)
        composeRule.onNode(listDetailCard(dday.detail.title)).performClick()
        composeRule.waitForIdle()
    }

    private fun openAddOnDetail(dday: DDay) {
        selectDDay(dday)
        assertDetailOnDetailPane(dday)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun waitUntilCard(dday: DDay) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(listDetailCard(dday.detail.title)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 목록 카드의 삭제 동작도 같은 이름이므로 상세 영역에 있는 삭제 버튼을 누른다.
    private fun clickDetailDelete() {
        val interactions = composeRule.onAllNodes(hasContentDescription(DEFAULT_DELETE_DESCRIPTION) and hasClickAction())
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailPaneLeft() }
        interactions[index].performClick()
        composeRule.waitForIdle()
    }

    // 카드는 목록 영역의 너비를 채우므로, 카드의 오른쪽 끝을 두 영역의 경계로 본다.
    private fun detailPaneLeft(): Float =
        composeRule
            .onAllNodes(hasTestTag(DDAY_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .maxOf { node -> node.boundsInRoot.right }

    private fun addActionLeftList(): List<Float> =
        composeRule
            .onAllNodesWithContentDescription(DEFAULT_ADD_DESCRIPTION)
            .fetchSemanticsNodes()
            .map { node -> node.boundsInRoot.left }

    private fun navigateUpRightList(): List<Float> =
        composeRule
            .onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION)
            .fetchSemanticsNodes()
            .map { node -> node.boundsInRoot.right }

    private fun cardBoundsList(titleList: List<String>): List<Rect> =
        titleList.map { title ->
            composeRule
                .onNode(listDetailCard(title))
                .fetchSemanticsNode()
                .boundsInRoot
        }

    // 상세 영역의 입력은 그 영역의 왼쪽 끝 가까이에서 시작하므로, 첫 입력의 왼쪽 끝보다 앞에서 끝나는 동작은 목록 영역에 있다.
    private infix fun Float.shouldBeLessThanDetailPane(expected: Boolean) {
        (this < titleInput().fetchSemanticsNode().boundsInRoot.left) shouldBe expected
    }

    private fun addActionRightList(): List<Float> =
        composeRule
            .onAllNodesWithContentDescription(DEFAULT_ADD_DESCRIPTION)
            .fetchSemanticsNodes()
            .map { node -> node.boundsInRoot.right }

    private fun singlePaneDirective(): PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 1)

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.editableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 3, day = 20)
        val PLACEHOLDER_BASE_DATE: LocalDate = LocalDate(year = 2025, month = 1, day = 15)
        val PLACEHOLDER_END_DATE: LocalDate = LocalDate(year = 2030, month = 1, day = 15)
        const val TODAY_TEXT = "Mar 20, 2026"
        const val TODAY_COUNT_TEXT = "D-Day"
        const val PLACEHOLDER_BASE_DATE_TEXT = "Jan 15, 2025"
        const val PLACEHOLDER_END_DATE_TEXT = "Jan 15, 2030"
        const val PLACEHOLDER_TITLE = "결혼"
        const val PLACEHOLDER_REPEAT_NUMBER = "100"
        const val LIST_TITLE = "list-dday"
        const val FIRST_TITLE = "A-$LIST_TITLE"
        const val SECOND_TITLE = "B-$LIST_TITLE"
        const val TAG_TITLE = "tag-a"
        const val ADDED_TITLE = "added-title"
        const val TYPED_ADD_TITLE = "typed-add-title"
        const val DEFAULT_ADD_TITLE = "Add D-Day"
        const val DEFAULT_ADD_DESCRIPTION = "Add D-Day"
        const val DEFAULT_UPDATE_DESCRIPTION = "Update D-Day"
        const val DEFAULT_DELETE_DESCRIPTION = "Delete D-Day"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter by tag"
        const val DEFAULT_COUNTS_BASE_DAY_LABEL = "Count start date as day 1"
        const val DEFAULT_END_DATE_NOT_SET = "Not set"
        const val DEFAULT_OVERFLOW_CLAMP = "Use last day of month"
        const val DEFAULT_ADD_REPEAT_DESCRIPTION = "Add interval"
        const val DEFAULT_FIRST_REPEAT_NUMBER_DESCRIPTION = "Interval 1 number"
        const val DEFAULT_REPEAT_UNIT_DAY = "Days"
        const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
        const val DEFAULT_SWITCH_TO_INPUT_DESCRIPTION = "Switch to text input mode"
        const val DEFAULT_CONFIRM = "OK"
        const val TIMEOUT_MILLIS = 5_000L

        val ddaySavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { dDayNavKeys() }
                    }
            }

        fun dday(
            title: String,
            baseDate: LocalDate = TODAY,
        ): DDay = listDetailDDay(title = title, baseDate = baseDate)

        fun LocalDate.toInputText(): String = "${month.ordinal.plus(1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}$year"
    }
}
