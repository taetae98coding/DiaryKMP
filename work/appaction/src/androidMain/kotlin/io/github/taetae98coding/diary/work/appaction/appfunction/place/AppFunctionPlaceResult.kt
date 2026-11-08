package io.github.taetae98coding.diary.work.appaction.appfunction.place

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlaceDetail

/** One page of places. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionPlacePage(
    /** Places in this page, at most 100. */
    val items: List<AppFunctionPlace>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** All places inside the area. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionPlaceList(
    /** Places inside the area. */
    val items: List<AppFunctionPlace>,
)

/** A place with its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionPlaceDetail(
    /** The place. */
    val place: AppFunctionPlace,
    /** Tags linked to the place. */
    val tags: List<AppFunctionTag>,
)

internal fun AppActionPage<AppActionPlace>.toAppFunctionPlacePage(): AppFunctionPlacePage = AppFunctionPlacePage(items = items.map { place -> place.toAppFunctionPlace() }, nextCursor = nextCursor)

internal fun List<AppActionPlace>.toAppFunctionPlaceList(): AppFunctionPlaceList = AppFunctionPlaceList(items = map { place -> place.toAppFunctionPlace() })

internal fun AppActionPlaceDetail.toAppFunctionPlaceDetail(): AppFunctionPlaceDetail = AppFunctionPlaceDetail(place = place.toAppFunctionPlace(), tags = tags.map { tag -> tag.toAppFunctionTag() })
