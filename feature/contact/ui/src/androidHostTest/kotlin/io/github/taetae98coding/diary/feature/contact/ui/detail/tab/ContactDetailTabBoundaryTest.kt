package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailUiState
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.FIRST_CONTACT_ID
import io.github.taetae98coding.diary.feature.contact.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.selectContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.setContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.testContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailTabBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-020 앱이 백그라운드에 다녀와도 선택한 탭이 유지된다`() {
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content())))
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    // 선택한 탭과 입력 내용은 기기에 남기지 않으므로, 앱을 다시 실행한 화면은 저장된 상태 없이 새로 그린 화면과 같다.
    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-013 TC-CONTACT-DETAIL-FEATURE-020 앱을 다시 실행해 들어오면 연락처 디테일 탭과 저장된 내용으로 시작한다`() {
        val stored = testContactDetail()

        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content(stored = stored))))

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.nameInput().assert(hasText(stored.name) and hasSetTextAction())
    }

    private fun content(stored: ContactDetail = testContactDetail()): ContactDetailUiState.Content = ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)
}
