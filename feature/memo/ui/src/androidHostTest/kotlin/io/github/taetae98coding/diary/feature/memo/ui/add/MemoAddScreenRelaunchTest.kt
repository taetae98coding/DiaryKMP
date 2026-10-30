package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.DEFAULT_CONTACT_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RELAUNCH_ADDED_TITLE: String = "MemoAddRelaunchAdded"

/**
 * 추가 화면으로 이동한 뒤 앱이 종료되면 추가 결과를 전달할 화면 상태와 결과 전달 경로가 모두 사라진다.
 * 앱을 다시 실행해 새로 진입하는 것은 새 키, 새 결과 전달 경로, 새 인스턴스로 화면을 다시 구성하는 것으로 재현한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-16d465ba-82c9-4421-b820-e412f1666456 연락처를 추가한 뒤 앱을 종료하고 새로 진입하면 추가한 연락처가 자동 선택되지 않는다`() {
        val addedContact = testContact(name = RELAUNCH_ADDED_TITLE)

        assertAddedItemNotSelectedAfterRelaunch(selectLabel = DEFAULT_CONTACT_SELECT_LABEL) { isRelaunched ->
            screenTestRealViewModel(contactList = if (isRelaunched) listOf(addedContact) else emptyList())
        }
    }

    @Test
    fun `TC-0bc68908-83de-4e1e-a4ae-d7d35afa803d 웹 항목을 추가한 뒤 앱을 종료하고 새로 진입하면 추가한 웹 항목이 자동 선택되지 않는다`() {
        val addedWeb = testWeb(title = RELAUNCH_ADDED_TITLE)

        assertAddedItemNotSelectedAfterRelaunch(selectLabel = DEFAULT_WEB_SELECT_LABEL) { isRelaunched ->
            screenTestRealViewModel(webList = if (isRelaunched) listOf(addedWeb) else emptyList())
        }
    }

    @Test
    fun `TC-1ecbb224-ea36-4be3-b4ec-ff2a1e2b8389 태그를 추가한 뒤 앱을 종료하고 새로 진입하면 추가한 태그가 자동 선택되지 않는다`() {
        val addedTag = testTag(title = RELAUNCH_ADDED_TITLE)

        assertAddedItemNotSelectedAfterRelaunch(selectLabel = DEFAULT_TAG_SELECT_LABEL) { isRelaunched ->
            screenTestRealViewModel(tagList = if (isRelaunched) listOf(addedTag) else emptyList())
        }
    }

    @Test
    fun `TC-c24f3789-a72a-408c-a5a8-f0bff1024cd3 장소를 추가한 뒤 앱을 종료하고 새로 진입하면 추가한 장소가 자동 선택되지 않는다`() {
        val addedPlace = testPlace(title = RELAUNCH_ADDED_TITLE)

        assertAddedItemNotSelectedAfterRelaunch(selectLabel = DEFAULT_PLACE_SELECT_LABEL) { isRelaunched ->
            screenTestRealViewModel(placeList = if (isRelaunched) listOf(addedPlace) else emptyList())
        }
    }

    // 선택할 수 있는 항목이 없으면 추가 항목의 첫 누름이 추가 화면 이동을 요청하므로, 그 누름으로 추가 화면에 다녀온 상태를 만든다.
    private fun assertAddedItemNotSelectedAfterRelaunch(
        selectLabel: String,
        viewModelsOf: (isRelaunched: Boolean) -> MemoAddScreenViewModels,
    ) {
        var launchCount by mutableIntStateOf(0)
        var addNavigationCount = 0
        composeRule.setContent {
            key(launchCount) {
                val viewModels = remember { viewModelsOf(launchCount > 0) }
                MemoAddScreenTestTheme(resultEventBus = remember { ResultEventBus() }) {
                    MemoAddScreen(
                        addRequestKey = TEST_ADD_REQUEST_KEY,
                        addViewModel = viewModels.viewModel,
                        tagViewModel = viewModels.tagViewModel,
                        webViewModel = viewModels.webViewModel,
                        contactViewModel = viewModels.contactViewModel,
                        placeViewModel = viewModels.placeViewModel,
                        placeMapViewModel = screenTestPlaceMapViewModel(),
                        placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                        geminiViewModel = screenTestGeminiViewModel(),
                        navigateUp = {},
                        navigateToTagAdd = { addNavigationCount += 1 },
                        navigateToTagDetail = {},
                        navigateToWebAdd = { addNavigationCount += 1 },
                        navigateToWebDetail = {},
                        navigateToContactAdd = { addNavigationCount += 1 },
                        navigateToContactDetail = {},
                        navigateToPlaceAdd = { addNavigationCount += 1 },
                        navigateToPlaceDetail = {},
                        initialDateTime = null,
                        componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                        isStandalone = true,
                    )
                }
            }
        }
        // 장소 카드의 추가 항목은 지도 영역과 겹칠 수 있어 위치를 누르는 대신 누름 동작을 직접 실행한다.
        composeRule.onNodeWithText(selectLabel).performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        addNavigationCount shouldBe 1

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(RELAUNCH_ADDED_TITLE).assertDoesNotExist()
    }
}
