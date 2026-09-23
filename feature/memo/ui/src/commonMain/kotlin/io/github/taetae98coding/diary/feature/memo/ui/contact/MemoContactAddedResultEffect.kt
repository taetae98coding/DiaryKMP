package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.contact.api.ContactAddedResult
import kotlin.uuid.Uuid

@Composable
internal fun MemoContactAddedResultEffect(
    onContactAdded: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    ResultEffect<ContactAddedResult>(resultEventBus = resultEventBus) { result ->
        onContactAdded(result.id)
    }
}
