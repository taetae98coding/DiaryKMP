package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public data class DiaryWebSession(
    val isRestoring: Boolean = false,
)

// 세션을 쓰지 않는 중간 화면이 세션을 전달하지 않도록 앱 루트가 한 번 제공하고 웹 표시 수단을 그리는 화면만 읽는다.
public val LocalDiaryWebSession: ProvidableCompositionLocal<DiaryWebSession> = compositionLocalOf { DiaryWebSession() }
