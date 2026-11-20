package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.NavKey

public interface ScreenNavKey : NavKey {
    /**
     * 화면 조회를 집계하는 이름이므로 그 화면에 전달된 값에 따라 달라지지 않는 고정된 값이어야 한다.
     *
     * backing field를 두면 저장 상태에 함께 직렬화되므로 반드시 getter로 선언한다.
     */
    public val screenName: String

    /**
     * 필터 Bottom Sheet처럼 아래 화면을 가리지 않고 그 위에 겹쳐 열리는 화면인지. 앱은 이런 화면을 건너뛴 아래 화면으로 내비게이션 바 표시를 정한다.
     */
    public val isOverlay: Boolean get() = false

    /**
     * 목록·상세 배치에서 이 화면이 상세 영역에 놓이는 주요 목적지 목록의 키. 그런 목록이 없는 화면은 `null`이다.
     *
     * 바로 아래 화면이 이 키일 때 앱이 목록과 함께 내비게이션 바를 표시한다.
     */
    public val topLevelListKey: ScreenNavKey? get() = null
}
