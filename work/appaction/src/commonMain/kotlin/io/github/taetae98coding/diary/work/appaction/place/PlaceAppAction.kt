package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrRandom
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import org.koin.core.annotation.Factory

private const val PLACE_ID = "placeId"

internal val placeSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class PlaceAppAction internal constructor(
    private val pagePlaceHomeUseCase: PagePlaceHomeUseCase,
    private val getPlaceListUseCase: GetPlaceListUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addPlaceUseCase: AddPlaceUseCase,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionPlace> =
        runAppAction {
            pagePlaceHomeUseCase
                .readPage(parameter = sort.toSortArgument(placeSortList), offset = cursor.toPageOffset())
                .map { place -> place.toAppActionPlace() }
        }

    public suspend fun listInArea(
        south: String,
        north: String,
        west: String,
        east: String,
        sort: String?,
    ): List<AppActionPlace> =
        runAppAction {
            val parameter =
                GetPlaceListUseCase.Parameter(
                    bounds = coordinateBoundsArgument(south = south, north = north, west = west, east = east),
                    sort = sort.toSortArgument(placeSortList),
                )

            getPlaceListUseCase.readFirst(parameter = parameter).map { place -> place.toAppActionPlace() }
        }

    public suspend fun get(placeId: String): AppActionPlaceDetail? = runAppAction { detail(placeId = placeId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(placeId: String): AppActionPlaceDetail? = runAppAction { detail(placeId = placeId, includesDeleted = true) }

    private suspend fun detail(
        placeId: String,
        includesDeleted: Boolean,
    ): AppActionPlaceDetail? {
        val id = placeId.toUuidArgument(name = PLACE_ID)
        val place = findPlaceUseCase.readFirst(parameter = id)?.takeIf { place -> includesDeleted || !place.isDeleted } ?: return null

        return AppActionPlaceDetail(
            place = place.toAppActionPlace(),
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
        title: String,
        latitude: String,
        longitude: String,
        address: String?,
        description: String?,
        color: String?,
        tagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddPlaceUseCase.Parameter(
                    detail =
                        PlaceDetail(
                            title = title,
                            description = description.orEmpty(),
                            color = color.toColorArgumentOrRandom(),
                            coordinate = Coordinate(latitude = latitude.toCoordinateArgument(), longitude = longitude.toCoordinateArgument()),
                            address = address.orEmpty(),
                        ),
                    tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"),
                )

            addPlaceUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        placeId: String,
        title: String?,
        description: String?,
        color: String?,
        address: String?,
        latitude: String?,
        longitude: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = placeId.toUuidArgument(name = PLACE_ID)
            val current = findPlaceUseCase.readActive(id = id, isDeleted = Place::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                PlaceDetail(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    color = color.toColorArgumentOrNull() ?: current.color,
                    coordinate =
                        Coordinate(
                            latitude = latitude.toCoordinateArgumentOrNull() ?: current.coordinate.latitude,
                            longitude = longitude.toCoordinateArgumentOrNull() ?: current.coordinate.longitude,
                        ),
                    address = address ?: current.address,
                )

            updatePlaceUseCase(parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun delete(placeId: String): AppActionChangedCount =
        runAppAction {
            val id = placeId.toUuidArgument(name = PLACE_ID)

            findPlaceUseCase.changeActive(id = id, isDeleted = Place::isDeleted) { deletePlaceUseCase(parameter = id) }
        }

    public suspend fun restore(placeId: String): AppActionChangedCount = runAppAction { restorePlaceUseCase(parameter = placeId.toUuidArgument(name = PLACE_ID)).getOrThrow().toAppActionChangedCount() }
}
