package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory

private const val CHECKLIST_ID = "checklistId"
private const val ITEM_ID = "itemId"

@Factory
public class ChecklistAppAction internal constructor(
    private val pageChecklistUseCase: PageChecklistUseCase,
    private val findChecklistUseCase: FindChecklistUseCase,
    private val getChecklistItemUseCase: GetChecklistItemUseCase,
    private val addChecklistUseCase: AddChecklistUseCase,
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val resetChecklistUseCase: ResetChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val restoreChecklistUseCase: RestoreChecklistUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val checkChecklistItemUseCase: CheckChecklistItemUseCase,
    private val uncheckChecklistItemUseCase: UncheckChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
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
    ): AppActionChecklistDetail? =
        runAppAction {
            val id = checklistId.toUuidArgument(name = CHECKLIST_ID)
            val checklist = findChecklistUseCase.readFirst(parameter = id) ?: return@runAppAction null
            val itemList = getChecklistItemUseCase.readFirst(parameter = GetChecklistItemUseCase.Parameter(checklistId = id, sort = itemSort.toChecklistItemSortArgument()))

            AppActionChecklistDetail(
                checklist = checklist.toAppActionChecklist(),
                items = itemList.map { item -> item.toAppActionChecklistItem() },
            )
        }

    public suspend fun add(
        title: String,
        description: String?,
        items: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddChecklistUseCase.Parameter(
                    detail = ChecklistDetail(title = title, description = description.orEmpty()),
                    itemContentList = items,
                    tagIdSet = emptySet(),
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
            val current = findChecklistUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail = ChecklistDetail(title = title ?: current.title, description = description ?: current.description)

            updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun reset(checklistId: String): AppActionChangedCount = runAppAction { resetChecklistUseCase(parameter = checklistId.toUuidArgument(name = CHECKLIST_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun delete(checklistId: String): AppActionChangedCount = runAppAction { deleteChecklistUseCase(parameter = checklistId.toUuidArgument(name = CHECKLIST_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(checklistId: String): AppActionChangedCount = runAppAction { restoreChecklistUseCase(parameter = checklistId.toUuidArgument(name = CHECKLIST_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun itemAdd(
        checklistId: String,
        content: String,
    ): AppActionChangedCount =
        runAppAction {
            val parameter = AddChecklistItemUseCase.Parameter(checklistId = checklistId.toUuidArgument(name = CHECKLIST_ID), content = content)

            addChecklistItemUseCase(parameter = parameter).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun itemCheck(itemId: String): AppActionChangedCount = runAppAction { checkChecklistItemUseCase(parameter = itemId.toUuidArgument(name = ITEM_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun itemUncheck(itemId: String): AppActionChangedCount = runAppAction { uncheckChecklistItemUseCase(parameter = itemId.toUuidArgument(name = ITEM_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun itemDelete(itemId: String): AppActionChangedCount = runAppAction { deleteChecklistItemUseCase(parameter = itemId.toUuidArgument(name = ITEM_ID)).getOrThrow().toAppActionChangedCount() }
}
