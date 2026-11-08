package io.github.taetae98coding.diary.work.appaction.appfunction.tag

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
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagDetail
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

/** One page of tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionTagPage(
    /** Tags in this page, at most 100. */
    val items: List<AppFunctionTag>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** A tag with the tags it links to. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionTagDetail(
    /** The tag. */
    val tag: AppFunctionTag,
    /** Tags the tag links to (its child tags). */
    val linkedTags: List<AppFunctionTag>,
)

/** One page of webs linked to a tag. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionTagWebPage(
    /** Webs in this page, at most 100. */
    val items: List<AppFunctionWeb>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** One page of places linked to a tag. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionTagPlacePage(
    /** Places in this page, at most 100. */
    val items: List<AppFunctionPlace>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionPage<AppActionTag>.toAppFunctionTagPage(): AppFunctionTagPage = AppFunctionTagPage(items = items.map { tag -> tag.toAppFunctionTag() }, nextCursor = nextCursor)

internal fun AppActionTagDetail.toAppFunctionTagDetail(): AppFunctionTagDetail = AppFunctionTagDetail(tag = tag.toAppFunctionTag(), linkedTags = linkedTags.map { linkedTag -> linkedTag.toAppFunctionTag() })

internal fun AppActionPage<AppActionWeb>.toAppFunctionTagWebPage(): AppFunctionTagWebPage = AppFunctionTagWebPage(items = items.map { web -> web.toAppFunctionWeb() }, nextCursor = nextCursor)

internal fun AppActionPage<AppActionPlace>.toAppFunctionTagPlacePage(): AppFunctionTagPlacePage = AppFunctionTagPlacePage(items = items.map { place -> place.toAppFunctionPlace() }, nextCursor = nextCursor)
