package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
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
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import org.koin.core.annotation.Factory

internal const val CHECKLIST_ID = "checklistId"

@Factory
public class ChecklistAppAction internal constructor(
    private val pageChecklistUseCase: PageChecklistUseCase,
    private val findChecklistUseCase: FindChecklistUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val getChecklistItemUseCase: GetChecklistItemUseCase,
    private val addChecklistUseCase: AddChecklistUseCase,
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val resetChecklistUseCase: ResetChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val restoreChecklistUseCase: RestoreChecklistUseCase,
) {
    public suspend fun list(cursor: String?): AppActionPage<AppActionChecklistSummary> =
        runAppAction {
            pageChecklistUseCase
                .readPage(parameter = Unit, offset = cursor.toPageOffset())
                .map { summary -> summary.toAppActionChecklistSummary() }
        }

    public suspend fun get(
        checklistId: String,
        itemSort: String?,
    ): AppActionChecklistDetail? = runAppAction { detail(checklistId = checklistId, itemSort = itemSort, includesDeleted = false) }

    public suspend fun getIncludingDeleted(
        checklistId: String,
        itemSort: String?,
    ): AppActionChecklistDetail? = runAppAction { detail(checklistId = checklistId, itemSort = itemSort, includesDeleted = true) }

    private suspend fun detail(
        checklistId: String,
        itemSort: String?,
        includesDeleted: Boolean,
    ): AppActionChecklistDetail? {
        val id = checklistId.toUuidArgument(name = CHECKLIST_ID)
        val checklist = findChecklistUseCase.readFirst(parameter = id)?.takeIf { checklist -> includesDeleted || !checklist.isDeleted } ?: return null
        val itemList = getChecklistItemUseCase.readFirst(parameter = GetChecklistItemUseCase.Parameter(checklistId = id, sort = itemSort.toChecklistItemSortArgument()))

        return AppActionChecklistDetail(
            checklist = checklist.toAppActionChecklist(),
            items = itemList.map { item -> item.toAppActionChecklistItem() },
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CHECKLIST, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
        title: String,
        description: String?,
        items: List<String>,
        tagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddChecklistUseCase.Parameter(
                    detail = ChecklistDetail(title = title, description = description.orEmpty()),
                    itemContentList = items,
                    tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"),
                )

            addChecklistUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        checklistId: String,
        title: String?,
        description: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = checklistId.toUuidArgument(name = CHECKLIST_ID)
            val current = findChecklistUseCase.readActive(id = id, isDeleted = Checklist::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail = ChecklistDetail(title = title ?: current.title, description = description ?: current.description)

            updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun reset(checklistId: String): AppActionChangedCount =
        runAppAction {
            val id = checklistId.toUuidArgument(name = CHECKLIST_ID)

            findChecklistUseCase.changeActive(id = id, isDeleted = Checklist::isDeleted) { resetChecklistUseCase(parameter = id) }
        }

    public suspend fun delete(checklistId: String): AppActionChangedCount =
        runAppAction {
            val id = checklistId.toUuidArgument(name = CHECKLIST_ID)

            findChecklistUseCase.changeActive(id = id, isDeleted = Checklist::isDeleted) { deleteChecklistUseCase(parameter = id) }
        }

    public suspend fun restore(checklistId: String): AppActionChangedCount = runAppAction { restoreChecklistUseCase(parameter = checklistId.toUuidArgument(name = CHECKLIST_ID)).getOrThrow().toAppActionChangedCount() }
}
