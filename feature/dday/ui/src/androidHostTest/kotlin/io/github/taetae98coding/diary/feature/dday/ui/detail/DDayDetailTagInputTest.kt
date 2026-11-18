package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRIMARY_TAG_SET
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRIMARY_TAG_UNSET
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_REPEAT_BLANK
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEditableText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInputRepeatNumber
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPrimaryTagChip
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-9c3b45db-e816-4692-9c08-404006eab365 태그를 연결하고 해제하면 바로 반영을 요청하고 고친 입력은 그대로다`() {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        setScreen(environment)
        editInput()

        composeRule.ddayDetailLinkTag(title = LINK_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.selectTag(tagId = linkTag.id) }
        composeRule.ddayDetailUnlinkTag(title = LINKED_TAG_TITLE)
        verify(exactly = 1) { environment.tagViewModel.unselectTag(tagId = linkedTag.id) }

        verify(exactly = 0) { environment.ddayViewModel.update(input = any()) }
        assertEditedInput()
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-3e664aa6-b831-4e72-b0d7-38b42cb3166b 대표 태그를 지정하고 해제하면 바로 반영을 요청하고 고친 입력은 그대로다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag))
        setScreen(environment)
        editInput()
        val updateButtonCount = composeRule.onAllNodes(ddayDetailUpdateButton()).fetchSemanticsNodes().size

        composeRule.ddayDetailClickPrimaryTagButton(title = LINKED_TAG_TITLE, description = DDAY_FORM_DEFAULT_PRIMARY_TAG_SET)
        verify(exactly = 1) { environment.tagViewModel.selectPrimaryTag(tagId = linkedTag.id) }
        // 저장된 대표 태그가 바뀌면 태그 입력에 그대로 반영된다.
        environment.tagUiState.value = PrimaryTagInputUiState(selectedTagList = listOf(linkedTag), primaryTagId = linkedTag.id)
        composeRule.waitForIdle()
        composeRule.onNode(ddayPrimaryTagChip()).assert(hasText(LINKED_TAG_TITLE))
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onAllNodes(ddayDetailUpdateButton()).fetchSemanticsNodes().size shouldBe updateButtonCount

        composeRule.ddayDetailClickPrimaryTagButton(title = LINKED_TAG_TITLE, description = DDAY_FORM_DEFAULT_PRIMARY_TAG_UNSET)
        verify(exactly = 1) { environment.tagViewModel.unselectPrimaryTag() }
        environment.tagUiState.value = PrimaryTagInputUiState(selectedTagList = listOf(linkedTag))
        composeRule.waitForIdle()
        composeRule.onNode(ddayPrimaryTagChip()).assertDoesNotExist()

        verify(exactly = 0) { environment.ddayViewModel.update(input = any()) }
        assertEditedInput()
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onAllNodes(ddayDetailUpdateButton()).fetchSemanticsNodes().size shouldBe updateButtonCount
    }

    @Test
    fun `TC-07e0044e-806c-40ca-8b91-792d8d5ab5a9 태그 입력에 저장된 대표 태그가 표시된다`() {
        val tagA = Arb.tag(title = LINK_TAG_TITLE).next()
        val tagB = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment()
        environment.tagUiState.value = PrimaryTagInputUiState(selectedTagList = listOf(tagA, tagB), primaryTagId = tagB.id)
        setScreen(environment)

        composeRule.onNode(tagChip(LINK_TAG_TITLE)).assertExists()
        composeRule.onNode(tagChip(LINKED_TAG_TITLE)).assertExists()
        composeRule.onAllNodes(ddayPrimaryTagChip()).assertCountEquals(1)
        composeRule.onNode(ddayPrimaryTagChip()).assert(hasText(LINKED_TAG_TITLE))
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 태그를 연결하거나 해제해도 수정 반영 동작은 나타나지 않는다`() {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        setScreen(environment)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()

        composeRule.ddayDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()

        composeRule.ddayDetailUnlinkTag(title = LINKED_TAG_TITLE)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-a7f12cde-89ba-4d73-a748-f99fd0cd4c12 다른 경로에서 태그가 연결되면 태그 입력에 나타나고 고친 제목은 그대로다`() {
        val storedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val newTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(storedTag))
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()
        composeRule.onNode(tagChip(LINK_TAG_TITLE)).assertDoesNotExist()

        environment.tagUiState.value = PrimaryTagInputUiState(selectedTagList = listOf(storedTag, newTag))
        composeRule.waitForIdle()

        composeRule.onNode(tagChip(LINKED_TAG_TITLE)).assertExists()
        composeRule.onNode(tagChip(LINK_TAG_TITLE)).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
    }

    @Test
    fun `TC-60798db3-cb43-4f8f-ad5d-5d8393d74d24 태그 연결이나 해제의 저장에 실패하면 안내 없이 저장된 연결을 보여 주고 다시 연결을 요청한다`() {
        val storedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val otherTag = Arb.tag(title = LINK_TAG_TITLE).next()
        // 저장이 실패하므로 저장된 연결은 처음 태그 하나에서 바뀌지 않는다.
        val environment = DDayDetailTestEnvironment(tagList = listOf(storedTag), selectableTagList = listOf(otherTag, storedTag))
        setScreen(environment)

        composeRule.ddayDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.ddayDetailUnlinkTag(title = LINKED_TAG_TITLE)
        composeRule.ddayDetailLinkTag(title = LINK_TAG_TITLE)

        verify(exactly = 2) { environment.tagViewModel.selectTag(tagId = otherTag.id) }
        verify(exactly = 1) { environment.tagViewModel.unselectTag(tagId = storedTag.id) }
        composeRule.onNode(tagChip(LINKED_TAG_TITLE)).assertExists()
        composeRule.onNode(tagChip(LINK_TAG_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertDoesNotExist()
    }

    @Test
    fun `TC-7562cfec-e663-4671-8644-91f2f241ea76 내용 수정을 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(isUpdateInProgress = true)
    }

    @Test
    fun `TC-7562cfec-e663-4671-8644-91f2f241ea76 삭제를 처리하는 동안에도 태그를 연결하고 해제할 수 있다`() {
        assertTagWhileInProgress(isUpdateInProgress = false)
    }

    private fun assertTagWhileInProgress(isUpdateInProgress: Boolean) {
        val linkTag = Arb.tag(title = LINK_TAG_TITLE).next()
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(linkedTag), selectableTagList = listOf(linkTag, linkedTag))
        environment.uiState.value = environment.content(isUpdateInProgress = isUpdateInProgress, isDeleteInProgress = !isUpdateInProgress)
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        composeRule.ddayDetailLinkTag(title = LINK_TAG_TITLE)
        composeRule.ddayDetailUnlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { environment.tagViewModel.selectTag(tagId = linkTag.id) }
        verify(exactly = 1) { environment.tagViewModel.unselectTag(tagId = linkedTag.id) }
    }

    @Test
    fun `TC-d7bd90da-4196-4810-aac3-1abc0adb9a65 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = DDayDetailTestEnvironment(tagList = listOf(linkedTag))
        setScreen(environment)

        composeRule.onNode(tagChip(LINKED_TAG_TITLE)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        environment.navigatedTagIdList shouldBe listOf(linkedTag.id)
    }

    private fun editInput() {
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")
    }

    private fun assertEditedInput() {
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.ddayRepeatList() shouldBe listOf("200" to DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    private fun setScreen(environment: DDayDetailTestEnvironment) {
        composeRule.setContent { DDayDetailTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val LINK_TAG_TITLE = "DDayDetailLinkTag"
        const val LINKED_TAG_TITLE = "DDayDetailLinkedTag"

        // 태그 입력에 놓인 연결 대상 태그다. 태그 선택 목록의 같은 이름 행과 구분한다.
        fun tagChip(title: String): SemanticsMatcher = hasText(title) and !hasAnyAncestor(isDialog())
    }
}
