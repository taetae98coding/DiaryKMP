package io.github.taetae98coding.diary.work.appaction.appfunction.search

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

/** One page of found tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionSearchTagPage(
    /** Tags in this page, at most 100. */
    val items: List<AppFunctionTag>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** One page of found places. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionSearchPlacePage(
    /** Places in this page, at most 100. */
    val items: List<AppFunctionPlace>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** One page of found webs. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionSearchWebPage(
    /** Webs in this page, at most 100. */
    val items: List<AppFunctionWeb>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionPage<AppActionTag>.toAppFunctionSearchTagPage(): AppFunctionSearchTagPage = AppFunctionSearchTagPage(items = items.map { tag -> tag.toAppFunctionTag() }, nextCursor = nextCursor)

internal fun AppActionPage<AppActionPlace>.toAppFunctionSearchPlacePage(): AppFunctionSearchPlacePage = AppFunctionSearchPlacePage(items = items.map { place -> place.toAppFunctionPlace() }, nextCursor = nextCursor)

internal fun AppActionPage<AppActionWeb>.toAppFunctionSearchWebPage(): AppFunctionSearchWebPage = AppFunctionSearchWebPage(items = items.map { web -> web.toAppFunctionWeb() }, nextCursor = nextCursor)
