package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val ITEM_ID = "itemId"

@Factory
public class ChecklistItemAppAction internal constructor(
    private val findChecklistUseCase: FindChecklistUseCase,
    private val findChecklistItemUseCase: FindChecklistItemUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val checkChecklistItemUseCase: CheckChecklistItemUseCase,
    private val uncheckChecklistItemUseCase: UncheckChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
) {
    public suspend fun add(
        checklistId: String,
        content: String,
    ): AppActionChangedCount =
        runAppAction {
            val parameter = AddChecklistItemUseCase.Parameter(checklistId = checklistId.toUuidArgument(name = CHECKLIST_ID), content = content)

            findChecklistUseCase.changeActive(id = parameter.checklistId, isDeleted = Checklist::isDeleted) { addChecklistItemUseCase(parameter = parameter) }
        }

    public suspend fun check(itemId: String): AppActionChangedCount = changeItem(itemId = itemId) { id -> checkChecklistItemUseCase(parameter = id) }

    public suspend fun uncheck(itemId: String): AppActionChangedCount = changeItem(itemId = itemId) { id -> uncheckChecklistItemUseCase(parameter = id) }

    public suspend fun delete(itemId: String): AppActionChangedCount = changeItem(itemId = itemId) { id -> deleteChecklistItemUseCase(parameter = id) }

    private suspend fun changeItem(
        itemId: String,
        change: suspend (Uuid) -> Result<Int>,
    ): AppActionChangedCount =
        runAppAction {
            val id = itemId.toUuidArgument(name = ITEM_ID)
            val item = findChecklistItemUseCase.readActive(id = id, isDeleted = ChecklistItem::isDeleted) ?: return@runAppAction 0.toAppActionChangedCount()

            findChecklistUseCase.changeActive(id = item.checklistId, isDeleted = Checklist::isDeleted) { change(id) }
        }
}
