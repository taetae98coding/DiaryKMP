package io.github.taetae98coding.diary.feature.core.paging

import androidx.paging.LoadState
import androidx.paging.LoadStates

public val LOADED_EMPTY_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
