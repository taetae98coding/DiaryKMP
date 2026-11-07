package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Immutable
import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

@Immutable
internal class TagDetailNavigation(
    val navigateUp: () -> Unit,
    val navigateToTagAdd: () -> Unit,
    val navigateToDetail: (Uuid) -> Unit,
    val navigateToMemoAdd: () -> Unit,
    val navigateToMemoDetail: (Uuid) -> Unit,
    val navigateToMemoFinishedList: () -> Unit,
    val navigateToWebAdd: () -> Unit,
    val navigateToWebDetail: (Uuid) -> Unit,
    val navigateToPlaceAdd: (Coordinate?) -> Unit,
    val navigateToPlaceDetail: (Uuid) -> Unit,
    val navigateToContactAdd: () -> Unit,
    val navigateToContactDetail: (Uuid) -> Unit,
    val navigateToChecklistAdd: () -> Unit,
    val navigateToChecklistDetail: (Uuid) -> Unit,
    val navigateToMusicAdd: () -> Unit,
    val navigateToMusicDetail: (Uuid) -> Unit,
    val navigateToQrAdd: () -> Unit,
    val navigateToQrDetail: (Uuid) -> Unit,
)
