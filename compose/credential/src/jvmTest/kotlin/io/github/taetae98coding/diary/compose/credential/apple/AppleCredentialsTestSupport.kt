package io.github.taetae98coding.diary.compose.credential.apple

import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

internal fun appleCredentialsConfig(): AppleCredentialsConfig =
    AppleCredentialsConfig(
        clientId = "io.github.taetae98coding.diary.web-${Arb.string().next()}",
        callbackUrl = "https://${Arb.long().next().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
    )
