package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.web.api.WebAddedResult
import kotlin.uuid.Uuid

@Composable
internal fun MemoWebAddedResultEffect(
    onWebAdded: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    ResultEffect<WebAddedResult>(resultEventBus = resultEventBus) { result ->
        onWebAdded(result.id)
    }
}
