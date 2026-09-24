package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * URL을 여는 [DiaryWebView]가 [LocalDiaryWebSession]으로 읽는 값이다. 앱 루트가 한 번 제공하고, 제공하지 않으면 기본값으로 동작한다.
 * [failureId]는 실패를 구분하는 번호라 같은 번호는 한 번만 알리며, 마지막 결과가 실패가 아니면 null이다.
 */
public data class DiaryWebSession(
    val isPreparing: Boolean = false,
    val importCount: Int = 0,
    val failureId: Int? = null,
)

public val LocalDiaryWebSession: ProvidableCompositionLocal<DiaryWebSession> = compositionLocalOf { DiaryWebSession() }
