package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.NavKey

public interface ScreenNavKey : NavKey {
    /**
     * 화면 조회를 집계하는 이름이므로 그 화면에 전달된 값에 따라 달라지지 않는 고정된 값이어야 한다.
     *
     * backing field를 두면 저장 상태에 함께 직렬화되므로 반드시 getter로 선언한다.
     */
    public val screenName: String
}
