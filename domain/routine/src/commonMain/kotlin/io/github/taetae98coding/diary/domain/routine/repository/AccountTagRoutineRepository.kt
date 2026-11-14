package io.github.taetae98coding.diary.domain.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagRoutineRepository {
    public fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>>

    public fun pageFinished(
        account: Account,
        tagId: Uuid,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>>
}
