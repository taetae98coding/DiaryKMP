package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreenTestHost
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailUiState
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.FIRST_CONTACT_ID
import io.github.taetae98coding.diary.feature.contact.ui.detail.prepareContactDetailTabViewModels
import io.github.taetae98coding.diary.feature.contact.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.selectContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.setContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.testContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
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
    fun `TC-aaabe24f-ef74-498f-a9b3-bdf9bbc9f92f 앱이 백그라운드에 다녀와도 선택한 탭이 유지된다`() {
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content())))
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-66fe1ae0-5c27-40f0-bafc-db93304bb0f5 TC-eaa0d3cf-24b5-47c0-b968-beab1d53116a 앱을 다시 실행해 들어오면 연락처 디테일 탭과 저장된 내용으로 시작한다`() {
        val stored = testContactDetail()
        var launchCount by mutableIntStateOf(0)
        val viewModelList = mutableListOf<ContactDetailViewModel>()
        prepareContactDetailTabViewModels()
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 화면 상태를 들고 있던 객체도 새로 만들어지므로, 새 키로 저장 상태 없이 새 인스턴스와 함께 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                val viewModel = remember { screenTestViewModel(uiState = MutableStateFlow(content(stored = stored))).also(viewModelList::add) }

                ContactDetailScreenTestHost {
                    ContactDetailScreen(
                        navigateUp = {},
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        navigateToMemoFinishedList = {},
                        id = FIRST_CONTACT_ID,
                        componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                        contactViewModel = viewModel,
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        tagViewModel = detailTagScreenTestViewModel(),
                    )
                }
            }
        }
        composeRule.nameInput().performTextReplacement(TYPED_NAME)
        composeRule.waitForIdle()
        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        viewModelList.size shouldBe 2
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.nameInput().assert(hasText(stored.name) and hasSetTextAction())
    }

    private fun content(stored: ContactDetail = testContactDetail()): ContactDetailUiState.Content = ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = stored)

    private companion object {
        const val TYPED_NAME = "ContactDetailBoundaryTypedName"
    }
}
