@file:OptIn(FlowPreview::class)

package io.github.taetae98coding.diary.library.coroutines.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

public fun Flow<String>.debounceSearchQuery(): Flow<String> = debounce { query -> if (query.isBlank()) Duration.ZERO else INPUT_IDLE_DELAY }

/**
 * 선택 목록처럼 화면이 검색어를 알려 준 뒤에야 조회를 시작하는 목록의 검색어 반영 방식이다.
 *
 * 화면이 아직 검색어를 알려 주지 않은 동안(`null`)에는 조회하지 않는다. 화면이 처음 알려 준 검색어는 기다리지 않고 곧바로 반영하므로,
 * 메모리 정리 뒤 복원된 검색어도 빈 목록이나 좁히지 않은 목록을 거치지 않고 처음부터 그 검색어로 조회한다.
 * 그 뒤의 변경은 [debounceSearchQuery]와 같이 입력을 멈춘 뒤 반영하고, 비운 검색어는 곧바로 반영한다.
 */
public fun Flow<String?>.debounceReportedSearchQuery(): Flow<String> =
    flow {
        var isFirst = true

        emitAll(
            filterNotNull().debounce { query ->
                if (isFirst) {
                    isFirst = false
                    Duration.ZERO
                } else if (query.isBlank()) {
                    Duration.ZERO
                } else {
                    INPUT_IDLE_DELAY
                }
            },
        )
    }
