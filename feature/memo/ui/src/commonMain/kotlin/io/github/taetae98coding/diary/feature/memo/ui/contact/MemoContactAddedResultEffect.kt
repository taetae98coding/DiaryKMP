package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.contact.api.ContactAddedResult
import io.github.taetae98coding.diary.feature.contact.api.contactAddedResultKey
import kotlin.uuid.Uuid

@Composable
internal fun MemoContactAddedResultEffect(
    requestKey: Uuid,
    onContactAdded: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    ResultEffect<ContactAddedResult>(
        resultKey = contactAddedResultKey(requestKey = requestKey),
        resultEventBus = resultEventBus,
    ) { result ->
        onContactAdded(result.id)
    }
}
