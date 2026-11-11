package io.github.taetae98coding.diary.work.appaction.web

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
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
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import org.koin.core.annotation.Factory

private const val WEB_ID = "webId"

internal val webSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class WebAppAction internal constructor(
    private val pageWebUseCase: PageWebUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addWebUseCase: AddWebUseCase,
    private val updateWebUseCase: UpdateWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionWeb> =
        runAppAction {
            pageWebUseCase
                .readPage(parameter = sort.toSortArgument(webSortList), offset = cursor.toPageOffset())
                .map { web -> web.toAppActionWeb() }
        }

    public suspend fun get(webId: String): AppActionWebDetail? = runAppAction { detail(webId = webId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(webId: String): AppActionWebDetail? = runAppAction { detail(webId = webId, includesDeleted = true) }

    private suspend fun detail(
        webId: String,
        includesDeleted: Boolean,
    ): AppActionWebDetail? {
        val id = webId.toUuidArgument(name = WEB_ID)
        val web = findWebUseCase.readFirst(parameter = id)?.takeIf { web -> includesDeleted || !web.isDeleted } ?: return null

        return AppActionWebDetail(
            web = web.toAppActionWebWithHeaders(),
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
        title: String,
        url: String,
        description: String?,
        headers: List<String>,
        tagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddWebUseCase.Parameter(
                    detail =
                        WebDetail(
                            title = title,
                            description = description.orEmpty(),
                            url = url,
                            headerList = headers.toWebHeaderListArgument(),
                        ),
                    tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"),
                )

            addWebUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    // 헤더를 담지 않으면 지금 헤더를 유지하고 빈 목록이면 모두 지우므로, 담지 않음을 null로 구분한다.
    public suspend fun update(
        webId: String,
        title: String?,
        description: String?,
        url: String?,
        headers: List<String>?,
    ): AppActionChangedCount =
        runAppAction {
            val id = webId.toUuidArgument(name = WEB_ID)
            val current = findWebUseCase.readActive(id = id, isDeleted = Web::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                WebDetail(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    url = url ?: current.url,
                    headerList = headers?.toWebHeaderListArgument() ?: current.headerList,
                )

            updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun delete(webId: String): AppActionChangedCount =
        runAppAction {
            val id = webId.toUuidArgument(name = WEB_ID)

            findWebUseCase.changeActive(id = id, isDeleted = Web::isDeleted) { deleteWebUseCase(parameter = id) }
        }

    public suspend fun restore(webId: String): AppActionChangedCount = runAppAction { restoreWebUseCase(parameter = webId.toUuidArgument(name = WEB_ID)).getOrThrow().toAppActionChangedCount() }
}
