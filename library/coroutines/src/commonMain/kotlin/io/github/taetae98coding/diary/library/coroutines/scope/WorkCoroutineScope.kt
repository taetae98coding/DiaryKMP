package io.github.taetae98coding.diary.library.coroutines.scope

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

public fun workCoroutineScope(dispatcher: CoroutineDispatcher = Dispatchers.Default): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, _ -> })
