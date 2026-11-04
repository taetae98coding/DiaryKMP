package io.github.taetae98coding.diary.compose.credential.apple

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne

internal fun FixtureMonkey.appleCredentialsConfig(): AppleCredentialsConfig =
    AppleCredentialsConfig(
        clientId = "io.github.taetae98coding.diary.web-${giveMeOne<String>()}",
        callbackUrl = "https://${giveMeOne<Long>().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
    )
