package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_copy_succeeded_message
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoCopiedResultEffect(
    id: Uuid,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    scaffoldState: MemoFormState = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
) {
    val resultKey = memoDetailCopiedResultKey(id = id)
    val copySucceededMessage = stringResource(Res.string.memo_detail_copy_succeeded_message)

    ResultEffect<MemoDetailCopiedResult>(
        resultKey = resultKey,
        resultEventBus = resultEventBus,
    ) {
        scaffoldState.hostState.showImmediate(message = copySucceededMessage)
    }

    DisposableEffect(resultEventBus, resultKey) {
        onDispose { resultEventBus.removeResult(resultKey = resultKey) }
    }
}
