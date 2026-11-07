package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant

internal fun itemContent(): String = "content-" + Arb.string().next()

internal fun getAccountUseCase(account: Account): GetAccountUseCase {
    val useCase = mockk<GetAccountUseCase>()
    every { useCase(parameter = Unit) } returns flowOf(Result.success(account))
    return useCase
}

internal fun failingGetAccountUseCase(throwable: Throwable): GetAccountUseCase {
    val useCase = mockk<GetAccountUseCase>()
    every { useCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
    return useCase
}

internal fun requestSyncUseCase(): RequestSyncUseCase {
    val useCase = mockk<RequestSyncUseCase>()
    coEvery { useCase(parameter = any()) } returns Result.success(Unit)
    return useCase
}

internal fun clock(now: Instant): Clock {
    val clock = mockk<Clock>()
    every { clock.now() } returns now
    return clock
}
