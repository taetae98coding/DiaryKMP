package io.github.taetae98coding.diary.data.account.mapper

import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser

internal fun SupabaseUser.toUserData(): UserData =
    UserData(
        id = id,
        email = email,
        profileImage = profileImage,
        loginProviderSet = providerSet.mapNotNull { provider -> provider.toLoginProvider() }.toSet(),
    )

private fun String.toLoginProvider(): LoginProvider? =
    when (this) {
        GOOGLE_PROVIDER -> LoginProvider.GOOGLE
        APPLE_PROVIDER -> LoginProvider.APPLE
        else -> null
    }

private const val GOOGLE_PROVIDER = "google"
private const val APPLE_PROVIDER = "apple"
