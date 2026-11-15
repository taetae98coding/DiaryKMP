package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlaceDetail
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction
import io.github.taetae98coding.diary.work.appaction.place.appActionCoordinateText

public data class DiaryAppActionPlacePage(
    val items: List<DiaryAppActionPlace>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionPlaceDetail(
    val place: DiaryAppActionPlace,
    val tags: List<DiaryAppActionTag>,
)

public object DiaryPlaceAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionPlacePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> placeAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionPlacePage(items = page.items.map { place -> place.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun listInArea(
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: DiaryAppActionSort?,
    ): List<DiaryAppActionPlace> =
        placeAppAction()
            .listInArea(
                south = appActionCoordinateText(value = south),
                north = appActionCoordinateText(value = north),
                west = appActionCoordinateText(value = west),
                east = appActionCoordinateText(value = east),
                sort = sort.toSortValue(),
            ).map { place -> place.toDiary() }

    public suspend fun get(placeId: String): DiaryAppActionPlaceDetail? = placeAppAction().get(placeId = placeId)?.toDiary()

    public suspend fun getIncludingDeleted(placeId: String): DiaryAppActionPlaceDetail? = placeAppAction().getIncludingDeleted(placeId = placeId)?.toDiary()

    public suspend fun add(
        title: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        placeDescription: String?,
        color: String?,
        tagIds: List<String>,
    ): String =
        placeAppAction()
            .add(
                title = title,
                latitude = appActionCoordinateText(value = latitude),
                longitude = appActionCoordinateText(value = longitude),
                address = address,
                description = placeDescription,
                color = color,
                tagIds = tagIds,
            ).id

    public suspend fun update(
        placeId: String,
        title: String?,
        placeDescription: String?,
        color: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
    ): Int =
        placeAppAction()
            .update(
                placeId = placeId,
                title = title,
                description = placeDescription,
                color = color,
                address = address,
                latitude = latitude?.let { value -> appActionCoordinateText(value = value) },
                longitude = longitude?.let { value -> appActionCoordinateText(value = value) },
            ).changedCount

    public suspend fun delete(placeId: String): Int = placeAppAction().delete(placeId = placeId).changedCount

    public suspend fun restore(placeId: String): Int = placeAppAction().restore(placeId = placeId).changedCount
}

private fun placeAppAction(): PlaceAppAction = diaryAppAction()

private fun AppActionPlaceDetail.toDiary(): DiaryAppActionPlaceDetail = DiaryAppActionPlaceDetail(place = place.toDiary(), tags = tags.map { tag -> tag.toDiary() })
