package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.taetae98coding.diary.core.permission.Permission

/**
 * 요청 지점마다 요청 조건을 이미 확인했는지 기억한다.
 *
 * 내비게이션은 뒤에 쌓인 화면을 composition에서 내리므로 화면 안의 `remember`는 화면을 떠났다 돌아오면 사라진다.
 * 화면 전체를 담는 자리에서 이 기록을 만들어 두면 화면 이동에는 기록이 남고, 앱이 재실행되거나 시스템이 화면을 재생성하면 기록도 새로 만들어진다.
 */
public class PermissionRequestHistory {
    private val requestedSet = mutableSetOf<Pair<Long, Permission>>()

    internal fun markRequested(
        requestPointId: Long,
        permission: Permission,
    ): Boolean = requestedSet.add(requestPointId to permission)
}

public val LocalPermissionRequestHistory: ProvidableCompositionLocal<PermissionRequestHistory?> =
    staticCompositionLocalOf { null }

@Composable
public fun rememberPermissionRequestHistory(): PermissionRequestHistory = remember { PermissionRequestHistory() }
