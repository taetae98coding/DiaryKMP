@file:OptIn(FlowPreview::class)

package io.github.taetae98coding.diary.library.coroutines.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration

public fun Flow<String>.debounceSearchQuery(): Flow<String> = debounce { query -> if (query.isBlank()) Duration.ZERO else INPUT_IDLE_DELAY }
