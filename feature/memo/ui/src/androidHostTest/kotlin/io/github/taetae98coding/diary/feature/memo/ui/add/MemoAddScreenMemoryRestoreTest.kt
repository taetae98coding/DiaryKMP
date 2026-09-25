package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_TAG_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenMemoryRestoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var restorationTesterHolder: StateRestorationTester

    @Test
    fun `TC-MEMO-ADD-FEATURE-046 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 태그가 진입 경로의 초기 선택으로 돌아간다`() {
        val targetTag = testTag(title = TARGET_TAG_TITLE)
        val otherTag = testTag(title = OTHER_TAG_TITLE)
        val viewModelsList =
            setRestorableMemoAddScreen {
                screenTestRealViewModel(initialPrimaryTagId = targetTag.id, tagList = listOf(targetTag, otherTag))
            }
        composeRule.runOnIdle { viewModelsList.last().tagViewModel.selectPrimaryTag(id = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(TARGET_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
        viewModelsList
            .last()
            .tagViewModel.selection.value.primaryTagId shouldBe targetTag.id
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-051 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 장소가 유지되지 않는다`() {
        val place = testPlace(title = PLACE_TITLE)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(placeList = listOf(place)) }
        composeRule.runOnIdle { viewModelsList.last().placeViewModel.selectPlace(id = place.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PLACE_TITLE).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-058 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 웹 항목이 유지되지 않는다`() {
        val web = testWeb(title = WEB_TITLE)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(webList = listOf(web)) }
        composeRule.runOnIdle { viewModelsList.last().webViewModel.selectWeb(id = web.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(WEB_TITLE).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-062 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 연락처가 유지되지 않는다`() {
        val contact = testContact(name = CONTACT_NAME)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(contactList = listOf(contact)) }
        composeRule.runOnIdle { viewModelsList.last().contactViewModel.selectContact(id = contact.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONTACT_NAME).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(CONTACT_NAME).assertDoesNotExist()
    }

    // 메모리 정리 뒤에는 선택을 들고 있던 상태 객체도 새로 만들어지므로, 복원할 때마다 같은 진입 경로의 새 인스턴스를 쓴다.
    private fun setRestorableMemoAddScreen(createViewModels: () -> MemoAddScreenViewModels): List<MemoAddScreenViewModels> {
        val viewModelsList = mutableListOf<MemoAddScreenViewModels>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTesterHolder = restorationTester
        restorationTester.setContent {
            val viewModels = remember { createViewModels().also(viewModelsList::add) }

            MemoAddScreenTestTheme {
                MemoryRestoreTestMemoAddScreen(viewModels = viewModels)
            }
        }
        composeRule.waitForIdle()

        return viewModelsList
    }

    private fun restore(viewModelsList: List<MemoAddScreenViewModels>) {
        restorationTesterHolder.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        viewModelsList.size shouldBe 2
    }

    private companion object {
        private const val TARGET_TAG_TITLE = "MemoryRestoreTargetTag"
        private const val OTHER_TAG_TITLE = "MemoryRestoreOtherTag"
        private const val PLACE_TITLE = "MemoryRestorePlace"
        private const val WEB_TITLE = "MemoryRestoreWeb"
        private const val CONTACT_NAME = "MemoryRestoreContact"
    }
}

@Composable
private fun MemoryRestoreTestMemoAddScreen(viewModels: MemoAddScreenViewModels) {
    MemoAddScreen(
        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
        addViewModel = viewModels.viewModel,
        tagViewModel = viewModels.tagViewModel,
        webViewModel = viewModels.webViewModel,
        contactViewModel = viewModels.contactViewModel,
        placeViewModel = viewModels.placeViewModel,
        placeMapViewModel = remember { screenTestPlaceMapViewModel() },
        geminiViewModel = remember { screenTestGeminiViewModel() },
        navigateUp = {},
        navigateToTagAdd = {},
        navigateToTagDetail = {},
        navigateToWebAdd = {},
        navigateToWebDetail = {},
        navigateToContactAdd = {},
        navigateToContactDetail = {},
        navigateToPlaceAdd = {},
        navigateToPlaceDetail = {},
        initialDateRange = null,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
        isStandalone = true,
    )
}
