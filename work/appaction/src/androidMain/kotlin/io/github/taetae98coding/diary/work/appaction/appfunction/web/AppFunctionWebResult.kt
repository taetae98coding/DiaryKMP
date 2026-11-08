package io.github.taetae98coding.diary.work.appaction.appfunction.web

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebDetail
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebHeader
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebWithHeaders

/** One page of webs. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionWebPage(
    /** Webs in this page, at most 100. */
    val items: List<AppFunctionWeb>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** A request header sent when the page is opened in the app. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionWebHeader(
    /** Name of the header. */
    val name: String,
    /** Value of the header. */
    val value: String,
)

/** A saved web page with its request headers. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionWebWithHeaders(
    /** Id of the web. */
    val id: String,
    /** Title of the web. */
    val title: String,
    /** Description of the web. */
    val description: String,
    /** Address of the web page. */
    val url: String,
    /** When the web was created, in ISO-8601. */
    val createdAt: String,
    /** When the web was last updated, in ISO-8601. */
    val updatedAt: String,
    /** Request headers of the web, in order. */
    val headers: List<AppFunctionWebHeader>,
)

/** A web with its request headers and linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionWebDetail(
    /** The web with its request headers. */
    val web: AppFunctionWebWithHeaders,
    /** Tags linked to the web. */
    val tags: List<AppFunctionTag>,
)

internal fun AppActionPage<AppActionWeb>.toAppFunctionWebPage(): AppFunctionWebPage = AppFunctionWebPage(items = items.map { web -> web.toAppFunctionWeb() }, nextCursor = nextCursor)

internal fun AppActionWebHeader.toAppFunctionWebHeader(): AppFunctionWebHeader = AppFunctionWebHeader(name = name, value = value)

internal fun AppActionWebWithHeaders.toAppFunctionWebWithHeaders(): AppFunctionWebWithHeaders =
    AppFunctionWebWithHeaders(
        id = id,
        title = title,
        description = description,
        url = url,
        createdAt = createdAt,
        updatedAt = updatedAt,
        headers = headers.map { header -> header.toAppFunctionWebHeader() },
    )

internal fun AppActionWebDetail.toAppFunctionWebDetail(): AppFunctionWebDetail = AppFunctionWebDetail(web = web.toAppFunctionWebWithHeaders(), tags = tags.map { tag -> tag.toAppFunctionTag() })
