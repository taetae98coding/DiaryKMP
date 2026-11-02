package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactNameQualifierTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-5afa9d0f-6e52-4ae2-ba53-416c5aaa3d63 이름이 같은 연락처의 칩에 알게 된 경로를 함께 표시한다`() {
        val contact = testContact(name = SAME_NAME, nameQualifier = FRIEND_HOW_WE_MET)

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = listOf(contact)))

        composeRule.onNodeWithText(KOREAN_FRIEND_DISPLAY_NAME).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-8667fb19-63e4-4dfd-9b3b-119206fd1096 이름이 같은 연락처를 선택 목록에서 알게 된 경로로 구분한다`() {
        val friend = testContact(name = SAME_NAME, nameQualifier = FRIEND_HOW_WE_MET)
        val coworker = testContact(name = SAME_NAME, nameQualifier = COWORKER_HOW_WE_MET)
        composeRule.setMemoContactPickerDialog(contactList = listOf(friend, coworker))
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(KOREAN_FRIEND_DISPLAY_NAME).assertExists()
        composeRule.contactDialogNodeWithText(KOREAN_COWORKER_DISPLAY_NAME).assertExists()
    }

    @Test
    fun `구분 경로가 없는 연락처는 칩과 선택 목록에 이름만 표시한다`() {
        val contact = testContact(name = SAME_NAME)
        composeRule.setMemoContactPickerDialog(
            contactList = listOf(contact),
            uiState = MemoContactInputUiState(selectedContactList = listOf(contact)),
        )
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(SAME_NAME).assertExists()
    }

    @Test
    fun `기본 환경에서는 이름과 괄호 사이를 한 칸 띄운다`() {
        val contact = testContact(name = SAME_NAME, nameQualifier = FRIEND_HOW_WE_MET)

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = listOf(contact)))

        composeRule.onNodeWithText("$SAME_NAME ($FRIEND_HOW_WE_MET)").assertExists()
    }

    public companion object {
        private const val SAME_NAME = "김철수"
        private const val FRIEND_HOW_WE_MET = "친구 소개"
        private const val COWORKER_HOW_WE_MET = "회사"
        private const val KOREAN_FRIEND_DISPLAY_NAME = "김철수(친구 소개)"
        private const val KOREAN_COWORKER_DISPLAY_NAME = "김철수(회사)"
    }
}
