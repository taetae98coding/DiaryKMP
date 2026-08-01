package io.github.taetae98coding.diary.feature.memo.ui.detail

import kotlin.uuid.Uuid

internal data object MemoDetailCopiedResult

internal fun memoDetailCopiedResultKey(id: Uuid): String = "MemoDetailCopiedResult:$id"
