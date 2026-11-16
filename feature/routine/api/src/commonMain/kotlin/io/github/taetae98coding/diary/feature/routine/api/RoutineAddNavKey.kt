package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class RoutineAddNavKey(
    val initialTagId: Uuid? = null,
    // 화면이 여러 번 쌓여도 서로 다른 루틴 입력으로 구분하도록 요청마다 다른 키를 받는다.
    val requestKey: Uuid? = null,
) : ScreenNavKey {
    override val screenName: String get() = "RoutineAdd"

    // 초기 태그가 있는 RoutineAdd는 TagDetail 루틴 탭에서 진입한 것이다.
    override val topLevelListKey: ScreenNavKey? get() = if (initialTagId == null) RoutineHomeNavKey else null
}
