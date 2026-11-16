package io.github.taetae98coding.diary.compose.core.lifecycle

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

// androidx의 `dropUnlessResumed`는 인자 없는 콜백만 받는다.
// 같은 이름의 overload로 두면 인자를 선언하지 않은 람다가 두 함수에 모두 맞아 호출이 모호해지므로 이름을 달리한다.
@Composable
public fun <T> dropUnlessResumedWithArg(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    block: (T) -> Unit,
): (T) -> Unit =
    { value ->
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            block(value)
        }
    }
