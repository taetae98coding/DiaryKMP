package io.github.taetae98coding.diary.work.appaction.appfunction.core

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId

/** The id of the item that was added. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionId(
    /** Id of the item. */
    val id: String,
)

/** How many items were changed. 0 means the item does not exist or was already in the requested state. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChangedCount(
    /** Number of changed items. */
    val changedCount: Int,
)

internal fun AppActionId.toAppFunctionId(): AppFunctionId = AppFunctionId(id = id)

internal fun AppActionChangedCount.toAppFunctionChangedCount(): AppFunctionChangedCount = AppFunctionChangedCount(changedCount = changedCount)
