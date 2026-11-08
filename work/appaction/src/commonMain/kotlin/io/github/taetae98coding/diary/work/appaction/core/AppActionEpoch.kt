package io.github.taetae98coding.diary.work.appaction.core

import kotlin.time.Instant

// iOS는 시각을 Date로 다루므로 결과의 ISO-8601 시각을 epoch 밀리초로 바꿔 넘긴다.
public fun appActionEpochMillis(instant: String): Long = Instant.parse(instant).toEpochMilliseconds()
