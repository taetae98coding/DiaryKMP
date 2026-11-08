package io.github.taetae98coding.diary.work.appaction.appfunction.core

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone

/** Tells that the request was carried out. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionDone(
    /** True when the request was carried out. */
    val done: Boolean,
)

internal fun AppActionDone.toAppFunctionDone(): AppFunctionDone = AppFunctionDone(done = done)
