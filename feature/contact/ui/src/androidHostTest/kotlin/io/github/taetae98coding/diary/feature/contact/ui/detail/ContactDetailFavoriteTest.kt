package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailFavoriteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-025 저장된 즐겨찾기 여부에 따라 즐겨찾기 표시가 갈린다`() {
        setContactDetailScaffold(uiState = content(isFavorite = true))

        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-025 즐겨찾기가 아니면 즐겨찾기에 추가하는 표시를 제공한다`() {
        setContactDetailScaffold(uiState = content(isFavorite = false))

        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-DETAIL-FEATURE-025 한국어 환경에서 즐겨찾기 변경의 이름을 제공한다`() {
        setContactDetailScaffold(uiState = content(isFavorite = false))

        composeRule.onNodeWithContentDescription(KOREAN_FAVORITE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-026 즐겨찾기를 선택하면 즐겨찾기 변경 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<ContactDetailScaffoldEvent>()

        setContactDetailScaffold(uiState = content(isFavorite = false), onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactDetailScaffoldEvent.ClickFavorite)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-027 즐겨찾기를 바꿔도 입력 중이던 내용과 수정 동작이 유지된다`() {
        val detail = testContactDetail(name = CONTACT_NAME, hometown = CONTACT_HOMETOWN)
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { ContactDetailUiState.Content(id = Uuid.random(), detail = detail, isFavorite = false) },
                )
            }
        }

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextInput(NAME_SUFFIX)
        composeRule.onNode(hasText(CONTACT_HOMETOWN) and hasSetTextAction()).performTextInput(HOMETOWN_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(CONTACT_NAME, substring = true) and hasText(NAME_SUFFIX, substring = true) and hasSetTextAction()).assertExists()
        composeRule.onNode(hasText(CONTACT_HOMETOWN, substring = true) and hasText(HOMETOWN_SUFFIX, substring = true) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-028 즐겨찾기 변경을 처리하는 동안 즐겨찾기 표시 대신 진행이 표시되고 수정과 삭제는 그대로 제공된다`() {
        val detail = testContactDetail(name = CONTACT_NAME)
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = {
                        ContactDetailUiState.Content(id = Uuid.random(), detail = detail, isFavorite = false, isFavoriteInProgress = true)
                    },
                )
            }
        }

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextInput(NAME_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-032 조회 중에는 즐겨찾기를 바꾸는 수단을 제공하지 않는다`() {
        setContactDetailScaffold(uiState = ContactDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_FAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_UNFAVORITE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun setContactDetailScaffold(
        uiState: ContactDetailUiState = ContactDetailUiState.Loading,
        onEvent: (ContactDetailScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ContactDetailScaffold(
                    onEvent = onEvent,
                    state = rememberContactDetailFormState(initialDetail = (uiState as? ContactDetailUiState.Content)?.detail ?: ContactDetail.EMPTY),
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactDetailFavoriteName"
        private const val CONTACT_HOMETOWN = "ContactDetailFavoriteHometown"
        private const val NAME_SUFFIX = "변경"
        private const val HOMETOWN_SUFFIX = "고향변경"

        private const val DEFAULT_FAVORITE_BUTTON_DESCRIPTION = "Add to favorites"
        private const val DEFAULT_UNFAVORITE_BUTTON_DESCRIPTION = "Remove from favorites"
        private const val KOREAN_FAVORITE_BUTTON_DESCRIPTION = "즐겨찾기에 추가"
        private const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update contact"
        private const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete contact"

        private fun content(isFavorite: Boolean): ContactDetailUiState.Content = ContactDetailUiState.Content(id = Uuid.random(), detail = testContactDetail(), isFavorite = isFavorite)
    }
}
