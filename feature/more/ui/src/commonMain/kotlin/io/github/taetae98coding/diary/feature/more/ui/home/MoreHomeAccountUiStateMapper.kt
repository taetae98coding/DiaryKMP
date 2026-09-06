package io.github.taetae98coding.diary.feature.more.ui.home

import io.github.taetae98coding.diary.core.model.account.Account

internal fun Account.toUiState(): MoreHomeAccountUiState =
    when (this) {
        is Account.Guest -> {
            MoreHomeAccountUiState.Guest
        }

        is Account.User -> {
            MoreHomeAccountUiState.User(
                profileImage = profileImage,
                email = email,
            )
        }
    }
