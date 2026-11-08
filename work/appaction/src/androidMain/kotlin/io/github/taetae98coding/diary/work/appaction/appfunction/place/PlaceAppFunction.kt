package io.github.taetae98coding.diary.work.appaction.appfunction.place

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.toAppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction

internal class PlaceAppFunction(
    private val placeAppAction: PlaceAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionPlacePage = appFunctionCall { placeAppAction.list(sort = sort, cursor = cursor).toAppFunctionPlacePage() }

    suspend fun listInArea(
        south: String,
        north: String,
        west: String,
        east: String,
        sort: String?,
    ): AppFunctionPlaceList = appFunctionCall { placeAppAction.listInArea(south = south, north = north, west = west, east = east, sort = sort).toAppFunctionPlaceList() }

    suspend fun get(placeId: String): AppFunctionPlaceDetail? = appFunctionCall { placeAppAction.get(placeId = placeId)?.toAppFunctionPlaceDetail() }

    suspend fun memoList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { placeAppAction.memoList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun memoFinishedList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppFunctionMemoPage = appFunctionCall { placeAppAction.memoFinishedList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    suspend fun add(
        title: String,
        latitude: String,
        longitude: String,
        address: String?,
        description: String?,
        color: String?,
        tagIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            placeAppAction
                .add(
                    title = title,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    description = description,
                    color = color,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    suspend fun update(
        placeId: String,
        title: String?,
        description: String?,
        color: String?,
        address: String?,
        latitude: String?,
        longitude: String?,
    ): AppFunctionChangedCount =
        appFunctionCall {
            placeAppAction
                .update(
                    placeId = placeId,
                    title = title,
                    description = description,
                    color = color,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                ).toAppFunctionChangedCount()
        }

    suspend fun delete(placeId: String): AppFunctionChangedCount = appFunctionCall { placeAppAction.delete(placeId = placeId).toAppFunctionChangedCount() }

    suspend fun restore(placeId: String): AppFunctionChangedCount = appFunctionCall { placeAppAction.restore(placeId = placeId).toAppFunctionChangedCount() }

    suspend fun tagAdd(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { placeAppAction.tagAdd(placeId = placeId, tagId = tagId).toAppFunctionDone() }

    suspend fun tagRemove(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { placeAppAction.tagRemove(placeId = placeId, tagId = tagId).toAppFunctionDone() }
}
