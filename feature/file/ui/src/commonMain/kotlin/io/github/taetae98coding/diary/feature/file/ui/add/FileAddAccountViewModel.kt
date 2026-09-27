package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.lifecycle.ViewModel
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileAddAccountViewModel(
    getAccountUseCase: GetAccountUseCase,
) : ViewModel() {
    val effect: Flow<FileAddAccountEffect> =
        getAccountUseCase(parameter = Unit).mapNotNull { result ->
            if (result.getOrNull() is Account.Guest) FileAddAccountEffect.BecameGuest else null
        }
}
