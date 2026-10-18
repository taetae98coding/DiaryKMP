package io.github.taetae98coding.diary.data.account.mapper

import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser

internal fun SupabaseUser.toUserData(): UserData =
    UserData(
        id = id,
        email = email,
        profileImage = profileImage,
    )
