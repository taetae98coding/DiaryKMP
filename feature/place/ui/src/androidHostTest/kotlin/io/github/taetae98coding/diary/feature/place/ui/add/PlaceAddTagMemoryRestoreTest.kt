package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddTagMemoryRestoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var restorationTesterHolder: StateRestorationTester

    @Test
    fun `TC-baeab7b1-e21d-42dd-b491-17b9fc0d4559 TagDetail 장소 탭에서 진입했으면 메모리 정리 뒤 복원할 때 대상 태그만 선택된다`() {
        val targetTag = placeTestTag(title = TARGET_TAG_TITLE)
        val otherTag = placeTestTag(title = OTHER_TAG_TITLE)
        val viewModelList = setRestorablePlaceAddScreen(initialTagId = targetTag.id, tagList = listOf(targetTag, otherTag))
        composeRule.runOnIdle { viewModelList.last().add(id = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restore(viewModelList)

        composeRule.onNodeWithText(TARGET_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        viewModelList
            .last()
            .selectionUiState.value.tagIdSet shouldBe setOf(targetTag.id)
    }

    @Test
    fun `TC-baeab7b1-e21d-42dd-b491-17b9fc0d4559 그 밖의 경로로 진입했으면 메모리 정리 뒤 복원할 때 선택된 태그가 없다`() {
        val tag = placeTestTag(title = OTHER_TAG_TITLE)
        val viewModelList = setRestorablePlaceAddScreen(initialTagId = null, tagList = listOf(tag))
        composeRule.runOnIdle { viewModelList.last().add(id = tag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restore(viewModelList)

        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        viewModelList
            .last()
            .selectionUiState.value.tagIdSet shouldBe emptySet()
    }

    @Test
    fun `TC-baeab7b1-e21d-42dd-b491-17b9fc0d4559 메모리 정리 뒤 복원하면 입력 내용과 컬러, 기본 지도를 다시 확인한 뒤의 지도 제공자와 위치가 복원된다`() {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val restorationTester = StateRestorationTester(composeRule)
        val defaultProviderHistory = mutableListOf<MapProvider?>()
        lateinit var state: PlaceFormState
        restorationTester.setContent {
            // 화면을 만들 때마다 기본 지도는 확인하기 전까지 비어 있다가 확인된 값으로 바뀐다.
            var defaultProvider by remember { mutableStateOf<MapProvider?>(null) }
            LaunchedEffect(Unit) { defaultProvider = MapProvider.NAVER }
            defaultProviderHistory += defaultProvider
            state = rememberPlaceAddFormState(defaultProvider = defaultProvider)
        }
        var savedColor = Color.Unspecified
        composeRule.runOnIdle {
            state.titleState.setText(TYPED_TITLE)
            state.descriptionState.setText(TYPED_DESCRIPTION)
            state.addressState.setTextAndPlaceCursorAtEnd(TYPED_ADDRESS)
            state.setCoordinate(coordinate)
            // 컬러는 화면을 새로 만들 때마다 무작위로 정해지므로, 복원 뒤에도 같으면 복원된 것이다.
            savedColor = state.colorState.color
            state.mapState.select(DiaryMapProvider.GOOGLE)
            state.mapState.moveTo(coordinate)
        }
        val savedLatitude = composeRule.runOnIdle { state.latitudeState.text.toString() }
        val savedLongitude = composeRule.runOnIdle { state.longitudeState.text.toString() }

        defaultProviderHistory.clear()
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            defaultProviderHistory shouldBe listOf(null, MapProvider.NAVER)
            state.mapState.provider shouldBe DiaryMapProvider.GOOGLE
            state.mapState.coordinate shouldBe coordinate
            state.titleState.text.toString() shouldBe TYPED_TITLE
            state.descriptionState.text.toString() shouldBe TYPED_DESCRIPTION
            state.addressState.text.toString() shouldBe TYPED_ADDRESS
            state.latitudeState.text.toString() shouldBe savedLatitude
            state.longitudeState.text.toString() shouldBe savedLongitude
            state.colorState.color shouldBe savedColor
        }
    }

    // 메모리 정리 뒤에는 태그 선택을 들고 있던 상태 객체도 새로 만들어지므로, 복원할 때마다 같은 진입 경로의 새 인스턴스를 쓴다.
    private fun setRestorablePlaceAddScreen(
        initialTagId: Uuid?,
        tagList: List<Tag>,
    ): List<PlaceAddTagViewModel> {
        val viewModelList = mutableListOf<PlaceAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTesterHolder = restorationTester
        restorationTester.setContent {
            val tagViewModel = remember { tagViewModel(initialTagId = initialTagId, tagList = tagList).also(viewModelList::add) }

            PlaceAddScreenTestTheme {
                PlaceAddScreen(
                    addedResultRequestKey = null,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    initialCoordinate = null,
                    addViewModel = remember { screenTestViewModel() },
                    searchViewModel = remember { searchScreenTestViewModel() },
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()

        return viewModelList
    }

    private fun restore(viewModelList: List<PlaceAddTagViewModel>) {
        restorationTesterHolder.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        viewModelList.size shouldBe 2
    }

    // 선택한 태그 조회는 선택할 수 있는 태그 가운데 요청한 식별자의 태그를 돌려준다.
    private fun tagViewModel(
        initialTagId: Uuid?,
        tagList: List<Tag>,
    ): PlaceAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(tagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val tagIdSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(tagList.filter { tag -> tag.id in tagIdSet }))
        }

        return PlaceAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        private const val TARGET_TAG_TITLE = "PlaceAddRestoreTargetTag"
        private const val OTHER_TAG_TITLE = "PlaceAddRestoreOtherTag"
        private const val TYPED_TITLE = "복원할 장소"
        private const val TYPED_DESCRIPTION = "복원할 설명"
        private const val TYPED_ADDRESS = "복원할 주소"

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
