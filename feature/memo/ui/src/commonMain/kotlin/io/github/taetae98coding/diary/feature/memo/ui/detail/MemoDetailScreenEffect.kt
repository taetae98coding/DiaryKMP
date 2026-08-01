package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailScreenEffect(
    navigateUp: () -> Unit,
    navigateToCopiedMemo: (Uuid) -> Unit,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    effect: Flow<MemoDetailEffect> = emptyFlow(),
    scaffoldState: MemoFormState = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.memo_detail_update_succeeded_message)

    CollectEffect(effect) { value ->
        when (value) {
            is MemoDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is MemoDetailEffect.CopySucceeded -> {
                resultEventBus.sendResult(
                    resultKey = memoDetailCopiedResultKey(id = value.id),
                    result = MemoDetailCopiedResult,
                )
                navigateToCopiedMemo(value.id)
            }

            is MemoDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
