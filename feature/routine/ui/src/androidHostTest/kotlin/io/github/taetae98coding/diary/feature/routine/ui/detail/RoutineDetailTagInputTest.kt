package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h3000dp")
class RoutineDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-3afc8986-deca-4c86-999b-a6282662d6ed 태그를 연결하고 해제하면 바로 반영을 요청하고 기록과 고친 입력은 그대로다`() {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = environment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        setScreen(environment)
        editInput()

        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.add(tagId = linkTag.id) }
        composeRule.routineDetailUnlinkTag(title = LINKED_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.remove(tagId = linkedTag.id) }

        verify(exactly = 0) { environment.routineViewModel.update(input = any()) }
        assertEditedInput()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-7aa9b736-ef04-4bd0-932d-c152ce7a3c26 대상이 삭제 상태가 된 뒤에도 태그를 연결하고 해제할 수 있다`() {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = environment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        setScreen(environment)
        editInput()

        // 삭제 상태가 된 루틴도 같은 저장 내용으로 다시 전달된다.
        environment.uiState.value = environment.content(detail = environment.detail.copy())
        composeRule.waitForIdle()

        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.add(tagId = linkTag.id) }
        composeRule.routineDetailUnlinkTag(title = LINKED_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.remove(tagId = linkedTag.id) }
        assertEditedInput()
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 태그를 연결하거나 해제해도 수정 반영 동작은 나타나지 않는다`() {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = environment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        setScreen(environment)
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()

        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()

        composeRule.routineDetailUnlinkTag(title = LINKED_TAG_TITLE)
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-800b1f3a-ca90-4fd8-83d9-5bb0573e0c19 다른 경로에서 태그가 연결되면 태그 입력에 나타나고 고친 제목은 그대로다`() {
        val storedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val newTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val environment = environment(tagList = listOf(storedTag))
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.onNode(routineDetailTagChip(LINK_TAG_TITLE)).assertDoesNotExist()

        environment.tagUiState.value = EntityTagInputUiState(tagList = listOf(storedTag, newTag))
        composeRule.waitForIdle()

        composeRule.onNode(routineDetailTagChip(LINKED_TAG_TITLE)).assertExists()
        composeRule.onNode(routineDetailTagChip(LINK_TAG_TITLE)).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
    }

    @Test
    fun `TC-30791ed1-ddd1-4e42-93fa-a5ce644d560a 태그 연결이나 해제의 저장에 실패하면 안내 없이 저장된 연결을 보여 주고 다시 연결을 요청한다`() {
        val storedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val otherTag = Arb.tag(title = LINK_TAG_TITLE).next()
        // 저장이 실패하므로 저장된 연결은 처음 태그 하나에서 바뀌지 않는다.
        val environment = environment(tagList = listOf(storedTag), selectableTagList = listOf(otherTag, storedTag))
        setScreen(environment)

        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.routineDetailUnlinkTag(title = LINKED_TAG_TITLE)
        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)

        verify(exactly = 2) { environment.tagViewModel.add(tagId = otherTag.id) }
        verify(exactly = 1) { environment.tagViewModel.remove(tagId = storedTag.id) }
        composeRule.onNode(routineDetailTagChip(LINKED_TAG_TITLE)).assertExists()
        composeRule.onNode(routineDetailTagChip(LINK_TAG_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertDoesNotExist()
    }

    @Test
    fun `TC-d1be929e-fe9d-4699-baa6-48724025959d 내용 수정을 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(inProgress = InProgress.UPDATE)
    }

    @Test
    fun `TC-d1be929e-fe9d-4699-baa6-48724025959d 완료를 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(inProgress = InProgress.FINISH)
    }

    @Test
    fun `TC-d1be929e-fe9d-4699-baa6-48724025959d 삭제를 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(inProgress = InProgress.DELETE)
    }

    @Test
    fun `TC-d1be929e-fe9d-4699-baa6-48724025959d 기록 넣기를 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(inProgress = InProgress.RECORD_ADD)
    }

    private fun assertTagWhileInProgress(inProgress: InProgress) {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = environment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        environment.uiState.value = environment.content(isUpdateInProgress = inProgress == InProgress.UPDATE, isFinishInProgress = inProgress == InProgress.FINISH, isDeleteInProgress = inProgress == InProgress.DELETE)
        environment.recordUiState.value = RoutineDetailRecordUiState(isAddInProgress = inProgress == InProgress.RECORD_ADD)
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        val progressButton =
            when (inProgress) {
                InProgress.UPDATE -> routineDetailUpdateButton()
                InProgress.FINISH -> routineDetailFinishButton()
                InProgress.DELETE -> routineDetailDeleteButton()
                InProgress.RECORD_ADD -> routineDetailRecordAddButton()
            }
        composeRule.onNode(progressButton and hasRoutineDetailProgress(), useUnmergedTree = true).assertExists()

        composeRule.routineDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.routineDetailUnlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { environment.tagViewModel.add(tagId = linkTag.id) }
        verify(exactly = 1) { environment.tagViewModel.remove(tagId = linkedTag.id) }
    }

    @Test
    fun `TC-580cb060-7017-464b-925b-0505c43c30db 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = environment(tagList = listOf(linkedTag))
        setScreen(environment)

        composeRule.onNode(routineDetailTagChip(LINKED_TAG_TITLE)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        environment.navigatedTagIdList shouldBe listOf(linkedTag.id)
    }

    // 제목과 목표량을 고치고 기록의 양에 500을 입력해 둔다.
    private fun editInput() {
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "2500")
        composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500")
    }

    private fun assertEditedInput() {
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "2500"
        composeRule.routineDetailRecordAmountInput().routineDetailEditableText() shouldBe "500"
        composeRule.routineDetailRecordRowList() shouldBe listOf(routineDetailDisplayText(RECORD_DATE) to "250ml")
    }

    private fun environment(
        tagList: List<Tag>,
        selectableTagList: List<Tag> = emptyList(),
    ): RoutineDetailTestEnvironment =
        RoutineDetailTestEnvironment(
            recordList = listOf(routineDetailRecordOf(routineId = Arb.kotlinUuid().next(), date = RECORD_DATE, amount = 250)),
            tagList = tagList,
            selectableTagList = selectableTagList,
        )

    private fun setScreen(environment: RoutineDetailTestEnvironment) {
        composeRule.setContent { RoutineDetailTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private enum class InProgress { UPDATE, FINISH, DELETE, RECORD_ADD }

    private companion object {
        const val LINK_TAG_TITLE = "RoutineDetailLinkTag"
        const val LINKED_TAG_TITLE = "RoutineDetailLinkedTag"
        val RECORD_DATE = LocalDate(2026, 3, 5)
    }
}
