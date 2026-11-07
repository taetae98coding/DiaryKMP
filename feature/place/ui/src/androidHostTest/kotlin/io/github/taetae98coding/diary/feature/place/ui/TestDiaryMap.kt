package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverrideScope
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride

internal const val TEST_DIARY_MAP_TAG: String = "TestDiaryMap"

// 지도 SDK의 표시 요소는 호스트 테스트에서 만들 수 없으므로, 지도 자리에 표시 노드만 그리고 각 지도가 받은 상태와 입력 통로를 표시된 순서대로 기록한다.
internal class TestDiaryMap : DiaryMapContentOverride {
    private val scopeMap = LinkedHashMap<DiaryMapState, DiaryMapContentOverrideScope>()

    val isShown: Boolean
        get() = scopeMap.isNotEmpty()

    fun firstScope(): DiaryMapContentOverrideScope = scopeMap.values.first()

    fun lastScope(): DiaryMapContentOverrideScope = scopeMap.values.last()

    @Composable
    override fun DiaryMapContentOverrideScope.MapContent() {
        val scope = this

        SideEffect { scopeMap[scope.state] = scope }
        DisposableEffect(scope.state) {
            onDispose { scopeMap.remove(scope.state) }
        }
        Box(modifier = modifier.testTag(TEST_DIARY_MAP_TAG))
    }
}

@Composable
internal fun ProvideTestDiaryMap(
    map: TestDiaryMap,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDiaryMapContentOverride provides map, content = content)
}
