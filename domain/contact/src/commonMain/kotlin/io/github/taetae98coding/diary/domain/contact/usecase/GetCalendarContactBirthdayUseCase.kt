@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarContactBirthdayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarContactBirthdayRepository: AccountCalendarContactBirthdayRepository,
) : FlowUseCase<LocalDateRange, List<CalendarContactBirthday>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarContactBirthday>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountCalendarContactBirthdayRepository
                        .get(
                            account = account,
                            dateRange = parameter,
                        ).map { birthdayList -> Result.success(birthdayList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
