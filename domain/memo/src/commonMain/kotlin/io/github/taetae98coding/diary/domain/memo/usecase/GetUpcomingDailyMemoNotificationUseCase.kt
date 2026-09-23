package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory
import kotlin.time.Clock

@Factory
public class GetUpcomingDailyMemoNotificationUseCase internal constructor(
    private val getDailyMemoUseCase: GetDailyMemoUseCase,
    private val clock: Clock,
) : FlowUseCase<Unit, List<UpcomingDailyMemoNotification>>() {
    override fun execute(parameter: Unit): Flow<Result<List<UpcomingDailyMemoNotification>>> {
        val dateList = upcomingDailyMemoNotificationDateList(now = clock.now(), timeZone = TimeZone.currentSystemDefault())

        return combine(dateList.map { date -> getDailyMemoUseCase(parameter = date) }) { resultArray ->
            val isUnavailable = resultArray.any { result -> result.isFailure }
            val upcomingList =
                dateList.mapIndexed { index, date ->
                    UpcomingDailyMemoNotification(
                        date = date,
                        content =
                            if (isUnavailable) {
                                DailyMemoNotificationContent.Unavailable
                            } else {
                                DailyMemoNotificationContent.Loaded(memoList = resultArray[index].getOrThrow())
                            },
                    )
                }

            Result.success(upcomingList)
        }
    }
}
