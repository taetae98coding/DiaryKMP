package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.ROUTINE_RECORD_ROW_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 입력과 기록 내역이 한 화면에 모두 놓이도록 세로로 긴 창에서 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailRecordScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        routineRecordSetToday(TODAY)
    }

    @After
    fun tearDown() {
        routineRecordReleaseToday()
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 회인 루틴의 기록은 날짜와 1500회로 표시한다`() {
        assertKoreanRecordText(unit = "회", amountText = "1500회")
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 ml인 루틴의 기록은 날짜와 1500ml로 표시한다`() {
        assertKoreanRecordText(unit = "ml", amountText = AMOUNT_1500_ML_TEXT)
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 m인 루틴의 기록은 날짜와 1500m로 표시한다`() {
        assertKoreanRecordText(unit = "m", amountText = AMOUNT_1500_M_TEXT)
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 쪽인 루틴의 기록은 날짜와 1500쪽으로 표시한다`() {
        assertKoreanRecordText(unit = "쪽", amountText = "1500쪽")
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 ML인 루틴의 기록은 날짜와 1500ML로 표시한다`() {
        assertKoreanRecordText(unit = "ML", amountText = "1500ML")
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-c90234bb-fb36-4bbd-b52c-07b70c25a6f3 단위가 비어 있는 루틴의 기록은 날짜와 1500으로 표시한다`() {
        assertKoreanRecordText(unit = "", amountText = "1500")
    }

    private fun assertKoreanRecordText(
        unit: String,
        amountText: String,
    ) {
        val environment =
            RoutineRecordTestEnvironment(
                detail = routineRecordDetailOf(unit = unit),
                recordList = listOf(routineRecordOf(date = LocalDate(2026, 3, 5), amount = 1500)),
            )
        setScreen(environment)

        composeRule
            .onNode(hasText(KOREAN_MARCH_5) and hasText(amountText) and hasAnyAncestor(hasTestTag(ROUTINE_RECORD_ROW_TEST_TAG)))
            .assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w480dp-h891dp")
    fun `TC-ce51be69-6ca3-40dd-b31f-0892dd763b73 기록 내역은 앞부분 20개부터 나타나고 끝에 다가가면 다음 기록이 이어진다`() {
        val recordList = List(PAGED_RECORD_COUNT) { index -> routineRecordOf(date = PAGED_LAST_DATE.minus(index, DateTimeUnit.DAY), amount = index + 1) }
        val environment = RoutineRecordTestEnvironment()
        // 기기에 저장된 기록을 날짜가 늦은 순으로 한 번에 PAGE_SIZE씩 나누어 불러오는 목록을 실제 페이지 조회로 구성한다.
        every { environment.recordViewModel.recordPagingData } returns
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = 1,
                        enablePlaceholders = false,
                        initialLoadSize = PAGE_SIZE,
                    ),
                pagingSourceFactory = recordList.asPagingSourceFactory(),
            ).flow
        setScreen(environment)
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) { contentItemCount() == HEADER_ITEM_COUNT + PAGE_SIZE }
        val itemCountList = mutableListOf(contentItemCount())

        // 목록의 끝에 다가가면 다음 기록이 이어져 끝이 다시 밀리므로, 사용자가 끝에 닿을 때까지 이어서 내리는 것처럼 끝으로 다시 이동한다.
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) {
            composeRule.routineRecordContentList().performScrollToIndex(contentItemCount() - 1)
            composeRule.waitForIdle()
            if (contentItemCount() != itemCountList.last()) itemCountList += contentItemCount()
            contentItemCount() == HEADER_ITEM_COUNT + PAGED_RECORD_COUNT
        }

        itemCountList shouldBe listOf(HEADER_ITEM_COUNT + PAGE_SIZE, HEADER_ITEM_COUNT + PAGE_SIZE * 2, HEADER_ITEM_COUNT + PAGED_RECORD_COUNT)
        recordList.forEachIndexed { index, record ->
            composeRule.routineRecordScrollToRecord(index)

            composeRule.onNode(routineRecordRow(date = record.date, amountText = "${record.amount}ml")).assertIsDisplayed()
        }
    }

    @Test
    fun `TC-809e2751-3547-4f99-8780-f7dfd8793316 기록이 없으면 빈 상태 안내를 표시하고 다른 동작은 그대로 쓸 수 있다`() {
        val tag =
            Arb.tag().next().let { value ->
                value.copy(detail = value.detail.copy(emoji = "", title = TAG_TITLE), isFinished = false, isDeleted = false)
            }
        val environment = RoutineRecordTestEnvironment(selectableTagList = listOf(tag))
        setScreen(environment)

        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_EMPTY_TITLE).assertIsDisplayed()
        composeRule.routineRecordRowNameList().shouldBeEmpty()

        composeRule.routineRecordClickAdd()
        verify(exactly = 1) { environment.recordViewModel.add(date = TODAY, amount = environment.detail.amountPerRecord) }

        composeRule.routineRecordOpenTagPicker()
        composeRule.routineRecordAwaitText(text = TAG_TITLE)
        composeRule.onNodeWithText(TAG_TITLE).performClick()
        composeRule.waitForIdle()
        verify(exactly = 1) { environment.tagViewModel.add(tagId = tag.id) }

        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()
        composeRule.routineRecordClickUpdate()
        verify(exactly = 1) { environment.routineViewModel.update(input = any()) }

        composeRule.routineRecordClickDelete()
        verify(exactly = 1) { environment.routineViewModel.delete() }
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `TC-809e2751-3547-4f99-8780-f7dfd8793316 기록이 없으면 아직 기록이 없습니다 안내를 표시한다`() {
        setScreen(RoutineRecordTestEnvironment())

        composeRule.onNodeWithText(ROUTINE_RECORD_KOREAN_EMPTY_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-fe03a63a-b504-4762-9cbb-7b25c4af5e2a 기록 내역을 불러오는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        val environment = RoutineRecordTestEnvironment()
        environment.recordPagingData.value = routineRecordLoadingPagingData()
        setScreen(environment)

        composeRule.onNode(routineRecordAddButton()).assertIsDisplayed()
        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-615c8fe9-5ec2-4def-978c-2159397dbc93 기록 내역을 조회하지 못하면 빈 상태 안내만 표시한다`() {
        val environment = RoutineRecordTestEnvironment()
        val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
        every { pageRoutineRecordUseCase(parameter = environment.id) } returns flowOf(Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE)))
        val recordViewModel = recordViewModel(environment = environment, pageRoutineRecordUseCase = pageRoutineRecordUseCase)
        setScreen(environment = environment, recordViewModel = recordViewModel)
        composeRule.routineRecordAwaitText(text = ROUTINE_RECORD_DEFAULT_EMPTY_TITLE)

        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_EMPTY_TITLE).assertIsDisplayed()
        composeRule.routineRecordRowNameList().shouldBeEmpty()
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
        composeRule.onNode(hasText(RETRY, substring = true, ignoreCase = true) or hasContentDescription(RETRY, substring = true, ignoreCase = true)).assertDoesNotExist()
    }

    @Test
    fun `TC-02ff65d7-823e-4877-9baf-d0fd5bbbc780 기록을 선택해도 아무 일도 일어나지 않는다`() {
        val environment = RoutineRecordTestEnvironment(recordList = listOf(routineRecordOf(date = MARCH_10, amount = 300)))
        setScreen(environment)
        val titleText = composeRule.routineRecordTitleInput().routineRecordEditableText()

        composeRule.onNode(routineRecordRow(date = MARCH_10, amountText = AMOUNT_300_ML_TEXT)).assert(!hasClickAction()).performClick()
        composeRule.waitForIdle()

        environment.navigateUpCount shouldBe 0
        environment.navigatedTagIdList.shouldBeEmpty()
        verify(exactly = 0) { environment.recordViewModel.add(date = any(), amount = any()) }
        verify(exactly = 0) { environment.recordViewModel.delete(recordId = any()) }
        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_300_ML_TEXT))
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe titleText
        composeRule.onNode(routineRecordUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-e5a3bb58-f07a-4742-938b-8ef525e4d717 진행 중 루틴의 기록 넣기는 오늘과 한 번에 기록할 양으로 시작한다`() {
        assertInitialRecordInput(startDate = LocalDate(2026, 3, 1), expected = TODAY)
    }

    @Test
    fun `TC-e5a3bb58-f07a-4742-938b-8ef525e4d717 오늘 시작하는 진행 중 루틴의 기록 넣기는 오늘로 시작한다`() {
        assertInitialRecordInput(startDate = TODAY, expected = TODAY)
    }

    @Test
    fun `TC-e5a3bb58-f07a-4742-938b-8ef525e4d717 시작 전 루틴의 기록 넣기는 시작 날짜로 시작한다`() {
        assertInitialRecordInput(startDate = LocalDate(2026, 4, 1), expected = LocalDate(2026, 4, 1))
    }

    @Test
    fun `TC-fd2fa509-b105-4cd5-b0c5-c60563db6eb4 완료된 진행 중 루틴의 기록 넣기도 오늘과 한 번에 기록할 양으로 시작한다`() {
        assertInitialRecordInput(startDate = LocalDate(2026, 3, 1), expected = TODAY, isFinished = true)
    }

    @Test
    fun `TC-fd2fa509-b105-4cd5-b0c5-c60563db6eb4 완료된 시작 전 루틴의 기록 넣기도 시작 날짜로 시작한다`() {
        assertInitialRecordInput(startDate = LocalDate(2026, 4, 1), expected = LocalDate(2026, 4, 1), isFinished = true)
    }

    private fun assertInitialRecordInput(
        startDate: LocalDate,
        expected: LocalDate,
        isFinished: Boolean = false,
    ) {
        setScreen(RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = startDate, amountPerRecord = 250), isFinished = isFinished))

        composeRule.onNode(routineRecordDateButton(expected)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
    }

    @Test
    fun `TC-c3d457c6-cc8f-40a2-8bfb-d5a4fe319d39 기록 날짜 고르기는 기록할 날짜에서 시작하고 확인하면 고른 날짜가 남는다`() {
        setScreen(RoutineRecordTestEnvironment())

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerSelection(TODAY).assertExists()
        composeRule.routineRecordPickerDayCell(MARCH_10).performClick()
        composeRule.waitForIdle()
        composeRule.routineRecordConfirmDatePicker()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
    }

    @Test
    fun `TC-c3d457c6-cc8f-40a2-8bfb-d5a4fe319d39 기록 날짜 고르기는 기록할 날짜에서 시작하고 취소하면 고르기 전 날짜가 남는다`() {
        setScreen(RoutineRecordTestEnvironment())

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerSelection(TODAY).assertExists()
        composeRule.routineRecordPickerDayCell(MARCH_10).performClick()
        composeRule.waitForIdle()
        composeRule.routineRecordCancelDatePicker()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
    }

    @Test
    fun `TC-8c5dd64b-098a-49a4-8c9e-27c1575bfefb 기록 날짜는 저장된 시작 날짜부터 오늘보다 뒤의 날짜까지 고를 수 있다`() {
        setScreen(RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1))))

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 1)).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(TODAY).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 25)).assertIsEnabled()
        composeRule.routineRecordCancelDatePicker()

        assertNotPickable(date = LocalDate(2026, 2, 28), current = TODAY)
        assertPickable(date = LocalDate(2026, 3, 1))
        assertPickable(date = TODAY)
        assertPickable(date = LocalDate(2026, 3, 25))
        assertPickable(date = LocalDate(2030, 12, 31))
    }

    @Test
    fun `TC-bb4ede54-503e-4b4a-bdff-9bbe70adba24 평일만 루틴의 기록 날짜는 하는 요일이 아니어도 시작 날짜부터 고를 수 있다`() {
        setScreen(RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycle.WEEKDAYS)))

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 7)).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 8)).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 21)).assertIsEnabled()
        composeRule.routineRecordCancelDatePicker()

        assertNotPickable(date = LocalDate(2026, 2, 27), current = TODAY)
        assertPickable(date = LocalDate(2026, 3, 7))
        assertPickable(date = LocalDate(2026, 3, 8))
        assertPickable(date = LocalDate(2026, 3, 21))
    }

    @Test
    fun `TC-bb4ede54-503e-4b4a-bdff-9bbe70adba24 주말만 루틴의 기록 날짜는 하는 요일이 아니어도 시작 날짜부터 고를 수 있다`() {
        setScreen(RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycle.WEEKENDS)))

        // 하는 요일이 아닌 오늘 2026-03-20(금)이 처음부터 기록할 날짜로 골라져 있다.
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 9)).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(TODAY).assertIsEnabled()
        composeRule.routineRecordCancelDatePicker()

        assertNotPickable(date = LocalDate(2026, 2, 28), current = TODAY)
        assertPickable(date = LocalDate(2026, 3, 9))
        assertPickable(date = TODAY)
    }

    @Test
    fun `TC-add8a0a3-95fd-4222-91c4-b228512ba867 반영하지 않고 앞으로 고친 시작 날짜는 기록할 날짜의 범위에 쓰지 않는다`() {
        assertUnappliedStartDateIgnored(editedStartDate = LocalDate(2026, 3, 1))
    }

    @Test
    fun `TC-add8a0a3-95fd-4222-91c4-b228512ba867 반영하지 않고 뒤로 고친 시작 날짜는 기록할 날짜의 범위에 쓰지 않는다`() {
        assertUnappliedStartDateIgnored(editedStartDate = LocalDate(2026, 3, 15))
    }

    private fun assertUnappliedStartDateIgnored(editedStartDate: LocalDate) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = MARCH_10))
        setScreen(environment)
        composeRule.routineRecordPickFormDate(from = MARCH_10, to = editedStartDate)
        composeRule.routineRecordFormDateButton(editedStartDate).assertExists()
        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 5)).assertIsNotEnabled()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 12)).assertIsEnabled()
        composeRule.routineRecordCancelDatePicker()

        assertNotPickable(date = LocalDate(2026, 3, 5), current = TODAY)
        assertPickable(date = LocalDate(2026, 3, 12))
    }

    @Test
    fun `TC-d76022c5-e498-43d6-a24c-2b4bf77c2feb 기록할 날짜는 지울 수 없다`() {
        setScreen(RoutineRecordTestEnvironment())

        // 화면의 지우기는 모두 글자 입력의 것이고 날짜를 지우는 동작은 없다.
        composeRule
            .onAllNodes(hasContentDescription(CLEAR, substring = true, ignoreCase = true))
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.ContentDescription].first() }
            .filterNot { description -> description == CLEAR_TEXT }
            .shouldBeEmpty()

        composeRule.routineRecordOpenDatePicker()

        composeRule
            .onAllNodes(hasAnyAncestor(isDialog()) and (hasText(CLEAR, substring = true, ignoreCase = true) or hasContentDescription(CLEAR, substring = true, ignoreCase = true)))
            .assertCountEquals(0)
        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_DATE_PICKER_CONFIRM).assertExists()
        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_DATE_PICKER_CANCEL).assertExists()
    }

    @Test
    fun `TC-a086b641-7897-4ce3-9022-29eedf496272 기록의 양에는 반각 숫자만 일곱 자리까지 반영한다`() {
        setScreen(RoutineRecordTestEnvironment())

        listOf(
            Triple("", "0500", "0500"),
            Triple("12", "a", "12"),
            Triple("12", "３", "12"),
            Triple(SEVEN_DIGIT_TEXT, "8", SEVEN_DIGIT_TEXT),
            Triple("", "123456789", SEVEN_DIGIT_TEXT),
            Triple("12", "3a4", "1234"),
        ).forEach { (before, input, expected) ->
            composeRule.routineRecordAmountInput().performTextClearance()
            if (before.isNotEmpty()) composeRule.routineRecordAmountInput().performTextInput(before)
            composeRule.waitForIdle()
            composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe before

            composeRule.routineRecordAmountInput().performTextInput(input)
            composeRule.waitForIdle()

            composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe expected
        }
    }

    @Test
    fun `TC-3dde44f7-d0d9-4fcf-b0dc-ccfd95b8b7f5 기록의 양이 비어 있거나 0이면 기록 넣기를 제공하지 않는다`() {
        val environment = RoutineRecordTestEnvironment()
        setScreen(environment)

        listOf("", "0", "000").forEach { amount ->
            composeRule.routineRecordAmountInput().performTextClearance()
            if (amount.isNotEmpty()) composeRule.routineRecordAmountInput().performTextInput(amount)
            composeRule.waitForIdle()

            composeRule.onNode(routineRecordAddButton()).assertIsNotEnabled()
        }
        verify(exactly = 0) { environment.recordViewModel.add(date = any(), amount = any()) }

        listOf("1" to 1, "0500" to 500).forEach { (amount, expected) ->
            composeRule.routineRecordInputAmount(amount)

            composeRule.onNode(routineRecordAddButton()).assertIsEnabled()
            composeRule.routineRecordClickAdd()
            verify(exactly = 1) { environment.recordViewModel.add(date = TODAY, amount = expected) }
        }
    }

    @Test
    fun `TC-2b467619-0cc1-48f7-af73-e89304f9f6f6 기록을 넣으면 기록 내역에 나타나고 날짜는 그대로, 양은 처음 값으로 돌아간다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(unit = "ml", amountPerRecord = 250))
        environment.storeOnAdd()
        setScreen(environment)
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordAmountInput().performClick()
        composeRule.routineRecordInputAmount("500")
        composeRule.routineRecordAmountInput().assertIsFocused()

        composeRule.onNode(routineRecordAddButton()).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.recordViewModel.add(date = MARCH_10, amount = 500) }
        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_500_ML_TEXT))
        composeRule.onNode(routineRecordRow(date = MARCH_10, amountText = AMOUNT_500_ML_TEXT)).assertIsDisplayed()
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.routineRecordAmountInput().assertIsFocused()
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
    }

    @Test
    fun `TC-361b9531-05df-4eb6-beb5-1b16d73e35b7 반영하지 않은 한 번에 기록할 양은 넣은 뒤 되돌리는 양에 쓰지 않는다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(amountPerRecord = 250))
        environment.storeOnAdd()
        setScreen(environment)
        composeRule.routineRecordAmountPerRecordInput().performTextReplacement("300")
        composeRule.waitForIdle()

        assertAmountAfterAdd(expected = "250")
        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }
    }

    @Test
    fun `TC-361b9531-05df-4eb6-beb5-1b16d73e35b7 수정을 반영한 한 번에 기록할 양으로 넣은 뒤의 양을 되돌린다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(amountPerRecord = 250))
        environment.storeOnAdd()
        environment.storeOnUpdate()
        setScreen(environment)
        composeRule.routineRecordAmountPerRecordInput().performTextReplacement("300")
        composeRule.waitForIdle()
        composeRule.routineRecordClickUpdate()
        environment.currentDetail.amountPerRecord shouldBe 300

        assertAmountAfterAdd(expected = "300")
    }

    @Test
    fun `TC-361b9531-05df-4eb6-beb5-1b16d73e35b7 다른 경로에서 바뀐 한 번에 기록할 양으로 넣은 뒤의 양을 되돌린다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(amountPerRecord = 250))
        environment.storeOnAdd()
        setScreen(environment)
        composeRule.runOnIdle { environment.updateDetail { detail -> detail.copy(amountPerRecord = 400) } }
        composeRule.waitForIdle()

        assertAmountAfterAdd(expected = "400")
    }

    private fun assertAmountAfterAdd(expected: String) {
        composeRule.routineRecordInputAmount("500")

        composeRule.routineRecordClickAdd()

        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = TODAY, amountText = AMOUNT_500_ML_TEXT))
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe expected
    }

    @Test
    fun `TC-c913a811-72d3-4dd5-88b2-7d1bd63e17c6 같은 날짜에 같은 양의 기록이 있어도 새 기록으로 넣는다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(amountPerRecord = 250))
        environment.setRecordList(listOf(routineRecordOf(date = MARCH_10, amount = 250, routineId = environment.id)))
        environment.storeOnAdd()
        setScreen(environment)
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("250")

        composeRule.routineRecordClickAdd()

        verify(exactly = 1) { environment.recordViewModel.add(date = MARCH_10, amount = 250) }
        composeRule.routineRecordRowNameList() shouldBe List(2) { routineRecordRowName(date = MARCH_10, amountText = "250ml") }
    }

    @Test
    fun `TC-91fbaabb-32ea-4348-a407-2d25eeea989c 기록 넣기를 처리하는 동안 진행을 표시하고 날짜와 양, 다른 동작은 그대로 쓴다`() {
        val environment = RoutineRecordTestEnvironment()
        val result = CompletableDeferred<Result<Uuid>>()
        val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
        coEvery { addRoutineRecordUseCase(parameter = any()) } coAnswers { result.await() }
        val recordViewModel = recordViewModel(environment = environment, addRoutineRecordUseCase = addRoutineRecordUseCase)
        setScreen(environment = environment, recordViewModel = recordViewModel)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()

        composeRule.routineRecordClickAdd()
        composeRule.onNode(routineRecordAddButton() and routineRecordProgress()).assertExists()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("700")
        composeRule.routineRecordClickAdd()
        composeRule.routineRecordClickUpdate()

        composeRule.onNode(routineRecordAddButton() and routineRecordProgress()).assertExists()
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "700"
        coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }
        coVerify(exactly = 1) {
            addRoutineRecordUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = environment.id, date = TODAY, amount = environment.detail.amountPerRecord))
        }
        verify(exactly = 1) { environment.routineViewModel.update(input = any()) }

        composeRule.runOnIdle { result.complete(Result.success(Arb.kotlinUuid().next())) }
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(routineRecordAddButton() and routineRecordProgress()).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNode(routineRecordAddButton() and routineRecordProgress()).assertDoesNotExist()
    }

    @Test
    fun `TC-ec9f33e1-5294-4a84-a408-09bd17f5f7c1 기록 넣기를 저장하지 못하면 안내 없이 고른 날짜와 입력한 양을 그대로 둔다`() {
        val environment = RoutineRecordTestEnvironment()
        val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
        coEvery { addRoutineRecordUseCase(parameter = any()) } returns Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
        val recordViewModel = recordViewModel(environment = environment, addRoutineRecordUseCase = addRoutineRecordUseCase)
        setScreen(environment = environment, recordViewModel = recordViewModel)
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")

        composeRule.routineRecordClickAdd()

        val parameter = AddRoutineRecordUseCase.Parameter(routineId = environment.id, date = MARCH_10, amount = 500)
        coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = parameter) }
        composeRule.routineRecordRowNameList().shouldBeEmpty()
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "500"
        composeRule.onNode(routineRecordAddButton() and routineRecordProgress()).assertDoesNotExist()
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.routineRecordClickAdd()

        coVerify(exactly = 2) { addRoutineRecordUseCase(parameter = parameter) }
    }

    @Test
    fun `TC-0299a25e-1a9f-4315-b927-ec9c5e79a69c 기록을 빼면 확인 없이 기록 내역에서 사라지고 남은 기록의 순서는 그대로다`() {
        val environment = RoutineRecordTestEnvironment()
        val removed = routineRecordOf(date = MARCH_10, amount = 200, routineId = environment.id)
        environment.setRecordList(
            listOf(
                routineRecordOf(date = TODAY, amount = 100, routineId = environment.id),
                removed,
                routineRecordOf(date = LocalDate(2026, 3, 5), amount = 300, routineId = environment.id),
            ),
        )
        environment.storeOnDelete()
        setScreen(environment)
        composeRule.routineRecordRowNameList() shouldBe
            listOf(
                routineRecordRowName(date = TODAY, amountText = AMOUNT_100_ML_TEXT),
                routineRecordRowName(date = MARCH_10, amountText = AMOUNT_200_ML_TEXT),
                routineRecordRowName(date = LocalDate(2026, 3, 5), amountText = AMOUNT_300_ML_TEXT),
            )

        composeRule.onNode(routineRecordRemoveButton(date = MARCH_10, amountText = AMOUNT_200_ML_TEXT)).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        verify(exactly = 1) { environment.recordViewModel.delete(recordId = removed.id) }
        verify(exactly = 1) { environment.recordViewModel.delete(recordId = any()) }
        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }
        composeRule.routineRecordRowNameList() shouldBe
            listOf(
                routineRecordRowName(date = TODAY, amountText = AMOUNT_100_ML_TEXT),
                routineRecordRowName(date = LocalDate(2026, 3, 5), amountText = AMOUNT_300_ML_TEXT),
            )
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
        composeRule.onAllNodesWithText(UNDO, substring = true, ignoreCase = true).assertCountEquals(0)
    }

    @Test
    fun `TC-51808f86-30a0-4170-8c4f-6e18952a728f 시작 날짜보다 앞에 놓인 기록도 뺄 수 있다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = MARCH_10))
        environment.setRecordList(
            listOf(
                routineRecordOf(date = LocalDate(2026, 3, 5), amount = 100, routineId = environment.id),
                routineRecordOf(date = LocalDate(2026, 3, 15), amount = 200, routineId = environment.id),
            ),
        )
        environment.storeOnDelete()
        setScreen(environment)

        composeRule.routineRecordClickRemove(date = LocalDate(2026, 3, 5), amountText = AMOUNT_100_ML_TEXT)

        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = LocalDate(2026, 3, 15), amountText = AMOUNT_200_ML_TEXT))
    }

    @Test
    fun `TC-47d2739f-e2df-4839-9625-19f106877989 기록 빼기를 저장하지 못하면 안내 없이 그 기록이 남는다`() {
        val environment = RoutineRecordTestEnvironment()
        val record = routineRecordOf(date = MARCH_10, amount = 200, routineId = environment.id)
        val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
        coEvery { deleteRoutineRecordUseCase(parameter = record.id) } returns Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
        val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
        every { pageRoutineRecordUseCase(parameter = environment.id) } returns flowOf(Result.success(routineRecordLoadedPagingDataOf(listOf(record))))
        val recordViewModel =
            recordViewModel(
                environment = environment,
                deleteRoutineRecordUseCase = deleteRoutineRecordUseCase,
                pageRoutineRecordUseCase = pageRoutineRecordUseCase,
            )
        setScreen(environment = environment, recordViewModel = recordViewModel)
        composeRule.routineRecordAwaitText(text = routineRecordDisplayText(MARCH_10))

        composeRule.routineRecordClickRemove(date = MARCH_10, amountText = AMOUNT_200_ML_TEXT)

        coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = record.id) }
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_200_ML_TEXT))
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.routineRecordClickRemove(date = MARCH_10, amountText = AMOUNT_200_ML_TEXT)

        coVerify(exactly = 2) { deleteRoutineRecordUseCase(parameter = record.id) }
    }

    @Test
    fun `TC-6e0fa583-6f8f-49d5-8656-62f50340a37e 다른 경로에서 저장된 단위가 바뀌면 기록의 양의 문구가 바뀐 단위로 바뀐다`() {
        val environment = unitChangeEnvironment()
        setScreen(environment)
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_1500_ML_TEXT))

        composeRule.runOnIdle { environment.updateDetail { detail -> detail.copy(unit = "m") } }
        composeRule.waitForIdle()

        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_1500_M_TEXT))
        composeRule.onNode(routineRecordRow(date = MARCH_10, amountText = AMOUNT_1500_M_TEXT)).assertIsDisplayed()
    }

    @Test
    fun `TC-6e0fa583-6f8f-49d5-8656-62f50340a37e 이 화면에서 단위 수정을 반영하면 기록의 양의 문구가 바뀐 단위로 바뀐다`() {
        val environment = unitChangeEnvironment()
        environment.storeOnUpdate()
        setScreen(environment)

        composeRule.routineInputUnit(unit = ROUTINE_RECORD_UNIT_METER)
        // 수정을 반영하기 전에는 기록이 저장된 단위로 읽힌다.
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_1500_ML_TEXT))
        composeRule.routineRecordClickUpdate()

        environment.currentDetail.unit shouldBe "m"
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = AMOUNT_1500_M_TEXT))
        composeRule.onNode(routineRecordRow(date = MARCH_10, amountText = AMOUNT_1500_M_TEXT)).assertIsDisplayed()
    }

    private fun unitChangeEnvironment(): RoutineRecordTestEnvironment =
        RoutineRecordTestEnvironment(
            detail = routineRecordDetailOf(unit = "ml"),
            recordList = listOf(routineRecordOf(date = MARCH_10, amount = 1500)),
        )

    @Test
    fun `TC-ce9cddd7-0f11-4623-8af9-d23f1190a1ec 다른 경로에서 시작 날짜가 뒤로 바뀌면 앞선 기록할 날짜를 시작 날짜로 맞춘다`() {
        assertRecordDateCoercedExternally(picked = LocalDate(2026, 3, 5), expected = MARCH_10)
    }

    @Test
    fun `TC-ce9cddd7-0f11-4623-8af9-d23f1190a1ec 다른 경로에서 시작 날짜가 기록할 날짜와 같아지면 기록할 날짜는 그대로다`() {
        assertRecordDateCoercedExternally(picked = MARCH_10, expected = MARCH_10)
    }

    @Test
    fun `TC-ce9cddd7-0f11-4623-8af9-d23f1190a1ec 다른 경로에서 시작 날짜가 바뀌어도 그 뒤의 기록할 날짜는 그대로다`() {
        assertRecordDateCoercedExternally(picked = LocalDate(2026, 3, 25), expected = LocalDate(2026, 3, 25))
    }

    private fun assertRecordDateCoercedExternally(
        picked: LocalDate,
        expected: LocalDate,
    ) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1)))
        setScreen(environment)
        composeRule.routineRecordPickDate(picked)
        composeRule.onNode(routineRecordDateButton(picked)).assertExists()

        composeRule.runOnIdle { environment.updateDetail { detail -> detail.copy(startDate = MARCH_10) } }
        composeRule.waitForIdle()

        composeRule.onNode(routineRecordDateButton(expected)).assertExists()
        assertPickerStartsAt(startDate = MARCH_10)
    }

    @Test
    fun `TC-ce9cddd7-0f11-4623-8af9-d23f1190a1ec 이 화면에서 시작 날짜 수정을 반영하면 앞선 기록할 날짜를 시작 날짜로 맞춘다`() {
        assertRecordDateCoercedByUpdate(picked = LocalDate(2026, 3, 5), expected = MARCH_10)
    }

    @Test
    fun `TC-ce9cddd7-0f11-4623-8af9-d23f1190a1ec 이 화면에서 시작 날짜 수정을 반영해도 그 뒤의 기록할 날짜는 그대로다`() {
        assertRecordDateCoercedByUpdate(picked = LocalDate(2026, 3, 25), expected = LocalDate(2026, 3, 25))
    }

    private fun assertRecordDateCoercedByUpdate(
        picked: LocalDate,
        expected: LocalDate,
    ) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1)))
        environment.storeOnUpdate()
        setScreen(environment)
        composeRule.routineRecordPickDate(picked)
        composeRule.routineRecordPickFormDate(from = LocalDate(2026, 3, 1), to = MARCH_10)
        // 수정을 반영하기 전에는 고른 날짜를 그대로 둔다.
        composeRule.onNode(routineRecordDateButton(picked)).assertExists()

        composeRule.routineRecordClickUpdate()

        environment.currentDetail.startDate shouldBe MARCH_10
        composeRule.onNode(routineRecordDateButton(expected)).assertExists()
        assertPickerStartsAt(startDate = MARCH_10)
    }

    // 바뀐 시작 날짜는 2026년 3월 안에 있으므로, 고르기가 보여 주는 3월 달력에서 범위의 경계를 확인한다.
    private fun assertPickerStartsAt(startDate: LocalDate) {
        composeRule.routineRecordOpenDatePicker()

        composeRule.routineRecordPickerDayCell(startDate).assertIsEnabled()
        composeRule.routineRecordPickerDayCell(startDate.minus(1, DateTimeUnit.DAY)).assertIsNotEnabled()
        composeRule.routineRecordPickerDayCell(LocalDate(2026, 3, 31)).assertIsEnabled()
        composeRule.routineRecordCancelDatePicker()
    }

    @Test
    fun `TC-3183f2df-8553-4291-9ebe-c6067fd9869d 입력 내용과 기록 넣기의 날짜와 양은 처음 조회한 값으로 한 번만 채운다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(goalAmount = 2000, amountPerRecord = 250))
        setScreen(environment)
        composeRule.onNode(routineRecordUpdateButton()).assertDoesNotExist()

        composeRule.runOnIdle {
            environment.updateDetail { detail -> detail.copy(description = EXTERNAL_DESCRIPTION, goalAmount = 3000, amountPerRecord = 400) }
        }
        composeRule.waitForIdle()

        composeRule.routineRecordDescriptionInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_DESCRIPTION
        composeRule.routineRecordGoalInput().routineRecordEditableText() shouldBe "2000"
        composeRule.routineRecordAmountPerRecordInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordUpdateButton()).assertExists()
    }

    @Test
    fun `TC-7d82228c-a2f2-499d-9a12-ce5616c0df8f 내용 수정을 처리하는 동안에도 기록할 날짜를 고를 수 있다`() {
        assertRecordDatePickableWhile { environment ->
            environment.uiState.value = RoutineDetailUiState.Content(id = environment.id, detail = environment.detail, isFinished = false, isUpdateInProgress = true)
        }
    }

    @Test
    fun `TC-7d82228c-a2f2-499d-9a12-ce5616c0df8f 삭제를 처리하는 동안에도 기록할 날짜를 고를 수 있다`() {
        assertRecordDatePickableWhile { environment ->
            environment.uiState.value = RoutineDetailUiState.Content(id = environment.id, detail = environment.detail, isFinished = false, isDeleteInProgress = true)
        }
    }

    @Test
    fun `TC-7d82228c-a2f2-499d-9a12-ce5616c0df8f 기록 넣기를 처리하는 동안에도 기록할 날짜를 고를 수 있다`() {
        assertRecordDatePickableWhile { environment ->
            environment.recordUiState.value = RoutineDetailRecordUiState(isAddInProgress = true)
        }
    }

    @Test
    fun `TC-7d82228c-a2f2-499d-9a12-ce5616c0df8f 기록 빼기를 처리하는 동안에도 기록할 날짜를 고를 수 있다`() {
        // 빼기 요청에 답하지 않는 대역은 저장이 끝나지 않은 처리 중 상태다.
        assertRecordDatePickableWhile { environment ->
            composeRule.routineRecordClickRemove(date = LocalDate(2026, 3, 5), amountText = AMOUNT_100_ML_TEXT)
            verify(exactly = 1) { environment.recordViewModel.delete(recordId = any()) }
        }
    }

    private fun assertRecordDatePickableWhile(startProcessing: (RoutineRecordTestEnvironment) -> Unit) {
        val environment = RoutineRecordTestEnvironment(recordList = listOf(routineRecordOf(date = LocalDate(2026, 3, 5), amount = 100)))
        setScreen(environment)
        startProcessing(environment)
        composeRule.waitForIdle()

        composeRule.routineRecordOpenDatePicker()
        composeRule.routineRecordPickerDayCell(MARCH_10).performClick()
        composeRule.waitForIdle()
        composeRule.routineRecordConfirmDatePicker()

        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
    }

    private fun assertPickable(date: LocalDate) {
        composeRule.routineRecordPickDate(date)

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(routineRecordDateButton(date)).assertExists()
    }

    // 고를 수 없는 날짜는 직접 입력해도 선택되지 않아 확인해도 기록할 날짜가 바뀌지 않는다.
    private fun assertNotPickable(
        date: LocalDate,
        current: LocalDate,
    ) {
        composeRule.routineRecordPickDate(date)
        if (composeRule.onAllNodes(isDialog()).fetchSemanticsNodes().isNotEmpty()) composeRule.routineRecordCancelDatePicker()

        composeRule.onNode(routineRecordDateButton(current)).assertExists()
        composeRule.onNode(routineRecordDateButton(date)).assertDoesNotExist()
    }

    private fun contentItemCount(): Int =
        composeRule
            .routineRecordContentList()
            .fetchSemanticsNode()
            .config[SemanticsProperties.CollectionInfo]
            .rowCount

    private fun recordViewModel(
        environment: RoutineRecordTestEnvironment,
        addRoutineRecordUseCase: AddRoutineRecordUseCase = mockk(),
        deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase = mockk(),
        pageRoutineRecordUseCase: PageRoutineRecordUseCase = loadedEmptyPageRoutineRecordUseCase(),
    ): RoutineDetailRecordViewModel =
        RoutineDetailRecordViewModel(
            routineId = environment.id,
            addRoutineRecordUseCase = addRoutineRecordUseCase,
            deleteRoutineRecordUseCase = deleteRoutineRecordUseCase,
            pageRoutineRecordUseCase = pageRoutineRecordUseCase,
        )

    private fun loadedEmptyPageRoutineRecordUseCase(): PageRoutineRecordUseCase {
        val pagingData: PagingData<RoutineRecord> = routineRecordLoadedPagingDataOf(emptyList())

        return mockk<PageRoutineRecordUseCase>().apply {
            every { this@apply(parameter = any()) } returns flowOf(Result.success(pagingData))
        }
    }

    private fun setScreen(
        environment: RoutineRecordTestEnvironment,
        recordViewModel: RoutineDetailRecordViewModel = environment.recordViewModel,
    ) {
        composeRule.setContent { RoutineRecordTestScreen(environment = environment, recordViewModel = recordViewModel) }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val MARCH_10: LocalDate = LocalDate(2026, 3, 10)
        val PAGED_LAST_DATE: LocalDate = LocalDate(2026, 6, 30)
        const val KOREAN_MARCH_5 = "2026. 3. 5."
        const val TAG_TITLE = "RoutineRecordTag"
        const val EXTERNAL_DESCRIPTION = "다른 경로에서 바뀐 설명"
        const val CLEAR = "clear"
        const val CLEAR_TEXT = "Clear text"
        const val RETRY = "retry"
        const val UNDO = "undo"
        const val PAGED_RECORD_COUNT = 45
        const val PAGE_SIZE = 20
        const val HEADER_ITEM_COUNT = 3
        const val PAGING_TIMEOUT_MILLIS = 10_000L
    }
}

private const val AMOUNT_1500_ML_TEXT = "1500ml"

private const val AMOUNT_1500_M_TEXT = "1500m"

private const val AMOUNT_300_ML_TEXT = "300ml"

private const val SEVEN_DIGIT_TEXT = "1234567"

private const val AMOUNT_500_ML_TEXT = "500ml"

private const val AMOUNT_100_ML_TEXT = "100ml"

private const val AMOUNT_200_ML_TEXT = "200ml"

private const val SAVE_FAILURE_MESSAGE = "저장 실패"
