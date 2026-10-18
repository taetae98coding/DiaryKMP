@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

public fun <T> GetAccountUseCase.flatMapAccount(transform: (Account) -> Flow<Result<T>>): Flow<Result<T>> =
    invoke(parameter = Unit).flatMapLatest { result ->
        result.fold(
            onSuccess = transform,
            onFailure = { throwable -> flowOf(Result.failure(throwable)) },
        )
    }

public suspend fun GetAccountUseCase.requireAccount(): Account = invoke(parameter = Unit).first().getOrThrow()
