package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.qr.ui.DEFAULT_ENTITY_TAG_LABEL
import io.github.taetae98coding.diary.feature.qr.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.qr.ui.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.qr.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.qr.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-a6b4e5a1-4a94-4e4b-a51c-cb32efbcd577 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        setQrAddScreen(
            addViewModel = effectViewModel(effect = QrAddEffect.AddSucceeded),
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        composeRule.onTitleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        clickAdd()

        composeRule.titleInputText() shouldBe ""
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-a6fde504-b12a-4648-9b7f-cad5ca5d3fd0 추가할 때 고른 태그를 함께 넘긴다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val addViewModel = effectViewModel(effect = QrAddEffect.AddSucceeded)
        setQrAddScreen(addViewModel = addViewModel, tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)))

        clickAdd()

        verify(exactly = 1) { addViewModel.add(detail = any(), tagIdSet = setOf(tag.id)) }
    }

    @Test
    fun `TC-418bc4dd-077d-4558-a43d-e9dd5ba4e003 탭을 바꿔도 고른 태그가 유지된다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        setQrAddScreen(tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()

        composeRule.selectTab(INFO_TAB_INDEX)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-102e70af-e5fc-4495-97dd-41bc2a53dec6 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrAddScreenContent(addViewModel = remember { screenTestViewModel() }, tagViewModel = tagViewModel)
        }
        composeRule.onTitleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe TYPED_TITLE
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-33312428-abb8-4346-8440-8788ac4455fa TagDetail QR 탭에서 진입했으면 복원 뒤 입력은 남고 태그는 그 탭의 태그만 선택된다`() {
        assertMemoryRestore(initialTag = Arb.tag(title = INITIAL_TAG_TITLE).next(), expectedTitleListAfterRestore = listOf(INITIAL_TAG_TITLE))
    }

    @Test
    fun `TC-33312428-abb8-4346-8440-8788ac4455fa 그 밖의 경로로 진입했으면 복원 뒤 입력은 남고 선택된 태그가 없다`() {
        assertMemoryRestore(initialTag = null, expectedTitleListAfterRestore = emptyList())
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 고른 태그로 더한다`() {
        val tagViewModel = addTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        val tagId = Uuid.random()
        setQrAddScreen(tagViewModel = tagViewModel, resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = tagId)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = tagId) }
    }

    @Test
    fun `고를 수 있는 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setQrAddScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    private fun assertMemoryRestore(
        initialTag: Tag?,
        expectedTitleListAfterRestore: List<String>,
    ) {
        val otherTag = Arb.tag(title = OTHER_TAG_TITLE).next()
        val allTagList = listOfNotNull(initialTag, otherTag)
        val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel = remember { tagViewModel(initialTagId = initialTag?.id, allTagList = allTagList).also(tagViewModelList::add) }

            QrAddScreenContent(addViewModel = remember { screenTestViewModel() }, tagViewModel = tagViewModel)
        }
        composeRule.waitForIdle()
        composeRule.onTitleInput().performTextInput(TYPED_TITLE)
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.titleInputText() shouldBe TYPED_TITLE
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        expectedTitleListAfterRestore.forEach { title -> composeRule.onNodeWithText(title).assertExists() }
        composeRule.runOnIdle {
            tagViewModelList
                .last()
                .selectionUiState.value.tagIdSet shouldBe setOfNotNull(initialTag?.id)
        }
    }

    private fun tagViewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): EntityAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns MutableStateFlow(Result.success(PagingData.from(allTagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            MutableStateFlow(Result.success(allTagList.filter { tag -> tag.id in idSet }))
        }

        return EntityAddTagViewModel(initialTagId = initialTagId, pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase)
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setQrAddScreen(
        addViewModel: QrAddViewModel = screenTestViewModel(),
        tagViewModel: EntityAddTagViewModel = addTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            QrAddScreenContent(
                addViewModel = addViewModel,
                tagViewModel = tagViewModel,
                navigateToTagAdd = navigateToTagAdd,
                resultEventBus = resultEventBus,
            )
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun QrAddScreenContent(
        addViewModel: QrAddViewModel,
        tagViewModel: EntityAddTagViewModel,
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = remember { ResultEventBus() },
    ) {
        DiaryTheme {
            QrAddScreen(
                navigateUp = {},
                navigateToScan = {},
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = {},
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                permissionManager = rememberPermissionManager(),
                resultEventBus = resultEventBus,
                componentVisibleProvider = { QrAddScaffoldComponentVisible() },
                addViewModel = addViewModel,
                tagViewModel = tagViewModel,
            )
        }
    }

    private companion object {
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val TYPED_TITLE = "QrAddTagTypedTitle"
        const val SELECTED_TAG_TITLE = "QrAddSelectedTag"
        const val INITIAL_TAG_TITLE = "QrAddRestoreInitialTag"
        const val OTHER_TAG_TITLE = "QrAddRestoreOtherTag"
    }
}
