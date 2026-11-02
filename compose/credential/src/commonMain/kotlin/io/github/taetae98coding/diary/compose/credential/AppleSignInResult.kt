package io.github.taetae98coding.diary.compose.credential

import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential

public data class AppleSignInResult(
    val credential: AppleCredential,
    val authorization: AppleAuthorization?,
)
