package io.github.taetae98coding.diary.domain.memo.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
public class GetMemoPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoPlaceRepository: AccountMemoPlaceRepository,
) : FlowUseCase<Uuid, List<Place>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Place>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountMemoPlaceRepository
                .getPlaceList(
                    account = account,
                    memoId = parameter,
                ).map { placeList -> Result.success(placeList) }
        }
}
