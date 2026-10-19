package io.github.taetae98coding.diary.feature.checklist.ui.home

import kotlin.uuid.Uuid

internal sealed interface ChecklistHomeEffect {
    data class Deleted(
        val id: Uuid,
    ) : ChecklistHomeEffect
}
