package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.map
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
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.memoSortList
import io.github.taetae98coding.diary.work.appaction.memo.toAppActionMemo
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import org.koin.core.annotation.Factory

private const val PLACE_ID = "placeId"
private const val TAG_ID = "tagId"

internal val placeSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class PlaceAppAction internal constructor(
    private val pagePlaceHomeUseCase: PagePlaceHomeUseCase,
    private val getPlaceListUseCase: GetPlaceListUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val pagePlaceMemoUseCase: PagePlaceMemoUseCase,
    private val pageFinishedPlaceMemoUseCase: PageFinishedPlaceMemoUseCase,
    private val addPlaceUseCase: AddPlaceUseCase,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
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

    public suspend fun get(placeId: String): AppActionPlaceDetail? =
        runAppAction {
            val id = placeId.toUuidArgument(name = PLACE_ID)
            val place = findPlaceUseCase.readFirst(parameter = id) ?: return@runAppAction null

            AppActionPlaceDetail(
                place = place.toAppActionPlace(),
                tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = id)).map { tag -> tag.toAppActionTag() },
            )
        }

    public suspend fun memoList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            pagePlaceMemoUseCase
                .readPage(
                    parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId.toUuidArgument(name = PLACE_ID), sort = sort.toSortArgument(memoSortList)),
                    offset = cursor.toPageOffset(),
                ).map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun memoFinishedList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            pageFinishedPlaceMemoUseCase
                .readPage(
                    parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId.toUuidArgument(name = PLACE_ID), sort = sort.toSortArgument(memoSortList)),
                    offset = cursor.toPageOffset(),
                ).map { memo -> memo.toAppActionMemo() }
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
            val current = findPlaceUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
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

    public suspend fun delete(placeId: String): AppActionChangedCount = runAppAction { deletePlaceUseCase(parameter = placeId.toUuidArgument(name = PLACE_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(placeId: String): AppActionChangedCount = runAppAction { restorePlaceUseCase(parameter = placeId.toUuidArgument(name = PLACE_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun tagAdd(
        placeId: String,
        tagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId.toUuidArgument(name = PLACE_ID), tagId = tagId.toUuidArgument(name = TAG_ID))

            addEntityTagUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }

    public suspend fun tagRemove(
        placeId: String,
        tagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId.toUuidArgument(name = PLACE_ID), tagId = tagId.toUuidArgument(name = TAG_ID))

            removeEntityTagUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }
}
