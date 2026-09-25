package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchUiState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * PlaceAdd 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun PlaceAddScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

internal const val TYPED_TITLE = "PlaceTitleInput"
internal const val TYPED_DESCRIPTION = "PlaceDescriptionInput"
internal const val TYPED_ADDRESS = "PlaceAddressInput"
internal const val TYPED_LATITUDE = "37.5665"
internal const val TYPED_LONGITUDE = "126.9780"
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add place"
internal const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search place"
internal const val DESCRIPTION_INDEX = 1
internal const val ADDRESS_INDEX = 2
internal const val LATITUDE_INDEX = 3
internal const val LONGITUDE_INDEX = 4

private val hexRegex = Regex(pattern = "#[0-9A-F]{6}")

internal fun screenTestViewModel(
    effect: Flow<PlaceAddEffect> = emptyFlow(),
    uiState: PlaceAddUiState = PlaceAddUiState(),
): PlaceAddViewModel {
    val viewModel = mockk<PlaceAddViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun searchScreenTestViewModel(uiState: PlaceSearchUiState = PlaceSearchUiState.Idle): PlaceSearchViewModel {
    val viewModel = mockk<PlaceSearchViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}

internal fun hasHexText(): SemanticsMatcher =
    SemanticsMatcher(description = "Hex color text") { node ->
        node.config
            .getOrNull(SemanticsProperties.Text)
            ?.any { hexRegex.containsMatchIn(it.text) } == true
    }

internal fun ComposeContentTestRule.currentColorHex(): String =
    onNode(hasHexText() and hasClickAction())
        .fetchSemanticsNode()
        .config[SemanticsProperties.Text]
        .firstNotNullOf { text -> hexRegex.find(text.text)?.value }

internal fun addTagScreenTestViewModel(tagList: List<Tag> = emptyList()): PlaceAddTagViewModel {
    val viewModel = mockk<PlaceAddTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagIdSet } returns MutableStateFlow(tagList.map { tag -> tag.id }.toSet())
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    return viewModel
}

internal fun placeTestTag(title: String): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = "", title = title, description = "", color = 0xFF3A7BD5),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun ComposeContentTestRule.setPlaceAddScreen(
    viewModel: PlaceAddViewModel,
    navigateUp: () -> Unit = {},
    navigateToTagDetail: (Uuid) -> Unit = {},
    searchViewModel: PlaceSearchViewModel = searchScreenTestViewModel(),
    tagViewModel: PlaceAddTagViewModel = addTagScreenTestViewModel(),
) {
    setContent {
        PlaceAddScreenTestTheme {
            PlaceAddScreen(
                navigateToTagAdd = {},
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                navigateUp = navigateUp,
                initialCoordinate = null,
                addViewModel = viewModel,
                searchViewModel = searchViewModel,
                navigateToTagDetail = navigateToTagDetail,
                tagViewModel = tagViewModel,
            )
        }
    }
}

internal fun ComposeContentTestRule.setPlaceAddScaffoldState(): PlaceFormState {
    lateinit var state: PlaceFormState

    setContent {
        DiaryTheme {
            state = rememberPlaceAddFormState()
        }
    }
    waitForIdle()

    return state
}

internal fun FixtureMonkey.mapCoordinateInFormPrecision(): DiaryMapCoordinate = coordinateInFormPrecision().toDiaryMapCoordinate()
