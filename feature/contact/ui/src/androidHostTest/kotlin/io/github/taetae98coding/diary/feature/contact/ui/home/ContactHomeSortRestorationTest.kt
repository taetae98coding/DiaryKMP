package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeSortRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-HOME-DOMAIN-016 시스템이 앱을 정리했다가 다시 만들면 정렬은 이름순으로 돌아가고 보던 목록 위치는 복원한다`() {
        val nameOrderList =
            List(RESTORATION_CONTACT_COUNT) { index ->
                testContact(name = "연락처-${index.toString().padStart(length = 2, padChar = '0')}-${fixtureMonkey.giveMeOne<String>()}")
            }
        val recentOrderList = nameOrderList.reversed()
        val pageContactUseCase = mockk<PageContactUseCase>()
        every { pageContactUseCase(parameter = ListSort.NAME) } returns flowOf(Result.success(contactPagingDataOf(nameOrderList)))
        every { pageContactUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns flowOf(Result.success(contactPagingDataOf(recentOrderList)))
        val syncViewModel = mockk<ContactHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(ContactHomeUiState())
        justRun { syncViewModel.refresh() }
        var viewModel = contactHomeViewModel(pageContactUseCase = pageContactUseCase)
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                ContactHomeScreen(
                    navigateUp = {},
                    navigateToAdd = {},
                    navigateToDetail = {},
                    componentVisibleProvider = { ContactHomeScaffoldComponentVisible() },
                    contactViewModel = viewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        waitUntilNameExists(nameOrderList.first().detail.name)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        waitUntilNameExists(recentOrderList.first().detail.name)
        composeRule.onNodeWithTag(CONTACT_HOME_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(recentOrderList[RESTORATION_SCROLL_INDEX].detail.name).assertIsDisplayed()

        // 시스템이 앱을 정리하면 화면 상태를 보관하던 객체도 사라지므로, 되살린 화면에는 새로 만든 객체를 준다.
        viewModel = contactHomeViewModel(pageContactUseCase = pageContactUseCase)
        restorationTester.emulateSavedInstanceStateRestore()
        waitUntilNameExists(nameOrderList[RESTORATION_SCROLL_INDEX].detail.name)

        viewModel.sort.value shouldBe ListSort.NAME
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(nameOrderList[RESTORATION_SCROLL_INDEX].detail.name).assertIsDisplayed()
        composeRule.onNodeWithText(nameOrderList.first().detail.name).assertDoesNotExist()
    }

    private fun contactHomeViewModel(pageContactUseCase: PageContactUseCase): ContactHomeViewModel =
        ContactHomeViewModel(
            pageContactUseCase = pageContactUseCase,
            deleteContactUseCase = mockk<DeleteContactUseCase>(),
            restoreContactUseCase = mockk<RestoreContactUseCase>(),
        )

    private fun waitUntilNameExists(name: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val RESTORATION_CONTACT_COUNT = 60
        const val RESTORATION_SCROLL_INDEX = 50
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
    }
}
