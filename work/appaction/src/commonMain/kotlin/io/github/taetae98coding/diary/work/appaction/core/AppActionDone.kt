package io.github.taetae98coding.diary.work.appaction.core

public data class AppActionDone(
    val done: Boolean,
)

internal val appActionDone: AppActionDone = AppActionDone(done = true)
