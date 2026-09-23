@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetMemoContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoContactRepository: AccountMemoContactRepository,
) : FlowUseCase<Uuid, List<Contact>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Contact>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountMemoContactRepository
                        .getContactList(
                            account = account,
                            memoId = parameter,
                        ).map { contactList -> Result.success(contactList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
