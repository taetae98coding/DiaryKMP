package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddInitialValueTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `공유로 연 화면은 받은 제목과 설명을 채우고 기간은 고르지 않는다`() {
        setMemoAddScreen(key = MemoAddNavKey(initialTitle = SHARED_TITLE, initialDescription = SHARED_DESCRIPTION, isStandalone = true))

        composeRule.titleInput().assert(hasText(SHARED_TITLE))
        composeRule.descriptionInput().assert(hasText(SHARED_DESCRIPTION))
        composeRule.onNode(hasRole(Role.Switch)).assertIsOff()
    }

    @Test
    fun `일정으로 연 화면은 제목, 설명과 기간을 함께 채운다`() {
        val start = LocalDate(year = 2026, month = Month.JULY, day = 14)
        val endInclusive = LocalDate(year = 2026, month = Month.JULY, day = 17)
        setMemoAddScreen(
            key =
                MemoAddNavKey(
                    initialDateRange = MemoAddNavKey.InitialDateRange(start = start, endInclusive = endInclusive),
                    initialTitle = SHARED_TITLE,
                    initialDescription = SHARED_DESCRIPTION,
                    isStandalone = true,
                ),
        )

        composeRule.titleInput().assert(hasText(SHARED_TITLE))
        composeRule.descriptionInput().assert(hasText(SHARED_DESCRIPTION))
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onNodeWithText(start.toDefaultDisplayText()).assertExists()
        composeRule.onNodeWithText(endInclusive.toDefaultDisplayText()).assertExists()
    }

    @Test
    fun `공유로 연 화면도 처음 진입할 때와 같이 제목 입력에 초점이 있다`() {
        setMemoAddScreen(key = MemoAddNavKey(initialTitle = SHARED_TITLE, initialDescription = SHARED_DESCRIPTION, isStandalone = true))

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-f49a958d-1e39-4a0c-afba-4bb224e063da 공유로 연 화면은 태그, 웹, 연락처, 장소 선택 없이 시작한다`() {
        val key = MemoAddNavKey(initialTitle = SHARED_TITLE, isStandalone = true)
        val viewModels =
            screenTestRealViewModel(
                initialPrimaryTagId = key.primaryTagId,
                initialContactId = key.initialContactId,
                initialPlaceId = key.initialPlaceId,
                initialWebId = key.initialWebId,
                tagList = listOf(testTag(title = STORED_TAG_TITLE)),
                webList = listOf(testWeb(title = STORED_WEB_TITLE)),
                contactList = listOf(testContact(name = STORED_CONTACT_NAME)),
                placeList = listOf(testPlace(title = STORED_PLACE_TITLE)),
            )

        setMemoAddScreen(key = key, viewModels = viewModels)

        listOf(STORED_TAG_TITLE, STORED_WEB_TITLE, STORED_CONTACT_NAME, STORED_PLACE_TITLE).forEach { text ->
            composeRule.onNodeWithText(text).assertDoesNotExist()
        }
        composeRule.onAllNodes(hasPrimaryTagState()).assertCountEquals(0)
        viewModels.tagViewModel.selectionUiState.value.tagIdSet shouldBe emptySet()
        viewModels.tagViewModel.selectionUiState.value.primaryTagId shouldBe null
        viewModels.webViewModel.selectionUiState.value.webIdSet shouldBe emptySet()
        viewModels.contactViewModel.selectionUiState.value.contactIdSet shouldBe emptySet()
        viewModels.placeViewModel.selectionUiState.value.placeIdSet shouldBe emptySet()
    }

    @Test
    fun `TC-713137bd-b0dc-4b28-ac7f-6fbbe6fae8d4 공유로 화면을 열기만 해서는 메모를 추가하지 않는다`() {
        val addMemoUseCase = mockk<AddMemoUseCase>()

        setMemoAddScreen(
            key = MemoAddNavKey(initialTitle = SHARED_TITLE, initialDescription = SHARED_DESCRIPTION, isStandalone = true),
            viewModels = screenTestRealViewModel(addMemoUseCase = addMemoUseCase),
        )

        coVerify(exactly = 0) { addMemoUseCase(parameter = any()) }
    }

    @Test
    fun `공유로 채운 값을 고친 뒤 화면이 재생성되면 고친 값을 유지한다`() {
        val key = MemoAddNavKey(initialTitle = SHARED_TITLE, initialDescription = SHARED_DESCRIPTION, isStandalone = true)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            InitialValueMemoAddScreen(key = key, viewModels = remember { screenTestViewModel() })
        }
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(SHARED_DESCRIPTION))
    }

    @Test
    fun `TC-a97443d4-bf42-4ed0-8f26-c6ae3eda0d8c 공유로 진입한 화면을 복원하면 태그 선택 없이 시작한다`() {
        val key = MemoAddNavKey(initialTitle = SHARED_TITLE, isStandalone = true)
        val primaryTag = testTag(title = STORED_TAG_TITLE)
        val otherTag = testTag(title = OTHER_TAG_TITLE)
        val viewModelsList = mutableListOf<MemoAddScreenViewModels>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            val viewModels =
                remember {
                    screenTestRealViewModel(
                        initialPrimaryTagId = key.primaryTagId,
                        initialContactId = key.initialContactId,
                        initialPlaceId = key.initialPlaceId,
                        initialWebId = key.initialWebId,
                        tagList = listOf(primaryTag, otherTag),
                    ).also(viewModelsList::add)
                }

            InitialValueMemoAddScreen(key = key, viewModels = viewModels)
        }
        composeRule.runOnIdle {
            viewModelsList.last().tagViewModel.selectTag(id = otherTag.id)
            viewModelsList.last().tagViewModel.selectPrimaryTag(id = primaryTag.id)
        }
        composeRule.runOnIdle {
            viewModelsList
                .last()
                .tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(primaryTag.id, otherTag.id)
        }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(STORED_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(SHARED_TITLE))
        composeRule.runOnIdle {
            val selection =
                viewModelsList
                    .last()
                    .tagViewModel.selectionUiState.value

            viewModelsList.size shouldBe 2
            selection.tagIdSet shouldBe emptySet()
            selection.primaryTagId shouldBe null
        }
    }

    private fun setMemoAddScreen(
        key: MemoAddNavKey,
        viewModels: MemoAddScreenViewModels = screenTestViewModel(),
    ) {
        composeRule.setContent {
            InitialValueMemoAddScreen(key = key, viewModels = viewModels)
        }
        composeRule.waitForIdle()
    }

    private fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction()).onFirst()

    private fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

    private companion object {
        const val SHARED_TITLE = "장보기"
        const val SHARED_DESCRIPTION = "우유\n계란"
        const val TYPED_TITLE = "MemoTitleInput"
        const val STORED_TAG_TITLE = "SharedStoredTag"
        const val OTHER_TAG_TITLE = "SharedOtherTag"
        const val STORED_WEB_TITLE = "SharedStoredWeb"
        const val STORED_CONTACT_NAME = "SharedStoredContact"
        const val STORED_PLACE_TITLE = "SharedStoredPlace"

        val DEFAULT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

        fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"
    }
}

@Composable
private fun InitialValueMemoAddScreen(
    key: MemoAddNavKey,
    viewModels: MemoAddScreenViewModels,
) {
    MemoAddScreenTestTheme {
        MemoAddScreen(
            navigateUp = {},
            navigateToTagAdd = {},
            navigateToTagDetail = {},
            navigateToWebAdd = {},
            navigateToWebDetail = {},
            navigateToContactAdd = {},
            navigateToContactDetail = {},
            navigateToPlaceAdd = {},
            navigateToPlaceDetail = {},
            navigateToGeminiSetting = {},
            initialTitle = key.initialTitle,
            initialDescription = key.initialDescription,
            initialDateTime = key.initialDateTime(),
            addRequestKey = TEST_ADD_REQUEST_KEY,
            componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = true) },
            isStandalone = true,
            addViewModel = viewModels.viewModel,
            tagViewModel = viewModels.tagViewModel,
            webViewModel = viewModels.webViewModel,
            contactViewModel = viewModels.contactViewModel,
            placeViewModel = viewModels.placeViewModel,
            placeMapViewModel = remember { screenTestPlaceMapViewModel() },
            placePickerMapViewModel = remember { screenTestPlacePickerMapViewModel() },
            geminiViewModel = remember { screenTestGeminiViewModel() },
        )
    }
}
