package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.tag.hasPrimaryTagState
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
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

    private var launchCount by mutableIntStateOf(0)

    @Test
    fun `TC-a97443d4-bf42-4ed0-8f26-c6ae3eda0d8c 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 태그가 진입 경로의 초기 선택으로 돌아간다`() {
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
        composeRule.onAllNodes(hasPrimaryTagState()).assertCountEquals(1)
        viewModelsList
            .last()
            .tagViewModel.selectionUiState.value.primaryTagId shouldBe targetTag.id
    }

    @Test
    fun `TC-a4cdefb7-ef84-44c5-bab8-f8828ce45883 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 장소가 유지되지 않는다`() {
        val place = testPlace(title = PLACE_TITLE)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(placeList = listOf(place)) }
        composeRule.runOnIdle { viewModelsList.last().placeViewModel.selectPlace(id = place.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PLACE_TITLE).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-81245be2-b619-4a1a-840a-f1be2758cbf3 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 웹 항목이 유지되지 않는다`() {
        val web = testWeb(title = WEB_TITLE)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(webList = listOf(web)) }
        composeRule.runOnIdle { viewModelsList.last().webViewModel.selectWeb(id = web.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(WEB_TITLE).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-2903649b-8a21-4f71-b1f3-5667f5786470 시스템이 앱을 정리한 뒤 화면을 복원하면 선택한 연락처가 유지되지 않는다`() {
        val contact = testContact(name = CONTACT_NAME)
        val viewModelsList = setRestorableMemoAddScreen { screenTestRealViewModel(contactList = listOf(contact)) }
        composeRule.runOnIdle { viewModelsList.last().contactViewModel.selectContact(id = contact.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONTACT_NAME).assertExists()

        restore(viewModelsList)

        composeRule.onNodeWithText(CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-5e4cd809-9061-411a-ba45-dcc116a4e91e 앱을 다시 실행한 뒤 MemoHome에서 새로 진입하면 이전 선택이 없는 상태로 시작한다`() {
        assertSelectionClearedAfterRelaunch(navKey = MemoAddNavKey())
    }

    @Test
    fun `TC-5e4cd809-9061-411a-ba45-dcc116a4e91e 앱을 다시 실행한 뒤 캘린더 홈이나 황금연휴 화면에서 기간을 선택해 새로 진입하면 이전 선택이 없는 상태로 시작한다`() {
        assertSelectionClearedAfterRelaunch(
            navKey =
                MemoAddNavKey(
                    initialDateRange =
                        MemoAddNavKey.InitialDateRange(
                            start = LocalDate(year = 2026, month = Month.JULY, day = 14),
                            endInclusive = LocalDate(year = 2026, month = Month.JULY, day = 17),
                        ),
                ),
        )
    }

    @Test
    fun `TC-5e4cd809-9061-411a-ba45-dcc116a4e91e 앱을 다시 실행한 뒤 캘린더 시간표 화면에서 기간을 선택해 새로 진입하면 이전 선택이 없는 상태로 시작한다`() {
        val date = LocalDate(year = 2026, month = Month.JULY, day = 14)
        assertSelectionClearedAfterRelaunch(
            navKey =
                MemoAddNavKey(
                    initialDateTimeRange =
                        MemoAddNavKey.InitialDateTimeRange(
                            start = LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 0)),
                            endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 10, minute = 0)),
                        ),
                ),
        )
    }

    @Test
    fun `TC-00ba6723-b40c-46de-85bc-d879183158b1 앱을 다시 실행한 뒤 TagDetail 메모 탭에서 새로 진입하면 대상 태그만 대표 태그로 선택된다`() {
        val targetTag = testTag(title = TARGET_TAG_TITLE)
        val otherTag = testTag(title = OTHER_TAG_TITLE)
        val viewModelsList =
            setRelaunchableMemoAddScreen {
                screenTestRealViewModel(initialPrimaryTagId = targetTag.id, tagList = listOf(targetTag, otherTag))
            }
        composeRule.runOnIdle { viewModelsList.last().tagViewModel.selectPrimaryTag(id = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TARGET_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        relaunch(viewModelsList)

        composeRule.onNodeWithText(TARGET_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasPrimaryTagState()).assertCountEquals(1)
        viewModelsList
            .last()
            .tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(targetTag.id)
        viewModelsList
            .last()
            .tagViewModel.selectionUiState.value.primaryTagId shouldBe targetTag.id
    }

    @Test
    fun `TC-4c0eec42-5097-496d-8425-2cc27213ebfa 앱을 다시 실행한 뒤 ContactDetail 메모 탭에서 새로 진입하면 대상 연락처만 선택된다`() {
        val targetContact = testContact(name = CONTACT_NAME)
        val otherContact = testContact(name = OTHER_CONTACT_NAME)
        val viewModelsList =
            setRelaunchableMemoAddScreen {
                screenTestRealViewModel(initialContactId = targetContact.id, contactList = listOf(targetContact, otherContact))
            }
        composeRule.runOnIdle { viewModelsList.last().contactViewModel.selectContact(id = otherContact.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(OTHER_CONTACT_NAME).assertExists()

        relaunch(viewModelsList)

        composeRule.onNodeWithText(CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(OTHER_CONTACT_NAME).assertDoesNotExist()
        viewModelsList
            .last()
            .contactViewModel.selectionUiState.value.contactIdSet shouldBe setOf(targetContact.id)
    }

    @Test
    fun `TC-4c0eec42-5097-496d-8425-2cc27213ebfa 앱을 다시 실행한 뒤 PlaceDetail 메모 탭에서 새로 진입하면 대상 장소만 선택된다`() {
        val targetPlace = testPlace(title = PLACE_TITLE)
        val otherPlace = testPlace(title = OTHER_PLACE_TITLE)
        val viewModelsList =
            setRelaunchableMemoAddScreen {
                screenTestRealViewModel(initialPlaceId = targetPlace.id, placeList = listOf(targetPlace, otherPlace))
            }
        composeRule.runOnIdle { viewModelsList.last().placeViewModel.selectPlace(id = otherPlace.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_PLACE_TITLE).assertExists()

        relaunch(viewModelsList)

        composeRule.onNodeWithText(PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_PLACE_TITLE).assertDoesNotExist()
        viewModelsList
            .last()
            .placeViewModel.selectionUiState.value.placeIdSet shouldBe setOf(targetPlace.id)
    }

    @Test
    fun `TC-4c0eec42-5097-496d-8425-2cc27213ebfa 앱을 다시 실행한 뒤 WebDetail 메모 탭에서 새로 진입하면 대상 웹 항목만 선택된다`() {
        val targetWeb = testWeb(title = WEB_TITLE)
        val otherWeb = testWeb(title = OTHER_WEB_TITLE)
        val viewModelsList =
            setRelaunchableMemoAddScreen {
                screenTestRealViewModel(initialWebId = targetWeb.id, webList = listOf(targetWeb, otherWeb))
            }
        composeRule.runOnIdle { viewModelsList.last().webViewModel.selectWeb(id = otherWeb.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(WEB_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_WEB_TITLE).assertExists()

        relaunch(viewModelsList)

        composeRule.onNodeWithText(WEB_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_WEB_TITLE).assertDoesNotExist()
        viewModelsList
            .last()
            .webViewModel.selectionUiState.value.webIdSet shouldBe setOf(targetWeb.id)
    }

    private fun assertSelectionClearedAfterRelaunch(navKey: MemoAddNavKey) {
        val firstTag = testTag(title = TARGET_TAG_TITLE)
        val secondTag = testTag(title = OTHER_TAG_TITLE)
        val web = testWeb(title = WEB_TITLE)
        val contact = testContact(name = CONTACT_NAME)
        val place = testPlace(title = PLACE_TITLE)
        val viewModelsList =
            setRelaunchableMemoAddScreen(initialDateTime = navKey.initialDateTime()) {
                screenTestRealViewModel(
                    tagList = listOf(firstTag, secondTag),
                    webList = listOf(web),
                    contactList = listOf(contact),
                    placeList = listOf(place),
                )
            }
        composeRule.runOnIdle {
            viewModelsList.last().tagViewModel.selectTag(id = firstTag.id)
            viewModelsList.last().tagViewModel.selectPrimaryTag(id = secondTag.id)
            viewModelsList.last().webViewModel.selectWeb(id = web.id)
            viewModelsList.last().contactViewModel.selectContact(id = contact.id)
            viewModelsList.last().placeViewModel.selectPlace(id = place.id)
        }
        composeRule.waitForIdle()
        listOf(TARGET_TAG_TITLE, OTHER_TAG_TITLE, WEB_TITLE, CONTACT_NAME, PLACE_TITLE).forEach { text ->
            composeRule.onNodeWithText(text).assertExists()
        }
        composeRule.onAllNodes(hasPrimaryTagState()).assertCountEquals(1)

        relaunch(viewModelsList)

        listOf(TARGET_TAG_TITLE, OTHER_TAG_TITLE, WEB_TITLE, CONTACT_NAME, PLACE_TITLE).forEach { text ->
            composeRule.onNodeWithText(text).assertDoesNotExist()
        }
        composeRule.onAllNodes(hasPrimaryTagState()).assertCountEquals(0)
        val relaunchedViewModels = viewModelsList.last()
        relaunchedViewModels.tagViewModel.selectionUiState.value.tagIdSet shouldBe emptySet()
        relaunchedViewModels.tagViewModel.selectionUiState.value.primaryTagId shouldBe null
        relaunchedViewModels.webViewModel.selectionUiState.value.webIdSet shouldBe emptySet()
        relaunchedViewModels.contactViewModel.selectionUiState.value.contactIdSet shouldBe emptySet()
        relaunchedViewModels.placeViewModel.selectionUiState.value.placeIdSet shouldBe emptySet()
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

    // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 선택을 들고 있던 상태 객체도 새로 만들어지므로,
    // 새 키로 저장 상태 없이 같은 진입 경로의 새 인스턴스와 함께 화면을 다시 구성한다.
    private fun setRelaunchableMemoAddScreen(
        initialDateTime: DiaryDateTimeInputValue? = null,
        createViewModels: () -> MemoAddScreenViewModels,
    ): List<MemoAddScreenViewModels> {
        val viewModelsList = mutableListOf<MemoAddScreenViewModels>()
        composeRule.setContent {
            key(launchCount) {
                val viewModels = remember { createViewModels().also(viewModelsList::add) }

                MemoAddScreenTestTheme {
                    MemoryRestoreTestMemoAddScreen(viewModels = viewModels, initialDateTime = initialDateTime)
                }
            }
        }
        composeRule.waitForIdle()

        return viewModelsList
    }

    private fun relaunch(viewModelsList: List<MemoAddScreenViewModels>) {
        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()
        viewModelsList.size shouldBe 2
    }

    private companion object {
        private const val TARGET_TAG_TITLE = "MemoryRestoreTargetTag"
        private const val OTHER_TAG_TITLE = "MemoryRestoreOtherTag"
        private const val PLACE_TITLE = "MemoryRestorePlace"
        private const val WEB_TITLE = "MemoryRestoreWeb"
        private const val CONTACT_NAME = "MemoryRestoreContact"
        private const val OTHER_PLACE_TITLE = "MemoryRestoreOtherPlace"
        private const val OTHER_WEB_TITLE = "MemoryRestoreOtherWeb"
        private const val OTHER_CONTACT_NAME = "MemoryRestoreOtherContact"
    }
}

@Composable
private fun MemoryRestoreTestMemoAddScreen(
    viewModels: MemoAddScreenViewModels,
    initialDateTime: DiaryDateTimeInputValue? = null,
) {
    MemoAddScreen(
        addRequestKey = TEST_ADD_REQUEST_KEY,
        addViewModel = viewModels.viewModel,
        tagViewModel = viewModels.tagViewModel,
        webViewModel = viewModels.webViewModel,
        contactViewModel = viewModels.contactViewModel,
        placeViewModel = viewModels.placeViewModel,
        placeMapViewModel = remember { screenTestPlaceMapViewModel() },
        placePickerMapViewModel = remember { screenTestPlacePickerMapViewModel() },
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
        initialDateTime = initialDateTime,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
        isStandalone = true,
    )
}
