package io.github.taetae98coding.diary.work.sync.work

import kotlin.uuid.Uuid

internal class SyncStep(
    val prerequisiteList: List<SyncStep>,
    val push: suspend (accountId: Uuid) -> Unit,
    val pull: suspend (accountId: Uuid) -> Unit,
)

internal fun MutableList<SyncStep>.step(
    prerequisiteList: List<SyncStep> = emptyList(),
    push: suspend (accountId: Uuid) -> Unit,
    pull: suspend (accountId: Uuid) -> Unit,
): SyncStep = SyncStep(prerequisiteList = prerequisiteList, push = push, pull = pull).also { step -> add(step) }
