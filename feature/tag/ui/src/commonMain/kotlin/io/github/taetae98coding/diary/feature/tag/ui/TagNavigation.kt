package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToTagDetail(id: Uuid) {
    while (lastOrNull().isTagDetailPaneKey()) {
        removeLastOrNull()
    }

    add(TagDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToTagAdd() {
    add(TagAddNavKey())
}

// 목록에서 고른 상세는 다른 태그를 고를 때 같은 자리가 새 대상으로 바뀌는 것이므로, 대상이 바뀌어도 한 화면으로 이어지게 내용 키를 고정한다.
// 이 화면은 대상에 따라 초기화할 상태와 이어 갈 장소 탭 상태를 스스로 나눈다.
// 연결이 순환하면 같은 태그의 상세가 전환 이력에 여러 번 놓이므로, 다른 경로로 연 상세는 대상 대신 놓인 자리로 구분한다.
internal fun List<ScreenNavKey>.tagDetailContentKey(key: TagDetailNavKey): Any {
    val index = entryIndexOf(key)

    return if (index > 0 && this[index - 1] == TagHomeNavKey) {
        LIST_SELECTED_TAG_DETAIL_CONTENT_KEY
    } else {
        "$key@$index"
    }
}

private fun ScreenNavKey?.isTagDetailPaneKey(): Boolean = this is TagDetailNavKey || this is TagAddNavKey

private const val LIST_SELECTED_TAG_DETAIL_CONTENT_KEY: String = "TagDetailNavKey(listSelected)"

internal fun List<ScreenNavKey>.isNavigatedFromTagDetail(key: ScreenNavKey): Boolean {
    val index = entryIndexOf(key)

    return index > 0 && this[index - 1] is TagDetailNavKey
}

// 같은 값의 키가 여러 자리에 놓일 수 있어 값이 아니라 전환 이력에 넣은 그 인스턴스로 자리를 찾는다.
// 전환 이력의 항목은 놓일 때마다 새로 만들어지므로 인스턴스가 곧 한 자리를 가리킨다.
private fun List<ScreenNavKey>.entryIndexOf(key: ScreenNavKey): Int {
    val index = indexOfFirst { entry -> entry === key }

    return if (index >= 0) index else lastIndexOf(key)
}

internal fun NavBackStack<ScreenNavKey>.navigateToWebAddFromTagDetail(tagId: Uuid) {
    add(WebAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToPlaceAddFromTagDetail(
    tagId: Uuid,
    coordinate: Coordinate?,
) {
    add(
        PlaceAddNavKey(
            latitude = coordinate?.latitude,
            longitude = coordinate?.longitude,
            initialTagId = tagId,
        ),
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromTagMemoFinishedList() {
    val index = indexOfLast { key -> key is TagMemoFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
