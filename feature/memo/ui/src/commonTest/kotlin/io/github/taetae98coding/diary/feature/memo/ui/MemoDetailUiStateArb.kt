package io.github.taetae98coding.diary.feature.memo.ui

import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailUiState
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

internal fun Arb.Companion.memoDetailUiStateContent(): Arb<MemoDetailUiState.Content> =
    arbitrary {
        MemoDetailUiState.Content(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.memoDetail().bind(),
            isFinished = Arb.boolean().bind(),
            isInProgress = Arb.boolean().bind(),
            isFinishInProgress = Arb.boolean().bind(),
            isCopyInProgress = Arb.boolean().bind(),
            isDeleteInProgress = Arb.boolean().bind(),
        )
    }
