package io.github.taetae98coding.diary.library.coroutines.flow

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 사용자가 입력을 멈췄다고 보고 값을 반영하기까지 기다리는 시간이다.
 * 값은 `docs/spec/input-idle-delay.md`의 `대기 시간`을 따른다.
 */
public val INPUT_IDLE_DELAY: Duration = 200.milliseconds
