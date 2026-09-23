@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetSelectedContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountContactRepository: AccountContactRepository,
) : FlowUseCase<Set<Uuid>, List<Contact>>() {
    override fun execute(parameter: Set<Uuid>): Flow<Result<List<Contact>>> {
        if (parameter.isEmpty()) return flowOf(Result.success(emptyList()))

        return getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountContactRepository.get(account = account, contactIdSet = parameter).map { contactList ->
                        Result.success(contactList)
                    }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
    }
}
